package ai.serverapi.order.repository;

import ai.serverapi.global.querydsl.QuerydslConfig;
import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.domain.entity.QOrderEntity;
import ai.serverapi.order.domain.entity.QOrderItemEntity;
import ai.serverapi.order.domain.entity.QOrderOptionEntity;
import ai.serverapi.order.domain.entity.QOrderProductEntity;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.entity.SellerEntity;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OrderCustomJpaRepositoryImpl implements OrderCustomJpaRepository {

    private final QuerydslConfig q;

    @Override
    public Page<Order> findAll(final Pageable pageable, final String search,
        final OrderStatus status,
        final SellerEntity sellerEntity,
        final MemberEntity memberEntity) {
        QOrderEntity order = QOrderEntity.orderEntity;
        QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
        QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;
        QOrderOptionEntity orderOption = QOrderOptionEntity.orderOptionEntity;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(order.status.eq(OrderStatus.COMPLETE));

        if (sellerEntity != null) {
            builder.and(orderProduct.seller.id.eq(sellerEntity.getId()));
        }

        if (memberEntity != null) {
            builder.and(order.member.id.eq(memberEntity.getId()));
        }

        if (search != null || !search.equals("")) {
            builder.and(orderProduct.mainTitle.contains(search));
        }

        List<OrderEntity> fetch = q.query()
                                   .selectFrom(order)
                                   .join(orderItem)
                                   .on(order.id.eq(orderItem.order.id))
                                   .join(orderProduct)
                                   .on(orderProduct.id.eq(orderItem.orderProduct.id))
                                   .leftJoin(orderOption)
                                   .on(orderProduct.orderOption.id.eq(orderOption.id))
                                   .where(builder)
                                   .orderBy(order.createdAt.desc())
                                   .offset(pageable.getOffset())
                                   .limit(pageable.getPageSize())
                                   .fetch();

        List<Order> content = fetch.stream().map(OrderEntity::toModel).collect(Collectors.toList());

        long total = q.query()
                      .from(order)
                      .join(orderItem)
                      .on(order.id.eq(orderItem.order.id))
                      .join(orderProduct)
                      .on(orderProduct.id.eq(orderItem.orderProduct.id))
                      .leftJoin(orderOption)
                      .on(orderProduct.orderOption.id.eq(orderOption.id))
                      .where(builder)
                      .stream()
                      .count();

        return new PageImpl<>(content, pageable, total);
    }

//    @Override
//    public Optional<OrderVo> findByIdAndSeller(final Long orderId,
//        final SellerEntity sellerEntity) {
//
//        QOrderEntity order = QOrderEntity.orderEntity;
//        QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
//        QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;
//        QOrderOptionEntity orderOption = QOrderOptionEntity.orderOptionEntity;
//
//        BooleanBuilder builder = new BooleanBuilder();
//        builder.and(order.id.eq(orderId));
//        builder.and(orderProduct.seller.id.eq(sellerEntity.getId()));
//
//        OrderEntity fetch = q.query().selectFrom(order)
//                             .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
//                             .join(orderProduct)
//                             .on(orderProduct.id.eq(orderItem.orderProduct.id)).fetchJoin()
//                             .leftJoin(orderOption)
//                             .on(orderItem.orderOption.id.eq(orderOption.id)).fetchJoin()
//                             .where(builder)
//                             .fetchFirst();
//
//        return Optional.ofNullable(new OrderVo(fetch));
//    }
}
