package pro.belbix.tim.history;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.Evolution;
import pro.belbix.tim.properties.EvolutionProperties;
import pro.belbix.tim.properties.NsrsiProperties;
import pro.belbix.tim.protobuf.neuron.NeuroLinkModel;
import pro.belbix.tim.protobuf.neuron.NeuronLayerModel;
import pro.belbix.tim.protobuf.neuron.NeuronModel;
import pro.belbix.tim.protobuf.neuron.Nsrsi;
import pro.belbix.tim.strategies.NsrsiStrategy;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static pro.belbix.tim.history.EvolutionHistory.jsonToProperty;
import static pro.belbix.tim.history.MutateService.ChanceType.*;
import static pro.belbix.tim.history.MutateService.isChance;

@Service
public class NeuronReproductionService {
    private static final Logger log = LoggerFactory.getLogger(NeuronReproductionService.class);
    private static final int MAX_CANDLE_INDEX = 2;

    private final EvolutionProperties evolutionProperties;
    private final NsrsiStrategy nsrsiStrategy;

    public NeuronReproductionService(EvolutionProperties evolutionProperties,
                                     NsrsiStrategy nsrsiStrategy) {
        this.evolutionProperties = evolutionProperties;
        this.nsrsiStrategy = nsrsiStrategy;
    }


    String bornChildModel(Evolution parent) {
        NsrsiProperties parentProperties = extractProperties(parent);
        Nsrsi parentNsrsi = parentProperties.toNsrsi();

        Nsrsi childNsrsi = mutateNsrsi(parentNsrsi);
        log.info("New child: " + childNsrsi);
        return Hex.encodeHexString(childNsrsi.toByteArray()).toUpperCase();
    }

    private Nsrsi mutateNsrsi(Nsrsi nsrsi) {
        Nsrsi.Builder builder = Nsrsi.newBuilder();
        builder.setOpenLong(mutateNeuroLinkModel(nsrsi.getOpenLong(),
                nsrsiStrategy.getLongOpenNL().getCountsOfSuccess()));

        builder.setCloseLong(mutateNeuroLinkModel(nsrsi.getCloseLong(),
                nsrsiStrategy.getLongCloseNL().getCountsOfSuccess()));

        builder.setOpenShort(mutateNeuroLinkModel(nsrsi.getOpenShort(),
                nsrsiStrategy.getShortOpenNL().getCountsOfSuccess()));

        builder.setCloseShort(mutateNeuroLinkModel(nsrsi.getCloseShort(),
                nsrsiStrategy.getShortCloseNL().getCountsOfSuccess()));
        return builder.build();
    }

    private NeuroLinkModel mutateNeuroLinkModel(NeuroLinkModel neuroLinkModel, Map<Integer, Map<Long, Long>> countOfSuccess) {
        NeuroLinkModel.Builder builder = NeuroLinkModel.newBuilder();

        builder.setInput(mutateInputLayer(neuroLinkModel, countOfSuccess.get(0)));

        int maxHiddenId = Integer.MIN_VALUE;
        for (Map.Entry<Integer, NeuronLayerModel> e : neuroLinkModel.getHiddenMap().entrySet()) {
            builder.putHidden(e.getKey(), mutateHiddenLayer(e.getKey(), neuroLinkModel, countOfSuccess.get(e.getKey())));
            if (e.getKey() > maxHiddenId) {
                maxHiddenId = e.getKey();
            }
        }

        MutateService mutateService = new MutateService(neuroLinkModel.getHiddenOrThrow(maxHiddenId), evolutionProperties.getFactor());
        boolean isCreateHidden = isChance(mutateService.calcChance(0, CHANCE_HIDDEN));
        if (isCreateHidden) {
            createLayer(builder, maxHiddenId);
        }

        return builder.setResult(neuroLinkModel.getResult())
                .setType(neuroLinkModel.getType())
                .build();
    }

    private void createLayer(NeuroLinkModel.Builder neuroLinkModel, int maxHiddenId) {
        NeuronLayerModel.Builder lastLayer = neuroLinkModel.getHiddenOrThrow(maxHiddenId).toBuilder();
        MutateService mutateService = new MutateService(lastLayer.build(), evolutionProperties.getFactor());

        NeuronLayerModel.Builder builder = NeuronLayerModel.newBuilder();
        mutateLayersChance(builder, lastLayer.build(), mutateService);

        NeuronModel newNeuron = createNewNeuron(builder.getNeuronsList(), null, mutateService);
        builder.addNeurons(newNeuron);

        List<NeuronModel> lastLayerNeuronsNew = new ArrayList<>();
        for (NeuronModel n : lastLayer.getNeuronsList()) {
            NeuronModel.Builder nB = n.toBuilder();
            nB.addLinks(newNeuron.getId());
            lastLayerNeuronsNew.add(nB.build());
        }
        lastLayer.clearNeurons();
        lastLayer.addAllNeurons(lastLayerNeuronsNew);
        neuroLinkModel.putHidden(maxHiddenId, lastLayer.build());
    }

    private NeuronLayerModel mutateInputLayer(NeuroLinkModel neuroLinkModel, Map<Long, Long> countOfSuccess) {
        NeuronLayerModel input = neuroLinkModel.getInput();
        NeuronLayerModel nextLayer = findMinHidden(neuroLinkModel, 0);
        MutateService mutateService = new MutateService(input, evolutionProperties.getFactor());

        NeuronLayerModel.Builder builder = NeuronLayerModel.newBuilder();
        mutateLayersChance(builder, input, mutateService);

        for (NeuronModel n : input.getNeuronsList()) {
            builder.addNeurons(mutateNeuronModel(n, nextLayer, countOfSuccess.get(n.getId()), mutateService));
        }

        boolean isCreateNeuron = isChance(mutateService.calcChance(0, CHANCE_NEURON));
        if (isCreateNeuron) {
            if (input.getNeuronsList().isEmpty()) {
                builder.addNeurons(createNewNeuron(input.getNeuronsList(), nextLayer, mutateService));
            } else {
                builder.addNeurons(cloneRandomNeuron(input.getNeuronsList(), mutateService));
            }
        }
        return builder.build();
    }

    private void mutateLayersChance(NeuronLayerModel.Builder builder, NeuronLayerModel input, MutateService mutateService) {
        long epochCount = 0;
        builder.setMutateDoubleMax(mutateService.mutateDouble(input.getMutateDoubleMax(), epochCount));
        builder.setMutateDoubleBase(mutateService.mutateDouble(input.getMutateDoubleBase(), epochCount));
        builder.setBaseChanceDouble(mutateService.mutateDouble(input.getBaseChanceDouble(), epochCount));
        builder.setBaseChanceLink(mutateService.mutateDouble(input.getBaseChanceLink(), epochCount));
        builder.setBaseChanceNeuron(mutateService.mutateDouble(input.getBaseChanceNeuron(), epochCount));
        builder.setCountBase(mutateService.mutateDouble(input.getCountBase(), epochCount));
        builder.setCountMin(mutateService.mutateDouble(input.getCountMin(), epochCount));
    }

    private NeuronModel cloneRandomNeuron(List<NeuronModel> actualNeurons, MutateService mutateService) {
        NeuronModel parent;
        if (actualNeurons.size() == 1) {
            parent = actualNeurons.get(0);
        } else {
            parent = actualNeurons.get(
                    ThreadLocalRandom.current().nextInt(0, actualNeurons.size() - 1));
        }
        NeuronModel.Builder builder = parent.toBuilder().clone();
        long maxId = 0;
        int currentMaxCandleId = Integer.MIN_VALUE;
        for (NeuronModel n : actualNeurons) {
            if (n.getId() > maxId) {
                maxId = n.getId();
            }
            if (n.hasIndexOfCandle() && n.getIndexOfCandle() > currentMaxCandleId) {
                currentMaxCandleId = n.getIndexOfCandle();
            }
        }

        if (currentMaxCandleId != Integer.MIN_VALUE) {
            int indexOfCandle;
            //try to add next index of candle
            if (isChance(mutateService.calcChance(0, CHANCE_LINK))
                    && currentMaxCandleId < MAX_CANDLE_INDEX) {
                indexOfCandle = currentMaxCandleId + 1;
            } else {
                if (currentMaxCandleId == 0) {
                    indexOfCandle = 0;
                } else {
                    indexOfCandle = ThreadLocalRandom.current().nextInt(0, currentMaxCandleId);
                }
            }
            builder.setIndexOfCandle(indexOfCandle);
        }

        boolean isPositive = isChance(mutateService.calcChance(0, FIFTY_CHANCE));

        if (parent.getPositive() == isPositive) {
            return builder
                    .setPositive(isPositive)
                    .setId(maxId + 1)
                    .setEpochCount(0)
                    .build();
        } else {
            return builder
                    .setPositive(isPositive)
                    .setId(maxId + 1)
                    .setEpochCount(0)
                    .setThreshold(0)
                    .setWeight(1)
                    .build();
        }
    }

    private NeuronModel createNewNeuron(List<NeuronModel> actualNeurons,
                                        NeuronLayerModel nextLayer,
                                        MutateService mutateService) {
        long maxId = 0;
        int currentMaxCandleId = Integer.MIN_VALUE;
        for (NeuronModel n : actualNeurons) {
            if (n.getId() > maxId) {
                maxId = n.getId();
            }
            if (n.hasIndexOfCandle() && n.getIndexOfCandle() > currentMaxCandleId) {
                currentMaxCandleId = n.getIndexOfCandle();
            }
        }

        NeuronModel.Builder builder = NeuronModel.newBuilder();

        if (currentMaxCandleId != Integer.MIN_VALUE) {
            int indexOfCandle;
            //try to add next index of candle
            if (isChance(mutateService.calcChance(0, CHANCE_LINK))
                    && currentMaxCandleId < MAX_CANDLE_INDEX) {
                indexOfCandle = currentMaxCandleId + 1;
            } else {
                if (currentMaxCandleId == 0) {
                    indexOfCandle = 0;
                } else {
                    indexOfCandle = ThreadLocalRandom.current().nextInt(0, currentMaxCandleId);
                }
            }
            builder.setIndexOfCandle(indexOfCandle);
        }

        if (!actualNeurons.isEmpty() && actualNeurons.iterator().next().getLinksCount() > 0) {
            builder.addLinks(findRandomLink(
                    nextLayer.getNeuronsList().stream().map(NeuronModel::getId).collect(Collectors.toSet())
            ));
        }

        return builder
                .setId(maxId + 1)
                .setPositive(isChance(mutateService.calcChance(0, FIFTY_CHANCE)))
                .setThreshold(0)
                .setWeight(1)
                .setEpochCount(0)
                .build();
    }

    private long findRandomLink(Set<Long> ids) {
        long min = ids.stream().min(Comparator.naturalOrder()).orElse(0L);
        long max = ids.stream().max(Comparator.naturalOrder()).orElse(0L);
        if (min == max) {
            return min;
        }
        long link;
        int count = 0;
        while (true) {
            link = ThreadLocalRandom.current().nextLong(min, max);
            if (ids.contains(link)) {
                break;
            }
            count++;
            if (count > (max - min) * 100) {
                break;
            }
        }
        return link;
    }

    private NeuronModel mutateNeuronModel(NeuronModel n,
                                          NeuronLayerModel nextLayer,
                                          Long countOfSuccess,
                                          MutateService mutateService) {
        long epochCount = n.getEpochCount();
        if (countOfSuccess == null) {
            countOfSuccess = 0L;
        }
        NeuronModel.Builder builder = NeuronModel.newBuilder();
        builder.setId(n.getId());
        if (n.hasIndexOfCandle()) {
            builder.setIndexOfCandle(n.getIndexOfCandle());
        }
        builder.setPositive(n.getPositive());


        Set<Long> currentLinks = new HashSet<>();
        for (long link : n.getLinksList()) {
            boolean isDeathLink = isChance(mutateService.calcChance(countOfSuccess, CHANCE_LINK));
            if (isDeathLink) {
                continue;
            }
            builder.addLinks(link);
            currentLinks.add(link);
        }

        boolean isCreateLink = isChance(mutateService.calcChance(epochCount, CHANCE_LINK));
        if (isCreateLink && nextLayer != null) {
            Set<Long> nextIds = new HashSet<>();
            for (NeuronModel nextNeuron : nextLayer.getNeuronsList()) {
                nextIds.add(nextNeuron.getId());
            }
            nextIds.removeAll(currentLinks);
            if (!nextIds.isEmpty()) {
                builder.addLinks(findRandomLink(nextIds));
            }
        }

        double newThreshold = mutateService.mutateDouble(n.getThreshold(), epochCount);
        builder.setThreshold(newThreshold);
        double newWeight = mutateService.mutateDouble(n.getWeight(), epochCount);
        builder.setWeight(newWeight);
        if (newThreshold != n.getThreshold() || newWeight != n.getWeight()) {
            builder.setEpochCount(epochCount + 1);
        } else {
            builder.setEpochCount(epochCount);
        }
        return builder.build();
    }

    private NeuronLayerModel findMinHidden(NeuroLinkModel neuroLinkModel, int after) {
        int min = Integer.MAX_VALUE;
        for (Map.Entry<Integer, NeuronLayerModel> e : neuroLinkModel.getHiddenMap().entrySet()) {
            if (e.getKey() < min && e.getKey() > after) {
                min = e.getKey();
            }
        }
        return neuroLinkModel.getHiddenOrThrow(min);
    }

    private NeuronLayerModel mutateHiddenLayer(Integer id, NeuroLinkModel neuroLinkModel, Map<Long, Long> countOfSuccess) {
        NeuronLayerModel hidden = neuroLinkModel.getHiddenOrThrow(id);
        MutateService mutateService = new MutateService(hidden, evolutionProperties.getFactor());

        NeuronLayerModel.Builder builder = NeuronLayerModel.newBuilder();
        mutateLayersChance(builder, hidden, mutateService);

        NeuronLayerModel nextLayer = neuroLinkModel.getHiddenOrDefault(id + 1, null);

        for (NeuronModel n : hidden.getNeuronsList()) {
            builder.addNeurons(mutateNeuronModel(n, nextLayer, countOfSuccess.get(n.getId()), mutateService));
        }

        boolean isCreateNeuron = isChance(mutateService.calcChance(0, CHANCE_NEURON));
        if (isCreateNeuron) {
            if (hidden.getNeuronsList().isEmpty()) {
                builder.addNeurons(createNewNeuron(hidden.getNeuronsList(), nextLayer, mutateService));
            } else {
                builder.addNeurons(cloneRandomNeuron(hidden.getNeuronsList(), mutateService));
            }
        }

        return builder.build();
    }


    private NsrsiProperties extractProperties(Evolution e) {
        return (NsrsiProperties) jsonToProperty(e.getProperties(), new NsrsiProperties());
    }
}
