package com.InventoryManagementSystem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Vendor discount rules (all discounts are on the ACTUAL PRODUCT MRP).
 * The vendor gives ONE of these; the other values are calculated:
 *
 *  - Flat price (Rs)   : MRP 1000, vendor flat price 900 -> discount amount = 100, discount % = 10
 *                        (the flat price IS the purchase price)
 *  - Flat discount (Rs): MRP 1000, flat discount 100 -> discount % = 10, purchase price = 900
 *  - Percent discount  : MRP 1000, 10% discount      -> discount amount = 100, purchase price = 900
 *
 * Priority if several are sent: flat price, then flat discount amount, then percent. A flat amount of null or 0 means "not given"
 * (use 100% for a free item).
 */
public final class VendorPricingUtil {

    private VendorPricingUtil() {}

    public record Result(double discountPercentage, double discountAmount, double purchasePrice) {}

    /**
     * @param productMrp         actual product MRP
     * @param discountPct        vendor discount % (nullable)
     * @param flatDiscountAmount vendor flat discount in rupees per unit (nullable)
     */
    public static Result resolve(Double productMrp, Double discountPct, Double flatDiscountAmount) {
        return resolve(productMrp, discountPct, flatDiscountAmount, null);
    }

    /**
     * @param flatPurchasePrice  vendor flat price per unit = the purchase price (nullable, 0 = not given)
     */
    public static Result resolve(Double productMrp, Double discountPct, Double flatDiscountAmount, Double flatPurchasePrice) {
        double mrp = productMrp == null ? 0.0 : productMrp;
        if (mrp < 0) throw new RuntimeException("Product MRP cannot be negative");

        // Case 0: vendor gave a flat PRICE (purchase price) -> calculate discount amount and discount %.
        if (flatPurchasePrice != null && flatPurchasePrice < 0) {
            throw new RuntimeException("Vendor flat price cannot be negative");
        }
        if (flatPurchasePrice != null && flatPurchasePrice > 0) {
            if (mrp <= 0) throw new RuntimeException("Product MRP is required to apply a vendor flat price");
            if (flatPurchasePrice > mrp) {
                throw new RuntimeException("Vendor flat price (" + flatPurchasePrice + ") cannot be more than product MRP (" + mrp + ")");
            }
            double amount = mrp - flatPurchasePrice;
            double pct = (amount * 100.0) / mrp;
            return new Result(round(pct), round(amount), round(flatPurchasePrice));
        }

        // Case 1: vendor gave a flat discount in rupees -> calculate the discount % and purchase price.
        if (flatDiscountAmount != null && flatDiscountAmount < 0) {
            throw new RuntimeException("Vendor flat discount cannot be negative");
        }
        if (flatDiscountAmount != null && flatDiscountAmount > 0) {
            if (mrp <= 0) throw new RuntimeException("Product MRP is required to apply a flat vendor discount");
            if (flatDiscountAmount > mrp) {
                throw new RuntimeException("Vendor flat discount (" + flatDiscountAmount + ") cannot be more than product MRP (" + mrp + ")");
            }
            double pct = (flatDiscountAmount * 100.0) / mrp;
            return new Result(round(pct), round(flatDiscountAmount), round(mrp - flatDiscountAmount));
        }

        // Case 2: vendor gave a percentage (or nothing = 0%) -> calculate discount amount and purchase price.
        double pct = discountPct == null ? 0.0 : discountPct;
        if (pct < 0 || pct > 100) throw new RuntimeException("Vendor discount percentage must be between 0 and 100");
        double amount = (mrp * pct) / 100.0;
        return new Result(round(pct), round(amount), round(mrp - amount));
    }

    public static double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
