package pro.belbix.tim.download;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.TIMRetryException;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.Exchange;
import pro.belbix.tim.models.CreateCandleRequest;
import pro.belbix.tim.properties.TickDownloaderProperties;
import pro.belbix.tim.services.AppContextService;
import pro.belbix.tim.services.DBCandleService;
import pro.belbix.tim.services.Schedulable;
import pro.belbix.tim.services.TickService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static pro.belbix.tim.utils.Common.minDateFromTicks;

@Component
public class TickDownloader implements Schedulable {
    private static final Logger log = LoggerFactory.getLogger(TickDownloader.class);
    private final List<Tick> ticksForSaving = new ArrayList<>();
    private final TickService tickService;
    private final AppContextService appContextService;
    private final DBCandleService candleService;
    private final TickDownloaderProperties prop;
    private boolean stopped = false;
    private LocalDateTime currentDate;
    private Exchange exchange;
    private List<String> existTicksId;
    private Tick firstTick;
    private Tick lastTick;
    private int skipped;
    private int startRequestLimit = 0;

    @Autowired
    public TickDownloader(TickService tickService,
                          AppContextService appContextService,
                          DBCandleService candleService,
                          TickDownloaderProperties prop) {
        this.tickService = tickService;
        this.appContextService = appContextService;
        this.candleService = candleService;
        this.prop = prop;
    }

    @Override
    public void start() {
        if (stopped) {
            return;
        }
        try {
            download();
        } catch (Exception e) {
            log.error("Download error " + e.getMessage());
            if (e.getCause() != null) {
                Throwable eCause1 = e.getCause();
                if (eCause1.getCause() != null && eCause1.getCause().getMessage().startsWith("Duplicate entry")) {
                    LocalDateTime after = this.currentDate;
                    LocalDateTime before = this.currentDate.plus(prop.getFailStepSec(), ChronoUnit.SECONDS);
                    List<Tick> ticks = tickService.getTicks(
                            prop.getServer(),
                            prop.getSymbol(),
                            after,
                            before,
                            false
                    );
                    this.currentDate = minDateFromTicks(ticks, 60);
                    log.info("Find currentSrsiMin date in range " + prop.getFailStepSec() + "sec, now it " + this.currentDate);
                }
            } else {
                clear();
                throw e;
            }
        }

    }

    @Override
    public void stop() {
        stopped = true;
    }

    private void download() {
        Instant start = Instant.now();
        Instant startMainLoop = Instant.now();
        init();
        log.debug("Init end: " + Duration.between(start, Instant.now()));

        LocalDateTime currentDate = this.currentDate;

        start = Instant.now();
        List<Tick> ticks = loadTicks(this.currentDate);
        log.debug("Load ticks end: " + Duration.between(start, Instant.now()));

        start = Instant.now();
        ticks = deleteExistTicks(ticks);
        log.debug("Delete exist ticks end: " + Duration.between(start, Instant.now()));

        start = Instant.now();
        saveTicks(ticks, this.currentDate);
        log.debug("Save ticks end: " + Duration.between(start, Instant.now()));

        nextDate(ticks, skipped, lastTick, firstTick);

        start = Instant.now();
        List<Candle> candles = buildCandlesNew(this.currentDate);
        log.debug("Build candles end: " + Duration.between(start, Instant.now()));

        start = Instant.now();
        saveCandles(candles);
        log.debug("Save candles end: " + Duration.between(start, Instant.now()));

        if (ticks != null) {
            log.info("Loop ticks: " + ticks.size() + "(skipped: " + skipped + ") from "
                    + currentDate + " for " + Duration.between(startMainLoop, Instant.now()));
        }
    }

    public void init() {
        if (currentDate == null) {
            initStartTime();
        }
        if (exchange == null) {
            findExchanges();
        }
        if (existTicksId == null) {
            initExist();
        }
    }

    public void initExist() {
        existTicksId = new ArrayList<>();
        CreateCandleRequest r = new CreateCandleRequest();
        r.setServer(prop.getServer());
        r.setSymbol(prop.getSymbol());
        r.setStartDate(currentDate.minus(10, ChronoUnit.MINUTES));
        if (prop.isInitExist()) {
            r.setTimeframes(prop.getTimeframes());
        } else {
            Set<Integer> ts = new HashSet<>();
            ts.add(1);
            r.setTimeframes(ts);
        }


        List<Tick> newTicks = null;
        try {
            newTicks = candleService.loadNewTicks(r);
            for (Tick tick : newTicks) {
                existTicksId.add(tick.getStrId());
            }
            log.info("Load in exist: " + newTicks.size());
        } catch (Exception e) {
            log.info("Load new ticks: " + e.getMessage());
        }
    }

    public void initStartTime() {
        LocalDateTime lastDateFromDb = tickService.getMaxDateFromDb(prop.getServer(), prop.getSymbol());
        if (prop.getDateStart() != null && !prop.getDateStart().isBlank()) {
            LocalDateTime startDateFromProp = LocalDateTime.parse(prop.getDateStart());
            if (lastDateFromDb == null) {
                currentDate = startDateFromProp;
                log.info("Set last date to " + currentDate);
            } else {
                if (startDateFromProp.isAfter(lastDateFromDb)) {
                    throw new TIMRuntimeException("startDateFromProp.isAfter(lastDateFromDb), " +
                            "you can miss ticks, change start date or delete");

                }
                currentDate = startDateFromProp.plus(1, ChronoUnit.SECONDS);
                log.info("Set last date from PROP to " + currentDate);
            }
        } else {
            if (lastDateFromDb == null) {
                currentDate = LocalDateTime.now().minus(50, ChronoUnit.YEARS);
                log.info("Set last date to " + currentDate);
            } else {
                currentDate = lastDateFromDb.plus(1, ChronoUnit.SECONDS);
                log.info("Set last date from DB to " + lastDateFromDb);
            }
        }
    }

    public void findExchanges() {
        exchange = appContextService.findExchange(prop.getServer());
        if (exchange == null) throw new TIMRuntimeException("Exchange not found");
    }

    private void saveTicks(List<Tick> ticks, LocalDateTime startDate) {
        if (ticks == null || ticks.isEmpty()) return;
        ticksForSaving.addAll(ticks);
        if (ticksForSaving.size() < prop.getTickBuffer()) {
            return;
        }
        Instant start = Instant.now();
        tickService.saveTicks(ticksForSaving);
        log.trace("Save ticks: " + ticksForSaving.size() + "(skipped: " + skipped + ") from "
                + startDate + " for " + Duration.between(start, Instant.now()));
        ticksForSaving.clear();
    }

    private void clear() {
        lastTick = null;
        currentDate = null;
        existTicksId = new ArrayList<>();
        skipped = 0;
        initStartTime();
        initExist();
    }

    private void saveCandles(List<Candle> candles) {
        if (candles == null || candles.isEmpty()) return;
        for (Candle candle : candles) {
            candleService.saveCandle(candle);
        }
    }

    private void nextDate(List<Tick> ticks, int skipped, Tick lastTick, Tick firstTick) {
        boolean equalsDate = false;
        if (ticks == null || ticks.isEmpty()) {
            if (skipped == prop.getBatch()) {
                log.info("We received ticks, but all skipped(" + skipped + ") with first date: "
                        + firstTick.getDate() + " last date: " + lastTick.getDate());
                if (firstTick.getDate().equals(lastTick.getDate())) {
                    log.info("First and last date equal, set startRequestLimit for next request");
                    equalsDate = true;
                } else {
                    log.warn("!!!!!!!!!!!!!!!!!!!I don't know how may be doing this case!!!!!!!!!!!!!!!!!!! May be wrong getting date from last values");
                    log.info("Try to go next, just add 1 ,second for last tick");
                    this.currentDate = lastTick.getDate().plus(1, ChronoUnit.SECONDS);
                }
            } else {
                log.info("We didn't receive any ticks from this date (if date very old - add time in config)");
            }
        } else {
            //normal way
            this.currentDate = lastTick.getDate();
        }

        if (equalsDate) {
            startRequestLimit += prop.getBatch();
            log.info("startRequestLimit: " + startRequestLimit);
        } else {
            startRequestLimit = 0;
        }
    }

    private List<Tick> deleteExistTicks(List<Tick> ticks) {
        List<Tick> clearTicks = new ArrayList<>();
        if (ticks == null || ticks.isEmpty()) return clearTicks;
        skipped = 0;
        for (Tick tick : ticks) {
            if (existTicksId.contains(tick.getStrId())) {
                skipped++;
                continue;
            }
            existTicksId.add(tick.getStrId());
            if (existTicksId.size() > prop.getExistCount()) {
                existTicksId.remove(0);
            }
            clearTicks.add(tick);
        }
        log.trace("Was skipped: " + skipped);
        return clearTicks;
    }

    public List<Tick> loadTicks(LocalDateTime startDate) {
        List<Tick> ticks = Collections.emptyList();
        try {
            if (prop.isUseWs()) {
                ticks = exchange.historyTicksWs(prop.getSymbol(), prop.getBatch(), startDate, null, startRequestLimit);
            }
            if (ticks == null || ticks.isEmpty()) {
                if (prop.isUseWs()) {
                    log.info("Empty ticks from ws, try to get from rest");
                }
                ticks = exchange.historyTicks(prop.getSymbol(), prop.getBatch(), startDate, null, startRequestLimit);
            }
        } catch (TIMRetryException e) {
            log.error("Error load ticks", e);
            return null;
        }
        if (ticks == null || ticks.isEmpty()) {
            log.info("Ticks from exchange is empty");
            return null;
        }

        ticks = new CopyOnWriteArrayList<>(ticks);
        log.debug("Get ticks from exchange: " + ticks.size());
        firstTick = ticks.get(0);
        lastTick = ticks.get(ticks.size() - 1);
        log.trace("Received ticks date first: " + firstTick.getDate() + " last: " + lastTick.getDate());

        return ticks;
    }

    public List<Candle> buildCandlesNew(LocalDateTime currentDate) {
        if (!prop.isBuildCandle()) return null;
        int maxTimeFrame = candleService.maxTimeFrame(prop.getTimeframes());
        LocalDateTime start = Candle.calcStartDateFromTimeFrame(currentDate, maxTimeFrame, Candle.Accuracy.DAY);
        LocalDateTime end = start.plus(maxTimeFrame, ChronoUnit.MINUTES);
        TreeSet<Tick> treeForCache = new TreeSet<>();
        return candleService.processTicksForCreatingCandles(
                prop.getServer(),
                prop.getSymbol(),
                treeForCache,
                start,
                end,
                prop.getTimeframes(),
                Candle.Accuracy.DAY,
                prop.isOnlyClose());
    }

    @Override
    public String getThreadName() {
        return "TICK_DOWNLOADER";
    }
}
