package pro.belbix.tim.neurons;

import pro.belbix.tim.entity.Candle;
import pro.belbix.tim.protobuf.neuron.NeuroLinkModel;
import pro.belbix.tim.protobuf.neuron.NeuronLayerModel;
import pro.belbix.tim.protobuf.neuron.NeuronModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NeuroLink {
    private final List<SimpleNeuron> inputLayer;
    private final Map<Integer, Map<Long, SimpleNeuron>> hiddenLayers;
    private final SimpleNeuron resultNeuron;
    private boolean finalState = false;
    private Map<Integer, Map<Long, Long>> countsOfSuccess = new HashMap<>();

    public NeuroLink(NeuroLinkModel model) {
        this.inputLayer = model.getInput().getNeuronsList().stream()
                .map(SimpleNeuron::new)
                .collect(Collectors.toUnmodifiableList());
        countsOfSuccess.put(0, new HashMap<>());

        Map<Integer, Map<Long, SimpleNeuron>> tmpMap = new HashMap<>();
        for (Map.Entry<Integer, NeuronLayerModel> entry : model.getHiddenMap().entrySet()) {
            countsOfSuccess.put(entry.getKey(), new HashMap<>());
            Map<Long, SimpleNeuron> tmpInnerMap = new HashMap<>();
            for (NeuronModel n : entry.getValue().getNeuronsList()) {
                tmpInnerMap.put(n.getId(), new SimpleNeuron(n));
            }
            tmpMap.put(entry.getKey(), Collections.unmodifiableMap(tmpInnerMap));
        }
        this.hiddenLayers = Collections.unmodifiableMap(tmpMap);

        this.resultNeuron = new SimpleNeuron(model.getResult());
    }

    public boolean handleCandles(List<Candle> candles, Function<Candle, Double> getValueFromCandle) {
        clear();
        for (SimpleNeuron neuron : inputLayer) {
            Candle candle;
            try {
                candle = candles.get(neuron.getIndexOfCandle());
            } catch (IndexOutOfBoundsException e) {
                continue;
            }
            double value = getValueFromCandle.apply(candle);
            handleNeuron(value, neuron, 1);
        }
        return finalState;
    }

    private void handleNeuron(double value, SimpleNeuron neuron, int nextLayerId) {
        double answer = neuron.handleSignal(value);
        writeCountOfSuccess(neuron, nextLayerId - 1);
        Map<Long, SimpleNeuron> nextLayer = hiddenLayers.get(nextLayerId);
        if (nextLayer == null) {
            if (resultNeuron.handleSignal(answer) != 0) {
                finalState = true;
            }
            return;
        }
        for (Long link : neuron.getLinks()) {
            handleNeuron(answer, nextLayer.get(link), nextLayerId + 1);
        }
    }

    private void writeCountOfSuccess(SimpleNeuron neuron, int layerId) {
        Map<Long, Long> c = countsOfSuccess.get(layerId);
        c.put(neuron.getId(), neuron.getCountOfSuccess());
    }

    public Map<Integer, Map<Long, Long>> getCountsOfSuccess() {
        return countsOfSuccess;
    }

    private void clear() {
        inputLayer.forEach(SimpleNeuron::clear);
        hiddenLayers.values().forEach(m -> m.values().forEach(SimpleNeuron::clear));
        resultNeuron.clear();
    }

}
