package pro.belbix.tim.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.belbix.tim.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Integer> {
}
