package ai.serverapi.order.repository.port;


import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.member.domain.model.Member;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.order.repository.OrderCustomJpaRepository;
import ai.serverapi.order.repository.OrderJpaRepository;
import ai.serverapi.product.domain.entity.SellerEntity;
import ai.serverapi.product.domain.model.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderCustomJpaRepository orderCustomJpaRepository;

    @Override
    public Order save(final Order order) {
        return orderJpaRepository.save(OrderEntity.from(order)).toModel();
    }

    @Override
    public Order findById(final Long orderId) {
        return orderJpaRepository.findById(orderId).orElseThrow(
            () -> new IllegalArgumentException("유효하지 않은 주문 번호입니다.")).toModel();
    }

    @Override
    public Page<Order> findAllBySeller(final Pageable pageable, final String search,
        final OrderStatus status,
        final Seller seller) {
        return orderCustomJpaRepository.findAllBySeller(pageable, search, status,
            SellerEntity.from(seller));
    }

    @Override
    public Page<Order> findAllByMember(final Pageable pageable, final String search,
        final OrderStatus status,
        final Member member) {
        return orderCustomJpaRepository.findAllByMember(pageable, search, status,
            MemberEntity.from(member));
    }

    @Override
    public Order findByIdAndSeller(final Long orderId, final Seller seller) {
        return orderCustomJpaRepository.findByIdAndSeller(orderId, SellerEntity.from(seller))
                                       .orElseThrow(
                                           () -> new IllegalArgumentException("유효하지 않은 주문 번호입니다."));
    }

//    @Override
//    public OrderVo findByIdAndSeller(final Long orderId, final Seller seller) {
//        return orderCustomJpaRepository.findByIdAndSeller(orderId, SellerEntity.from(seller))
//                                       .orElseThrow(
//                                           () -> new IllegalArgumentException("유효하지 않은 주문 번호입니다."));
//    }
}
