package ai.serverapi.order.controller;

import ai.serverapi.global.base.Api;
import ai.serverapi.global.base.MessageVo;
import ai.serverapi.global.base.ResultCode;
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
import ai.serverapi.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api-prefix}/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Api<PostTempOrderResponse>> postOrder(
        @RequestBody @Validated TempOrderRequest tempOrderRequest,
        HttpServletRequest request,
        BindingResult bindingResult
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(
                                 new Api<>(
                                     ResultCode.POST.code,
                                     ResultCode.POST.message,
                                     orderService.postTempOrder(tempOrderRequest, request))
                             );
    }

    @GetMapping("/temp/{order_id}")
    public ResponseEntity<Api<TempOrderResponse>> getOrderInfo(
        @PathVariable(name = "order_id") Long orderId,
        HttpServletRequest request) {
        return ResponseEntity.ok(
            new Api<>(
                ResultCode.SUCCESS.code,
                ResultCode.SUCCESS.message,
                orderService.getOrderInfo(orderId, request))
        );
    }

    @GetMapping("/member")
    public ResponseEntity<Api<OrderListResponse>> getOrderByMember(
        @PageableDefault(size = 10, page = 0) Pageable pageable,
        @RequestParam(required = false, name = "search") String search,
        @RequestParam(required = false, name = "status") String status,
        HttpServletRequest request) {
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                orderService.getOrderListByMember(pageable, search, status, request))
        );
    }

    @PatchMapping("/complete")
    public ResponseEntity<Api<CompleteOrderResponse>> completeOrder(
        @RequestBody @Validated CompleteOrderRequest completeOrderRequest,
        HttpServletRequest request,
        BindingResult bindingResult) {
        return ResponseEntity.ok(
            new Api<>(
                ResultCode.SUCCESS.code,
                ResultCode.SUCCESS.message,
                orderService.completeOrder(completeOrderRequest, request))
        );
    }

    @GetMapping("/seller")
    public ResponseEntity<Api<OrderListResponse>> getOrderBySeller(
        @PageableDefault(size = 10, page = 0) Pageable pageable,
        @RequestParam(required = false, name = "search") String search,
        @RequestParam(required = false, name = "status") String status,
        HttpServletRequest request) {
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                orderService.getOrderListBySeller(pageable, search, status, request))
        );
    }

    @GetMapping("/seller/{order_id}")
    public ResponseEntity<Api<OrderResponse>> getOrderDetailBySeller(
        @PathVariable(name = "order_id") Long orderId,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                orderService.getOrderDetailBySeller(orderId, request))
        );
    }

    @GetMapping("/member/{order_id}")
    public ResponseEntity<Api<OrderResponse>> getOrderDetailByMember(
        @PathVariable(name = "order_id") Long orderId,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                orderService.getOrderDetailByMember(orderId, request))
        );
    }

    @PatchMapping("/member/cancel")
    public ResponseEntity<Api<MessageVo>> cancelOrder(
        @RequestBody @Validated CancelOrderRequest cancelOrderRequest,
        HttpServletRequest request) {
        orderService.cancelOrder(cancelOrderRequest, request);

        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                MessageVo.builder().message("주문이 취소되었습니다.").build())
        );
    }

    @PatchMapping("/seller/cancel")
    public ResponseEntity<Api<MessageVo>> cancelOrderBySeller(
        @RequestBody @Validated CancelOrderRequest cancelOrderRequest,
        HttpServletRequest request) {
        orderService.cancelOrderBySeller(cancelOrderRequest, request);
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                MessageVo.builder().message("주문이 취소되었습니다.").build())
        );
    }

    @PatchMapping("/seller/processing")
    public ResponseEntity<Api<MessageVo>> processingOrder(
        @RequestBody @Validated ProcessingOrderRequest processingOrderRequest,
        HttpServletRequest request) {
        orderService.processingOrder(processingOrderRequest, request);
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                MessageVo.builder().message("주문 상태가 변경되었습니다.").build())
        );
    }

    @PatchMapping("/seller/confirm")
    public ResponseEntity<Api<MessageVo>> confirmOrder(
        @RequestBody @Validated ConfirmOrderRequest confirmOrderRequest,
        HttpServletRequest request) {
        orderService.confirmOrder(confirmOrderRequest, request);
        return ResponseEntity.ok(
            new Api<>(ResultCode.SUCCESS.code, ResultCode.SUCCESS.message,
                MessageVo.builder().message("주문 상태가 변경되었습니다.").build())
        );
    }
}
