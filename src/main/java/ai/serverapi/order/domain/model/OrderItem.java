package ai.serverapi.order.domain.model;

import ai.serverapi.order.controller.response.OrderItemResponse;
import ai.serverapi.order.controller.response.TempOrderItemResponse;
import ai.serverapi.order.enums.OrderItemStatus;
import ai.serverapi.product.enums.ProductType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItem {

    private Long id;
    private Order order;
    private OrderProduct orderProduct;
    private OrderOption orderOption;
    private OrderItemStatus status;
    private int ea;
    private int productPrice;
    private int productTotalPrice;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static OrderItem create(Order order, OrderProduct orderProduct, int ea) {
        OrderOption orderOption = orderProduct.getOrderOption();
        int price = orderProduct.getType() == ProductType.OPTION ? orderProduct.getPrice()
            + orderOption.getExtraPrice() : orderProduct.getPrice();
        int totalPrice = price * ea;

        return OrderItem.builder()
                        .order(order)
                        .orderProduct(orderProduct)
                        .status(OrderItemStatus.TEMP)
                        .ea(ea)
                        .productPrice(price)
                        .productTotalPrice(totalPrice)
                        .createdAt(LocalDateTime.now())
                        .modifiedAt(LocalDateTime.now())
                        .build();
    }

    public TempOrderItemResponse toTempResponse() {
        return TempOrderItemResponse.builder()
                                    .productId(orderProduct.getProductId())
                                    .status(orderProduct.getStatus())
                                    .ea(ea)
                                    .productPrice(productPrice)
                                    .productTotalPrice(productTotalPrice)
                                    .createdAt(createdAt)
                                    .modifiedAt(modifiedAt)
                                    .seller(orderProduct.getSeller().toResponse())
                                    .category(orderProduct.getCategory().toResponse())
                                    .option(orderProduct.getOrderOption() == null ? null
                                        : orderProduct.getOrderOption()
                                                      .toResponse())
                                    .mainTitle(orderProduct.getMainTitle())
                                    .mainExplanation(orderProduct.getMainExplanation())
                                    .productMainExplanation(
                                        orderProduct.getProductMainExplanation())
                                    .productSubExplanation(orderProduct.getProductSubExplanation())
                                    .originPrice(orderProduct.getOriginPrice())
                                    .price(orderProduct.getPrice())
                                    .purchaseInquiry(orderProduct.getPurchaseInquiry())
                                    .origin(orderProduct.getOrigin())
                                    .producer(orderProduct.getProducer())
                                    .mainImage(orderProduct.getMainImage())
                                    .image1(orderProduct.getImage1())
                                    .image2(orderProduct.getImage2())
                                    .image3(orderProduct.getImage3())
                                    .viewCnt(orderProduct.getViewCnt())
                                    .type(orderProduct.getType())
                                    .build();
    }

    public void cancel() {
        this.status = OrderItemStatus.CANCEL;
    }

    public void processing() {
        this.status = OrderItemStatus.PROCESSING;
    }

    public OrderItemResponse toResponse() {
        return OrderItemResponse.builder()
                                .orderItemId(id)
                                .orderProduct(orderProduct.toResponse())
                                .status(status)
                                .ea(ea)
                                .productPrice(productPrice)
                                .productTotalPrice(productTotalPrice)
                                .createdAt(createdAt)
                                .modifiedAt(modifiedAt)
                                .build();
    }
}