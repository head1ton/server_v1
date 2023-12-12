package ai.serverapi.order.domain.model;

import ai.serverapi.member.domain.model.Member;
import ai.serverapi.order.controller.response.OrderResponse;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.product.domain.model.Product;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Order {

    private Long id;
    private Member member;
    private String orderNumber;
    private List<OrderItem> orderItemList;
    private Delivery delivery;
    private OrderStatus status;
    private String orderName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Order create(Member member, List<Product> productList) {
        int productListSize = productList.size();
        String productExtraName = productListSize > 1 ? " 외 " + (productListSize - 1) + "개" : "";
        final String orderNameBuilder = productList.get(0).getMainTitle()
            + productExtraName;

        return Order.builder()
                    .member(member)
                    .orderItemList(new ArrayList<>())
                    .status(OrderStatus.TEMP)
                    .orderName(orderNameBuilder)
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build();
    }

    public void createOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        final String orderNumberBuilder = "ORDER"
            + "-"
            + now.format(formatter)
            + "-"
            + id;

        this.orderNumber = orderNumberBuilder;
    }

    public void checkOrder(Member member) {
        if (!this.member.getId().equals(member.getId())) {
            throw new IllegalArgumentException("유효하지 않은 주문입니다.");
        }
    }

    public void ordered() {
        this.status = OrderStatus.ORDERED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCEL;
    }

    public OrderResponse toResponse() {
        return OrderResponse.builder()
                            .orderId(id)
                            .member(member.toResponseForOthers())
                            .orderNumber(orderNumber)
                            .orderItemList(orderItemList.stream().map(OrderItem::toResponse)
                                                        .collect(Collectors.toList()))
                            .delivery(delivery == null ? null : delivery.toResponse())
                            .status(status)
                            .orderName(orderName)
                            .createdAt(createdAt)
                            .modifiedAt(modifiedAt)
                            .build();
    }

    public void delivery(final Delivery delivery) {
        this.delivery = delivery;
    }
}
