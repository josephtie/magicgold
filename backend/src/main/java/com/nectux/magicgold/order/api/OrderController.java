package com.nectux.magicgold.order.api;

import com.nectux.magicgold.order.domain.Order;
import com.nectux.magicgold.order.service.OrderItemCommand;
import com.nectux.magicgold.order.service.OrderService;
import com.nectux.magicgold.security.TenantContext;
import com.nectux.magicgold.shared.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    public record OrderRequest(
            UUID customerId,
            UUID outletId,
            BigDecimal discountAmount,
            @NotNull @Valid List<OrderItemCommand> items) {
    }

    public record OrderResponse(
            UUID id,
            String orderNumber,
            OrderStatus status,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal taxAmount,
            BigDecimal totalAmount,
            String currency) {
        static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getOrderNumber(), o.getStatus(), o.getSubtotal(),
                    o.getDiscountAmount(), o.getTaxAmount(), o.getTotalAmount(), o.getCurrency());
        }
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest request) {
        Order order = orderService.createOrder(currentMerchant(), request.customerId(), request.outletId(),
                request.discountAmount(), request.items());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(OrderResponse.from(orderService.get(currentMerchant(), id)));
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.list(currentMerchant()).stream().map(OrderResponse::from).toList();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<OrderResponse> transition(@PathVariable UUID id, @RequestBody StatusRequest request) {
        Order order = orderService.transition(currentMerchant(), id, request.target());
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<OrderResponse> markPaid(@PathVariable UUID id) {
        Order order = orderService.markPaid(currentMerchant(), id);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    public record StatusRequest(@NotNull OrderStatus target) {
    }

    private UUID currentMerchant() {
        UUID merchantId = TenantContext.get();
        if (merchantId == null) {
            throw new IllegalStateException("Contexte merchant absent");
        }
        return merchantId;
    }
}
