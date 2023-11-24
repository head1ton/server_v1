package ai.serverapi.order.controller;

import static ai.serverapi.Base.MEMBER_EMAIL;
import static ai.serverapi.Base.MEMBER_LOGIN;
import static ai.serverapi.Base.PRODUCT_ID_MASK;
import static ai.serverapi.Base.PRODUCT_ID_PEAR;
import static ai.serverapi.Base.PRODUCT_OPTION_ID_MASK;
import static ai.serverapi.Base.PRODUCT_OPTION_ID_PEAR;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ai.serverapi.RestdocsBaseTest;
import ai.serverapi.member.domain.entity.MemberEntity;
import ai.serverapi.member.repository.MemberJpaRepository;
import ai.serverapi.member.service.MemberAuthServiceImpl;
import ai.serverapi.order.controller.request.CancelOrderRequest;
import ai.serverapi.order.controller.request.CompleteOrderRequest;
import ai.serverapi.order.controller.request.TempOrderDto;
import ai.serverapi.order.controller.request.TempOrderRequest;
import ai.serverapi.order.domain.entity.OrderEntity;
import ai.serverapi.order.repository.DeliveryJpaRepository;
import ai.serverapi.order.repository.OrderItemJpaRepository;
import ai.serverapi.order.repository.OrderJpaRepository;
import ai.serverapi.order.repository.OrderOptionJpaRepository;
import ai.serverapi.order.repository.OrderProductJpaRepository;
import ai.serverapi.product.repository.CategoryJpaRepository;
import ai.serverapi.product.repository.OptionJpaRepository;
import ai.serverapi.product.repository.ProductJpaRepository;
import ai.serverapi.product.repository.SellerJpaRepository;
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
        deliveryJpaRepository.deleteAll();
        orderItemJpaRepository.deleteAll();
        orderProductJpaRepository.deleteAll();
        orderOptionJpaRepository.deleteAll();
        orderJpaRepository.deleteAll();
        optionJpaRepository.deleteAll();
        productJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        sellerJpaRepository.deleteAll();
        memberJpaRepository.deleteAll();
    }

    @Test
    @DisplayName(PREFIX + " (POST)")
    void postTempOrder() throws Exception {

        List<TempOrderDto> orderList = new ArrayList<>();
        int orderEa1 = 3;
        int orderEa2 = 2;

        TempOrderDto order1 = TempOrderDto.builder()
                                          .productId(PRODUCT_ID_MASK)
                                          .optionId(PRODUCT_OPTION_ID_MASK)
                                          .ea(orderEa1)
                                          .build();
        TempOrderDto order2 = TempOrderDto.builder()
                                          .productId(PRODUCT_ID_PEAR)
                                          .optionId(PRODUCT_OPTION_ID_PEAR)
                                          .ea(orderEa2)
                                          .build();
        orderList.add(order1);
        orderList.add(order2);

        TempOrderRequest tempOrderRequest = new TempOrderRequest(orderList);

        ResultActions resultActions = mock.perform(
            post(PREFIX)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .content(objectMapper.writeValueAsString(tempOrderRequest))
        );

        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
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

        ResultActions resultActions = mock.perform(
            get(PREFIX + "/temp/{order_id}", ORDER_FIRST_ID)
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
        );

        resultActions.andExpect(status().is2xxSuccessful());
        resultActions.andDo(docs.document(
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

        MemberEntity memberEntity = memberJpaRepository.findByEmail(MEMBER_EMAIL).get();

        OrderEntity orderEntity = orderJpaRepository.save(OrderEntity.of(memberEntity, "테스트 상품"));
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

        ResultActions resultActions = mock.perform(
            patch(PREFIX + "/complete")
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(completeOrderRequest))
        );

        resultActions.andExpect(status().is2xxSuccessful());

        resultActions.andDo(docs.document(
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
        CancelOrderRequest cancelOrderRequest = CancelOrderRequest.builder().orderId(ORDER_FIRST_ID)
                                                                  .build();

        ResultActions perform = mock.perform(
            patch(PREFIX + "/member/cancel")
                .header(AUTHORIZATION, "Bearer " + MEMBER_LOGIN.getAccessToken())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(cancelOrderRequest))
        );

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
}
