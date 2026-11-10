package com.nectux.magicgold.cash.service;

import com.nectux.magicgold.cash.domain.CashMovement;
import com.nectux.magicgold.cash.domain.CashSession;
import com.nectux.magicgold.cash.repository.CashMovementRepository;
import com.nectux.magicgold.cash.repository.CashSessionRepository;
import com.nectux.magicgold.cash.repository.RegisterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    CashSessionRepository cashSessionRepository;
    @Mock
    CashMovementRepository cashMovementRepository;
    @Mock
    RegisterRepository registerRepository;

    CashService cashService;

    @BeforeEach
    void setUp() {
        cashService = new CashService(cashSessionRepository, cashMovementRepository, registerRepository);
    }

    @Test
    void closeComputesExpectedAndDifference() {
        UUID merchantId = UUID.randomUUID();
        CashSession session = CashSession.builder()
                .registerId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .openingBalance(new BigDecimal("10000"))
                .status("OPEN")
                .build();
        session.setMerchantId(merchantId);
        session.setId(UUID.randomUUID());

        CashMovement in = CashMovement.builder().type("IN").amount(new BigDecimal("5000")).build();
        CashMovement out = CashMovement.builder().type("OUT").amount(new BigDecimal("2000")).build();

        when(cashSessionRepository.findById(session.getId())).thenReturn(Optional.of(session));
        when(cashMovementRepository.findByCashSessionId(session.getId())).thenReturn(List.of(in, out));
        when(cashSessionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CashSession closed = cashService.close(merchantId, session.getId(), new BigDecimal("13000"));

        assertEquals(new BigDecimal("13000"), closed.getExpectedCash()); // 10000 + 5000 - 2000
        assertEquals(new BigDecimal("0"), closed.getDifference());       // 13000 - 13000
        assertEquals("CLOSED", closed.getStatus());
    }
}
