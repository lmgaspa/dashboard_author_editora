package com.dianaglobal.paineldoauthorbackend.adapter.in.dto.emails;

import java.math.BigDecimal;

/**
 * DTO para informações de cupom em e-mails de repasse.
 */
public record CouponInfoPayoutDTO(
        Boolean teveCupom,        // true se coupon_code IS NOT NULL
        String codigoCupom,       // o.coupon_code (pode ser null)
        BigDecimal valorDesconto  // o.discount_amount (0 se não tiver cupom)
) {}

