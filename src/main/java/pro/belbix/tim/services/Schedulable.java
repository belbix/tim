package pro.belbix.tim.services;

public interface Schedulable {
    void start();

    void stop();

    String getThreadName();
}
