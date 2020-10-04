package pro.belbix.tim.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.belbix.tim.download.TickDownloader;
import pro.belbix.tim.exceptions.TIMRuntimeException;
import pro.belbix.tim.strategies.SrsiStrategy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SchedulerService {
    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);
    private final EmailService emailService;
    private final ValuesService valuesService;
    private List<Schedulable> schedulables1 = new CopyOnWriteArrayList<>();
    private List<Schedulable> schedulables2 = new CopyOnWriteArrayList<>();
    private List<Schedulable> schedulables3 = new CopyOnWriteArrayList<>();

    @Autowired
    public SchedulerService(EmailService emailService, ValuesService valuesService) {
        this.emailService = emailService;
        this.valuesService = valuesService;
    }

    @Scheduled(fixedDelay = 1000)
    public void schedule1() {
        for (Schedulable schedulable : schedulables1) {
            handle(schedulable);
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void schedule2() {
        for (Schedulable schedulable : schedulables2) {
            handle(schedulable);
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void schedule3() {
        for (Schedulable schedulable : schedulables3) {
            handle(schedulable);
        }
    }

    //TODO separate threads
    public void handle(Schedulable schedulable) {
        try {
            schedulable.start();
            valuesService.addThreadStatus(schedulable.getThreadName(), "OK");
        } catch (Throwable e) {
            log.error("Error loop", e);
            emailService.sendError("Loop error: " + e.getMessage(), e);
            valuesService.addThreadStatus(schedulable.getThreadName(), e.getMessage());
            if (e instanceof Error) {
                schedulable.stop();
            }
        }
    }

    public void addSchedulable(Schedulable schedulable) {
        if (schedulable == null) throw new TIMRuntimeException("Schedulable is null");

        if (schedulable instanceof TickDownloader) {
            schedulables2.add(schedulable);
        } else if (schedulable instanceof OrdersWatcher) {
            schedulables3.add(schedulable);
        } else if (schedulable instanceof SrsiStrategy) {
            schedulables3.add(schedulable);
        } else {
            throw new TIMRuntimeException("Schedulable invalid type: " + schedulable.getClass().getName());
        }

    }

}
