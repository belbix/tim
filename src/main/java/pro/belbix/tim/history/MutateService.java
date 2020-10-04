package pro.belbix.tim.history;

import pro.belbix.tim.protobuf.neuron.NeuronLayerModel;
import pro.belbix.tim.utils.Common;

public class MutateService {
    private final double factor;
    private final double mutateDoubleMax;
    private final double mutateDoubleBase;
    private final double baseChanceDouble;
    private final double baseChanceLink;
    private final double baseChanceNeuron;
    private final double baseChanceHidden;
    private final double countBase;
    private final double countMin;

    public MutateService(NeuronLayerModel layer, double factor) {
        this.factor = factor;
        this.mutateDoubleMax = layer.getMutateDoubleMax();
        this.mutateDoubleBase = layer.getMutateDoubleBase();
        this.baseChanceDouble = layer.getBaseChanceDouble();
        this.baseChanceLink = layer.getBaseChanceLink();
        this.baseChanceNeuron = layer.getBaseChanceNeuron();
        this.baseChanceHidden = layer.getBaseChanceHidden();
        this.countBase = layer.getCountBase();
        this.countMin = layer.getCountMin();
    }

    public static boolean isChance(double minChance) {
        double currentChance = Math.random() * 100.0;
        return currentChance > 100 - minChance;
    }

    public double mutateDouble(double d, long epochCount) {
        if (!isChance(calcChance(epochCount, ChanceType.CHANCE_DOUBLE))) {
            return d;
        }
        double mutate = (Common.normalDistributionRandom() / 5) * mutateDoubleBase * factor;
        d = d + mutate;
        if (d > mutateDoubleMax) {
            d = mutateDoubleMax;
        }
        if (d < -mutateDoubleMax) {
            d = -mutateDoubleMax;
        }
        return d;
    }

    public double calcChance(double count, ChanceType chanceType) {
        double chance;
        switch (chanceType) {
            case CHANCE_LINK:
                chance = baseChanceLink;
                break;
            case CHANCE_DOUBLE:
                chance = this.baseChanceDouble;
                break;
            case CHANCE_NEURON:
                chance = baseChanceNeuron;
                break;
            case FIFTY_CHANCE:
                chance = 50.0;
                break;
            case CHANCE_HIDDEN:
                chance = baseChanceHidden;
                break;
            default:
                throw new IllegalStateException("Unknown chance type");
        }
        double t = (countBase - count);
        if (t < countMin) {
            t = countMin;
        }
        return chance * (0.0 + ((t) / countBase));
    }

    public enum ChanceType {
        CHANCE_DOUBLE, CHANCE_LINK, CHANCE_NEURON, CHANCE_HIDDEN, FIFTY_CHANCE
    }
}
