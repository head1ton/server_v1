package ai.serverapi.order.port;

import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.entity.SellerEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderCustomJpaRepository {

    Page<Order> findAllBySeller(Pageable pageable, String search, OrderStatus status,
        SellerEntity sellerEntity);

    Page<Order> findAllByMember(Pageable pageable, String search, OrderStatus status,
        MemberEntity memberEntity);

    Optional<Order> findByIdAndSeller(Long orderId, SellerEntity from);

//    Optional<OrderVo> findByIdAndSeller(Long orderId, SellerEntity from);
}
