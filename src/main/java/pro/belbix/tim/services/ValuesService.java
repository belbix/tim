package pro.belbix.tim.services;

import org.springframework.stereotype.Service;
import pro.belbix.tim.entity.Values;
import pro.belbix.tim.repositories.ValuesRepository;

import java.time.LocalDateTime;

@Service
public class ValuesService {
    final static String STATUS = "STATUS";
    private final ValuesRepository valuesRepository;

    public ValuesService(ValuesRepository valuesRepository) {
        this.valuesRepository = valuesRepository;
    }

    public void addThreadStatus(String threadName, String status) {
        valuesRepository.deleteOldValue(STATUS, threadName);

        Values values = new Values();
        values.setChanged(LocalDateTime.now());
        values.setType(STATUS);
        values.setName(threadName);
        values.setValue(status);
        valuesRepository.save(values);
    }

}
