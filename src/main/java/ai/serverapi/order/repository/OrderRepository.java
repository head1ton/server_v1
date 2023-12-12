package ai.serverapi.order.repository;

import ai.serverapi.member.domain.model.Member;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.model.Seller;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepository {

    Order save(Order order);

    Order findById(Long orderId);

    Page<Order> findAllBySeller(Pageable pageable, String search, OrderStatus status,
        Seller seller);

    Page<Order> findAllByMember(Pageable pageable, String search, OrderStatus status,
        Member member);

    Order findByIdAndSeller(Long orderId, Seller seller);

//    OrderVo findByIdAndSeller(Long orderId, Seller seller);
}
