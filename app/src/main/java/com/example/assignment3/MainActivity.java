package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CoffeeAdapter.CartListener{

    private RecyclerView recyclerView;
    private CoffeeAdapter adapter;
    private ArrayList<CoffeeItem> cart = new ArrayList<>();
    private TextView cartTotalText;
    private ArrayList<CoffeeItem> coffeeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        cartTotalText = findViewById(R.id.cartTotalText);
        Button checkoutButton = findViewById(R.id.buttonCheckout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        coffeeList = new ArrayList<>();
        coffeeList.add(new CoffeeItem("Latte", 8.99, "S/M/L", "Espresso with steamed milk", R.drawable.coffee_latte));
        coffeeList.add(new CoffeeItem("Americano", 4.50, "S/M/L", "Espresso with hot water", R.drawable.coffee_americano));
        coffeeList.add(new CoffeeItem("Mocha", 6.25, "S/M/L", "Chocolate, espresso, and milk", R.drawable.coffee_mocha));
        // STEP2> Prices become whole-number cents, images become string keys, each item gains an id:
        // STEP2> coffeeList.add(new CoffeeItem("latte", "Latte", 899L, "S/M/L",
        // STEP2>         "Espresso with steamed milk", "coffee_latte"));
        // STEP2> coffeeList.add(new CoffeeItem("americano", "Americano", 450L, "S/M/L",
        // STEP2>         "Espresso with hot water", "coffee_americano"));
        // STEP2> coffeeList.add(new CoffeeItem("mocha", "Mocha", 625L, "S/M/L",
        // STEP2>         "Chocolate, espresso, and milk", "coffee_mocha"));
        // STEP2> (This whole block moves into StaticMenuRepository in Step 3 — leaving it here for now.)

        adapter = new CoffeeAdapter(coffeeList, this);
        recyclerView.setAdapter(adapter);
        updateCartTotal();

        checkoutButton.setOnClickListener(v -> {
            if(cart.isEmpty()) {
                Toast.makeText(MainActivity.this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            double total = 0;
            for(CoffeeItem item : cart){
                total += item.getPrice();
            }
            // STEP2> Stale call site #1 of 3. Exact integer addition, no drift:
            // STEP2> long subtotalCents = 0L;
            // STEP2> for (CoffeeItem item : cart) {
            // STEP2>     subtotalCents += item.getBasePriceCents();
            // STEP2> }
            // STEP2> Tax is deliberately NOT computed here — ConfirmationActivity derives it from
            // STEP2> this subtotal. Computing it in both places is how the two screens drift apart.

            Intent intent = new Intent(MainActivity.this, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_TOTAL, total);
            // STEP2> intent.putExtra(ConfirmationActivity.EXTRA_SUBTOTAL_CENTS, subtotalCents);
            // STEP2> MUST change together with ConfirmationActivity's getLongExtra. Intent extras are
            // STEP2> type-keyed at runtime, not compile time: putExtra(long) + getDoubleExtra() does
            // STEP2> NOT fail to build — it silently returns the 0.0 default. Nothing catches this but you.
            // STEP2> Renaming the key is what protects you: the old EXTRA_TOTAL name stops compiling.
            startActivity(intent);
        });
    }
    @Override
    public void onAddToCart(CoffeeItem item) {
        cart.add(item);
        updateCartTotal();
    }

    private void updateCartTotal() {
        double total = 0;
        for(CoffeeItem item : cart){
            total += item.getPrice();
        }
        cartTotalText.setText(String.format("Cart Total: $%.2f", total));
        // STEP2> Stale call site #2 of 3:
        // STEP2> long totalCents = 0L;
        // STEP2> for (CoffeeItem item : cart) {
        // STEP2>     totalCents += item.getBasePriceCents();
        // STEP2> }
        // STEP2> cartTotalText.setText("Subtotal: " + Money.format(totalCents));
        // STEP2>
        // STEP2> Relabelled "Cart Total" -> "Subtotal" on purpose: now that tax is charged at
        // STEP2> confirmation, calling this figure the "total" would be telling the customer
        // STEP2> a number they will not actually be charged.
        // STEP2>
        // STEP2> The %.2f above is what HID the bug: it rounds 19.740000000000002 to "$19.74"
        // STEP2> on screen while the stored value stays wrong. Money.format() takes an exact
        // STEP2> integer, so displayed and stored values can't drift apart.
    }
}
