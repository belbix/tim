package pro.belbix.tim.repositories;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pro.belbix.tim.entity.Values;

public interface ValuesRepository extends CrudRepository<Values, Long> {

    @Transactional
    @Modifying
    @Query("delete from Values where type=:type and name=:name")
    void deleteOldValue(@Param("type") String type, @Param("name") String name);

}
