package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.NeuronState;

import java.util.List;

public interface NeuronStateRepository extends CrudRepository<NeuronState, Long> {

    @Query("select e from NeuronState e where" +
            " e.strategy = :strategy" +
            " and e.symbol = :symbol" +
            " and e.fee = :fee" +
            " and e.leverage = :leverage" +
            " and e.windowProp = :windowProp" +
            " and e.timeframe = :timeframe" +
            " and e.compressionProp = :compressionProp" +
            " and e.countOfSublayer = :countOfSublayer" +
            " and e.state is not null" +
            " order by e.result desc")
    List<NeuronState> getTopStates(@Param("strategy") String strategy,
                                   @Param("symbol") String symbol,
                                   @Param("fee") double fee,
                                   @Param("leverage") int leverage,
                                   @Param("windowProp") int windowProp,
                                   @Param("timeframe") int timeframe,
                                   @Param("compressionProp") double compressionProp,
                                   @Param("countOfSublayer") int countOfSublayer,
                                   Pageable pageable);

    @Query("select e from NeuronState e where" +
            " e.strategy = :strategy" +
            " and e.symbol = :symbol" +
            " and e.fee = :fee" +
            " and e.leverage = :leverage" +
            " and e.windowProp = :windowProp" +
            " and e.timeframe = :timeframe" +
            " and e.compressionProp = :compressionProp" +
            " and e.countOfSublayer = :countOfSublayer" +
            " and e.state is not null" +
            " order by e.created desc")
    List<NeuronState> getTopStatesLast(@Param("strategy") String strategy,
                                       @Param("symbol") String symbol,
                                       @Param("fee") double fee,
                                       @Param("leverage") int leverage,
                                       @Param("windowProp") int windowProp,
                                       @Param("timeframe") int timeframe,
                                       @Param("compressionProp") double compressionProp,
                                       @Param("countOfSublayer") int countOfSublayer,
                                       Pageable pageable);

}
