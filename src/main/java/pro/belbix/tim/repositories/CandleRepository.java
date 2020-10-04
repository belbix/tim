package pro.belbix.tim.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.belbix.tim.entity.Candle;

import java.time.LocalDateTime;
import java.util.List;

public interface CandleRepository extends JpaRepository<Candle, Integer> {
    @Query("select c from Candle c where" +
            " c.server = :server" +
            " and c.symbol = :symbol" +
            " and c.time = :time" +
            " and c.date <= :dateBefore" +
            " and c.date >= :dateAfter" +
            " order by c.date desc")
    List<Candle> findCandles(String server,
                             String symbol,
                             int time,
                             LocalDateTime dateBefore,
                             LocalDateTime dateAfter,
                             Pageable pageable);

    @Query("select c from Candle c where c.server = :server and c.symbol = :symbol and c.time = :time order by c.date desc")
    List<Candle> lastCandle(@Param("server") String server,
                            @Param("symbol") String symbol,
                            @Param("time") int time,
                            Pageable pageable);

    Candle getByServerAndSymbolAndDateAndTime(String server, String symbol, LocalDateTime date, int time);

    @Query("select c from Candle c where" +
            " c.server = :server" +
            " and c.symbol = :symbol" +
            " and c.time = :time" +
            " and c.date >= :date" +
            " order by c.date")
    List<Candle> loadCandlesAfter(String server,
                                  String symbol,
                                  int time,
                                  LocalDateTime date,
                                  Pageable pageable);
}
