package com.ecom.inventory.component;

import org.springframework.stereotype.Component;

/** Single place to resolve the authenticated user. Replace the stub once auth is wired. */
@Component
public class CurrentUser {
    public Long id() {
        // TODO: resolve from SecurityContextHolder (the PO code hardcodes 1L)
        return 1L;
    }
}
