package ai.serverapi.order.controller.vo;

import ai.serverapi.order.domain.entity.OrderItemEntity;
import ai.serverapi.product.controller.vo.OptionVo;
import ai.serverapi.product.enums.ProductStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
public class OrderItemVo {

    private Long productId;
    private int ea;
    private String mainTitle;
    private String mainExplanation;
    private String productMainExplanation;
    private String productSubExplanation;
    private int originPrice;
    private int price;
    private String purchaseInquiry;
    private String origin;
    private String producer;
    private String mainImage;
    private String image1;
    private String image2;
    private String image3;
    private Long viewCnt;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private SellerVo seller;
    private CategoryVo category;
    private OptionVo option;

    public static OrderItemVo from(OrderItemEntity orderItem) {
        return new OrderItemVo(orderItem.getOrderProduct().getProductId(), orderItem.getEa(),
            orderItem.getOrderProduct().getMainTitle(),
            orderItem.getOrderProduct().getMainExplanation(),
            orderItem.getOrderProduct().getProductMainExplanation(),
            orderItem.getOrderProduct().getProductSubExplanation(),
            orderItem.getOrderProduct().getOriginPrice(), orderItem.getOrderProduct().getPrice(),
            orderItem.getOrderProduct().getPurchaseInquiry(),
            orderItem.getOrderProduct().getOrigin(), orderItem.getOrderProduct().getProducer(),
            orderItem.getOrderProduct().getMainImage(), orderItem.getOrderProduct().getImage1(),
            orderItem.getOrderProduct().getImage2(), orderItem.getOrderProduct().getImage3(),
            orderItem.getOrderProduct().getViewCnt(), orderItem.getOrderProduct().getStatus(),
            orderItem.getOrderProduct().getCreatedAt(), orderItem.getModifiedAt(),
            SellerVo.of(orderItem.getOrderProduct().getSeller()),
            CategoryVo.of(orderItem.getOrderProduct().getCategory()),
            OptionVo.of(orderItem.getOrderOption())
        );

    }
}
