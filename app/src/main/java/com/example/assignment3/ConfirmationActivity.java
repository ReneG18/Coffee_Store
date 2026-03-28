package com.example.assignment3;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class ConfirmationActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL = "extra_total";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        TextView orderTotalText = findViewById(R.id.orderTotalText);

        double total = getIntent().getDoubleExtra(EXTRA_TOTAL, 0.0);

        orderTotalText.setText(String.format("Total: $%.2f", total));
    }
}
