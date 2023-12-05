package ai.serverapi.order.repository;

import ai.serverapi.order.domain.entity.DeliveryEntity;
import ai.serverapi.order.domain.model.Delivery;
import ai.serverapi.order.port.DeliveryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;

    @Override
    public Delivery save(final Delivery delivery) {
        return deliveryJpaRepository.save(DeliveryEntity.from(delivery)).toModel();
    }
}
