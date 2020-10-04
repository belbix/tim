package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.Tick;

import java.time.LocalDateTime;
import java.util.List;

public interface TickRepository extends JpaRepository<Tick, String> {

    //    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :dateStart" +
            " and t.date <= :dateEnd" +
            " order by t.date")
    List<Tick> getTicks(@Param("server") String server, @Param("symbol") String symbol,
                        @Param("dateStart") LocalDateTime dateStart,
                        @Param("dateEnd") LocalDateTime dateEnd);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :dateStart" +
            " and t.date <= :dateEnd" +
            " order by t.date desc")
    List<Tick> getTicksClose(@Param("server") String server, @Param("symbol") String symbol,
                             @Param("dateStart") LocalDateTime dateStart,
                             @Param("dateEnd") LocalDateTime dateEnd,
                             Pageable pageable);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :afterDate" +
            " order by t.date desc")
    List<Tick> getTicksAfterDate(@Param("server") String server, @Param("symbol") String symbol,
                                 @Param("afterDate") LocalDateTime afterDate,
                                 Pageable pageable);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " order by t.date desc")
    List<Tick> getTopTicks(@Param("server") String server, @Param("symbol") String symbol, Pageable pageable);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date <= :date" +
            " order by t.date desc")
    List<Tick> getTopTicksBeforeDate(@Param("server") String server,
                                     @Param("symbol") String symbol,
                                     @Param("date") LocalDateTime date,
                                     Pageable pageable);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :afterDate" +
            " and t.date <= :beforDate" +
            " order by t.date desc")
    List<Tick> getTopTicksAfterAndBeforeDate(@Param("server") String server,
                                             @Param("symbol") String symbol,
                                             @Param("afterDate") LocalDateTime afterDate,
                                             @Param("beforDate") LocalDateTime beforDate,
                                             Pageable pageable);

    @Query("select t.date from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " order by t.date desc ")
    List<LocalDateTime> getMaxDate(@Param("server") String server, @Param("symbol") String symbol, Pageable pageable);

    @Query("select t.date from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " order by t.date asc")
    List<LocalDateTime> getMinDate(@Param("server") String server, @Param("symbol") String symbol, Pageable pageable);

    @Query("select t.date from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :after" +
            " and t.date <= :before" +
            " order by t.date asc")
    List<LocalDateTime> getMinDateAfterAndBefor(@Param("server") String server,
                                                @Param("symbol") String symbol,
                                                @Param("after") LocalDateTime after,
                                                @Param("before") LocalDateTime before,
                                                Pageable pageable);

    @Query("select t from Tick t " +
            "where t.server = :server " +
            " and t.symbol = :symbol" +
            " and t.date >= :dateStart" +
            " order by t.date")
    List<Tick> getTicksAtDate(@Param("server") String server,
                              @Param("symbol") String symbol,
                              @Param("dateStart") LocalDateTime dateStart,
                              Pageable pageable);
}
