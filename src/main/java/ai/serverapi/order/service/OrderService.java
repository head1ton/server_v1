package ai.serverapi.order.service;

import ai.serverapi.order.controller.request.CancelOrderRequest;
import ai.serverapi.order.controller.request.CompleteOrderRequest;
import ai.serverapi.order.controller.request.ConfirmOrderRequest;
import ai.serverapi.order.controller.request.ProcessingOrderRequest;
import ai.serverapi.order.controller.request.TempOrderRequest;
import ai.serverapi.order.controller.response.CompleteOrderResponse;
import ai.serverapi.order.controller.response.OrderListResponse;
import ai.serverapi.order.controller.response.OrderResponse;
import ai.serverapi.order.controller.response.PostTempOrderResponse;
import ai.serverapi.order.controller.response.TempOrderResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    PostTempOrderResponse postTempOrder(
        TempOrderRequest tempOrderRequest,
        HttpServletRequest request
    );

    TempOrderResponse getOrderInfo(Long orderId, HttpServletRequest request);

    CompleteOrderResponse completeOrder(
        CompleteOrderRequest completeOrderRequest,
        HttpServletRequest request);

    OrderListResponse getOrderListBySeller(Pageable pageable, String search, String status,
        HttpServletRequest request);

    void cancelOrder(CancelOrderRequest cancelOrderRequest, HttpServletRequest request);

    OrderListResponse getOrderListByMember(Pageable pageable, String search, String status,
        HttpServletRequest request);

    OrderResponse getOrderDetailBySeller(Long orderId, HttpServletRequest request);

    OrderResponse getOrderDetailByMember(Long orderId, HttpServletRequest request);

    void cancelOrderBySeller(CancelOrderRequest cancelOrderRequest, HttpServletRequest request);

    void processingOrder(ProcessingOrderRequest processingOrderRequest, HttpServletRequest request);

    void confirmOrder(ConfirmOrderRequest confirmOrderRequest, HttpServletRequest request);
}
