package pro.belbix.tim.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;

//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = SimpleApp.class)
public class TickRepositoryTest {

    //    @Autowired
    private TickRepository tickRepository;

    //    @Test
    public void getMaxDate() {
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(0, 1);
        System.out.println(tickRepository.getMaxDate("bitmex", "XBTUSD", pageable).iterator().next() + " for "
                + Duration.between(now, Instant.now()));

    }

    //    @Test
    public void getMinDate() {
    }
}
