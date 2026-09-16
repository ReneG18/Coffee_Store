package com.example.assignment3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.ViewHolder> {

    private ArrayList<CoffeeItem> coffeeItems;
    private CartListener cartListener;

    public interface CartListener {
        void onAddToCart(CoffeeItem item);
    }

    public CoffeeAdapter(ArrayList<CoffeeItem> coffeeItems, CartListener listener){
        this.coffeeItems = coffeeItems;
        this.cartListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.coffee_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CoffeeItem item = coffeeItems.get(position);

        holder.coffeeName.setText(item.getName());
        holder.coffeeDesc.setText(item.getDescription());
        holder.coffeePrice.setText(String.format("$%.2f", item.getPrice()));
        // STEP2> Stale call site #3 of 3:
        // STEP2> holder.coffeePrice.setText(Money.format(item.getBasePriceCents()));
        // STEP2> Also note the bug being removed: String.format without a Locale uses the device
        // STEP2> locale, so this line already renders "$8,99" on a German phone.
        holder.coffeeImage.setImageResource(item.getImageID());
        // STEP2> holder.coffeeImage.setImageResource(MenuImages.resolve(item.getImageKey()));
        // STEP2> The R-id lookup moves here, at the UI edge, where a fresh R id is always correct.
        // STEP2> CoffeeItem keeps only the string, so it stays safe to persist and free of resource coupling.

        holder.btnAddToCart.setOnClickListener(v -> {
            if(cartListener != null) {
                cartListener.onAddToCart(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return coffeeItems.size();
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coffeeImage;
        TextView coffeeName, coffeeDesc, coffeePrice;
        Button btnAddToCart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            coffeeImage = itemView.findViewById(R.id.coffeeImage);
            coffeeName = itemView.findViewById(R.id.coffeeName);
            coffeeDesc = itemView.findViewById(R.id.coffeeDesc);
            coffeePrice = itemView.findViewById(R.id.coffeePrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
