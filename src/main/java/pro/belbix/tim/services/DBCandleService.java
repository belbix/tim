package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.models.CreateCandleRequest;
import pro.belbix.tim.repositories.CandleRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class DBCandleService implements ICandleService {
    private static final Logger log = LoggerFactory.getLogger(DBCandleService.class);
    private static final boolean trace = false;
    private static final int MAX_CACHED_TICKS = 100_000_000;
    private final CandleRepository candleRepository;
    private final TickService tickService;
    TreeSet<Tick> cachedTicks = new TreeSet<>();


    @Autowired
    public DBCandleService(CandleRepository candleRepository, TickService tickService) {
        this.candleRepository = candleRepository;
        this.tickService = tickService;
    }

    public List<Candle> loadCandlesAfter(CandleRequest candleRequest) {
        Pageable pageable = PageRequest.of(0, candleRequest.getCount());
        return candleRepository.loadCandlesAfter(candleRequest.getServer(),
                candleRequest.getSymbol(),
                candleRequest.getTimeFrame(),
                candleRequest.getAfterDate(),
                pageable);
    }

    @Override
    public List<Candle> loadCandles(CandleRequest candleRequest) {
        Instant start = Instant.now();
        if (!candleRequest.isValid()) throw new TIMRuntimeException("Invalid candle request");
        Pageable pageable = PageRequest.of(0, candleRequest.getCount());
        LocalDateTime afterDate = candleRequest.getBeforeDate()
                .minus(candleRequest.getTimeFrame() * candleRequest.getCount(), ChronoUnit.MINUTES);
        List<Candle> candles = candleRepository.findCandles(candleRequest.getServer(),
                candleRequest.getSymbol(),
                candleRequest.getTimeFrame(),
                candleRequest.getBeforeDate(),
                afterDate,
                pageable);
        if (trace) {
            log.info("findCandles by " + Duration.between(start, Instant.now()).toMillis() + "ms");
            start = Instant.now();
        }
        if (candles != null && candles.size() != candleRequest.getCount()) {
            log.warn("Invalid candle count, expected " + candleRequest.getCount()
                    + " but was " + candles.size());
            return new ArrayList<>();
        }

        if (candles != null && !candles.isEmpty() && candleRequest.isFirstDynamic()) {
            try {
                Candle firstCandle = candleFromTicks(candles.get(0),
                        candleRequest.getBeforeDate(), candleRequest.isOnlyClose());
                candles.set(0, firstCandle);
            } catch (TIMRuntimeException e) {
                if (!e.getMessage().equals("Empty ticks")) {
                    log.error("Error add dynamic candle for " + candles.get(0) + " -> " + e.getMessage());
                } else {
                    Candle first = candles.get(0);
                    first.setClose(first.getOpen());
                    first.setHigh(first.getOpen());
                    first.setLow(first.getOpen());
                }
            }

        }

        return candles;
    }

    @Override
    public List<Candle> loadMultiCandles(CandleRequest candleRequest) {
        if (!candleRequest.isValid()) throw new TIMRuntimeException("Invalid candle request");
        Set<Integer> ts = new HashSet<>();
        ts.add(candleRequest.getTimeFrame());
        Set<Candle> candles = new TreeSet<>();
        candleRequest.setAfterDate(candleRequest.getBeforeDate().minus(candleRequest.getTimeFrame(), ChronoUnit.MINUTES));
        for (int i = 0; i < candleRequest.getCount(); i++) {
            List<Candle> cs = processTicksForCreatingCandles(candleRequest.getServer(),
                    candleRequest.getSymbol(),
                    cachedTicks,
                    candleRequest.getAfterDate(),
                    candleRequest.getBeforeDate(),
                    ts, Candle.Accuracy.MINUTE,
                    candleRequest.isOnlyClose());
            candleRequest.setAfterDate(candleRequest.getAfterDate().minus(candleRequest.getTimeFrame(), ChronoUnit.MINUTES));
            candleRequest.setBeforeDate(candleRequest.getBeforeDate().minus(candleRequest.getTimeFrame(), ChronoUnit.MINUTES));
            if (cs == null) continue;
            candles.addAll(cs);
        }
        return new ArrayList<>(candles);
    }

    @Override
    public Candle candleFromTicks(Candle candle, LocalDateTime dateBefore, boolean onlyClose) {
        if (!candle.isValidDate()) {
            throw new TIMRuntimeException("Candle date is invalid " + candle);
        }
        LocalDateTime candleMax = candle.getDate().plus(candle.getTime(), ChronoUnit.MINUTES);
        if (dateBefore.isAfter(candleMax)) {
            throw new TIMRuntimeException("Date before " + dateBefore + " is too high for " + candle);
        }
        Instant start = Instant.now();
        List<Tick> ticks = tickService.getTicksCached(
                candle.getServer(),
                candle.getSymbol(),
                candle.getDate(),
                dateBefore,
                onlyClose);
        if (trace) {
            log.info("getTicksCached by " + Duration.between(start, Instant.now()).toMillis() + "ms");
        }
        LocalDateTime dateAfter = Candle.calcStartDateFromTimeFrame(candle.getDate(), candle.getTime(), Candle.Accuracy.DAY);
        return TickService.candleFromTicks(ticks, candle.getTime(), dateAfter, dateBefore, true);
    }

    public int maxTimeFrame(Set<Integer> timeFrames) {
        int maxTimeFrame = 0;
        for (int tf : timeFrames) {
            if (tf > maxTimeFrame) maxTimeFrame = tf;
        }
        return maxTimeFrame;
    }

    public int minTimeFrame(Set<Integer> timeFrames) {
        int minTimeFrame = 0;
        for (int tf : timeFrames) {
            if (tf < minTimeFrame) minTimeFrame = tf;
        }
        return minTimeFrame;
    }

    public List<Tick> loadNewTicks(CreateCandleRequest r) {
        int maxTimeFrame = maxTimeFrame(r.getTimeframes());

        LocalDateTime startTimeTS = r.getStartDate()
                .minus(maxTimeFrame, ChronoUnit.MINUTES)
                .minus(10, ChronoUnit.SECONDS);
        LocalDateTime endTimeTS = r.getStartDate()
                .plus(maxTimeFrame, ChronoUnit.MINUTES)
                .plus(10, ChronoUnit.SECONDS
                );
        log.trace("Get ticks for candle from " + startTimeTS + " to " + endTimeTS);
        List<Tick> ticks = tickService.getTicksCached(r.getServer(), r.getSymbol(), startTimeTS, endTimeTS, false);
        if (ticks.isEmpty()) {
            throw new TIMRuntimeException("Empty ticks for building candles");
        }
        return ticks;
    }

    public Candle saveCandle(Candle candle) {
        return saveCandle(candle, true);
    }

    public Candle saveCandle(Candle candle, boolean saveExist) {
        if (candle == null) return null;
        Candle existCandle = candleRepository.getByServerAndSymbolAndDateAndTime(
                candle.getServer(),
                candle.getSymbol(),
                candle.getDate(),
                candle.getTime());

        if (existCandle != null) {
            if (saveExist) {
                candle.setId(existCandle.getId());
            } else {
                log.error("This candle exist: " + candle);
                return null;
            }

        }
        candleRepository.save(candle);
        return candle;
    }

    @Override
    public Candle lastCandle(String server, String symbol, int timeframe) {
        Pageable pageable = PageRequest.of(0, 1);
        List<Candle> candles = candleRepository.lastCandle(server, symbol, timeframe, pageable);
        if (candles == null || candles.isEmpty()) return null;
        return candles.get(0);
    }

    private void addInCacheTicks(TreeSet<Tick> cachedTicks, List<Tick> newTicks, int maxTimeFrame) {
        if (cachedTicks == null) return;
        cachedTicks.addAll(newTicks);
//        if (cachedTicks.size() > MAX_CACHED_TICKS) {
//            cachedTicks.removeIf(tick -> (
//                    tick.getDate().isBefore(
//                            tick.getDate().minus(maxTimeFrame * 10, ChronoUnit.MINUTES)
//                    )
//            ));
//        }
    }

    public List<Candle> processTicksForCreatingCandles(String server, String symbol, TreeSet<Tick> cachedTicks,
                                                       LocalDateTime startForNewTicks, LocalDateTime endForNewTicks,
                                                       Set<Integer> timeframes, Candle.Accuracy accuracy,
                                                       boolean onlyClose) {
        List<Tick> newTicks = tickService.getTicksCached(server, symbol, startForNewTicks, endForNewTicks, onlyClose);
        if (newTicks == null || newTicks.isEmpty()) {
            log.info("Empty ticks start " + startForNewTicks + " end " + endForNewTicks);
            return null;
        }
        int maxTimeFrame = maxTimeFrame(timeframes);
        addInCacheTicks(cachedTicks, newTicks, maxTimeFrame);

        log.debug("Ticks loaded:" + newTicks.size()
                + " first:" + cachedTicks.first().getDate() + " last: " + cachedTicks.last().getDate());
        List<Candle> candlesForSave = new ArrayList<>();
        for (int timeframe : timeframes) {
            TreeSet<Tick> loadedTicksTree = new TreeSet<>(newTicks);
            LocalDateTime firstDateForCandle = Candle.calcStartDateFromTimeFrame(loadedTicksTree.first().getDate(), timeframe, accuracy);
            LocalDateTime lastDateForCandle = Candle.calcStartDateFromTimeFrame(loadedTicksTree.last().getDate(), timeframe, accuracy);
            List<Candle> candles = createCandleFromTicks(cachedTicks, timeframe, firstDateForCandle, lastDateForCandle);
            if (candles.isEmpty()) {
                log.error("Candles not created for " + timeframe);
            }
            log.debug("Candles created: " + candles.size() + " for " + timeframe);
            candlesForSave.addAll(candles);
        }
        return candlesForSave;
    }

    private List<Tick> cropTicks(TreeSet<Tick> cachedTicks, LocalDateTime startDateForCandle, LocalDateTime endDateForCandle) {
        List<Tick> ticksForCandleWithClear = new ArrayList<>();

        Tick mockFirstTick = new Tick();
        mockFirstTick.setStrId(UUID.randomUUID().toString());
        mockFirstTick.setDate(startDateForCandle.minus(1, ChronoUnit.MILLIS));
        Tick mockLastTick = new Tick();
        mockLastTick.setStrId(UUID.randomUUID().toString());
        mockLastTick.setDate(endDateForCandle.plus(1, ChronoUnit.MILLIS));

        SortedSet<Tick> croppedTicks = cachedTicks.tailSet(mockFirstTick).headSet(mockLastTick);

        boolean hasFirst = false;
        for (Tick tick : croppedTicks) {
            if (tick.getDate().isBefore(startDateForCandle)) {
                log.error("Error tick(old) in cropped set " + tick); //TODO delete this section after sure this work
                continue;
            }
            if (tick.getDate().isAfter(endDateForCandle)) {
                log.error("Error tick(new) in cropped set " + tick);
                continue;
            }
            if (tick.getDate().equals(startDateForCandle)) hasFirst = true;
            ticksForCandleWithClear.add(tick);
        }

        if (!hasFirst) {
            SortedSet<Tick> headed = cachedTicks.headSet(mockFirstTick);
            if (!headed.isEmpty()) {
                Tick fakeFirst = headed.last().copy();
                fakeFirst.setDate(startDateForCandle);
                ticksForCandleWithClear.add(fakeFirst);
            }
        }

        return ticksForCandleWithClear;
    }

    public List<Candle> createCandleFromTicks(TreeSet<Tick> cachedTicks, int timeFrame, LocalDateTime firstDateForCandle, LocalDateTime lastDateForCandle) {
        List<Candle> candles = new ArrayList<>();
        LocalDateTime startDateForCandle = firstDateForCandle;

        while (startDateForCandle.isBefore(lastDateForCandle) || startDateForCandle.equals(lastDateForCandle)) {
            LocalDateTime endDateForCandle = startDateForCandle.plus(timeFrame, ChronoUnit.MINUTES);

            List<Tick> ticksForCandleWithClear = cropTicks(cachedTicks, startDateForCandle, endDateForCandle);
            if (ticksForCandleWithClear.isEmpty()) {
                log.error("ticksForCandle is empty");
                return candles;
            }

            Candle candle = TickService.candleFromTicks(ticksForCandleWithClear,
                    timeFrame,
                    startDateForCandle,
                    endDateForCandle,
                    true);
            if (candle == null) {
                log.error("Candle not created for " + timeFrame);
                break;
            }
            startDateForCandle = endDateForCandle;

            boolean exist = false;
            for (Candle c : candles) {
                if (c.getDate().equals(candle.getDate())) {
                    log.error("We have the same candle: " + candle);
                    exist = true;
                }
            }
            if (!exist) candles.add(candle);
        }
        if (candles.isEmpty()) {
            log.error("Candles not created for " + timeFrame);
        }
        return candles;
    }

}
