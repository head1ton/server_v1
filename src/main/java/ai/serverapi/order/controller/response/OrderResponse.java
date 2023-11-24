package ai.serverapi.order.controller.response;

import ai.serverapi.member.controller.response.MemberResponse;
import ai.serverapi.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
public class OrderResponse {

    private Long orderId;
    private MemberResponse member;
    private String orderNumber;
    private List<OrderItemResponse> orderItemList;
    private DeliveryResponse delivery;
    private OrderStatus status;
    private String orderName;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
