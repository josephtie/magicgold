package com.nectux.magicgold.shared;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Générateur de références métier (MG-MER-2026-00000001, MG-ORD-..., MG-PAY-..., MG-CUS-...).
 * NB : compteur en mémoire, suffisant pour un MVP mono-instance. Pour un déploiement multi-instance,
 * remplacer par une séquence PostgreSQL (cf. 17_Deployment.md).
 */
@Component
public class ReferenceGenerator {

    private final AtomicLong sequence = new AtomicLong(1);

    public String next(String type) {
        return String.format("MG-%s-%d-%08d", type, Year.now().getValue(), sequence.getAndIncrement());
    }
}
