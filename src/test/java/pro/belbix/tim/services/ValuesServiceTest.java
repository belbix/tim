package pro.belbix.tim.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.tim.SimpleApp;
import pro.belbix.tim.repositories.ValuesRepository;

import static pro.belbix.tim.services.ValuesService.STATUS;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SimpleApp.class)
public class ValuesServiceTest {
    @Autowired
    private ValuesService valuesService;
    @Autowired
    private ValuesRepository valuesRepository;

    @Test
    public void shouldSaveThreadStatus() {
        valuesService.addThreadStatus("TEST_THREAD_NAME", "OK");
        valuesRepository.deleteOldValue(STATUS, "TEST_THREAD_NAME");
    }
}
