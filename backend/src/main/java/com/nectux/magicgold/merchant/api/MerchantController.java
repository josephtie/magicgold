package com.nectux.magicgold.merchant.api;

import com.nectux.magicgold.merchant.domain.Merchant;
import com.nectux.magicgold.merchant.service.MerchantService;
import com.nectux.magicgold.shared.enums.MerchantStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
public class MerchantController {

    private final MerchantService merchantService;

    public record MerchantRequest(
            @NotBlank String commercialName,
            String legalName,
            String businessType,
            String registrationNumber,
            String taxNumber,
            String phoneNumber,
            String email,
            String address,
            String city,
            String country) {
    }

    public record MerchantResponse(
            UUID id,
            String merchantNumber,
            String legalName,
            String commercialName,
            String city,
            MerchantStatus status) {
        static MerchantResponse from(Merchant m) {
            return new MerchantResponse(m.getId(), m.getMerchantNumber(), m.getLegalName(),
                    m.getCommercialName(), m.getCity(), m.getStatus());
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public ResponseEntity<MerchantResponse> create(@RequestBody MerchantRequest request) {
        Merchant merchant = merchantService.create(
                request.commercialName(), request.legalName(), request.businessType(),
                request.registrationNumber(), request.taxNumber(), request.phoneNumber(),
                request.email(), request.address(), request.city(), request.country());
        return ResponseEntity.status(HttpStatus.CREATED).body(MerchantResponse.from(merchant));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(MerchantResponse.from(merchantService.get(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN')")
    public List<MerchantResponse> list() {
        return merchantService.list().stream().map(MerchantResponse::from).toList();
    }
}
