package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.tim.entity.EvoPropKey;
import pro.belbix.tim.entity.Evolution;

import java.util.List;

public interface EvolutePropertiesRepository extends CrudRepository<Evolution, Long> {

    @Query("select e from Evolution e where" +
            " e.evoPropKey = :key" +
            " order by e.result desc, e.created desc")
    List<Evolution> getTop(@Param("key") EvoPropKey key,
                           Pageable pageable);

    @Query(value = "select * from evolute_properties as p1\n" +
            "join\n" +
            "(select id\n" +
            "from (\n" +
            "         select id\n" +
            "         from evolute_properties\n" +
            "         where evo_prop_key_id = :key\n" +
            "          and result != 0" +
            "         order by result desc\n" +
            "         limit :count) as p\n" +
            "order by rand()\n" +
            "limit 1) as p2 on p1.id = p2.id",
            nativeQuery = true)
    List<Evolution> getRandomEvo(@Param("key") Long key,
                                 @Param("count") int count);

    @Query("select e.id from Evolution e where" +
            " e.evoPropKey = :key" +
            " order by e.result, e.created")
    List<Long> getBottomIds(@Param("key") EvoPropKey key,
                            Pageable pageable);

    @Query("select count(e.result) from Evolution e where" +
            " e.evoPropKey = :key")
    Long getCountByKey(@Param("key") EvoPropKey key);

    @Query("select e.id from Evolution e where" +
            " e.id not in :ids" +
            " and e.evoPropKey = :key")
    List<Long> getNotInListId(@Param("ids") List<Long> ids,
                              @Param("key") EvoPropKey key);

    @Transactional
    @Modifying
    @Query("delete from Evolution e where" +
            " e.id in :ids"
    )
    void deleteAllByIds(@Param("ids") List<Long> ids);
}
