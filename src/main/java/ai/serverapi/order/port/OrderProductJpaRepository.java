package ai.serverapi.order.port;

import ai.serverapi.order.domain.entity.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProductEntity, Long> {

}
