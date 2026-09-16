# Step 2 — proposed new files (for review)

The other half of Step 2 lives inline in the source as `// STEP2>` comments:

```bash
grep -rn "STEP2>" app/src/main/java
```

This document holds the files that are **entirely new**, so there were no existing lines to annotate. Nothing here exists as real code yet — reviewing this document and the inline comments together is the full Step 2 diff.

Delete this file once Step 2 is applied.

---

## 1. `Money.java`

`app/src/main/java/com/example/assignment3/Money.java`

One job: turn exact integer cents into a string a human reads. This is the **only** place cents become a decimal, which is what keeps display and storage from drifting apart.

```java
package com.example.assignment3;

import java.util.Locale;

public final class Money {

    private Money() {}

    public static String format(long cents) {
        long abs = Math.abs(cents);
        String sign = cents < 0 ? "-" : "";
        return String.format(Locale.US, "%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
```

Two details worth the review:

- **`Locale.US` is explicit.** Every `String.format` in the app today omits it, so it silently follows the device locale — on a German phone the menu already renders `$8,99`. Currency formatting is not a place to inherit ambiguity.
- **Negative numbers are handled by sign-splitting.** `-150 / 100` is `-1` and `-150 % 100` is `-50` in Java, which would print `$-1.-50`. You don't have negative prices today, but a refund or discount in Tier 1 would walk straight into it.

No `android.*` imports — that's deliberate, it's what lets `./gradlew test` cover this with no emulator.

---

## 2. `Pricing.java`

`app/src/main/java/com/example/assignment3/Pricing.java`

**Changed from the roadmap.** The roadmap specified `unitPriceCents(CoffeeItem, Size)` and a collection-summing `subtotalCents` — but `Size` and `CartLine` don't exist until Step 3. That was a forward dependency in my own plan.

So Step 2's `Pricing` is pure integer arithmetic with no domain coupling at all. The `Size`-aware and `CartLine`-summing methods get added in Step 3 when those types actually exist.

```java
package com.example.assignment3;

public final class Pricing {

    /** Sales tax in basis points: 1075 = 10.75%. */
    public static final int TAX_BASIS_POINTS = 1075;

    private Pricing() {}

    public static long lineTotalCents(long unitPriceCents, int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative: " + quantity);
        }
        return unitPriceCents * quantity;
    }

    /** Tax rate in basis points: 875 = 8.75%. Rounds half up. */
    public static long taxCents(long subtotalCents, int taxBasisPoints) {
        return (subtotalCents * taxBasisPoints + 5000) / 10000;
    }

    public static long totalCents(long subtotalCents, long taxCents) {
        return subtotalCents + taxCents;
    }
}
```

On the tax formula — `(subtotal * bp + 5000) / 10000`:

Java integer division truncates toward zero, so a bare `subtotal * bp / 10000` would always round *down* and quietly under-collect tax on most orders. Adding half the divisor (`5000`) before dividing converts truncation into **round-half-up**, which is the conventional rule for sales tax.

Worked example, 10.75% on a $2.00 order: exact tax is `21.5` cents — a dead-on half, the case that decides whether the formula is right. `(200 * 1075 + 5000) / 10000` = `(215000 + 5000) / 10000` = `220000 / 10000` = **22** cents. Truncating without the `+ 5000` gives `21`, shorting the tax on every half-cent order. That's the `taxRoundsUpAtExactHalf` test below.

Rates are **basis points as `int`**, not a `double` rate, for the same reason prices are cents: `0.1075` isn't exactly representable in binary, and reintroducing a float at the tax step would undo the entire exercise.

**A note on `TAX_BASIS_POINTS` as a constant:** a real store reads tax rates from its backend, since rates change by jurisdiction and over time. A hardcoded constant is the right scope here, but it's a deliberate simplification, not an oversight — it's a single well-named place to change when it needs to move.

---

## 3. `MenuImages.java`

`app/src/main/java/com/example/assignment3/MenuImages.java`

**Not specified in the roadmap** — it said "a static key→drawable map" without saying where it lives. It shouldn't live in `CoffeeItem`: that would recouple the model to generated resource ids, which is exactly what `imageKey` exists to avoid.

```java
package com.example.assignment3;

import java.util.HashMap;
import java.util.Map;

public final class MenuImages {

    private static final Map<String, Integer> KEY_TO_DRAWABLE = new HashMap<>();

    static {
        KEY_TO_DRAWABLE.put("coffee_latte", R.drawable.coffee_latte);
        KEY_TO_DRAWABLE.put("coffee_americano", R.drawable.coffee_americano);
        KEY_TO_DRAWABLE.put("coffee_mocha", R.drawable.coffee_mocha);
    }

    private MenuImages() {}

    public static int resolve(String imageKey) {
        Integer resId = KEY_TO_DRAWABLE.get(imageKey);
        return resId != null ? resId : R.drawable.ic_launcher_foreground;
    }
}
```

The fallback is a real decision: an unknown key means a menu item added later without a matching drawable. Returning a placeholder shows a wrong-looking row; throwing would crash the whole menu. For a catalog, degrading beats crashing.

This one *does* reference `R`, which is why it's separate from `Money`/`Pricing` — it can't be unit-tested without Android, and they can.

---

## 4. `MoneyTest.java`

`app/src/test/java/com/example/assignment3/MoneyTest.java`

```java
package com.example.assignment3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MoneyTest {

    @Test
    public void formatsMenuPrices() {
        assertEquals("$8.99", Money.format(899L));
        assertEquals("$4.50", Money.format(450L));
        assertEquals("$6.25", Money.format(625L));
    }

    @Test
    public void padsCentsBelowTen() {
        assertEquals("$5.05", Money.format(505L));
        assertEquals("$5.00", Money.format(500L));
    }

    @Test
    public void handlesZeroAndSubDollar() {
        assertEquals("$0.00", Money.format(0L));
        assertEquals("$0.07", Money.format(7L));
    }

    @Test
    public void handlesNegative() {
        assertEquals("-$1.50", Money.format(-150L));
    }

    @Test
    public void handlesLargeTotals() {
        assertEquals("$1234.56", Money.format(123456L));
    }
}
```

---

## 5. `PricingTest.java`

`app/src/test/java/com/example/assignment3/PricingTest.java`

The first two tests are the ones that matter — they're the exact cases where the current `double` code produces wrong answers.

```java
package com.example.assignment3;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PricingTest {

    private static final int TAX_BP = Pricing.TAX_BASIS_POINTS; // 1075 = 10.75%

    /** double gives 19.740000000000002 here, and (total == 19.74) is false. */
    @Test
    public void oneOfEachIsExact() {
        long subtotal = 899L + 450L + 625L;
        assertEquals(1974L, subtotal);
        assertEquals("$19.74", Money.format(subtotal));
    }

    /** double accumulates to 899.0000000000007 over 100 additions. */
    @Test
    public void repeatedAdditionDoesNotDrift() {
        long subtotal = 0L;
        for (int i = 0; i < 100; i++) {
            subtotal += 899L;
        }
        assertEquals(89900L, subtotal);
    }

    @Test
    public void lineTotalMultipliesExactly() {
        assertEquals(2697L, Pricing.lineTotalCents(899L, 3));
        assertEquals(0L, Pricing.lineTotalCents(899L, 0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsNegativeQuantity() {
        Pricing.lineTotalCents(899L, -1);
    }

    @Test
    public void taxRoundsHalfUp() {
        // 2697 * 1075 = 2899275 -> 289.9275 cents, rounds to 290
        assertEquals(290L, Pricing.taxCents(2697L, TAX_BP));
    }

    /** The test that justifies the "+ 5000": exactly 21.5 cents must become 22, not 21. */
    @Test
    public void taxRoundsUpAtExactHalf() {
        // 200 * 1075 = 215000 -> exactly 21.5 cents, half-up gives 22
        assertEquals(22L, Pricing.taxCents(200L, TAX_BP));
    }

    @Test
    public void taxOnZeroIsZero() {
        assertEquals(0L, Pricing.taxCents(0L, TAX_BP));
    }

    /** The exact numbers the confirmation screen shows for one of each drink. */
    @Test
    public void totalAddsSubtotalAndTax() {
        long subtotal = 1974L;
        long tax = Pricing.taxCents(subtotal, TAX_BP);
        assertEquals(212L, tax);
        assertEquals(2186L, Pricing.totalCents(subtotal, tax));
        assertEquals("$21.86", Money.format(Pricing.totalCents(subtotal, tax)));
    }
}
```

---

---

## 6. Tax wiring (decided: 10.75%, visible on confirmation)

Tax is charged at **1075 basis points (10.75%)**, roughly the combined rate for Hayward, CA — matching the pickup address already hardcoded in the app. Worth confirming the current figure if realism is being graded; it's one constant to change.

This makes Step 2 **not** a pure refactor. The app's behavior changes: the confirmation screen now shows three lines instead of one.

**Where each piece lives, and why:**

| Concern | Home | Reason |
|---|---|---|
| The rate | `Pricing.TAX_BASIS_POINTS` | One constant, one place to change |
| The rounding rule | `Pricing.taxCents` | Tested in isolation, no Android needed |
| Applying it | `ConfirmationActivity` | The one screen that shows a final charge |

**Only the subtotal crosses the Intent.** `MainActivity` sums the cart and passes `EXTRA_SUBTOTAL_CENTS`; `ConfirmationActivity` derives tax and total from it. If both screens computed tax independently, any later change to the rule would have to be made twice — and the bug would show as two screens quietly disagreeing about the price.

The Intent key is also **renamed** `EXTRA_TOTAL` → `EXTRA_SUBTOTAL_CENTS`. That's not cosmetic: because Intent extras are type-keyed at runtime, keeping the old name would let a forgotten `getDoubleExtra` compile fine and silently display `$0.00`. Renaming forces the compiler to point at every site.

**Also relabelled:** the menu screen's "Cart Total: $x" becomes "Subtotal: $x". Once tax exists, calling the pre-tax figure a "total" states a number the customer won't be charged.

Layout changes are annotated in `res/layout/activity_confirmation.xml` — two new `TextView`s, plus a re-anchor of `orderTotalText`, since `RelativeLayout` positions by explicit anchor rather than document order.

**What you'll see after applying** — one of each drink:

```
Subtotal:        $19.74
Tax (10.75%):     $2.12
Total:           $21.86
```
