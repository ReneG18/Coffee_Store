package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "extra_total";
    // STEP2> public static final String EXTRA_SUBTOTAL_CENTS = "extra_subtotal_cents";
    // STEP2> RENAMED, because what gets passed is no longer the final total — tax is applied here.
    // STEP2> Only the SUBTOTAL crosses the Intent. Tax and total are derived below from Pricing,
    // STEP2> so the tax rule lives in exactly one place and the two screens can't disagree.
    // STEP2> This file is not in the roadmap's "MainActivity x2, CoffeeAdapter x1" count —
    // STEP2> that count was wrong. This is the fourth file Step 2 has to touch.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        TextView orderTotalText = findViewById(R.id.orderTotalText);
        // STEP2> TextView orderSubtotalText = findViewById(R.id.orderSubtotalText);   // new view
        // STEP2> TextView orderTaxText = findViewById(R.id.orderTaxText);             // new view

        double total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0.0);
        // STEP2> long subtotalCents = getIntent().getLongExtra(EXTRA_SUBTOTAL_CENTS, 0L);
        // STEP2> long taxCents = Pricing.taxCents(subtotalCents, Pricing.TAX_BASIS_POINTS);
        // STEP2> long totalCents = Pricing.totalCents(subtotalCents, taxCents);
        // STEP2>
        // STEP2> The compiler will NOT flag it if you keep getDoubleExtra on a long-valued
        // STEP2> extra — it just returns 0.0 and the screen reads "$0.00" forever.

        orderTotalText.setText(String.format("Total: $%.2f", total));
        // STEP2> orderSubtotalText.setText("Subtotal: " + Money.format(subtotalCents));
        // STEP2> orderTaxText.setText("Tax (10.75%): " + Money.format(taxCents));
        // STEP2> orderTotalText.setText("Total: " + Money.format(totalCents));
        // STEP2>
        // STEP2> Showing all three matters: a customer who sees only a total can't tell whether
        // STEP2> tax was applied. It's also how you'd catch a rounding bug by eye.
    }
}
