package pro.belbix.tim.utils;

import com.google.common.collect.Lists;
import org.apache.commons.math3.stat.StatUtils;
import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.entity.Tick;
import pro.belbix.tim.exceptions.IndicatorException;
import pro.belbix.tim.exceptions.TIMRuntimeException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Indicators {
    private static final int RSI_DEPTH = 14;
    public static final int CANDLE_SIZE_MIN = RSI_DEPTH * 4 + 3;
    private static final int RSI_FINAL_DEPTH = 3;
    private static final int STOCHASTIC_DEPTH = 14;
    private static final double EWMA_SPAN = 27.08d;
    private static final double ALPHA = 2d / (EWMA_SPAN + 1d);

    public static void stochasticRsi(List<Candle> cs) {
        if (cs == null) return;
        if (cs.size() < CANDLE_SIZE_MIN) {
            throw new IndicatorException("Too small candles! " + cs.size() + " < " + CANDLE_SIZE_MIN);
        }
        if (cs.get(0).getSlowk() != null) {
            throw new IllegalStateException("Srsi already defined");
        }
        List<Candle> candles = Lists.reverse(cs);
        int candlesSize = candles.size();
        double[] us = new double[candlesSize];
        double[] ds = new double[candlesSize];
        for (int i = candlesSize - 1; i > 0; i--) {
            double currentClose = candles.get(i).getClose();
            double previusClose = candles.get(i - 1).getClose();
            double u = currentClose - previusClose;
            double d = previusClose - currentClose;
            if (u > 0) {
                d = 0;
            } else if (d > 0) {
                u = 0;
            } else {
                u = 0;
                d = 0;
            }
            us[i] = u;
            ds[i] = d;
        }

        double[] RSIs = new double[candlesSize];
        double emau = 0;
        double emad = 0;
        double u = 0;
        double d = 0;
        double r = 0;

        for (int i = 0; i < candlesSize; i++) {
            u = us[i];
            d = ds[i];
            if (i == 0) {
                emau = u;
                emad = d;
            } else {
                emau = ALPHA * u + (1 - ALPHA) * emau;
                emad = ALPHA * d + (1 - ALPHA) * emad;
            }
            if (emau + emad != 0) {
                r = 100d * (emau / (emau + emad));
            } else {
                r = 0;
            }
            RSIs[i] = r;
        }

        double[] rsis;
        int rsiLength = candlesSize - STOCHASTIC_DEPTH;
        double[] fastKs = new double[rsiLength];
        for (int i = 0; i < rsiLength; i++) {
            int from = rsiLength - i;
            int to = candlesSize - i;
            rsis = Arrays.copyOfRange(RSIs, from, to);
            if (rsis.length != 0) {
                double currentRsi = rsis[rsis.length - 1];
                double minRsi = Arrays.stream(rsis).min().getAsDouble();
                double maxRsi = Arrays.stream(rsis).max().getAsDouble();
                double deltaRsi = maxRsi - minRsi;
                double k;
                if (deltaRsi != 0) {
                    k = (currentRsi - minRsi) / deltaRsi;
                } else {
                    k = 0;
                }
                fastKs[i] = k * 100;
            } else {
                fastKs[i] = 0;
            }
        }

        double[] slowKs = new double[rsiLength];
        for (int i = candlesSize; i > RSI_FINAL_DEPTH + STOCHASTIC_DEPTH; i--) {
            double slowK;
            try {
                slowK = StatUtils.mean(fastKs, candlesSize - i, RSI_FINAL_DEPTH);
            } catch (Exception e) {
                throw new TIMRuntimeException(e);
            }

            slowKs[candlesSize - i] = slowK;
            candles.get(i - 1).setSlowk(slowK);
        }

        for (int i = candlesSize; i > RSI_FINAL_DEPTH + STOCHASTIC_DEPTH; i--) {
            double slowD = StatUtils.mean(slowKs, candlesSize - i, RSI_FINAL_DEPTH);
            candles.get(i - 1).setSlowd(slowD);
        }
    }

    private static double printPrice(double p) {
        return p - 3700;
    }

    public static void calcVelocity(Collection<Tick> ticks) {
        if (ticks == null || ticks.isEmpty()) return;
        Tick lastTick = null;
        double velocityAll = 0;
        for (Tick tick : ticks) {
            if (lastTick == null) {
                lastTick = tick;
                continue;
            }
            double velocity = velocity(lastTick, tick);
            tick.setVelocity(velocity);
            velocityAll += velocity;
            lastTick = tick;
        }

//        System.out.println("velocity " + ticks.iterator().next().getBuy() + ": " + velocityAll);
    }

    public static TreeSet<Tick> convolutedTicksMap(List<Tick> ts, Boolean buy, int compression) {
        TreeSet<Tick> ticks = new TreeSet<>(ts);
        if (ticks.size() != ts.size()) throw new TIMRuntimeException("Double ticks");
        Map<Double, Tick> convolutedTicks = new HashMap<>();
        LocalDateTime minDate = null;
        LocalDateTime maxDate = null;
        for (Tick tick : ticks) {
            if (buy != null && tick.getBuy() != buy) continue;

            double cPrice = tick.getPrice() - (tick.getPrice() % compression);

            Tick accum = convolutedTicks.get(cPrice);

            if (accum == null) {
                if (tick.getPrice() % compression != 0) continue;
                accum = tick.copy();
                minDate = accum.getDate();
                maxDate = accum.getDate();
                accum.setConvolutedTicksCount(1);
                accum.addConvolutedAmount(tick.getAmount());
                convolutedTicks.put(accum.getPrice(), accum);
            } else {
                if (minDate.isAfter(tick.getDate())) {
                    minDate = tick.getDate();
                }
                if (maxDate.isBefore(tick.getDate())) {
                    maxDate = tick.getDate();
                }

                long time = Duration.between(minDate, maxDate).toSeconds();
                accum.setConvolutedSeconds((int) time);

                accum.setConvolutedTicksCount(accum.getConvolutedTicksCount() + 1);

                accum.setAmount(accum.getAmount() + tick.getAmount());
                accum.addConvolutedAmount(tick.getAmount());

            }
        }

        return new TreeSet<>(convolutedTicks.values());
    }

    public static TreeSet<Tick> convolutedTicks(List<Tick> ts, Boolean buy, double compression) {
        TreeSet<Tick> ticks = new TreeSet<>(ts);
        if (ticks.size() != ts.size())
            throw new TIMRuntimeException("Double ticks ticks.size():" + ticks.size() + " ts.size():" + ts.size());
        TreeSet<Tick> convolutedTicks = new TreeSet<>();
        Tick accum = null;
        int convolutedTicksCount = 0;
        for (Tick tick : ticks) {
            if (buy != null && tick.getBuy() != buy) continue;

            if (accum == null) {
                accum = tick.copy();
                convolutedTicksCount++;
                accum.addConvolutedAmount(tick.getAmount());
            } else if (accum.getPrice().equals(tick.getPrice())) {
                accum.setAmount(accum.getAmount() + tick.getAmount());
                accum.addConvolutedAmount(tick.getAmount());
                convolutedTicksCount++;
            } else {
                long time = Duration.between(accum.getDate(), tick.getDate()).toSeconds();
                accum.setConvolutedSeconds((int) time);
                accum.setConvolutedTicksCount(convolutedTicksCount);
                convolutedTicks.add(accum);
                accum = tick.copy();
                convolutedTicksCount = 1;
                accum.addConvolutedAmount(tick.getAmount());
            }
        }

        if (accum != null) {
            long time = Duration.between(accum.getDate(), ticks.last().getDate()).toSeconds();
            accum.setConvolutedSeconds((int) time);
            accum.setConvolutedTicksCount(convolutedTicksCount);
            convolutedTicks.add(accum);
        }

        return compressConvTicks(convolutedTicks, compression);
    }

    private static TreeSet<Tick> compressConvTicks(TreeSet<Tick> ticks, double compression) {
        List<Tick> ticksList = new ArrayList<>();
        for (Tick tick : ticks) {
            double surplus = (tick.getPrice() % compression);
            double cPrice = tick.getPrice() - surplus;

            Tick accum = null;
            if (!ticksList.isEmpty()) {
                accum = ticksList.get(ticksList.size() - 1);
            } else {
                accum = tick.copy();
                if (tick.getPrice() % compression != 0) {
                    accum.setPrice(cPrice);
                }
                ticksList.add(accum);
                continue;
            }

            if (accum.getPrice().equals(cPrice)) {
                accum.accumulate(tick);
            } else {
                accum = tick.copy();
                if (surplus != 0) {
                    accum.setPrice(cPrice);
                }
                ticksList.add(accum);
            }

        }
        return new TreeSet<>(ticksList);
    }

    private static double velocity(Tick start, Tick end) {
        double distance = end.getPrice() - start.getPrice();
//        double time = end.getDate().toEpochSecond(ZoneOffset.UTC) - start.getDate().toEpochSecond(ZoneOffset.UTC);
        double time = (double) Duration.between(start.getDate(), end.getDate()).toMillis() / 1000;
        time += end.getConvolutedSeconds();
        if (time == 0) time = 1;
        end.setConvolutedSecondsFinal(time);
        return distance / time;
    }

    private static void addTickInDateMap(Map<LocalDateTime, List<Tick>> lastTicks, Tick tick) {
        List<Tick> ticks = lastTicks.get(tick.getDate());
        if (ticks == null) {
            ticks = new ArrayList<>();
            ticks.add(tick);
            lastTicks.put(tick.getDate(), ticks);
        } else {
            ticks.add(tick);
        }
    }

    public static void calcCandlesAccelerations(List<Candle> candles) {
        Candle previus = null;
        for (Candle candle : candles) {
            if (previus == null) {
                previus = candle;
                continue;
            }
            candle.setAcceleration(candleAcceleration(previus, candle));
            System.out.println(candle.getDate() + ";" +
                    candle.getClose() + ";" +
                    candle.velocity() + ";" +
                    candle.getAcceleration());
        }
    }

    private static long printAcceleration(Double a) {
        if (a == null) return 0;
        return Math.round(a * 1_000_000);
    }

    private static double candleAcceleration(Candle start, Candle end) {
        double velocityStart = start.velocity();
        double velocityEnd = end.velocity();
        double timeStart = start.getDate().toEpochSecond(ZoneOffset.UTC);
        double timeEnd = end.getDate().plus(end.getTime(), ChronoUnit.MINUTES).toEpochSecond(ZoneOffset.UTC);
        return (velocityEnd - velocityStart) / (timeStart - timeEnd);
    }

    public static Candle generateConvCandle(TreeSet<Tick> ts, int window) {
        if (ts == null || ts.size() < window) return null;
        List<Tick> ticks = new ArrayList<>(ts).subList(ts.size() - window, ts.size());
        Tick firstTick = ticks.get(0);
        Tick lastTick = ticks.get(ticks.size() - 1);

        double volume = 0;
        double[] velocityAverage = new double[ticks.size()];
        int i = 0;
        int convolutedTicksCount = 0;
        double convolutedSecondsFinal = 0;
        double maxPrice = 0;
        double minPrice = 0;
        for (Tick tick : ticks) {
            lastTick = tick;
            volume += tick.getAmount();
            convolutedTicksCount += tick.getConvolutedTicksCount();
            convolutedSecondsFinal += tick.getConvolutedSecondsFinal();
            velocityAverage[i] = tick.getVelocity();
            i++;
        }
        double velocity = StatUtils.mean(velocityAverage, 0, velocityAverage.length);
        Candle candle = new Candle();
        candle.setServer(firstTick.getServer());
        candle.setSymbol(firstTick.getSymbol());
        candle.setDate(firstTick.getDate());
        long time = Duration.between(firstTick.getDate(), lastTick.getDate()).toSeconds();
        candle.setTime((int) time);
        candle.setOpen(firstTick.getPrice());
        candle.setClose(lastTick.getPrice());
        candle.setVolume(volume);
        candle.setConvolutedTicksCount(convolutedTicksCount);
        candle.setConvolutedSecondsFinal(convolutedSecondsFinal);
        candle.setVelocity(velocity);
        candle.setAmount((double) ticks.size());
        return candle;
    }


    public static List<Candle> generateConvCandles(TreeSet<Tick> ticks, int size) {
        if (ticks == null || ticks.size() < size) return null;
        List<Candle> candles = new ArrayList<>();
        List<Tick> tickList = new ArrayList<>(ticks);
        for (int i = 0; i < size; i++) {
            TreeSet<Tick> ts = new TreeSet<>(tickList.subList(tickList.size() - i - 1, tickList.size() - i));
            Candle candle = generateConvCandle(ts, 1);
            if (candle == null) throw new TIMRuntimeException("Empty candle");
            candles.add(candle);
        }

        return candles;
    }


}
