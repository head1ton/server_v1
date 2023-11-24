package ai.serverapi.order.controller.response;

import ai.serverapi.order.domain.model.Order;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@AllArgsConstructor
@Getter
@Builder
public class OrderListResponse {

    private int totalPage;
    private Long totalElements;
    private int numberOfElements;
    private Boolean last;
    private Boolean empty;
    private List<OrderResponse> list;

    public static OrderListResponse from(Page<Order> orderPage) {
        return OrderListResponse.builder()
                                .totalPage(orderPage.getTotalPages())
                                .totalElements(orderPage.getTotalElements())
                                .numberOfElements(orderPage.getNumberOfElements())
                                .last(orderPage.isLast())
                                .empty(orderPage.isEmpty())
                                .list(orderPage.getContent().stream().map(Order::toResponse)
                                               .collect(Collectors.toList()))
                                .build();
    }
}
