package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.Srsi2Tick;
import pro.belbix.tim.entity.SrsiTickI;

import java.time.LocalDateTime;
import java.util.List;

public interface Srsi2TickRepository extends JpaRepository<Srsi2Tick, LocalDateTime> {
    @Query("select t from Srsi2Tick t " +
            " where t.date >= :dateStart" +
            " and t.date <= :dateEnd" +
            " order by t.date")
    List<SrsiTickI> load(@Param("dateStart") LocalDateTime dateStart,
                         @Param("dateEnd") LocalDateTime dateEnd);

    @Query("select count(t) from Srsi2Tick t")
    long fetchCount();

    @Query("select t.date from Srsi2Tick t order by t.date desc")
    List<LocalDateTime> lastDate(Pageable pageable);
}
