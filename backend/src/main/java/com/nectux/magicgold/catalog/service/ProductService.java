package com.nectux.magicgold.catalog.service;

import com.nectux.magicgold.catalog.domain.Product;
import com.nectux.magicgold.catalog.repository.ProductRepository;
import com.nectux.magicgold.shared.enums.ProductStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public Product create(UUID merchantId, String sku, String name, String description,
                          BigDecimal unitPrice, UUID categoryId) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name est obligatoire");
        }
        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice doit être >= 0");
        }
        Product product = Product.builder()
                .sku(sku != null ? sku : "")
                .name(name)
                .description(description)
                .unitPrice(unitPrice)
                .currency("XOF")
                .stockQuantity(BigDecimal.ZERO)
                .status(ProductStatus.ACTIVE)
                .categoryId(categoryId)
                .build();
        product.setMerchantId(merchantId);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Product get(UUID merchantId, UUID id) {
        return productRepository.findById(id)
                .filter(p -> p.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("Product introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Product> list(UUID merchantId) {
        return productRepository.findByMerchantId(merchantId);
    }
}
