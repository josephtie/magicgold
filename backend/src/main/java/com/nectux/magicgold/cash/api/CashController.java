package com.nectux.magicgold.cash.api;

import com.nectux.magicgold.cash.domain.CashSession;
import com.nectux.magicgold.cash.service.CashService;
import com.nectux.magicgold.security.TenantContext;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    public record OpenRequest(@NotNull UUID registerId, BigDecimal openingBalance) {
    }

    public record CloseRequest(@NotNull BigDecimal countedCash) {
    }

    public record SessionResponse(
            UUID id,
            UUID registerId,
            String status,
            BigDecimal openingBalance,
            BigDecimal expectedCash,
            BigDecimal countedCash,
            BigDecimal difference) {
        static SessionResponse from(CashSession s) {
            return new SessionResponse(s.getId(), s.getRegisterId(), s.getStatus(), s.getOpeningBalance(),
                    s.getExpectedCash(), s.getCountedCash(), s.getDifference());
        }
    }

    @PostMapping("/sessions/open")
    public ResponseEntity<SessionResponse> open(@RequestBody OpenRequest request) {
        CashSession session = cashService.open(currentMerchant(), request.registerId(),
                currentUserId(), request.openingBalance());
        return ResponseEntity.status(HttpStatus.CREATED).body(SessionResponse.from(session));
    }

    @PostMapping("/sessions/{id}/close")
    public ResponseEntity<SessionResponse> close(@PathVariable UUID id, @RequestBody CloseRequest request) {
        CashSession session = cashService.close(currentMerchant(), id, request.countedCash());
        return ResponseEntity.ok(SessionResponse.from(session));
    }

    private UUID currentMerchant() {
        UUID merchantId = TenantContext.get();
        if (merchantId == null) {
            throw new IllegalStateException("Contexte merchant absent");
        }
        return merchantId;
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }
        throw new IllegalStateException("Utilisateur non authentifié");
    }
}
