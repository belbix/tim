package pro.belbix.tim.neurons;

import pro.belbix.tim.protobuf.neuron.NeuronModel;

import java.util.Collections;
import java.util.List;

public class SimpleNeuron {
    private final long id;
    private final int indexOfCandle;
    private final double threshold;
    private final boolean positive;
    private final double weight;
    private final List<Long> links;
    private double state;
    private long countOfSuccess = 0;

    public SimpleNeuron(NeuronModel neuronModel) {
        this.id = neuronModel.getId();
        this.indexOfCandle = neuronModel.getIndexOfCandle();
        this.threshold = neuronModel.getThreshold();
        this.positive = neuronModel.getPositive();
        this.weight = neuronModel.getWeight();
        this.links = Collections.unmodifiableList(neuronModel.getLinksList());
    }

    public double handleSignal(double signal) {
        state += signal;
        boolean activate = false;
        if (positive) {
            if (state >= threshold) {
                activate = true;
            }
        } else {
            if (state <= threshold) {
                activate = true;
            }
        }
        if (activate) {
            countOfSuccess++;
            return weight;
        }
        return 0;
    }

    public void clear() {
        state = 0;
    }

    public long getId() {
        return id;
    }

    public int getIndexOfCandle() {
        return indexOfCandle;
    }

    public List<Long> getLinks() {
        return links;
    }

    public long getCountOfSuccess() {
        return countOfSuccess;
    }
}
