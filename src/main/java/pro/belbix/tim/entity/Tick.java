package pro.belbix.tim.entity;

import com.binance.api.client.domain.market.OrderBookEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.NonNull;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.exchanges.bitmex.dto.OrderBookResponse;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticks", indexes = {
        @Index(name = "idx_tick", columnList = "server, symbol, date")
})
/*
ALTER TABLE ticks
    PARTITION BY HASH(TO_DAYS(date))
        PARTITIONS 365;
*/
@Getter
@Setter
@ToString
public class Tick implements Comparable<Tick> {
    @Id
    @Column(length = 50)
    private String strId = UUID.randomUUID().toString();
    @Column(length = 15)
    private String server = "";
    @Column(length = 10)
    private String symbol = "";
    private Double amount;
    private Double price;
    private LocalDateTime date;
    private Boolean buy;
    @Transient
    private double velocity;
    @Transient
    private double convolutedAmount;
    @Transient
    private int convolutedSeconds;
    @Transient
    private double convolutedSecondsFinal;
    @Transient
    private int convolutedTicksCount;

    public Tick() {
    }

    public Tick(String strId, Double amount, Double price, LocalDateTime date, Boolean buy) {
        this.strId = strId;
        this.amount = amount;
        this.price = price;
        this.date = date;
        this.buy = buy;
    }

    public static Tick fromOrderBookResponse(OrderBookResponse book) {
        Tick tick = new Tick();
        tick.setStrId(UUID.randomUUID().toString());
        tick.setServer("bitmex");
        tick.setSymbol(book.getSymbol());
        tick.setStrId(book.getId() + "");
        tick.setBuy(book.getSide().equals("Buy"));
        tick.setPrice(book.getPrice());
        return tick;
    }

    public static Tick fromBinanceResponse(OrderBookEntry book) {
        Tick tick = new Tick();
        tick.setServer("binance");
        tick.setStrId(UUID.randomUUID().toString());
        tick.setAmount(Double.parseDouble(book.getQty()));
        tick.setPrice(Double.parseDouble(book.getPrice()));
        tick.setDate(LocalDateTime.now());
        return tick;
    }

    public void addConvolutedAmount(double amount) {
        if (buy) {
            convolutedAmount += amount;
        } else {
            convolutedAmount -= amount;
        }
    }

    public Tick copy() {
        Tick tick = new Tick();
        tick.setStrId(strId + "_c");
        tick.setDate(date);
        tick.setServer(server);
        tick.setSymbol(symbol);
        tick.setAmount(amount);
        tick.setPrice(price);
        tick.setBuy(buy);
        tick.setConvolutedSecondsFinal(convolutedSecondsFinal);
        tick.setConvolutedSeconds(convolutedSeconds);
        tick.setConvolutedAmount(convolutedAmount);
        tick.setConvolutedTicksCount(convolutedTicksCount);
        return tick;
    }

    public void accumulate(Tick tick) {
        this.setConvolutedTicksCount(this.getConvolutedTicksCount() + tick.getConvolutedTicksCount());
        this.setAmount(this.getAmount() + tick.getAmount());
        this.setConvolutedAmount(this.getConvolutedAmount() + tick.getConvolutedAmount());
        this.setConvolutedSeconds(this.getConvolutedSeconds() + tick.getConvolutedSeconds());
        this.setConvolutedSecondsFinal(this.getConvolutedSecondsFinal() + tick.getConvolutedSecondsFinal());
    }

    @Override
    public int compareTo(@NonNull Tick o) {
        if (this.equals(o)) return 0;
        if (this.date.isBefore(o.getDate())) return -1;
        if (this.date.isAfter(o.getDate())) return 1;

        if (this.date.equals(o.getDate())) {
            if (this.getPrice() < o.getPrice()) return -1;
            if (this.getPrice() > o.getPrice()) return 1;

            if (this.price.equals(o.getPrice())) {
                if (this.getAmount() < o.getAmount()) return -1;
                if (this.getAmount() > o.getAmount()) return 1;
                //i dont have another fields
                if (this.getStrId().hashCode() < o.getStrId().hashCode()) return -1;
                if (this.getStrId().hashCode() > o.getStrId().hashCode()) return 1;
            }
        }

        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tick)) return false;

        Tick tick = (Tick) o;

        if (this.getStrId() == null || tick.getStrId() == null)
            throw new TIMRuntimeException("Tick id is null " + this + " o:" + o);

        return getStrId().equals(tick.getStrId());
    }

    @Override
    public int hashCode() {
        if (this.getStrId() == null)
            throw new TIMRuntimeException("Tick id is null " + this);
        return getStrId().hashCode();
    }
}
