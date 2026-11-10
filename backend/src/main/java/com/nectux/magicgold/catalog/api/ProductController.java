package com.nectux.magicgold.catalog.api;

import com.nectux.magicgold.catalog.domain.Product;
import com.nectux.magicgold.catalog.service.ProductService;
import com.nectux.magicgold.security.TenantContext;
import com.nectux.magicgold.shared.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    public record ProductRequest(
            @NotBlank String name,
            String description,
            String sku,
            @NotNull @DecimalMin("0.0") BigDecimal unitPrice,
            UUID categoryId) {
    }

    public record ProductResponse(
            UUID id,
            String sku,
            String name,
            BigDecimal unitPrice,
            String currency,
            BigDecimal stockQuantity,
            ProductStatus status) {
        static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getUnitPrice(),
                    p.getCurrency(), p.getStockQuantity(), p.getStatus());
        }
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductRequest request) {
        Product product = productService.create(currentMerchant(), request.name(), request.description(),
                request.sku(), request.unitPrice(), request.categoryId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResponse.from(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ProductResponse.from(productService.get(currentMerchant(), id)));
    }

    @GetMapping
    public List<ProductResponse> list() {
        return productService.list(currentMerchant()).stream().map(ProductResponse::from).toList();
    }

    private UUID currentMerchant() {
        UUID merchantId = TenantContext.get();
        if (merchantId == null) {
            throw new IllegalStateException("Contexte merchant absent");
        }
        return merchantId;
    }
}
