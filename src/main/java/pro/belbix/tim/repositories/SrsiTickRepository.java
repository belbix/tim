package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.SrsiTick;
import pro.belbix.tim.entity.SrsiTickI;

import java.time.LocalDateTime;
import java.util.List;

public interface SrsiTickRepository extends JpaRepository<SrsiTick, Integer> {
    @Query("select t from SrsiTick t " +
            " where t.date >= :dateStart" +
            " and t.date <= :dateEnd" +
            " and t.ticktime = :ticktime" +
            " order by t.date")
    List<SrsiTickI> load(@Param("dateStart") LocalDateTime dateStart,
                         @Param("dateEnd") LocalDateTime dateEnd,
                         @Param("ticktime") int ticktime);

    @Query("select count(t) from SrsiTick t")
    long fetchCount();

    @Query("select t.date from SrsiTick t where t.ticktime = :ticktime order by t.date desc")
    List<LocalDateTime> lastDate(@Param("ticktime") int ticktime, Pageable pageable);
}
