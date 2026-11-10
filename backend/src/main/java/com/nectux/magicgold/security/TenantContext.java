package com.nectux.magicgold.security;

import java.util.UUID;

/**
 * Contexte du tenant courant (Merchant = Tenant). Positionné par TenantContextFilter.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID merchantId) {
        CURRENT.set(merchantId);
    }

    public static UUID get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}
