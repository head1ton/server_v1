package ai.serverapi.order.repository;

import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.order.controller.vo.OrderVo;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.entity.SellerEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface OrderCustomJpaRepository {

    Page<OrderVo> findAll(Pageable pageable, String search, OrderStatus status,
        SellerEntity sellerEntity, MemberEntity memberEntity);

    Optional<OrderVo> findByIdAndSeller(Long orderId, SellerEntity from);
}
