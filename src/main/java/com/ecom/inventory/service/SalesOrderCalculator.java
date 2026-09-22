package com.ecom.inventory.service;

import com.ecom.inventory.exception.UserInputValidationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure pricing logic, no Spring / DB. Unit-test this class directly.
 *
 * Discounts (line and order level) are optional. At each level the caller may send an amount OR a
 * percentage, never both:
 *   - amount given      -> used as is; percentage is NOT derived (stored as null)
 *   - percentage given  -> amount is derived and the percentage is kept as given
 *   - neither / zero    -> no discount (both stored as null)
 *
 * Line:   discount is PER UNIT.  % is taken on base_price.
 *         unit_price = base_price - discount;  extended = base_price * qty;  net = unit_price * qty
 * Order:  % is taken on the subtotal (SUM of line net amounts).
 *         total_amount = subtotal - order_discount
 *
 * Doubles are rounded (HALF_UP, 2 dp; 4 dp for percentages) after every step so binary
 * floating-point noise never reaches the totals or the DB.
 */
public final class SalesOrderCalculator {

    private SalesOrderCalculator() {}

    public record LineInput(Long productId, int quantity, double basePrice,
                            Double discountAmount, Double discountPercentage, String unitOfMeasure) {}

    /** discountAmount / discountPercentage are null when no discount applies (or amount was given, for %). */
    public record LineResult(Long productId, int quantity, double basePrice, double unitPrice,
                             double extendedPrice, double netAmount, boolean discountApplied,
                             Double discountAmount, Double discountPercentage, String unitOfMeasure) {}

    public record OrderResult(List<LineResult> lines, int totalQuantity, double subTotal,
                              boolean discountApplied, Double discountAmount, Double discountPercentage,
                              double totalAmount) {}

    public static OrderResult calculate(List<LineInput> inputs, Double orderDiscountAmount,
                                        Double orderDiscountPercentage) throws UserInputValidationException {

        List<LineResult> results = new ArrayList<>();
        int totalQty = 0;
        double subTotal = 0.0;

        for (LineInput in : inputs) {
            double base = round2(in.basePrice());

            Double amountIn = in.discountAmount();
            Double pctIn = in.discountPercentage();
            if (amountIn != null && pctIn != null) {
                throw new UserInputValidationException(
                        "Send either a discount amount or a discount percentage, not both (product id "
                                + in.productId() + ")");
            }

            double disc = 0.0;
            Double storedPct = null;

            if (amountIn != null) {
                disc = round2(amountIn);
                if (disc < 0) {
                    throw new UserInputValidationException("Discount cannot be negative (product id " + in.productId() + ")");
                }
                if (disc > base) {
                    throw new UserInputValidationException(
                            "Discount per unit cannot exceed the base price (product id " + in.productId() + ")");
                }
            } else if (pctIn != null) {
                if (pctIn < 0 || pctIn > 100) {
                    throw new UserInputValidationException(
                            "Discount percentage must be between 0 and 100 (product id " + in.productId() + ")");
                }
                disc = round2(base * pctIn / 100.0);
                storedPct = round(pctIn, 4);
            }

            boolean applied = disc > 0;
            double unitPrice = round2(base - disc);
            double extended = round2(base * in.quantity());
            double net = round2(unitPrice * in.quantity());

            results.add(new LineResult(in.productId(), in.quantity(), base, unitPrice, extended, net, applied,
                    applied ? disc : null, applied ? storedPct : null, in.unitOfMeasure()));

            totalQty += in.quantity();
            subTotal = round2(subTotal + net);
        }

        // ----- order level -----
        if (orderDiscountAmount != null && orderDiscountPercentage != null) {
            throw new UserInputValidationException(
                    "Send either an order discount amount or an order discount percentage, not both");
        }

        double orderDisc = 0.0;
        Double orderPct = null;

        if (orderDiscountAmount != null) {
            orderDisc = round2(orderDiscountAmount);
            if (orderDisc < 0) {
                throw new UserInputValidationException("Order discount cannot be negative");
            }
            if (orderDisc > subTotal) {
                throw new UserInputValidationException("Order discount cannot exceed the order subtotal");
            }
        } else if (orderDiscountPercentage != null) {
            if (orderDiscountPercentage < 0 || orderDiscountPercentage > 100) {
                throw new UserInputValidationException("Order discount percentage must be between 0 and 100");
            }
            orderDisc = round2(subTotal * orderDiscountPercentage / 100.0);
            orderPct = round(orderDiscountPercentage, 4);
        }

        boolean orderApplied = orderDisc > 0;
        return new OrderResult(results, totalQty, subTotal, orderApplied,
                orderApplied ? orderDisc : null, orderApplied ? orderPct : null,
                round2(subTotal - orderDisc));
    }

    private static double round2(double v) {
        return round(v, 2);
    }

    private static double round(double v, int scale) {
        return BigDecimal.valueOf(v).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }
}
