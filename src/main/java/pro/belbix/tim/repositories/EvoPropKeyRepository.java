package pro.belbix.tim.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.EvoPropKey;

import java.util.List;

public interface EvoPropKeyRepository extends CrudRepository<EvoPropKey, Long> {

    @Query("select e from EvoPropKey e where" +
            " e.strategy = :strategy" +
            " and e.server = :server" +
            " and e.symbol = :symbol" +
            " and e.dateStart = :dateStart" +
            " and e.dateEnd = :dateEnd" +
            " and e.tfLong = :tfLong" +
            " and e.tfShort = :tfShort" +
            " and e.ticktime = :ticktime" +
            " and e.fee = :fee" +
            " and e.leverage = :leverage" +
            " and e.batch = :batch")
    List<EvoPropKey> find(@Param("strategy") String strategy,
                          @Param("server") String server,
                          @Param("symbol") String symbol,
                          @Param("dateStart") String dateStart,
                          @Param("dateEnd") String dateEnd,
                          @Param("tfLong") int tfLong,
                          @Param("tfShort") int tfShort,
                          @Param("ticktime") int ticktime,
                          @Param("fee") double fee,
                          @Param("leverage") int leverage,
                          @Param("batch") int batch);
}
