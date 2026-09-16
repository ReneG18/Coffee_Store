package com.example.assignment3;

import java.io.Serializable;

public class CoffeeItem implements Serializable {
    //name, description, price, sizes
    // STEP2> private String id;   // NEW. Stable identity: "latte", "americano", "mocha".
    // STEP2>                      // Becomes the Room primary key (Tier 2) and half of the
    // STEP2>                      // CartLine merge key (Step 3). Name is a label, not an id —
    // STEP2>                      // renaming "Latte" to "Caffe Latte" must not orphan a cart line.
    private String name;
    private String description;
    private double price;
    // STEP2> private long basePriceCents;   // REPLACES `price`. 8.99 -> 899, 4.50 -> 450.
    // STEP2>                                // "base" because Step 3 adds a per-Size surcharge on top.
    private int imageID;
    // STEP2> private String imageKey;   // REPLACES `imageID`. Holds "coffee_latte", not R.drawable.coffee_latte.
    // STEP2>                            // R ids are regenerated every build, so a persisted one
    // STEP2>                            // silently points at the wrong image later. MenuImages resolves it.
    private String sizes;   // STEP2> unchanged here — deleted in Step 3 when the Size enum lands.

    public CoffeeItem(){
        this.name = "";
        this.description = "";
        this.price = 0.0;
        // STEP2> this.id = "";
        // STEP2> this.basePriceCents = 0L;
        this.sizes = "";
        this.imageID = 0;
        // STEP2> this.imageKey = "";
    }

    public CoffeeItem(String name, double price, String sizes, String description, int imageID){
    // STEP2> public CoffeeItem(String id, String name, long basePriceCents, String sizes,
    // STEP2>                   String description, String imageKey) {
    // STEP2>     this.id = id;
    // STEP2>     this.name = name;
    // STEP2>     this.basePriceCents = basePriceCents;
    // STEP2>     this.sizes = sizes;
    // STEP2>     this.description = description;
    // STEP2>     this.imageKey = imageKey;
    // STEP2> }
        this.name = name;
        this.price = price;
        this.sizes = sizes;
        this.description = description;
        this.imageID = imageID;
    }

    public String getName() {return name;}
    // STEP2> public String getId() {return id;}
    public String getSizes() {return sizes;}
    public String getDescription() {return description;}
    public double getPrice() {return price;}
    // STEP2> DELETE getPrice() — do not keep a `return basePriceCents / 100.0` delegate.
    // STEP2> Deleting it is the point: the compiler then lists every stale call site for you.
    // STEP2> public long getBasePriceCents() {return basePriceCents;}
    public int getImageID() {
        return imageID;
    }
    // STEP2> DELETE getImageID() too, replaced by:
    // STEP2> public String getImageKey() {return imageKey;}

}

