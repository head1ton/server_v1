package ai.serverapi.order.service;

import ai.serverapi.global.util.MemberUtil;
import ai.serverapi.member.domain.model.Member;
import ai.serverapi.order.controller.request.CancelOrderRequest;
import ai.serverapi.order.controller.request.CompleteOrderRequest;
import ai.serverapi.order.controller.request.ConfirmOrderRequest;
import ai.serverapi.order.controller.request.ProcessingOrderRequest;
import ai.serverapi.order.controller.request.TempOrderDto;
import ai.serverapi.order.controller.request.TempOrderRequest;
import ai.serverapi.order.controller.response.CompleteOrderResponse;
import ai.serverapi.order.controller.response.OrderListResponse;
import ai.serverapi.order.controller.response.OrderResponse;
import ai.serverapi.order.controller.response.PostTempOrderResponse;
import ai.serverapi.order.controller.response.TempOrderResponse;
import ai.serverapi.order.domain.model.Delivery;
import ai.serverapi.order.domain.model.Order;
import ai.serverapi.order.domain.model.OrderItem;
import ai.serverapi.order.domain.model.OrderOption;
import ai.serverapi.order.domain.model.OrderProduct;
import ai.serverapi.order.enums.OrderStatus;
import ai.serverapi.order.repository.DeliveryRepository;
import ai.serverapi.order.repository.OrderItemRepository;
import ai.serverapi.order.repository.OrderOptionRepository;
import ai.serverapi.order.repository.OrderProductRepository;
import ai.serverapi.order.repository.OrderRepository;
import ai.serverapi.product.domain.model.Option;
import ai.serverapi.product.domain.model.Product;
import ai.serverapi.product.domain.model.Seller;
import ai.serverapi.product.enums.ProductType;
import ai.serverapi.product.repository.OptionRepository;
import ai.serverapi.product.repository.ProductRepository;
import ai.serverapi.product.repository.SellerRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final MemberUtil memberUtil;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final OptionRepository optionRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderOptionRepository orderOptionRepository;
    private final OrderItemRepository orderItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final SellerRepository sellerRepository;

    @Transactional
    @Override
    public PostTempOrderResponse postTempOrder(
        TempOrderRequest tempOrderRequest,
        HttpServletRequest request
    ) {
        /**
         * 1. 주문 발급
         * 2. 주문 상품 등록
         * 3. 판매자 리스트 등록
         * 4. 상품 타입 체크
         * 4. 재고 확인
         */

        // 주문 발급
        Member member = memberUtil.getMember(request).toModel();
        List<Long> productIdList = tempOrderRequest.getOrderList().stream()
                                                   .mapToLong(TempOrderDto::getProductId).boxed()
                                                   .toList();
        List<Product> productList = productRepository.findAllById(productIdList);

        // 주문 상품 등록
        Order createOrder = Order.create(member, productList);
        Order order = orderRepository.save(createOrder);

        // orderProduct orderOption 등록
        Map<Long, Product> productMap = new HashMap<>();
        for (Product product : productList) {
            productMap.put(product.getId(), product);
        }

        List<OrderItem> orderItemList = new LinkedList<>();

        for (TempOrderDto tempOrderDto : tempOrderRequest.getOrderList()) {
            Product product = productMap.get(tempOrderDto.getProductId());

            // 재고 확인
            product.checkInStock(tempOrderDto);

            // OrderProduct 생성
            OrderProduct createOrderProduct = OrderProduct.create(product);

            // OrderOption 생성
            OrderOption orderOption = null;
            if (product.getType() == ProductType.OPTION) {
                Long optionId = tempOrderDto.getOptionId();
                OrderOption createOrderOption = OrderOption.create(product.getOptionList(),
                    optionId);
                orderOption = orderOptionRepository.save(createOrderOption);
                createOrderProduct.addOption(orderOption);
            }
            OrderProduct orderProduct = orderProductRepository.save(createOrderProduct);

            // OrderItem 생성
            OrderItem orderItem = OrderItem.create(order, orderProduct, tempOrderDto.getEa());
            orderItemList.add(orderItem);
        }

        orderItemRepository.saveAll(orderItemList);
        orderRepository.save(order);

        return PostTempOrderResponse.builder().orderId(order.getId()).build();
    }

    @Override
    public TempOrderResponse getOrderInfo(Long orderId, HttpServletRequest request) {
        /**
         * order id 와 member 정보로 임시 정보를 불러옴
         */
        Member member = memberUtil.getMember(request).toModel();

        Order order = orderRepository.findById(orderId);
        order.checkOrder(member);

        return TempOrderResponse.create(order);
    }

    /**
     * 주문 정보를 update 해야됨! 1. 주문 상태 변경 2. 주문자, 수령자 정보 등록 3. 주문 번호 만들기
     *
     * @param completeOrderRequest 주문 완료 요청 객체
     * @param request              HTTP 요청 객체
     * @return 완료된 주문 응답 객체
     */
    @Transactional
    @Override
    public CompleteOrderResponse completeOrder(
        CompleteOrderRequest completeOrderRequest,
        HttpServletRequest request) {
        /**
         * 주문 정보를 update 해야됨!
         *
         * 재고 확인
         * 상품 재고 마이너스 처리
         * 배송 정보 등록
         * 주문과 주문 상품 상태 수정
         * 주문 번호 생성
         */
        // 주문 확인
        Long orderId = completeOrderRequest.getOrderId();
        Order order = orderRepository.findById(orderId);
        order.checkOrder(memberUtil.getMember(request).toModel());

        // 재고 확인
        order.getOrderItemList().forEach(orderItem -> {
            OrderProduct orderProduct = orderItem.getOrderProduct();
            Long productId = orderProduct.getProductId();
            Product product = productRepository.findById(productId);
            ProductType type = product.getType();

            int ea = orderItem.getEa();
            Long optionId = orderItem.getOrderProduct().getOrderOption() == null ? null
                : orderItem.getOrderProduct().getOrderOption().getOptionId();

            // 상품 마이너스 처리
            if (type == ProductType.NORMAL) {
                product.checkInStock(ea, optionId);
                product.minusEa(ea);
                productRepository.save(product);
            } else if (type == ProductType.OPTION) {
                Option option = optionRepository.findById(optionId);
                option.checkInStock(ea);
                option.minusEa(ea);
                optionRepository.save(option);
            }
        });

        // 배송 정보 등록
        Delivery delivery = deliveryRepository.save(Delivery.create(completeOrderRequest));

        order.ordered();
        order.createOrderNumber();
        order.delivery(delivery);
        orderRepository.save(order);

        return CompleteOrderResponse.from(order);
    }

    @Override
    public OrderListResponse getOrderListBySeller(Pageable pageable, String search, String status,
        HttpServletRequest request) {
        /**
         * member로 seller 정보 가져오기
         * 주문 정보를 seller id로 가져오기
         */
        Member member = memberUtil.getMember(request).toModel();
        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
        Seller seller = sellerRepository.findByMember(member);

        Page<Order> orderPage = orderRepository.findAllBySeller(pageable, search, orderStatus,
            seller);

        return OrderListResponse.from(orderPage);
    }

//    @Override
//    public OrderResponse getOrderListByMember(final Pageable pageable, final String search,
//        final String status,
//        final HttpServletRequest request) {
//        Member member = memberUtil.getMember(request).toModel();
//        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
//        Page<OrderVo> orderPage = orderRepository.findAll(pageable, search, orderStatus, null,
//            member);
//
//        return OrderResponse.from(orderPage);
//    }

@Override
public OrderResponse getOrderDetailByMember(Long orderId, HttpServletRequest request) {
    Member member = memberUtil.getMember(request).toModel();
    Order order = orderRepository.findById(orderId);
    order.checkOrder(member);
    return OrderResponse.from(order);
}

    @Override
    @Transactional
    public void cancelOrderBySeller(final CancelOrderRequest cancelOrderRequest,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        Seller seller = sellerRepository.findByMember(member);
        Order order = orderRepository.findByIdAndSeller(cancelOrderRequest.getOrderId(),
            seller);

        order.cancel();
        orderRepository.save(order);

        List<OrderItem> orderItemList = order.getOrderItemList();
        orderItemList.forEach(orderItem -> orderItem.cancel());
        orderItemRepository.saveAll(orderItemList);
    }


    //
//    @Override
//    public OrderVo getOrderDetailBySeller(final Long orderId,
//        final HttpServletRequest request) {
//        Member member = memberUtil.getMember(request).toModel();
//        Seller seller = sellerRepository.findByMember(member);
//        OrderVo order = orderRepository.findByIdAndSeller(orderId, seller);
//
//        return order;
//    }
    @Override
    @Transactional
    public void cancelOrder(final CancelOrderRequest cancelOrderRequest,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        Order order = orderRepository.findById(cancelOrderRequest.getOrderId());
        order.checkOrder(member);
        order.cancel();
        orderRepository.save(order);

        List<OrderItem> orderItemList = order.getOrderItemList();
        orderItemList.forEach(OrderItem::cancel);
        orderItemRepository.saveAll(orderItemList);
    }

    @Override
    @Transactional
    public void processingOrder(final ProcessingOrderRequest processingOrderRequest,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        Seller seller = sellerRepository.findByMember(member);
        Order order = orderRepository.findByIdAndSeller(processingOrderRequest.getOrderId(),
            seller);
        order.processing();
        orderRepository.save(order);

        List<OrderItem> orderItemList = order.getOrderItemList();
        orderItemList.forEach(orderItem -> orderItem.processing());
        orderItemRepository.saveAll(orderItemList);
    }

    @Override
    @Transactional
    public void confirmOrder(final ConfirmOrderRequest confirmOrderRequest,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        Seller seller = sellerRepository.findByMember(member);
        Order order = orderRepository.findByIdAndSeller(confirmOrderRequest.getOrderId(), seller);
        order.confirm();
        orderRepository.save(order);

        List<OrderItem> orderItemList = order.getOrderItemList();
        orderItemList.forEach(orderItem -> orderItem.confirm());
        orderItemRepository.saveAll(orderItemList);
    }

    @Override
    public OrderListResponse getOrderListByMember(final Pageable pageable, final String search,
        final String status,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        OrderStatus orderStatus =
            status == null ? null : OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
        Page<Order> orderPage = orderRepository.findAllByMember(pageable, search, orderStatus,
            member);
        return OrderListResponse.from(orderPage);
    }

    @Override
    public OrderResponse getOrderDetailBySeller(final Long orderId,
        final HttpServletRequest request) {
        Member member = memberUtil.getMember(request).toModel();
        Seller seller = sellerRepository.findByMember(member);
        Order order = orderRepository.findByIdAndSeller(orderId, seller);
        return OrderResponse.from(order);
    }


}
