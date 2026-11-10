package com.nectux.magicgold.merchant.service;

import com.nectux.magicgold.merchant.domain.Merchant;
import com.nectux.magicgold.merchant.repository.MerchantRepository;
import com.nectux.magicgold.shared.ReferenceGenerator;
import com.nectux.magicgold.shared.enums.MerchantStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final ReferenceGenerator referenceGenerator;

    @Transactional
    public Merchant create(String commercialName, String legalName, String businessType,
                           String registrationNumber, String taxNumber, String phoneNumber,
                           String email, String address, String city, String country) {
        if (commercialName == null || commercialName.isBlank()) {
            throw new IllegalArgumentException("commercialName est obligatoire");
        }
        Merchant merchant = Merchant.builder()
                .merchantNumber(referenceGenerator.next("MER"))
                .commercialName(commercialName)
                .legalName(legalName)
                .businessType(businessType)
                .registrationNumber(registrationNumber)
                .taxNumber(taxNumber)
                .phoneNumber(phoneNumber)
                .email(email)
                .address(address)
                .city(city)
                .country(country)
                .status(MerchantStatus.PENDING)
                .build();
        return merchantRepository.save(merchant);
    }

    @Transactional(readOnly = true)
    public Merchant get(UUID id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Merchant introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Merchant> list() {
        return merchantRepository.findAll();
    }

    @Transactional
    public Merchant updateStatus(UUID id, MerchantStatus status) {
        Merchant merchant = get(id);
        merchant.setStatus(status);
        return merchantRepository.save(merchant);
    }
}
