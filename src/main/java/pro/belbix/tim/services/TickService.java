package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.properties.TickDownloaderProperties;
import pro.belbix.tim.repositories.TickRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

@Component
public class TickService {
    private static final Logger log = LoggerFactory.getLogger(TickService.class);
    private static boolean multiCandles = false; //TODO make non static
    private final TickRepository tickRepository;
    private final JpaService jpaService;
    private final AppContextService appContextService;
    private final TickDownloaderProperties tickDownloaderProperties;
    @Value("${tim.ticks.max-cached-hours}")
    private int maxCachedHours = 12;
    private Exchange exchange = null;
    private TreeSet<Tick> cachedTicks = new TreeSet<>();

    @Autowired
    public TickService(TickRepository tickRepository, JpaService jpaService, AppContextService appContextService, TickDownloaderProperties tickDownloaderProperties) {
        this.tickRepository = tickRepository;
        this.jpaService = jpaService;
        this.appContextService = appContextService;
        this.tickDownloaderProperties = tickDownloaderProperties;
    }

    public static Candle candleFromTicks(List<Tick> ticks,
                                         int timeframe,
                                         LocalDateTime start,
                                         LocalDateTime end,
                                         boolean clearTicks) {
        if (ticks == null || ticks.isEmpty())
            throw new TIMRuntimeException("Empty ticks");

        if (end.isAfter(start.plus(timeframe, ChronoUnit.MINUTES))) {
            throw new TIMRuntimeException("End date is invalid start:"
                    + start + "(" + start.plus(timeframe, ChronoUnit.MINUTES) + ") end:" + end + " timeframe:" + timeframe);
        }
//        LocalDateTime candleTime = Candle.calcStartDateFromTimeFrame(start, timeframe, Candle.Accuracy.DAY);
        LocalDateTime candleTime = start;

//        log.trace("Create candle from ticks, where time:" + timeframe + " start:" + start + " end:" + end + " soid:" + stopOnInvalidDate);

        Tick firstTick = ticks.get(0);
        Tick lastTick = ticks.get(ticks.size() - 1);
        Candle candle = new Candle();


        candle.setDate(candleTime);
        candle.setTime(timeframe);
        if (!multiCandles && !candle.isValidDate()) {
            log.warn("Candle have invalid date " + candle);
            return null;
        }

        candle.setServer(firstTick.getServer());
        candle.setSymbol(firstTick.getSymbol());

        Tick openTick = null;
        Tick closeTick = null;

        double maxPrice = 0;
        double minPrice = Float.MAX_VALUE;
        double volumeBuy = 0;
        double volumeSell = 0;
        double amount = 0;
        for (Tick tick : ticks) {
            if (tick.getDate().isBefore(candle.getDate()) || tick.getDate().isAfter(end)) {
                if (clearTicks) {
                    throw new TIMRuntimeException("Tick date invalid: " + tick.getDate()
                            + " start: " + candle.getDate() + " end: " + end);
                } else {
                    continue;
                }
            }

            if (openTick == null) {
                openTick = tick;
            } else {
                if (openTick.getDate().isAfter(tick.getDate())) {
                    openTick = tick;
                }
            }

            if (closeTick == null) {
                closeTick = tick;
            } else {
                if (closeTick.getDate().isBefore(tick.getDate())) {
                    closeTick = tick;
                }
            }
            if (tick.getBuy() != null) {
                if (tick.getBuy()) {
                    volumeBuy += tick.getAmount();
                } else {
                    volumeSell += tick.getAmount();
                }
            }
            amount += 1;
            if (tick.getPrice() > maxPrice) {
                maxPrice = tick.getPrice();
            }
            if (tick.getPrice() < minPrice) {
                minPrice = tick.getPrice();
            }
        }
        if (maxPrice == 0) {
            log.trace("No data for candle from this ticks");
            return null; // this case available if you dont stop with invalid date
        }
        candle.setOpen(openTick.getPrice());
        candle.setClose(closeTick.getPrice());
        candle.setAmount(amount);
        candle.setVolumeBuy(volumeBuy);
        candle.setVolumeSell(volumeSell);
        candle.setHigh(maxPrice);
        candle.setLow(minPrice);
        candle.setTickTime(lastTick.getDate());
        return candle;
    }

    @PostConstruct
    private void findExchanges() {
        this.exchange = appContextService.findExchange(tickDownloaderProperties.getServer());
        if (this.exchange == null)
            throw new TIMRuntimeException("Exchange not found: " + tickDownloaderProperties.getServer());
    }

    public List<Tick> getLastTicksFromExchange(int limit) throws TIMRetryException {
        return exchange.historyTicks(tickDownloaderProperties.getSymbol(), limit, null, null, 0);
    }

    public LocalDateTime getMaxDateFromDb(String server, String symbol) {
        return tickRepository.getMaxDate(server, symbol, PageRequest.of(0, 1))
                .iterator().next();
    }

    public List<Tick> getTicksAfterDate(String server, String symbol, LocalDateTime dateStart, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return tickRepository.getTicksAfterDate(server, symbol, dateStart, pageable);
    }

    public List<Tick> getTopTicks(String server, String symbol, int count) {
        Pageable pageable = PageRequest.of(0, count);
        return tickRepository.getTopTicks(server, symbol, pageable);
    }

    public List<Tick> getTopTicksBeforeDate(String server, String symbol, LocalDateTime before, int count) {
        Pageable pageable = PageRequest.of(0, count);
        return tickRepository.getTopTicksBeforeDate(server, symbol, before, pageable);
    }

    public List<Tick> getTicks(String server,
                               String symbol,
                               LocalDateTime dateStart,
                               LocalDateTime dateEnd,
                               boolean onlyClose) {
        log.debug("Load new ticks start: " + dateStart + " end: " + dateEnd);
        if (onlyClose) {
            Pageable pageable = PageRequest.of(0, 1);
            return tickRepository.getTicksClose(server, symbol, dateStart, dateEnd, pageable);
        } else {
            return tickRepository.getTicks(server, symbol, dateStart, dateEnd);
        }

    }

    public Tick getTickAtDate(String server, String symbol, LocalDateTime dateStart) {
        Pageable pageable = PageRequest.of(0, 1);
        List<Tick> ticks = tickRepository.getTicksAtDate(server, symbol, dateStart, pageable);
        if (ticks == null || ticks.isEmpty()) return null;
        return ticks.get(0);
    }

    public List<Tick> getTicksCached(String server,
                                     String symbol,
                                     LocalDateTime dateStart,
                                     LocalDateTime dateEnd,
                                     boolean onlyClose) {
        if (!tickDownloaderProperties.isUseCache()) {
            return getTicks(server, symbol, dateStart, dateEnd, onlyClose);
        }
        LocalDateTime afterDate;
        if (cachedTicks.isEmpty()) {
            afterDate = dateStart;
        } else {
            afterDate = cachedTicks.last().getDate();
            if (afterDate.isAfter(dateEnd)) afterDate = dateStart;
        }
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Tick> ticks = tickRepository
                .getTopTicksAfterAndBeforeDate(server, symbol, afterDate, dateEnd, pageable);
        if (ticks.isEmpty()) return ticks;

        addTicksInCache(ticks);

        Tick fakeTickStart = ticks.get(0).copy();
        fakeTickStart.setDate(dateStart);
        Tick fakeTickEnd = ticks.get(0).copy();
        fakeTickEnd.setDate(dateEnd);

        return new ArrayList<>(cachedTicks
                .tailSet(fakeTickStart, true)
                .headSet(fakeTickEnd, true)
                .descendingSet());
    }

    public List<Tick> getTopTicksBeforeDateCached(String server, String symbol, LocalDateTime before, int count) {
        if (!tickDownloaderProperties.isUseCache()) {
            return getTopTicksBeforeDate(server, symbol, before, count);
        }
        Pageable pageable = PageRequest.of(0, count);
        LocalDateTime afterDate;
        if (cachedTicks.isEmpty()) {
            afterDate = before.minus(maxCachedHours, ChronoUnit.HOURS);
        } else {
            afterDate = cachedTicks.last().getDate();
        }
        List<Tick> ticks = tickRepository
                .getTopTicksAfterAndBeforeDate(server, symbol, afterDate, before, pageable);
        addTicksInCache(ticks);
        return new ArrayList<>(cachedTicks.descendingSet());
    }

    public void saveTicks(List<Tick> ticks) {
        jpaService.batchNativeTickSave(ticks);
    }

    public LocalDateTime getMinDateFromDb(String server, String symbol) {
        return tickRepository.getMinDate(server, symbol, PageRequest.of(0, 1))
                .iterator().next();
    }

    public LocalDateTime getMinDateAfterFromDb(String server, String symbol, LocalDateTime after, LocalDateTime before) {
        return tickRepository.getMinDateAfterAndBefor(server, symbol, after, before, PageRequest.of(0, 1))
                .iterator().next();
    }

    private void addTicksInCache(List<Tick> ticks) {
        if (ticks == null || ticks.isEmpty() || maxCachedHours == 0) return;
        if (!tickDownloaderProperties.isUseCache()) throw new IllegalStateException("Use cache without permission!");
        cachedTicks.addAll(ticks);
        Tick fakeTick = ticks.get(0).copy();
        fakeTick.setDate(fakeTick.getDate().minus(maxCachedHours, ChronoUnit.HOURS));
        try {
            cachedTicks.headSet(fakeTick, true).clear();
//            log.info("cachedTicks size: " + cachedTicks.size());
//            BitmexCommon.fullValidateTicks(new ArrayList<>(cachedTicks));
        } catch (Exception e) {
            log.error("headSet: " + e.getMessage());
        }
    }

    public void clearCache() {
        cachedTicks.clear();
    }

    public void setMiltiCandles(boolean miltiCandles) {
        this.multiCandles = miltiCandles;
    }
}
