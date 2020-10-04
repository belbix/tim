package pro.belbix.tim.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.tim.entity.Srsi2Tick;
import pro.belbix.tim.entity.SrsiTick;
import pro.belbix.tim.entity.SrsiTickI;
import pro.belbix.tim.entity.Tick;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Collection;
import java.util.List;

@Service
public class JpaService {
    @PersistenceContext
    private EntityManager entityManager;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private int batchSize;

    @Transactional
    public <T> int bulkSave(Collection<T> entities) {
        int i = 0;
        for (T t : entities) {
            entityManager.persist(t);
            i++;
            if (i % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        return i;
    }

    @Transactional
    public int batchNativeTickSave(List<Tick> ticks) {
        String sql = nativeQueryFromTicks(ticks);
        Query q = entityManager.createNativeQuery(sql);
        return q.executeUpdate();
    }

    public String nativeQueryFromTicks(List<Tick> ticks) {
        StringBuilder sql = new StringBuilder("insert into ticks (amount, buy, date, price, server, symbol, str_id) values ");

        for (Tick tick : ticks) {
            String v = "(";
            v += tick.getAmount() + ",";
            v += tick.getBuy() + ",";
            v += "'" + tick.getDate() + "',";
            v += tick.getPrice() + ",";
            v += "'" + tick.getServer() + "',";
            v += "'" + tick.getSymbol() + "',";
            v += "'" + tick.getStrId() + "'";
            sql.append(v);
            sql.append(")").append(",");
        }
        sql.setLength(sql.length() - 1);
        return sql.toString();
    }

    @Transactional
    public int batchNativeSrsiTickSave(List<SrsiTickI> ticks, int version) {
        String sql;
        if (version == 1) {
            sql = nativeQueryFromSrsiTicks(ticks);
        } else {
            sql = nativeQueryFromSrsi2Ticks(ticks);
        }
        return entityManager.createNativeQuery(sql).executeUpdate();
    }

    public String nativeQueryFromSrsiTicks(List<SrsiTickI> ticks) {
        StringBuilder sql = new StringBuilder("insert into srsi (" +
                "date, ticktime,  first_close, first_slowd, first_slowk, " +
                "second_close, second_slowd, second_slowk," +
                "third_slowd, third_slowk" +
                ") values ");

        for (SrsiTickI t : ticks) {
            SrsiTick tick = (SrsiTick) t;
            String v = "(";
            v += "'" + tick.getDate() + "',";
            v += tick.getTicktime() + ",";
            v += tick.getFirstClose() + ",";
            v += tick.getFirstSlowd() + ",";
            v += tick.getFirstSlowk() + ",";

            v += tick.getSecondClose() + ",";
            v += tick.getSecondSlowd() + ",";
            v += tick.getSecondSlowk() + ",";

            v += tick.getThirdSlowd() + ",";
            v += tick.getThirdSlowk();

            sql.append(v);
            sql.append(")").append(",");
        }
        sql.setLength(sql.length() - 1);
        return sql.toString();
    }

    public String nativeQueryFromSrsi2Ticks(List<SrsiTickI> ticks) {
        StringBuilder sql = new StringBuilder("insert into srsi2 (" +
                "date, first_close, second_close, srsi" +
                ") values ");

        for (SrsiTickI t : ticks) {
            Srsi2Tick tick = (Srsi2Tick) t;
            sql.append("(")
                    .append("'").append(tick.getDate()).append("',")
                    .append(tick.getFirstClose()).append(",")
                    .append(tick.getSecondClose()).append(",")
                    .append("'").append(tick.getSrsi()).append("'")
                    .append(")").append(",");
        }
        sql.setLength(sql.length() - 1);
        return sql.toString();
    }
}
