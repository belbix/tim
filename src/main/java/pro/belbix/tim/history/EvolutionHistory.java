package pro.belbix.tim.history;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.EvoPropKey;
import pro.belbix.tim.entity.Evolution;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.properties.*;
import pro.belbix.tim.repositories.EvoPropKeyRepository;
import pro.belbix.tim.repositories.EvolutePropertiesRepository;
import pro.belbix.tim.services.ValuesService;
import pro.belbix.tim.strategies.Strategy;
import pro.belbix.tim.strategies.common.TradeStatusBuilder;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class EvolutionHistory {
    private static final Logger log = LoggerFactory.getLogger(EvolutionHistory.class);
    private final HistoryProcessor historyProcessor;
    private final StrategyProperties strategyProperties;
    private final EvolutePropertiesRepository evolutePropertiesRepository;
    private final HistoryProperties historyProperties;
    private final OrderServiceProperties orderServiceProperties;
    private final EvolutionProperties evoluteProperties;
    private final ReproductionService reproductionService;
    private final ValuesService valuesService;
    private final NeuronReproductionService neuronReproductionService;
    private final EvoPropKeyRepository evoPropKeyRepository;

    private EvoPropKey currentKey;

    @Autowired
    public EvolutionHistory(HistoryProcessor historyProcessor,
                            StrategyProperties strategyProperties,
                            EvolutePropertiesRepository evolutePropertiesRepository,
                            HistoryProperties historyProperties,
                            OrderServiceProperties orderServiceProperties,
                            EvolutionProperties evolutionProperties,
                            ReproductionService reproductionService,
                            ValuesService valuesService,
                            NeuronReproductionService neuronReproductionService,
                            EvoPropKeyRepository evoPropKeyRepository) {
        this.historyProcessor = historyProcessor;
        this.strategyProperties = strategyProperties;
        this.evolutePropertiesRepository = evolutePropertiesRepository;
        this.historyProperties = historyProperties;
        this.orderServiceProperties = orderServiceProperties;
        this.evoluteProperties = evolutionProperties;
        this.reproductionService = reproductionService;
        this.valuesService = valuesService;
        this.neuronReproductionService = neuronReproductionService;
        this.evoPropKeyRepository = evoPropKeyRepository;
    }


    private static void evolute(String propertyFromDb, MutableProperties newProperty, double factor) {
        MutableProperties propFromDB = jsonToProperty(propertyFromDb, newProperty);
        if (propFromDB == null) {
            return;
        }
        Common.evoluteMutate(propFromDB, newProperty, factor);
    }

    public static MutableProperties jsonToProperty(String properties, MutableProperties newProperty) {
        if (properties == null) {
            properties = "{}";
        }
        ObjectMapper mapper = new ObjectMapper();
        MutableProperties propFromDB = null;
        try {
            propFromDB = mapper.readValue(properties, mapPropertiesClass(newProperty));
        } catch (IOException e) {
            log.error("readValue error", e);
        }
        return propFromDB;
    }

    private static <T extends MutableProperties> Class<T> mapPropertiesClass(T prop) {
        if (prop instanceof SrsiProperties) {
            return (Class<T>) SrsiProperties.class;
        } else if (prop instanceof Srsi2Properties) {
            return (Class<T>) Srsi2Properties.class;
        } else if (prop instanceof OrderServiceProperties) {
            return (Class<T>) OrderServiceProperties.class;
        } else if (prop instanceof StrategyProperties) {
            return (Class<T>) StrategyProperties.class;
        } else if (prop instanceof NsrsiProperties) {
            return (Class<T>) NsrsiProperties.class;
        }
        throw new TIMRuntimeException("Property not found");
    }

    public void start() {
        log.info("Start evolution");
        Strategy strategy = historyProcessor.init(historyProperties);
        MutableProperties mutableProperties = strategy.getStrategyPropertiesI();


        while (true) {
            Evolution p = evoluteProperties(mutableProperties, strategy);
            log.debug("evoluteProperties: " + mutableProperties);
            TradeStatusBuilder tradeStatusBuilder = null;
            try {
                log.info("Start processing");
                Instant start = Instant.now();
                tradeStatusBuilder = historyProcessor.processing(historyProperties);
                saveStatus("OK");
                log.info("End processing: " + Duration.between(start, Instant.now()).toSeconds() + "s");
            } catch (Throwable e) {
                log.error("historyProcessor: " + e.getMessage(), e);
                saveStatus(e.getMessage());
                strategy.finalClear();
                historyProcessor.clear();
                continue;
            }

            saveResult(tradeStatusBuilder, strategy, p);
            strategy.finalClear();
            historyProcessor.clear();
            if (evoluteProperties.isDeath()) {
                deathEvolutions();
            }
        }
    }


    private void deathEvolutions() {
        log.info("Kill not top evolutions");

        Long count = evolutePropertiesRepository.getCountByKey(findKey());
        long toDieCount = count - aliveCount();
        if (toDieCount == 0) {
            return;
        }
        if (toDieCount < 0) {
            log.warn("Gentle warning, you have another instance who kill evo more: " + toDieCount);
            return;
        }
        log.info("toDieCount: " + toDieCount);
        List<Long> toDie = evolutePropertiesRepository.getBottomIds(findKey(), PageRequest.of(0, (int) toDieCount));
        log.info("toDie Ids: " + toDie.size());
        evolutePropertiesRepository.deleteAllByIds(toDie);
        log.info("Killed " + toDie.size());
    }

    private Evolution getTopProp(int top) {
        log.info("Get top from " + top + " evos");
        List<Evolution> evos = evolutePropertiesRepository.getRandomEvo(findKey().getId(), top);
        if (evos.isEmpty()) {
            return null;
        } else {
            return evos.get(0);
        }
    }

    private EvoPropKey findKey() {
        if (currentKey != null) {
            return currentKey;
        }
        List<EvoPropKey> keys = evoPropKeyRepository.find(historyProperties.getStrategyName(),
                strategyProperties.getServer(),
                strategyProperties.getSymbol(),
                historyProperties.getDateStart(),
                historyProperties.getDateEnd(),
                strategyProperties.getTimeframeLong(),
                strategyProperties.getTimeframeShort(),
                historyProperties.getTicktime(),
                historyProperties.getFee(),
                historyProperties.getLeverage(),
                historyProperties.getBatch());
        if (keys.isEmpty()) {
            return evoPropKeyRepository.save(EvoPropKey.createEvoPropKey(strategyProperties, historyProperties));
        }
        if (keys.size() != 1) {
            throw new IllegalStateException("Get more than 1 key: " + keys);
        }
        currentKey = keys.get(0);
        return currentKey;
    }

    private Evolution evoluteProperties(MutableProperties currentProperty, Strategy strategy) {
        Evolution parent = getTopProp(evoluteProperties.getTop());
        if (parent == null) {
            log.info("Top properties is empty");
            parent = Evolution.createEvoProp(
                    strategy,
                    strategyProperties,
                    new TradeStatusBuilder(1000, 0, 1),
                    historyProperties,
                    orderServiceProperties,
                    historyProcessor,
                    findKey(),
                    0
            );
        }

        if (evoluteProperties.isMain()) {
            if (currentProperty instanceof Srsi2Properties) {
                ((Srsi2Properties) currentProperty).setNodes(reproductionService.bornChildNodes(parent));
            } else if (currentProperty instanceof NsrsiProperties) {
                ((NsrsiProperties) currentProperty).setModel(neuronReproductionService.bornChildModel(parent));
            } else {
                evolute(parent.getProperties(), currentProperty, evoluteProperties.getFactor());
            }
        }
        if (evoluteProperties.isOrders()) {
            evolute(parent.getOrderProperties(), orderServiceProperties, evoluteProperties.getFactor());
        }
        if (evoluteProperties.isStrategy()) {
            evolute(parent.getStrategyProperties(), strategyProperties, evoluteProperties.getFactor());
        }
        return parent;
    }

    private void saveResult(TradeStatusBuilder tradeStatusBuilder, Strategy strategy, Evolution p) {
        if (tradeStatusBuilder == null) return;
        long prevId = 0;
        if (p != null) prevId = p.getId();
        Evolution evolution =
                Evolution.createEvoProp(
                        strategy,
                        strategyProperties,
                        tradeStatusBuilder,
                        historyProperties,
                        orderServiceProperties,
                        historyProcessor,
                        findKey(),
                        prevId
                );
        double bottomResult = 0.0;
        List<Evolution> bottomFromTop =
                evolutePropertiesRepository.getTop(findKey(), PageRequest.of(1, aliveCount() - 1));
        if (bottomFromTop != null && !bottomFromTop.isEmpty()) {
            bottomResult = bottomFromTop.get(0).getResult();
            if (evolution.getResult() < bottomResult) {
                log.info("Result is not so good: " + evolution.getResult() + " Bottom: " + bottomResult);
                return;
            }
        }
        evolutePropertiesRepository.save(evolution);
        log.info("Result " + evolution.getResult() + " is saving " + evolution.getId()
                + " bottom result:" + bottomResult);
    }

    private Evolution getBestEvo(List<Evolution> evos) {
        int i = ThreadLocalRandom.current().nextInt(0, evos.size() - 1);
        return evos.get(i);
    }

    private void saveStatus(String status) {
        valuesService.addThreadStatus("EVO_" + historyProperties.getStation().toUpperCase(),
                status);
    }

    private int aliveCount() {
        return evoluteProperties.getTop() * evoluteProperties.getAlive();
    }


}
