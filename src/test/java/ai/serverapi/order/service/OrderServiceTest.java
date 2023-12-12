package ai.serverapi.order.service;

import static ai.serverapi.Base.MEMBER_LOGIN;
import static ai.serverapi.Base.PRODUCT_ID_MASK;
import static ai.serverapi.Base.PRODUCT_ID_NORMAL;
import static ai.serverapi.Base.PRODUCT_OPTION_ID_MASK;
import static ai.serverapi.Base.SELLER_LOGIN;
import static ai.serverapi.OrderBase.ORDER_FIRST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import ai.serverapi.member.port.MemberJpaRepository;
import ai.serverapi.member.service.MemberAuthServiceImpl;
import ai.serverapi.order.controller.request.CancelOrderRequest;
import ai.serverapi.order.controller.request.CompleteOrderRequest;
import ai.serverapi.order.controller.request.ProcessingOrderRequest;
import ai.serverapi.order.controller.request.TempOrderDto;
import ai.serverapi.order.controller.request.TempOrderRequest;
import ai.serverapi.order.controller.response.CompleteOrderResponse;
import ai.serverapi.order.controller.response.OrderListResponse;
import ai.serverapi.order.controller.response.OrderResponse;
import ai.serverapi.order.controller.response.PostTempOrderResponse;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.enums.OrderItemStatus;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.order.port.DeliveryJpaRepository;
import ai.serverapi.order.port.OrderItemJpaRepository;
import ai.serverapi.order.port.OrderJpaRepository;
import ai.serverapi.product.port.CategoryJpaRepository;
import ai.serverapi.product.port.OptionJpaRepository;
import ai.serverapi.product.port.ProductJpaRepository;
import ai.serverapi.product.port.SellerJpaRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@SqlGroup({
    @Sql(scripts = {"/sql/init.sql", "/sql/product.sql",
        "/sql/order.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Transactional(readOnly = true)
@Execution(ExecutionMode.CONCURRENT)
class OrderServiceTest {

    @Autowired
    private MemberAuthServiceImpl memberAuthService;
    private final MockHttpServletRequest request = new MockHttpServletRequest();
    @Autowired
    private OrderServiceImpl orderService;
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private SellerJpaRepository sellerJpaRepository;
    @Autowired
    private OptionJpaRepository optionJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;
    @Autowired
    private DeliveryJpaRepository deliveryJpaRepository;

    @AfterEach
    void cleanUp() {
        deliveryJpaRepository.deleteAll();
        orderItemJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        optionJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("임시 주문 등록")
    void tempOrder() {
        TempOrderRequest tempOrderRequest = TempOrderRequest.builder()
                                                            .orderList(List.of(
                                                                TempOrderDto.builder()
                                                                            .ea(2)
                                                                            .productId(
                                                                                PRODUCT_ID_NORMAL)
                                                                            .build(),
                                                                TempOrderDto.builder()
                                                                            .ea(3)
                                                                            .productId(
                                                                                PRODUCT_ID_MASK)
                                                                            .optionId(
                                                                                PRODUCT_OPTION_ID_MASK)
                                                                            .build()
                                                            ))
                                                 .build();

        request.addHeader(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken());

        PostTempOrderResponse postTempOrderResponse = orderService.postTempOrder(tempOrderRequest,
            request);

        assertThat(postTempOrderResponse.getOrderId()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("주문 완료")
    void completeOrder() {
        request.addHeader(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken());
        CompleteOrderRequest completeOrderRequest = CompleteOrderRequest.builder()
                                                                        .orderId(ORDER_FIRST_ID)
                                                                        .ownerAddress("주소")
                                                                        .ownerAddressDetail("상세 주소")
                                                                        .ownerName("주문자")
                                                                        .ownerTel("01012341234")
                                                                        .recipientAddress("수령인 주소")
                                                                        .recipientAddressDetail(
                                                                            "수령인 상세 주소")
                                                                        .recipientName("수령인")
                                                                        .recipientTel("01012341234")
                                                                        .build();

        CompleteOrderResponse completeOrderResponse = orderService.completeOrder(
            completeOrderRequest, request);

        assertThat(completeOrderResponse.getOrderNumber()).isNotNull();
    }

    @Test
    @DisplayName("관리자툴에서 주문 불러오기 성공")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void getOrderListBySeller() {
        //given
        request.addHeader(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken());
        //when
        Pageable pageable = Pageable.ofSize(10);
        OrderListResponse complete = orderService.getOrderListBySeller(pageable, "",
            OrderStatus.ORDERED.name(),
            request);
        //then
        assertThat(complete.getTotalElements()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("사용자 주문 불러오기 성공")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void getOrderListByMember() {
        request.addHeader(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken());

        Pageable pageable = Pageable.ofSize(10);
        OrderListResponse complete = orderService.getOrderListByMember(pageable, "",
            OrderStatus.ORDERED.name(),
            request);

        assertThat(complete.getTotalElements()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("관리자툴에서 주문 상세 불러오기 성공")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void getOrderDetailByMember() {

        request.addHeader(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken());

        OrderResponse orderDetail = orderService.getOrderDetailByMember(ORDER_FIRST_ID, request);

        assertThat(orderDetail).isNotNull();
    }

    @Test
    @DisplayName("관리자툴에서 주문 상세 불러오기 성공")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void getOrderDetailBySeller() {
        request.addHeader(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken());

        OrderResponse orderDetail = orderService.getOrderDetailBySeller(ORDER_FIRST_ID, request);

        assertThat(orderDetail).isNotNull();
    }

    @Test
    @DisplayName("주문 취소 성공")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void cancelOrder() {
        request.addHeader(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken());

        CancelOrderRequest cancelOrderRequest = CancelOrderRequest.builder().orderId(ORDER_FIRST_ID)
                                                                  .build();

        orderService.cancelOrder(cancelOrderRequest, request);
        OrderEntity orderEntity = orderJpaRepository.findById(ORDER_FIRST_ID).get();

        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CANCEL);

        orderEntity.getOrderItemList().forEach(orderItemEntity -> assertThat(
            orderItemEntity.getStatus()).isEqualTo(OrderItemStatus.CANCEL));
    }

    @Test
    @DisplayName("주문 취소 성공 by Seller")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void cancelOrderBySeller() {
        request.addHeader(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken());

        CancelOrderRequest cancelOrderRequest = CancelOrderRequest.builder().orderId(ORDER_FIRST_ID)
                                                                  .build();

        orderService.cancelOrderBySeller(cancelOrderRequest, request);
        OrderEntity orderEntity = orderJpaRepository.findById(ORDER_FIRST_ID).get();

        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CANCEL);

        orderEntity.getOrderItemList().forEach(orderItemEntity -> assertThat(
            orderItemEntity.getStatus()).isEqualTo(OrderItemStatus.CANCEL));
    }

    @Test
    @DisplayName("주문 확인, 상품 준비중 성공 by Seller")
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    void processingOrder() {
        request.addHeader(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken());
        ProcessingOrderRequest processingOrderRequest = ProcessingOrderRequest.builder().orderId(
            ORDER_FIRST_ID).build();

        orderService.processingOrder(processingOrderRequest, request);

        OrderEntity orderEntity = orderJpaRepository.findById(ORDER_FIRST_ID).get();
        assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        orderEntity.getOrderItemList().forEach(orderItemEntity ->
            assertThat(orderItemEntity.getStatus()).isEqualTo(OrderItemStatus.PROCESSING));
    }
}
