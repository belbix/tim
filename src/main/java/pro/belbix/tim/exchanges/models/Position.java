package pro.belbix.tim.exchanges.models;

import pro.belbix.tim.entity.Order;

public interface Position {
    Double currentQty();

    String symbol();

    String server();

    boolean compareWithOrder(Order order);
}
