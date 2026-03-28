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

            Intent intent = new Intent(MainActivity.this, ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_TOTAL, total);
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
    }
}
