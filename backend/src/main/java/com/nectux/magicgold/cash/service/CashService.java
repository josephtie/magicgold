package com.nectux.magicgold.cash.service;

import com.nectux.magicgold.cash.domain.CashMovement;
import com.nectux.magicgold.cash.domain.CashSession;
import com.nectux.magicgold.cash.domain.Register;
import com.nectux.magicgold.cash.repository.CashMovementRepository;
import com.nectux.magicgold.cash.repository.CashSessionRepository;
import com.nectux.magicgold.cash.repository.RegisterRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CashService {

    private final CashSessionRepository cashSessionRepository;
    private final CashMovementRepository cashMovementRepository;
    private final RegisterRepository registerRepository;

    @Transactional
    public CashSession open(UUID merchantId, UUID registerId, UUID userId, BigDecimal openingBalance) {
        registerRepository.findById(registerId)
                .filter(r -> r.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("Register introuvable : " + registerId));

        cashSessionRepository.findByRegisterIdAndStatus(registerId, "OPEN")
                .ifPresent(s -> {
                    throw new IllegalStateException("Une session est déjà ouverte pour cette caisse");
                });

        CashSession session = CashSession.builder()
                .registerId(registerId)
                .userId(userId)
                .openedAt(Instant.now())
                .openingBalance(openingBalance != null ? openingBalance : BigDecimal.ZERO)
                .status("OPEN")
                .build();
        session.setMerchantId(merchantId);
        return cashSessionRepository.save(session);
    }

    @Transactional
    public CashSession close(UUID merchantId, UUID sessionId, BigDecimal countedCash) {
        CashSession session = cashSessionRepository.findById(sessionId)
                .filter(s -> s.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("CashSession introuvable : " + sessionId));

        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalStateException("Session déjà clôturée");
        }

        BigDecimal expected = expectedCash(session);
        BigDecimal counted = countedCash != null ? countedCash : BigDecimal.ZERO;

        session.setClosedAt(Instant.now());
        session.setExpectedCash(expected);
        session.setCountedCash(counted);
        session.setDifference(counted.subtract(expected));
        session.setStatus("CLOSED");
        return cashSessionRepository.save(session);
    }

    @Transactional
    public CashMovement addMovement(UUID merchantId, UUID sessionId, String type, BigDecimal amount, String reason) {
        CashSession session = cashSessionRepository.findById(sessionId)
                .filter(s -> s.getMerchantId().equals(merchantId))
                .orElseThrow(() -> new EntityNotFoundException("CashSession introuvable : " + sessionId));
        if (!"OPEN".equals(session.getStatus())) {
            throw new IllegalStateException("Impossible d'ajouter un mouvement sur une session clôturée");
        }
        if (!("IN".equals(type) || "OUT".equals(type))) {
            throw new IllegalArgumentException("type doit être IN ou OUT");
        }
        CashMovement movement = CashMovement.builder()
                .cashSessionId(sessionId)
                .type(type)
                .amount(amount)
                .reason(reason)
                .build();
        movement.setMerchantId(merchantId);
        return cashMovementRepository.save(movement);
    }

    private BigDecimal expectedCash(CashSession session) {
        List<CashMovement> movements = cashMovementRepository.findByCashSessionId(session.getId());
        BigDecimal expected = session.getOpeningBalance() != null ? session.getOpeningBalance() : BigDecimal.ZERO;
        for (CashMovement m : movements) {
            if ("IN".equals(m.getType())) {
                expected = expected.add(m.getAmount());
            } else if ("OUT".equals(m.getType())) {
                expected = expected.subtract(m.getAmount());
            }
        }
        return expected;
    }
}
