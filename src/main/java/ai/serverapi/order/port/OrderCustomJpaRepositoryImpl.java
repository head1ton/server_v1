package ai.serverapi.order.port;

import ai.serverapi.global.querydsl.QuerydslConfig;
import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.domain.entity.QDeliveryEntity;
import ai.serverapi.order.domain.entity.QOrderEntity;
import ai.serverapi.order.domain.entity.QOrderItemEntity;
import ai.serverapi.order.domain.entity.QOrderOptionEntity;
import ai.serverapi.order.domain.entity.QOrderProductEntity;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.entity.SellerEntity;
import com.querydsl.core.BooleanBuilder;
import java.util.List;
import java.util.Optional;
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
    public Page<Order> findAllBySeller(final Pageable pageable, final String search,
        final OrderStatus status,
        final SellerEntity sellerEntity) {
        QOrderEntity order = QOrderEntity.orderEntity;
        QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
        QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;
        QOrderOptionEntity orderOption = QOrderOptionEntity.orderOptionEntity;
        QDeliveryEntity delivery = QDeliveryEntity.deliveryEntity;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(orderProduct.seller.id.eq(sellerEntity.getId()));

        if (status != null) {
            builder.and(order.status.eq(status));
        } else {
            builder.and(order.status.ne(OrderStatus.TEMP));
        }

        if (search != null && !search.isBlank()) {
            builder.and(orderProduct.mainTitle.contains(search));
        }

        List<OrderEntity> fetch = q.query().selectFrom(order)
                                   .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
                                   .join(orderProduct)
                                   .on(orderProduct.id.eq(orderItem.orderProduct.id)).fetchJoin()
                                   .leftJoin(orderOption)
                                   .on(orderOption.id.eq(orderItem.orderProduct.orderOption.id))
                                   .fetchJoin()
                                   .leftJoin(delivery).on(order.delivery.id.eq(delivery.id))
                                   .fetchJoin()
                                   .where(builder)
                                   .orderBy(order.id.desc())
                                   .offset(pageable.getOffset())
                                   .limit(pageable.getPageSize())
                                   .fetch();

        List<Order> content = fetch.stream().map(OrderEntity::toModel).collect(Collectors.toList());

        Long total = q.query()
                      .from(order)
                      .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
                      .join(orderProduct).on(orderProduct.id.eq(orderItem.orderProduct.id))
                      .fetchJoin()
                      .leftJoin(orderOption)
                      .on(orderOption.id.eq(orderItem.orderProduct.orderOption.id)).fetchJoin()
                      .leftJoin(delivery).on(order.delivery.id.eq(delivery.id)).fetchJoin()
                      .where(builder).stream().count();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Order> findAllByMember(final Pageable pageable, final String search,
        final OrderStatus status,
        final MemberEntity memberEntity) {
        QOrderEntity order = QOrderEntity.orderEntity;
        QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
        QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;
        QOrderOptionEntity orderOption = QOrderOptionEntity.orderOptionEntity;
        QDeliveryEntity delivery = QDeliveryEntity.deliveryEntity;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(order.member.id.eq(memberEntity.getId()));

        if (status != null) {
            builder.and(order.status.eq(status));
        } else {
            builder.and(order.status.ne(OrderStatus.TEMP));
        }

        if (search != null && !search.isBlank()) {
            builder.and(orderProduct.mainTitle.contains(search));
        }

        List<OrderEntity> fetch = q.query().selectFrom(order)
                                   .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
                                   .join(orderProduct)
                                   .on(orderProduct.id.eq(orderItem.orderProduct.id)).fetchJoin()
                                   .leftJoin(orderOption)
                                   .on(orderOption.id.eq(orderItem.orderProduct.orderOption.id))
                                   .fetchJoin()
                                   .leftJoin(delivery).on(order.delivery.id.eq(delivery.id))
                                   .fetchJoin()
                                   .where(builder)
                                   .orderBy(order.id.desc())
                                   .offset(pageable.getOffset())
                                   .limit(pageable.getPageSize())
                                   .fetch();

        List<Order> content = fetch.stream().map(OrderEntity::toModel).collect(Collectors.toList());

        Long total = q.query()
                      .from(order)
                      .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
                      .join(orderProduct).on(orderProduct.id.eq(orderItem.orderProduct.id))
                      .fetchJoin()
                      .leftJoin(orderOption)
                      .on(orderOption.id.eq(orderItem.orderProduct.orderOption.id)).fetchJoin()
                      .leftJoin(delivery).on(order.delivery.id.eq(delivery.id)).fetchJoin()
                      .where(builder).stream().count();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<Order> findByIdAndSeller(final Long orderId,
        final SellerEntity sellerEntity) {

        QOrderEntity order = QOrderEntity.orderEntity;
        QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
        QOrderProductEntity orderProduct = QOrderProductEntity.orderProductEntity;
        QOrderOptionEntity orderOption = QOrderOptionEntity.orderOptionEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(order.id.eq(orderId));
        builder.and(orderProduct.seller.id.eq(sellerEntity.getId()));

        OrderEntity fetch = q.query().selectFrom(order)
                             .join(orderItem).on(order.id.eq(orderItem.order.id)).fetchJoin()
                             .join(orderProduct).on(orderProduct.id.eq(orderItem.orderProduct.id))
                             .fetchJoin()
                             .leftJoin(orderOption)
                             .on(orderProduct.orderOption.id.eq(orderOption.id)).fetchJoin()
                             .where(builder)
                             .fetchFirst();

        return Optional.ofNullable(fetch.toModel());
    }
}
