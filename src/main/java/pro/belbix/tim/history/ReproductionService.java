package pro.belbix.tim.history;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.Evolution;
import pro.belbix.tim.properties.EvolutionProperties;
import pro.belbix.tim.properties.Srsi2Properties;
import pro.belbix.tim.protobuf.srsi.DoubleValue;
import pro.belbix.tim.protobuf.srsi.Srsi;
import pro.belbix.tim.protobuf.srsi.SrsiNode;
import pro.belbix.tim.repositories.EvolutePropertiesRepository;
import pro.belbix.tim.utils.Common;

import java.util.List;

import static pro.belbix.tim.history.EvolutionHistory.jsonToProperty;

@Service
public class ReproductionService {
    private static final Logger log = LoggerFactory.getLogger(ReproductionService.class);
    private final static double BASE_CHANCE = 50.0;
    private final static double COUNT_BASE = 100.0;
    private final static double COUNT_MIN = 1.0;
    private final static double MUTATE_BASE = 0.1;
    private final static double MAX_VALUE = 100.0;

    private final EvolutionProperties evolutionProperties;
    private final EvolutePropertiesRepository evolutePropertiesRepository;

    public ReproductionService(EvolutionProperties evolutionProperties,
                               EvolutePropertiesRepository evolutePropertiesRepository) {
        this.evolutionProperties = evolutionProperties;
        this.evolutePropertiesRepository = evolutePropertiesRepository;
    }

    public static double calcChance(double count) {
        double t = (COUNT_BASE - count);
        if (t < COUNT_MIN) {
            t = COUNT_MIN;
        }
        return BASE_CHANCE * (0.0 + ((t) / COUNT_BASE));
    }

    public static boolean isChance(double minChance) {
        double currentChance = Math.random() * 100.0;
        return currentChance > 100 - minChance;
    }

    public String bornChildNodes(Evolution parent) {

//        List<Srsi2Properties> grandParents = new ArrayList<>(); //TODO implement
//        findParents(parent, grandParents);
        Srsi2Properties parentProperties = extractProperties(parent);
        Srsi parentSrsi = parentProperties.toSrsi();
        Srsi childSrsi = mutateSrsi(parentSrsi);
        log.info("New child: " + childSrsi);
        return Hex.encodeHexString(childSrsi.toByteArray()).toUpperCase();
    }

    private Srsi mutateSrsi(Srsi srsi) {
        Srsi.Builder builder = Srsi.newBuilder();

        for (SrsiNode node : srsi.getNodesLongOpenList()) {
            builder.addNodesLongOpen(mutateNode(node));
        }
        for (SrsiNode node : srsi.getNodesLongCloseList()) {
            builder.addNodesLongClose(mutateNode(node));
        }
        for (SrsiNode node : srsi.getNodesShortOpenList()) {
            builder.addNodesShortOpen(mutateNode(node));
        }
        for (SrsiNode node : srsi.getNodesShortCloseList()) {
            builder.addNodesShortClose(mutateNode(node));
        }

        return builder.build();
    }

    private SrsiNode mutateNode(SrsiNode srsiNode) {
        SrsiNode.Builder builder = SrsiNode.newBuilder();
        builder.setIndex(srsiNode.getIndex());
        builder.setSrsiDiffMin(mutateDoubleValue(srsiNode.getSrsiDiffMin()));
        builder.setSrsiDiffMax(mutateDoubleValue(srsiNode.getSrsiDiffMax()));
        builder.setSrsiMin(mutateDoubleValue(srsiNode.getSrsiMin()));
        builder.setSrsiMax(mutateDoubleValue(srsiNode.getSrsiMax()));
        builder.setPriceDiffMin(mutateDoubleValue(srsiNode.getPriceDiffMin()));
        builder.setPriceDiffMax(mutateDoubleValue(srsiNode.getPriceDiffMax()));
        return builder.build();
    }

    private DoubleValue mutateDoubleValue(DoubleValue doubleValue) {
        if (!(isChance(calcChance(doubleValue.getCount())))) {
            return doubleValue;
        }

        DoubleValue.Builder builder = DoubleValue.newBuilder();
        builder.setValue(mutateDouble(doubleValue.getValue()));
        builder.setCount(doubleValue.getCount() + 1);
        return builder.build();
    }

    private double mutateDouble(double d) {
        double mutate = (Common.normalDistributionRandom() / 5) * MUTATE_BASE * evolutionProperties.getFactor();
        d = d + mutate;
        if (d > MAX_VALUE) {
            d = MAX_VALUE;
        }
        if (d < -MAX_VALUE) {
            d = -MAX_VALUE;
        }
        return d;
    }

    private Srsi2Properties extractProperties(Evolution e) {
        return (Srsi2Properties) jsonToProperty(e.getProperties(), new Srsi2Properties());
    }

    private void findParents(Evolution child, List<Srsi2Properties> parents) {
        if (child == null) return;
        Evolution parent = findParent(child);
        if (parent != null) {
            parents.add(extractProperties(parent));
        }
        findParents(parent, parents);
    }

    private Evolution findParent(Evolution child) {
        Long id = child.getPrevId();
        if (id == null || id == 0) return null;
        return evolutePropertiesRepository.findById(id).orElse(null);
    }

}
