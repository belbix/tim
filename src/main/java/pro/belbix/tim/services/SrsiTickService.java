package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Srsi2Tick;
import pro.belbix.tim.entity.SrsiTick;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.properties.HistoryProperties;
import pro.belbix.tim.properties.StrategyProperties;
import pro.belbix.tim.repositories.Srsi2TickRepository;
import pro.belbix.tim.repositories.SrsiTickRepository;
import pro.belbix.tim.utils.Indicators;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;
import static pro.belbix.tim.history.HistoryProcessor.createCandleRequest;
import static pro.belbix.tim.history.HistoryProcessor.loadCandles;

@Service
public class SrsiTickService {
    private static final Logger log = LoggerFactory.getLogger(SrsiTickService.class);

    private final boolean trace = false;

    private final TickService tickService;
    private final StrategyProperties strategyProperties;
    private final HistoryProperties historyProperties;
    private final DBCandleService dbCandleService;
    private final SrsiTickRepository srsiTickRepository;
    private final Srsi2TickRepository srsi2TickRepository;
    private final JpaService jpaService;

    private Map<Long, List<SrsiTickI>> srsiTicks = new HashMap<>();

    public SrsiTickService(TickService tickService,
                           StrategyProperties strategyProperties,
                           HistoryProperties historyProperties,
                           DBCandleService dbCandleService,
                           SrsiTickRepository srsiTickRepository,
                           JpaService jpaService,
                           Srsi2TickRepository srsi2TickRepository) {
        this.tickService = tickService;
        this.strategyProperties = strategyProperties;
        this.historyProperties = historyProperties;
        this.dbCandleService = dbCandleService;
        this.srsiTickRepository = srsiTickRepository;
        this.jpaService = jpaService;
        this.srsi2TickRepository = srsi2TickRepository;
    }

    public void create(int version) {
        log.info("Start creating compressed SRSI");

        List<Candle> loadedCandles = new ArrayList<>();
        LocalDateTime dateStart;
        LocalDateTime lastDate = lastDate(version);
        if (lastDate == null) {
            dateStart = tickService.getMinDateFromDb(strategyProperties.getServer(), strategyProperties.getSymbol());
        } else {
            dateStart = lastDate.plus(1, SECONDS);
        }
        LocalDateTime dateEnd =
                tickService.getMaxDateFromDb(strategyProperties.getServer(), strategyProperties.getSymbol());
        ICandleService.CandleRequest candleRequest = createCandleRequest(
                strategyProperties,
                historyProperties.getBatch(),
                strategyProperties.getTimeframeLong());
        List<SrsiTickI> buffer = new ArrayList<>();
        int count = 0;
        Instant startBatch = Instant.now();
        while (dateStart.isBefore(dateEnd)) {
            Instant startLoop = Instant.now();
            candleRequest.setBeforeDate(dateStart);
            Instant start = Instant.now();
            List<Candle> candles = loadCandles(dbCandleService, candleRequest, loadedCandles, historyProperties.getBatch());
            if (trace) {
                log.info("Load candles by: " + Duration.between(start, Instant.now()).toMillis() + "ms");
                start = Instant.now();
            }
            if (candles == null || candles.isEmpty()) {
                LocalDateTime newDateStart = dateStart.plus(historyProperties.getEmptyTicktime(), ChronoUnit.MINUTES);
                log.info(dateStart + " Empty candles, continue to " + newDateStart + "\n" + candleRequest);
                dateStart = newDateStart;
                continue;
            }

            Indicators.stochasticRsi(candles);
            if (trace) {
                log.info("Create stochastic by: " + Duration.between(start, Instant.now()).toMillis() + "ms");
                start = Instant.now();
            }
            SrsiTickI srsiTick = createFromCandle(candles, dateStart, version);

            if (trace) {
                log.info("Create srsi by: " + Duration.between(start, Instant.now()).toMillis() + "ms");
            }

            buffer.add(srsiTick);
            count++;

            if (buffer.size() >= 10_000) {
                log.info("start creating, last date is " + dateStart);
                jpaService.batchNativeSrsiTickSave(buffer, version);
                buffer.clear();
                log.info("srsi tick created, " + count
                        + " for " + Duration.between(startBatch, Instant.now()).toSeconds() + "s");
                startBatch = Instant.now();
            }

            dateStart = dateStart.plus(historyProperties.getTicktime(), SECONDS);
            if (trace) {
                log.info("End loop by: " + Duration.between(startLoop, Instant.now()).toMillis() + "ms");
            }
        }
        log.info("SrsiTicks creating complete");
    }

    public List<SrsiTickI> deleteDuplicate(List<SrsiTickI> srsiTicks, int version) {
        SrsiTickI prev = null;
        List<SrsiTickI> newSrsi = new ArrayList<>();
        int count = 0;
        for (SrsiTickI srsiTick : srsiTicks) {
            if (prev == null) {
                prev = srsiTick;
                continue;
            }

            if (srsiTick.compare(prev)) {
                delete(srsiTick, version);
                count++;
                continue;
            }
            newSrsi.add(srsiTick);
            prev = srsiTick;

        }

        log.info(count + " SrsiTicks removed, new size: " + newSrsi.size());
        return newSrsi;
    }

    public List<SrsiTickI> load(LocalDateTime start, LocalDateTime end, int version) {
        Long key = Duration.between(start, end).toSeconds();
        if (srsiTicks.containsKey(key)) {
            return srsiTicks.get(key);
        } else {
            List<SrsiTickI> ticks = loadFromDb(start, end, version);
            if (strategyProperties.isDeleteDuplicate()) {
                ticks = deleteDuplicate(ticks, version);
            }
            srsiTicks.put(key, ticks);
            return ticks;
        }
    }

    private List<SrsiTickI> loadFromDb(LocalDateTime start, LocalDateTime end, int version) {
        List<SrsiTickI> ticks;
        if (version == 1) {
            ticks = srsiTickRepository.load(start, end, historyProperties.getTicktime());
        } else {
            ticks = srsi2TickRepository.load(start, end);
        }
        return ticks;
    }

    private LocalDateTime lastDate(int version) {
        List<LocalDateTime> lastDate;
        if (version == 1) {
            lastDate = srsiTickRepository.lastDate(historyProperties.getTicktime(), PageRequest.of(0, 1));
        } else {
            lastDate = srsi2TickRepository.lastDate(PageRequest.of(0, 1));
        }
        log.info("Compressed SRSI lastDate" + lastDate);
        if (lastDate != null && !lastDate.isEmpty()) return lastDate.get(0);
        return null;
    }

    private SrsiTickI createFromCandle(List<Candle> candles, LocalDateTime date, int version) {
        SrsiTickI srsiTick;
        if (version == 1) {
            srsiTick = SrsiTick.createFromCandle(candles, date, historyProperties.getTicktime());
        } else {
            srsiTick = Srsi2Tick.createFromCandle(candles, date, historyProperties.getSrsi2Deep());
        }
        return srsiTick;
    }

    private void delete(SrsiTickI t, int version) {
        if (version == 1) {
            srsiTickRepository.delete((SrsiTick) t);
        } else {
            srsi2TickRepository.delete((Srsi2Tick) t);
        }
    }
}
