package pro.belbix.tim.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import pro.belbix.tim.models.OrderSide;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "candles", indexes = {
        @Index(name = "idx_candle", columnList = "server, symbol, time, date")
})
@Cacheable(false)
@Getter
@Setter
@ToString
public class Candle implements Comparable<Candle> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(length = 15)
    private String server = "";
    @Column(length = 10)
    private String symbol = "";
    private Integer time = 0;
    private LocalDateTime date;
    private Double amount;
    private Double close;
    private Double high;
    private Double low;
    private Double open;
    private Double volumeBuy;
    private Double volumeSell;
    private Double slowk;
    private Double slowd;
    @Transient
    private OrderSide orderSide;
    @Transient
    private Double volume;
    @Transient
    private Double acceleration;
    @Transient
    private double velocity;
    @Transient
    private int convolutedTicksCount;
    @Transient
    private double convolutedSecondsFinal;
    @Transient
    private LocalDateTime tickTime;

    public Candle() {
    }

    public Candle(Double close) {
        this.close = close;
    }

    public Candle(Candle c) {
        this.id = c.getId();
        this.server = c.getServer();
        this.symbol = c.getSymbol();
        this.time = c.getTime();
        this.date = c.getDate();
        this.amount = c.getAmount();
        this.close = c.getClose();
        this.high = c.getHigh();
        this.low = c.getLow();
        this.open = c.getOpen();
        this.volumeBuy = c.getVolumeBuy();
        this.volumeSell = c.getVolumeSell();
        this.slowk = c.getSlowk();
        this.slowd = c.getSlowd();
        this.orderSide = c.getOrderSide();
        this.volume = c.getVolume();
        this.acceleration = c.getAcceleration();
        this.tickTime = c.getTickTime();
    }

    public static LocalDateTime calcStartDateFromTimeFrame(LocalDateTime date, int time, Accuracy accuracy) {
//        date = date.atOffset(ZoneOffset.UTC);
        LocalDateTime startDate;
        if (accuracy.equals(Accuracy.DAY)) {
            startDate = date.toLocalDate().atStartOfDay();
        } else if (accuracy.equals(Accuracy.HOUR)) {
            startDate = date.minus(date.getMinute(), ChronoUnit.MINUTES);
            startDate = startDate.minus(date.getSecond(), ChronoUnit.SECONDS);
        } else {
            startDate = date.minus(date.getSecond(), ChronoUnit.SECONDS);
        }
        LocalDateTime prevStartDate;
        do {
            prevStartDate = startDate;
            startDate = startDate.plus(time, ChronoUnit.MINUTES);
            if (startDate.equals(date.truncatedTo(ChronoUnit.MINUTES))) return startDate;
        } while (startDate.isBefore(date));
//        prevStartDate = prevStartDate.atOffset(OffsetDateTime.now().getOffset());
        return prevStartDate;
    }

    public static LocalDateTime plusTF(LocalDateTime date, int tf) {
        return date.plus(tf, ChronoUnit.MINUTES);
    }

    public static LocalDateTime minusTF(LocalDateTime date, int tf) {
        return date.minus(tf, ChronoUnit.MINUTES);
    }

    public LocalDateTime tickTime() {
        if (tickTime != null) return tickTime;
        return date;
    }

    public double calcDeltaK() {
        if (slowk != null && slowd != null) {
            return (slowk - slowd);
        } else {
            return 0d;
        }
    }

    public double calcDiffWithPrev(Candle prevCandle) {
        if (close != null && prevCandle != null && prevCandle.getClose() != null && close != 0) {
            return ((prevCandle.getClose() - close) / close) * 100;
        } else {
            return 0d;
        }
    }

    public boolean isValidDate() {
        boolean valid = true;
        if (date.getSecond() != 0) {
            valid = false;
        }
        if (time >= 60 && (time % 10) == 0) {
            if (date.getMinute() != 0) {
                valid = false;
            }
            if (time >= 1440 && (time % 60) == 0) {
                if (date.getHour() != 0) {
                    valid = false;
                }
            }
        }
        return valid;
    }

    public boolean isBuy() {
        boolean buy;
        OrderSide orderSide = getOrderSide();
        if (orderSide.isLong()) {
            return orderSide.isOpen();
        } else {
            return !orderSide.isOpen();
        }
    }

    public double velocity() {
        double time = this.time;
        return (close - open) / time;
    }

    public Candle lightCopy() {
        Candle c = new Candle();
        c.setDate(date);
        c.setClose(close);
        c.setOrderSide(orderSide);
        c.setTickTime(tickTime);
        c.setSymbol(symbol);
        c.setServer(server);
        c.setAmount(amount);
        c.setOpen(open);
        c.setHigh(high);
        c.setLow(low);
        return c;
    }

    @Override
    public int compareTo(@NonNull Candle o) {
        if (!this.getServer().equals(o.getServer())
                || !this.getSymbol().equals(o.getSymbol())
                || !this.getTime().equals(o.getTime()))
            throw new IllegalStateException("Compare with different state " + this + " " + o);

        if (this.equals(o)) return 0;
        if (this.date.isBefore(o.getDate())) return 1;
        if (this.date.isAfter(o.getDate())) return -1;
        return 0;
    }

    public void clear() {
        id = null;
        server = "";
        symbol = "";
//        time = 0;
//        date = null;
//        amount = null;
//        close = null;
//        high = null;
//        low = null;
//        open = null;
//        volumeBuy = null;
//        volumeSell = null;
//        slowk = null;
//        slowd = null;
        orderSide = null;
        volume = null;
        acceleration = null;
//        tickTime = null;
    }

    public enum Accuracy {
        DAY, HOUR, MINUTE
    }
}
