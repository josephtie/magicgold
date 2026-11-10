package com.nectux.magicgold.customer.domain;

import com.nectux.magicgold.shared.TenantEntity;
import com.nectux.magicgold.shared.enums.CustomerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Acheteur. Peut être anonyme. N'est pas un User (00_Master.md §2.2).
 */
@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends TenantEntity {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "customer_number", unique = true, nullable = false, length = 32)
    private String customerNumber;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "gender", length = 16)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private CustomerType type;

    @Column(name = "registration_source", length = 32)
    private String registrationSource;

    @Column(name = "status", nullable = false, length = 32)
    private String status;
}
