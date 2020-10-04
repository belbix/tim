package pro.belbix.tim.history;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.properties.MutableProperties;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Common {
    private static final Logger log = LoggerFactory.getLogger(Common.class);
    private static final Random rand = new Random();
    private static final Set<String> MANDATORY_FIELD_FOR_EVOLUTION = Set.of(
            "model",
            "nodes",
            "nodesLongOpen",
            "nodesLongClose",
            "nodesShortOpen",
            "nodesShortClose"
    );

    public static void evoluteMutate(MutableProperties fromDb, MutableProperties prop, double factor) {
        ReflectionUtils.doWithFields(fromDb.getClass(), field -> {
            Double mutateValue = null;
            Mutate a = null;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (!(annotation instanceof Mutate)) continue;
                a = (Mutate) annotation;
                mutateValue = a.value();
            }

            if (a == null) return;
            double r = Math.random();
            if (r > 1d - a.chance()) return;

            mutateValue = Math.random() * mutateValue * factor;
            if (Math.random() < 0.5) {
                mutateValue = -mutateValue;
            }

            field.setAccessible(true);
            Double propV = (Double) field.get(fromDb);
            if (propV == null) throw new TIMRuntimeException("Null field " + field.getName());
            double finalValue = propV + mutateValue;
            if (a.max() != 0 && finalValue > a.max()) finalValue = a.max();
            if (a.min() != 0 && finalValue < a.min()) finalValue = a.min();

            updateValue(prop, field.getName(), finalValue);
        });
    }

//    public static void evaluateSrsi2(Srsi2Properties fromDb, Srsi2Properties prop, double factor) {
//        evaluateSrsi2Node(fromDb.getNodesLongOpen(), prop.getNodesLongOpen(), factor);
//        evaluateSrsi2Node(fromDb.getNodesLongClose(), prop.getNodesLongClose(), factor);
//        evaluateSrsi2Node(fromDb.getNodesShortOpen(), prop.getNodesShortOpen(), factor);
//        evaluateSrsi2Node(fromDb.getNodesShortClose(), prop.getNodesShortClose(), factor);
//    }
//
//    private static void evaluateSrsi2Node(List<SrsiNode> nodesFromDb, List<SrsiNode> nodesNew, double factor) {
//        for (int i = 0; i < nodesFromDb.size(); i++) {
//            SrsiNode nodeFromDb = nodesFromDb.get(i);
//            SrsiNode nodeNew = nodesNew.get(i);
//            evaluateSrsiNode(nodeFromDb, nodeNew, factor);
//        }
//    }
//
//    private static void evaluateSrsiNode(SrsiNode fromDb, SrsiNode n, double factor) {
//        if (fromDb.getComparedIndex() != n.getComparedIndex()
//                || fromDb.getCurrentIndex() != n.getCurrentIndex())
//            throw new IllegalStateException("nodes not valid " + fromDb + "\n" + n);
//
//        n.setCurrentSrsiDiff(fromDb.getCurrentSrsiDiff() + mFactor(factor));
//        n.setComparedSrsiDiff(fromDb.getComparedSrsiDiff() + mFactor(factor));
////        n.setCurrentSrsiMax(fromDb.getCurrentSrsiMax() + mFactor(factor));
////        n.setCurrentSrsiMin(fromDb.getCurrentSrsiMin() + mFactor(factor));
////        n.setMaxPriceDiff(fromDb.getMaxPriceDiff() + mFactor(factor));
//    }

    private static double mFactor(double factor) {
        if (rand.nextDouble() < 0.1d) return 1;
        double v = rand.nextDouble() * factor;
        if (rand.nextDouble() < 0.5) {
            return -v;
        }
        return v;
    }

    private static void updateValue(Object prop, String fieldName, Double newValue) {
        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(prop.getClass());
        } catch (IntrospectionException e) {
            throw new TIMRuntimeException(e);
        }
        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (!pd.getName().equals(fieldName)) continue;
            Method setter = pd.getWriteMethod();
            log.info(prop.getClass().getSimpleName() + " value of " + fieldName + " is " + newValue);
            ReflectionUtils.invokeMethod(setter, prop, newValue);
        }
    }

    public static Map<String, Object> propToMap(MutableProperties prop) {
        Map<String, Object> map = new HashMap<>();
        ReflectionUtils.doWithFields(prop.getClass(), field -> {
            String name = field.getName();
            boolean exist = false;
            for (Annotation annotation : field.getDeclaredAnnotations()) {
                if (!(annotation instanceof Mutate)) continue;
                exist = true;
                break;
            }
            if (MANDATORY_FIELD_FOR_EVOLUTION.contains(name)) {
                exist = true;
            }

            if (!exist) return;
            try {
                String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                Method getter = prop.getClass().getDeclaredMethod(getterName);
                getter.setAccessible(true);
                Object o = getter.invoke(prop);
                addToMap(name, o, map);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }


        });
        return map;
    }

    private static void addToMap(String key, Object o, Map<String, Object> map) {
        map.put(key, o);
    }
}
