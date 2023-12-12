package ai.serverapi.order.controller;

import static ai.serverapi.Base.MEMBER_EMAIL;
import static ai.serverapi.Base.MEMBER_LOGIN;
import static ai.serverapi.Base.PRODUCT_ID_MASK;
import static ai.serverapi.Base.PRODUCT_ID_PEAR;
import static ai.serverapi.Base.PRODUCT_OPTION_ID_MASK;
import static ai.serverapi.Base.PRODUCT_OPTION_ID_PEAR;
import static ai.serverapi.Base.SELLER_LOGIN;
import static ai.serverapi.OrderBase.ORDER_FIRST_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.patch;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.serverapi.RestdocsBaseTest;
import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.member.port.MemberJpaRepository;
import ai.serverapi.member.service.MemberAuthServiceImpl;
import ai.serverapi.order.controller.request.CancelOrderRequest;
import ai.serverapi.order.controller.request.CompleteOrderRequest;
import ai.serverapi.order.controller.request.TempOrderDto;
import ai.serverapi.order.controller.request.TempOrderRequest;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.order.port.DeliveryJpaRepository;
import ai.serverapi.order.port.OrderItemJpaRepository;
import ai.serverapi.order.port.OrderJpaRepository;
import ai.serverapi.order.port.OrderOptionJpaRepository;
import ai.serverapi.order.port.OrderProductJpaRepository;
import ai.serverapi.product.port.CategoryJpaRepository;
import ai.serverapi.product.port.OptionJpaRepository;
import ai.serverapi.product.port.ProductJpaRepository;
import ai.serverapi.product.port.SellerJpaRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.jdbc.SqlGroup;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
@SqlGroup({
    @Sql(scripts = {"/sql/init.sql",
        "/sql/product.sql", "/sql/order.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
})
@DirtiesContext(classMode = ClassMode.BEFORE_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class OrderControllerDocs extends RestdocsBaseTest {
    private final static String PREFIX = "/api/order";
    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private CategoryJpaRepository categoryJpaRepository;
    @Autowired
    private MemberAuthServiceImpl memberAuthService;
    @Autowired
    private SellerJpaRepository sellerJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;
    @Autowired
    private OrderJpaRepository orderJpaRepository;
    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;
    @Autowired
    private OptionJpaRepository optionJpaRepository;
    @Autowired
    private DeliveryJpaRepository deliveryJpaRepository;
    @Autowired
    private OrderProductJpaRepository orderProductJpaRepository;
    @Autowired
    private OrderOptionJpaRepository orderOptionJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @AfterEach
    void cleanUp() {
        orderItemJpaRepository.deleteAll();
        orderProductJpaRepository.deleteAll();
        orderOptionJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        deliveryJpaRepository.deleteAll();
        optionJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName(PREFIX + " (POST)")
    void postTempOrder() throws Exception {
        //given
        List<TempOrderDto> orderList = new ArrayList<>();
        int orderEa1 = 3;
        int orderEa2 = 2;
        TempOrderDto order1 = TempOrderDto.builder()
                                          .productId(PRODUCT_ID_MASK)
                                          .ea(orderEa1)
                                          .optionId(PRODUCT_OPTION_ID_MASK)
                                          .build();
        TempOrderDto order2 = TempOrderDto.builder()
                                          .productId(PRODUCT_ID_PEAR)
                                          .ea(orderEa2)
                                          .optionId(PRODUCT_OPTION_ID_PEAR)
                                          .build();
        orderList.add(order1);
        orderList.add(order2);
        TempOrderRequest tempOrderRequest = new TempOrderRequest(orderList);
        //when
        ResultActions perform = mock.perform(
            post(PREFIX).contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                        .content(objectMapper.writeValueAsString(tempOrderRequest))
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            requestFields(
                fieldWithPath("order_list").type(JsonFieldType.ARRAY).description("주문 리스트"),
                fieldWithPath("order_list[].product_id").type(JsonFieldType.NUMBER)
                                                        .description("상품 id"),
                fieldWithPath("order_list[].ea").type(JsonFieldType.NUMBER).description("주문 개수"),
                fieldWithPath("order_list[].option_id").type(JsonFieldType.NUMBER)
                                                       .description("상품 옵션 id").optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id")
            )
        ));
    }
    @Test
    @DisplayName(PREFIX + "/temp/{order_id} (GET)")
    void getTempOrder() throws Exception {
        //given
        //when
        ResultActions perform = mock.perform(
            get(PREFIX + "/temp/{order_id}", ORDER_FIRST_ID)
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
        );
        //then
        perform.andExpect(status().is2xxSuccessful());
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            pathParameters(
                parameterWithName("order_id").description("주문 id")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id"),
                fieldWithPath("data.order_item_list").type(JsonFieldType.ARRAY)
                                                     .description("주문 상품 리스트"),
                fieldWithPath("data.order_item_list[].product_id").type(JsonFieldType.NUMBER)
                                                                  .description("상품 id"),
                fieldWithPath("data.order_item_list[].ea").type(JsonFieldType.NUMBER)
                                                          .description("상품 주문 개수"),
                fieldWithPath("data.order_item_list[].main_title").type(JsonFieldType.STRING)
                                                                  .description("주문 상품명"),
                fieldWithPath("data.order_item_list[].type").type(JsonFieldType.STRING)
                                                            .description("상품 상태"),
                fieldWithPath("data.order_item_list[].main_explanation").type(JsonFieldType.STRING)
                                                                        .description("주문 상품 메인 설명"),
                fieldWithPath("data.order_item_list[].product_main_explanation").type(
                    JsonFieldType.STRING).description("주문 상품 상세 설명"),
                fieldWithPath("data.order_item_list[].product_sub_explanation").type(
                    JsonFieldType.STRING).description("주문 상품 상세 서브 설명"),
                fieldWithPath("data.order_item_list[].origin_price").type(JsonFieldType.NUMBER)
                                                                    .description("주문 상품 원가"),
                fieldWithPath("data.order_item_list[].price").type(JsonFieldType.NUMBER)
                                                             .description("주문 상품 판매가"),
                fieldWithPath("data.order_item_list[].product_price").type(JsonFieldType.NUMBER)
                                                                     .description("상품 판매가"),
                fieldWithPath("data.order_item_list[].product_total_price").type(
                    JsonFieldType.NUMBER).description("상품 총 판매가"),
                fieldWithPath("data.order_item_list[].purchase_inquiry").type(JsonFieldType.STRING)
                                                                        .description(
                                                                            "주문 상품 취급 주의 사항"),
                fieldWithPath("data.order_item_list[].origin").type(JsonFieldType.STRING)
                                                              .description("원산지"),
                fieldWithPath("data.order_item_list[].producer").type(JsonFieldType.STRING)
                                                                .description("공급자"),
                fieldWithPath("data.order_item_list[].main_image").type(JsonFieldType.STRING)
                                                                  .description("메인 이미지"),
                fieldWithPath("data.order_item_list[].image1").type(JsonFieldType.STRING)
                                                              .description("이미지1"),
                fieldWithPath("data.order_item_list[].image2").type(JsonFieldType.STRING)
                                                              .description("이미지2"),
                fieldWithPath("data.order_item_list[].image3").type(JsonFieldType.STRING)
                                                              .description("이미지3"),
                fieldWithPath("data.order_item_list[].view_cnt").type(JsonFieldType.NUMBER)
                                                                .description("조회수"),
                fieldWithPath("data.order_item_list[].status").type(JsonFieldType.STRING)
                                                              .description("상품 상태"),
                fieldWithPath("data.order_item_list[].created_at").type(JsonFieldType.STRING)
                                                                  .description("상품 생성일"),
                fieldWithPath("data.order_item_list[].modified_at").type(JsonFieldType.STRING)
                                                                   .description("상품 수정일"),
                fieldWithPath("data.order_item_list[].seller").type(JsonFieldType.OBJECT)
                                                              .description("상품 판매자"),
                fieldWithPath("data.order_item_list[].seller.seller_id").type(JsonFieldType.NUMBER)
                                                                        .description("상품 판매자 id"),
                fieldWithPath("data.order_item_list[].seller.email").type(JsonFieldType.STRING)
                                                                    .description("상품 판매자 email"),
                fieldWithPath("data.order_item_list[].seller.company").type(JsonFieldType.STRING)
                                                                      .description("상품 판매자 회사명"),
                fieldWithPath("data.order_item_list[].seller.zonecode").type(JsonFieldType.STRING)
                                                                       .description("상품 판매자 우편 주소"),
                fieldWithPath("data.order_item_list[].seller.address").type(JsonFieldType.STRING)
                                                                      .description("상품 판매자 주소"),
                fieldWithPath("data.order_item_list[].seller.address_detail").type(
                    JsonFieldType.STRING).description("상품 판매자 상세 주소"),
                fieldWithPath("data.order_item_list[].seller.tel").type(JsonFieldType.STRING)
                                                                  .description("상품 판매자 연락처"),
                fieldWithPath("data.order_item_list[].category").type(JsonFieldType.OBJECT)
                                                                .description("상품 카테고리"),
                fieldWithPath("data.order_item_list[].category.category_id").type(
                    JsonFieldType.NUMBER).description("상품 카테고리 id"),
                fieldWithPath("data.order_item_list[].category.name").type(JsonFieldType.STRING)
                                                                     .description("상품 카테고리명"),
                fieldWithPath("data.order_item_list[].category.status").type(JsonFieldType.STRING)
                                                                       .description("상품 카테고리 상태"),
                fieldWithPath("data.order_item_list[].category.created_at").type(
                    JsonFieldType.STRING).description("상품 카테고리 생성일"),
                fieldWithPath("data.order_item_list[].category.modified_at").type(
                    JsonFieldType.STRING).description("상품 카테고리 수정일"),
                fieldWithPath("data.order_item_list[].option").type(JsonFieldType.OBJECT)
                                                              .description("상품 옵션").optional(),
                fieldWithPath("data.order_item_list[].option.option_id").type(JsonFieldType.NUMBER)
                                                                        .description("상품 옵션 id")
                                                                        .optional(),
                fieldWithPath("data.order_item_list[].option.name").type(JsonFieldType.STRING)
                                                                   .description("상품 옵션명")
                                                                   .optional(),
                fieldWithPath("data.order_item_list[].option.status").type(JsonFieldType.STRING)
                                                                     .description("상품 옵션 상태")
                                                                     .optional(),
                fieldWithPath("data.order_item_list[].option.extra_price").type(
                    JsonFieldType.NUMBER).description("상품 옵션 추가 금액").optional(),
                fieldWithPath("data.order_item_list[].option.ea").type(JsonFieldType.NUMBER)
                                                                 .description("상품 옵션 재고")
                                                                 .optional(),
                fieldWithPath("data.order_item_list[].option.created_at").type(JsonFieldType.STRING)
                                                                         .description("상품 옵션 생성일")
                                                                         .optional(),
                fieldWithPath("data.order_item_list[].option.modified_at").type(
                    JsonFieldType.STRING).description("상품 옵션 수정일").optional()
            )
        ));
    }
    @Test
    @DisplayName(PREFIX + "/complete (PATCH)")
    void complete() throws Exception {
        //given
        MemberEntity memberEntity = memberJpaRepository.findByEmail(MEMBER_EMAIL).get();
        Order order = Order.builder()
                           .member(memberEntity.toModel())
                           .orderName("테스트 상품")
                           .build();
        OrderEntity orderEntity = orderJpaRepository.save(OrderEntity.from(order));
        Long orderId = orderEntity.getId();
        CompleteOrderRequest completeOrderRequest = CompleteOrderRequest.builder()
                                                                        .orderId(orderId)
                                                                        .ownerName("주문자")
                                                                        .ownerZonecode("1234567")
                                                                        .ownerAddress("주문자 주소")
                                                                        .ownerAddressDetail(
                                                                            "주문자 상세 주소")
                                                                        .ownerTel("주문자 연락처")
                                                                        .recipientName("수령인")
                                                                        .recipientZonecode(
                                                                            "1234567")
                                                                        .recipientAddress("수령인 주소")
                                                                        .recipientAddressDetail(
                                                                            "수령인 상세 주소")
                                                                        .recipientTel("수령인 연락처")
                                                                        .build();
        //when
        ResultActions perform = mock.perform(
            patch(PREFIX + "/complete")
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(completeOrderRequest))
        );
        //then
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            requestFields(
                fieldWithPath("order_id").description("주문 id"),
                fieldWithPath("owner_name").description("주문자"),
                fieldWithPath("owner_zonecode").description("주문자 우편번호"),
                fieldWithPath("owner_address").description("주문자 주소"),
                fieldWithPath("owner_address_detail").description("주문자 상세 주소"),
                fieldWithPath("owner_tel").description("주문자 연락처"),
                fieldWithPath("recipient_name").description("수령인"),
                fieldWithPath("recipient_zonecode").description("수령인 우편번호"),
                fieldWithPath("recipient_address").description("수령인 주소"),
                fieldWithPath("recipient_address_detail").description("수령인 상세 주소"),
                fieldWithPath("recipient_tel").description("수령인 연락처")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.order_id").type(JsonFieldType.NUMBER).description("주문 id"),
                fieldWithPath("data.order_number").type(JsonFieldType.STRING).description("주문 번호")
            )
        ));
    }
    @Test
    @DisplayName(PREFIX + "/member/cancel (PATCH)")
    void cancelOrder() throws Exception {
        //given
        CancelOrderRequest cancelOrderRequest = CancelOrderRequest.builder().orderId(ORDER_FIRST_ID)
                                                                  .build();
        //when
        ResultActions perform = mock.perform(
            patch(PREFIX + "/member/cancel")
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelOrderRequest))
        );
        //then
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            requestFields(
                fieldWithPath("order_id").description("주문 id").type(JsonFieldType.NUMBER)
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메세지")
            )
        ));
    }

    @Test
    @DisplayName(PREFIX + "/seller/cancel (PATCH)")
    void cancelOrderBySeller() throws Exception {
        //given
        CancelOrderRequest cancelOrderRequest = CancelOrderRequest.builder().orderId(ORDER_FIRST_ID)
                                                                  .build();
        //when
        ResultActions perform = mock.perform(
            patch(PREFIX + "/seller/cancel")
                .header(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cancelOrderRequest))
        );
        //then
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            requestFields(
                fieldWithPath("order_id").description("주문 id").type(JsonFieldType.NUMBER)
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.message").type(JsonFieldType.STRING).description("결과 메세지")
            )
        ));
    }


    @Test
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    @DisplayName(PREFIX + "/seller (GET)")
    void getOrderBySeller() throws Exception {
        //given
        //when
        ResultActions perform = mock.perform(
            get(PREFIX + "/seller")
                .header(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken())
                .param("search", "")
                .param("page", "0")
                .param("size", "5")
                .param("status", OrderStatus.ORDERED.name())
        );
        //then
        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            queryParameters(
                parameterWithName("search").description("검색어").optional(),
                parameterWithName("page").description("페이지 (기본값 0)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값 10)").optional(),
                parameterWithName("status").description(
                                               "상태값  (TEMP : 임시, / ORDERED : 주문완료 / CANCEL : 취소 / PROCESSING : 주문확인, 상품준비중 / CONFIRM : 배송완료, 발송완료)")
                                           .optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.total_page").description("총 페이지 수").type(JsonFieldType.NUMBER),
                fieldWithPath("data.total_elements").description("총 요소 수")
                                                    .type(JsonFieldType.NUMBER),
                fieldWithPath("data.number_of_elements").description("현재 페이지의 요소 수")
                                                        .type(JsonFieldType.NUMBER),
                fieldWithPath("data.last").description("마지막 페이지 여부").type(JsonFieldType.BOOLEAN),
                fieldWithPath("data.empty").description("비어있는 목록 여부").type(JsonFieldType.BOOLEAN),
                fieldWithPath("data.list[].order_id").description("주문 id")
                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_number").description("주문 번호")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].status").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_name").description("주문 이름")
                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].total_price").description("주문 총금액")
                                                        .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].created_at").description("주문 등록일")
                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].modified_at").description("주문 수정일")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.delivery_id").description("배송지 id")
                                                                 .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].delivery.status").description("배송지 상태")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_name").description("배송지 주문자")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_zonecode").description("배송지 주문자 우편번호")
                                                                    .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_address").description("배송지 주문자 주소")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_address_detail").description(
                    "배송지 주문자 상세 주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_tel").description("배송지 주문자 연락처")
                                                               .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_name").description("배송지 수령자")
                                                                    .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_zonecode").description("배송지 수령자 우편번호")
                                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_address").description("배송지 수령자 주소")
                                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_address_detail").description(
                    "배송지 수령자 상세 주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_tel").description("배송지 수령자 연락처")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.created_at").description("배송지 등록일")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.modified_at").description("배송지 수정일")
                                                                 .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.email").description("회원 이메일")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.nickname").description("회원 닉네임")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.name").description("회원 이름")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.status").description("회원 상태")
                                                          .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_item_id").description(
                    "주문 item id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].status").description("주문 item 상태")
                                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].ea").description("주문 item 개수")
                                                                 .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].product_price").description(
                    "주문 item 상품 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].product_total_price").description(
                    "주문 item 상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].created_at").description(
                    "주문 item 상품 등록일").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].modified_at").description(
                    "주문 item 상품 수정일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.order_product_id").description(
                    "주문 상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.product_id").description(
                    "주문 상품 original ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.product_id").description(
                    "상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.product_main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.product_sub_explanation").description(
                    "상품 보조 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.main_title").description(
                    "상품 제목").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.origin_price").description("원래 가격")
                                                                               .type(
                                                                                   JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.price").description("가격")
                                                                                  .type(
                                                                                      JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.purchase_inquiry").description(
                    "취급방법").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.origin").description(
                    "원산지").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.producer").description(
                    "공급자").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.main_image").description(
                    "상품 메인 이미지 URL").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image1").description(
                    "이미지1").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image2").description(
                    "이미지2").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image3").description(
                    "이미지3").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.type").description(
                    "주문 상품 type").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.view_cnt").description(
                    "조회수").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.status").description(
                    "상품 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.created_at").description(
                    "상품 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.modified_at").description("상품 수정일")
                                                                              .type(
                                                                                  JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.seller_id").description(
                    "판매자 id").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.email").description(
                    "판매자 이메일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.company").description(
                    "판매자 회사명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.zonecode").description(
                    "판매자 우편번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.address").description(
                    "판매자 주소").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.address_detail").description(
                    "판매자 상세주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.seller.tel").description(
                    "판매자 전화번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.category_id").description(
                    "카테고리 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.name").description(
                    "카테고리 이름").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.created_at").description(
                    "카테고리 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.modified_at").description(
                    "카테고리 수정일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.status").description(
                    "카테고리 상태").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.option_id").description(
                    "옵션 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.name").description("옵션 이름")
                                                                              .type(
                                                                                  JsonFieldType.STRING)
                                                                              .optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.extra_price").description(
                    "옵션 추가 가격").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.list[].order_item_list[].order_product.option.ea").description(
                    "옵션 개수").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.created_at").description(
                    "옵션 생성일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.modified_at").description(
                    "옵션 수정일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.status").description(
                    "옵션 상태").type(JsonFieldType.STRING).optional()
            )
        ));
    }

    @Test
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    @DisplayName(PREFIX + "/member (GET)")
    void getOrderByMember() throws Exception {
        ResultActions perform = mock.perform(
            get(PREFIX + "/member")
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .param("search", "")
                .param("page", "0")
                .param("size", "5")
                .param("status", OrderStatus.ORDERED.name())
        );

        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            queryParameters(
                parameterWithName("search").description("검색어").optional(),
                parameterWithName("page").description("페이지 (기본값 0)").optional(),
                parameterWithName("size").description("페이지 크기 (기본값 10)").optional(),
                parameterWithName("status").description(
                                               "상태값 (TEMP : 임시 / ORDERED : 주문완료 / CANCEL : 취소 / PROCESSING : 주문확인, 상품준비중 / CONFIRM : 배송완료, 발송완료)")
                                           .optional()
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.total_page").description("총 페이지 수").type(JsonFieldType.NUMBER),
                fieldWithPath("data.total_elements").description("총 요소 수")
                                                    .type(JsonFieldType.NUMBER),
                fieldWithPath("data.number_of_elements").description("현재 페이지의 요소 수")
                                                        .type(JsonFieldType.NUMBER),
                fieldWithPath("data.last").description("마지막 페이지 여부").type(JsonFieldType.BOOLEAN),
                fieldWithPath("data.empty").description("비어있는 목록 여부").type(JsonFieldType.BOOLEAN),
                fieldWithPath("data.list[].order_id").description("주문 id")
                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_number").description("주문 번호")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].status").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_name").description("주문 이름")
                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].total_price").description("주문 총금액")
                                                        .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].created_at").description("주문 등록일")
                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].modified_at").description("주문 수정일")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.delivery_id").description("배송지 id")
                                                                 .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].delivery.status").description("배송지 상태")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_name").description("배송지 주문자")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_zonecode").description("배송지 주문자 우편번호")
                                                                    .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_address").description("배송지 주문자 주소")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_address_detail").description(
                    "배송지 주문자 상세 주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.owner_tel").description("배송지 주문자 연락처")
                                                               .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_name").description("배송지 수령자")
                                                                    .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_zonecode").description("배송지 수령자 우편번호")
                                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_address").description("배송지 수령자 주소")
                                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_address_detail").description(
                    "배송지 수령자 상세 주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.recipient_tel").description("배송지 수령자 연락처")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.created_at").description("배송지 등록일")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].delivery.modified_at").description("배송지 수정일")
                                                                 .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.email").description("회원 이메일")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.nickname").description("회원 닉네임")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.name").description("회원 이름")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].member.status").description("회원 상태")
                                                          .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_item_id").description(
                    "주문 item id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].status").description("주문 item 상태")
                                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].ea").description("주문 item 개수")
                                                                 .type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].product_price").description(
                    "주문 item 상품 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].product_total_price").description(
                    "주문 item 상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].created_at").description(
                    "주문 item 상품 등록일").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].modified_at").description(
                    "주문 item 상품 수정일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.order_product_id").description(
                    "주문 상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.product_id").description(
                    "주문 상품 original ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.product_id").description(
                    "상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.product_main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.product_sub_explanation").description(
                    "상품 보조 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.main_title").description(
                    "상품 제목").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.origin_price").description("원래 가격")
                                                                               .type(
                                                                                   JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.price").description("가격")
                                                                                  .type(
                                                                                      JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.purchase_inquiry").description(
                    "취급방법").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.origin").description(
                    "원산지").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.producer").description(
                    "공급자").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.main_image").description(
                    "상품 메인 이미지 URL").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image1").description(
                    "이미지1").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image2").description(
                    "이미지2").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.image3").description(
                    "이미지3").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.type").description(
                    "주문 상품 type").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.view_cnt").description(
                    "조회수").type(JsonFieldType.NUMBER),
                fieldWithPath("data.list[].order_item_list[].order_product.status").description(
                    "상품 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.created_at").description(
                    "상품 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.modified_at").description("상품 수정일")
                                                                              .type(
                                                                                  JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.seller_id").description(
                    "판매자 id").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.email").description(
                    "판매자 이메일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.company").description(
                    "판매자 회사명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.zonecode").description(
                    "판매자 우편번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.address").description(
                    "판매자 주소").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.seller.address_detail").description(
                    "판매자 상세주소").type(JsonFieldType.STRING),
                fieldWithPath("data.list[].order_item_list[].order_product.seller.tel").description(
                    "판매자 전화번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.category_id").description(
                    "카테고리 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.name").description(
                    "카테고리 이름").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.created_at").description(
                    "카테고리 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.modified_at").description(
                    "카테고리 수정일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.category.status").description(
                    "카테고리 상태").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.option_id").description(
                    "옵션 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.name").description("옵션 이름")
                                                                              .type(
                                                                                  JsonFieldType.STRING)
                                                                              .optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.extra_price").description(
                    "옵션 추가 가격").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.list[].order_item_list[].order_product.option.ea").description(
                    "옵션 개수").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.created_at").description(
                    "옵션 생성일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.modified_at").description(
                    "옵션 수정일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.list[].order_item_list[].order_product.option.status").description(
                    "옵션 상태").type(JsonFieldType.STRING).optional()
            )
        ));
    }

    @Test
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    @DisplayName(PREFIX + "/seller/{order_id} (GET)")
    void getOrderDetailBySeller() throws Exception {
        ResultActions perform = mock.perform(
            get(PREFIX + "/seller/{order_id}", ORDER_FIRST_ID)
                .header(AUTHORIZATION, "Bearer " + SELLER_LOGIN.getAccessToken())
        );

        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            pathParameters(
                parameterWithName("order_id").description("주문 id")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.order_id").description("주문 id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_number").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("data.status").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_name").description("주문 이름").type(JsonFieldType.STRING),
                fieldWithPath("data.created_at").description("주문 등록일").type(JsonFieldType.STRING),
                fieldWithPath("data.modified_at").description("주문 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.delivery_id").description("배송지 id")
                                                          .type(JsonFieldType.NUMBER),
                fieldWithPath("data.delivery.status").description("배송지 상태")
                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_name").description("배송지 주문자")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_zonecode").description("배송지 주문자 우편번호")
                                                             .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_address").description("배송지 주문자 주소")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_address_detail").description("배송지 주문자 상세 주소")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_tel").description("배송지 주문자 연락처")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_name").description("배송지 수령자")
                                                             .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_zonecode").description("배송지 수령자 우편번호")
                                                                 .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_address").description("배송지 수령자 주소")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_address_detail").description("배송지 수령자 상세 주소")
                                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_tel").description("배송지 수령자 연락처")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.created_at").description("배송지 등록일")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.modified_at").description("배송지 수정일")
                                                          .type(JsonFieldType.STRING),
                fieldWithPath("data.member.email").description("회원 이메일").type(JsonFieldType.STRING),
                fieldWithPath("data.member.nickname").description("회원 닉네임")
                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.member.name").description("회원 이름").type(JsonFieldType.STRING),
                fieldWithPath("data.member.status").description("회원 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_item_id").description("주문 item id")
                                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].status").description("주문 item 상태")
                                                              .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].ea").description("주문 item 개수")
                                                          .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].product_price").description("주문 item 상품 가격")
                                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].product_total_price").description(
                    "주문 item 상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].created_at").description("주문 item 상품 등록일")
                                                                  .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].modified_at").description("주문 item 상품 수정일")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.order_product_id").description(
                    "주문 상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.product_id").description(
                    "주문 상품 original ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.product_id").description(
                    "상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.order_item_list[].order_product.product_main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.product_sub_explanation").description(
                    "상품 보조 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_title").description(
                    "상품 제목").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.origin_price").description(
                    "원래 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.price").description("가격").type(
                    JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.purchase_inquiry").description(
                    "취급방법").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.origin").description("원산지")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.producer").description("공급자")
                                                                              .type(
                                                                                  JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_image").description(
                    "상품 메인 이미지 URL").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image1").description("이미지1")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image2").description("이미지2")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image3").description("이미지3")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.type").description("주문 상품 type")
                                                                          .type(
                                                                              JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.view_cnt").description("조회수")
                                                                              .type(
                                                                                  JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.status").description("상품 상태")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.created_at").description(
                    "상품 생성일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.modified_at").description(
                    "상품 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.seller_id").description(
                    "판매자 id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.seller.email").description(
                    "판매자 이메일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.company").description(
                    "판매자 회사명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.zonecode").description(
                    "판매자 우편번호").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.address").description(
                    "판매자 주소").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.seller.address_detail").description(
                    "판매자 상세주소").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.tel").description(
                    "판매자 전화번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.category_id").description(
                    "카테고리 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.category.name").description(
                    "카테고리 이름").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.created_at").description(
                    "카테고리 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.modified_at").description(
                    "카테고리 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.category.status").description(
                    "카테고리 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.option.option_id").description(
                    "옵션 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.order_item_list[].order_product.option.name").description(
                    "옵션 이름").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.order_item_list[].order_product.option.extra_price").description(
                    "옵션 추가 가격").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.order_item_list[].order_product.option.ea").description("옵션 개수")
                                                                               .type(
                                                                                   JsonFieldType.NUMBER)
                                                                               .optional(),
                fieldWithPath("data.order_item_list[].order_product.option.created_at").description(
                    "옵션 생성일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.order_item_list[].order_product.option.modified_at").description("옵션 수정일")
                                                                              .type(
                                                                                  JsonFieldType.STRING)
                                                                              .optional(),
                fieldWithPath("data.order_item_list[].order_product.option.status").description(
                    "옵션 상태").type(JsonFieldType.STRING).optional()
            )
        ));
    }

    @Test
    @SqlGroup({
        @Sql(scripts = {"/sql/init.sql", "/sql/product.sql", "/sql/order.sql",
            "/sql/delivery.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD),
    })
    @DisplayName(PREFIX + "/member/{order_id} (GET)")
    void getOrderDetailByMember() throws Exception {

        ResultActions perform = mock.perform(
            get(PREFIX + "/member/{order_id}", ORDER_FIRST_ID)
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
        );

        perform.andDo(docs.document(
            requestHeaders(
                headerWithName(AUTHORIZATION).description("access token (MEMBER 권한 이상)")
            ),
            pathParameters(
                parameterWithName("order_id").description("주문 id")
            ),
            responseFields(
                fieldWithPath("code").type(JsonFieldType.STRING).description("결과 코드"),
                fieldWithPath("message").type(JsonFieldType.STRING).description("결과 메세지"),
                fieldWithPath("data.order_id").description("주문 id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_number").description("주문 번호").type(JsonFieldType.STRING),
                fieldWithPath("data.status").description("주문 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_name").description("주문 이름").type(JsonFieldType.STRING),
                fieldWithPath("data.created_at").description("주문 등록일").type(JsonFieldType.STRING),
                fieldWithPath("data.modified_at").description("주문 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.delivery_id").description("배송지 id")
                                                          .type(JsonFieldType.NUMBER),
                fieldWithPath("data.delivery.status").description("배송지 상태")
                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_name").description("배송지 주문자")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_zonecode").description("배송지 주문자 우편번호")
                                                             .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_address").description("배송지 주문자 주소")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_address_detail").description("배송지 주문자 상세 주소")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.owner_tel").description("배송지 주문자 연락처")
                                                        .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_name").description("배송지 수령자")
                                                             .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_zonecode").description("배송지 수령자 우편번호")
                                                                 .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_address").description("배송지 수령자 주소")
                                                                .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_address_detail").description("배송지 수령자 상세 주소")
                                                                       .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.recipient_tel").description("배송지 수령자 연락처")
                                                            .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.created_at").description("배송지 등록일")
                                                         .type(JsonFieldType.STRING),
                fieldWithPath("data.delivery.modified_at").description("배송지 수정일")
                                                          .type(JsonFieldType.STRING),
                fieldWithPath("data.member.email").description("회원 이메일").type(JsonFieldType.STRING),
                fieldWithPath("data.member.nickname").description("회원 닉네임")
                                                     .type(JsonFieldType.STRING),
                fieldWithPath("data.member.name").description("회원 이름").type(JsonFieldType.STRING),
                fieldWithPath("data.member.status").description("회원 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_item_id").description("주문 item id")
                                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].status").description("주문 item 상태")
                                                              .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].ea").description("주문 item 개수")
                                                          .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].product_price").description("주문 item 상품 가격")
                                                                     .type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].product_total_price").description(
                    "주문 item 상품 총 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].created_at").description("주문 item 상품 등록일")
                                                                  .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].modified_at").description("주문 item 상품 수정일")
                                                                   .type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.order_product_id").description(
                    "주문 상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.product_id").description(
                    "주문 상품 original ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.product_id").description(
                    "상품 ID").type(JsonFieldType.NUMBER),
                fieldWithPath(
                    "data.order_item_list[].order_product.product_main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.product_sub_explanation").description(
                    "상품 보조 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_title").description(
                    "상품 제목").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_explanation").description(
                    "상품 메인 설명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.origin_price").description(
                    "원래 가격").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.price").description("가격").type(
                    JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.purchase_inquiry").description(
                    "취급방법").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.origin").description("원산지")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.producer").description("공급자")
                                                                              .type(
                                                                                  JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.main_image").description(
                    "상품 메인 이미지 URL").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image1").description("이미지1")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image2").description("이미지2")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.image3").description("이미지3")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.type").description("주문 상품 type")
                                                                          .type(
                                                                              JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.view_cnt").description("조회수")
                                                                              .type(
                                                                                  JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.status").description("상품 상태")
                                                                            .type(
                                                                                JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.created_at").description(
                    "상품 생성일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.modified_at").description(
                    "상품 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.seller_id").description(
                    "판매자 id").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.seller.email").description(
                    "판매자 이메일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.company").description(
                    "판매자 회사명").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.zonecode").description(
                    "판매자 우편번호").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.address").description(
                    "판매자 주소").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.seller.address_detail").description(
                    "판매자 상세주소").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.seller.tel").description(
                    "판매자 전화번호").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.category_id").description(
                    "카테고리 ID").type(JsonFieldType.NUMBER),
                fieldWithPath("data.order_item_list[].order_product.category.name").description(
                    "카테고리 이름").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.created_at").description(
                    "카테고리 생성일").type(JsonFieldType.STRING),
                fieldWithPath(
                    "data.order_item_list[].order_product.category.modified_at").description(
                    "카테고리 수정일").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.category.status").description(
                    "카테고리 상태").type(JsonFieldType.STRING),
                fieldWithPath("data.order_item_list[].order_product.option.option_id").description(
                    "옵션 ID").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.order_item_list[].order_product.option.name").description(
                    "옵션 이름").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.order_item_list[].order_product.option.extra_price").description(
                    "옵션 추가 가격").type(JsonFieldType.NUMBER).optional(),
                fieldWithPath("data.order_item_list[].order_product.option.ea").description("옵션 개수")
                                                                               .type(
                                                                                   JsonFieldType.NUMBER)
                                                                               .optional(),
                fieldWithPath("data.order_item_list[].order_product.option.created_at").description(
                    "옵션 생성일").type(JsonFieldType.STRING).optional(),
                fieldWithPath(
                    "data.order_item_list[].order_product.option.modified_at").description("옵션 수정일")
                                                                              .type(
                                                                                  JsonFieldType.STRING)
                                                                              .optional(),
                fieldWithPath("data.order_item_list[].order_product.option.status").description(
                    "옵션 상태").type(JsonFieldType.STRING).optional()
            )
        ));
    }


}
