# Coffee Store — Build Roadmap

A working checklist for turning the current demo into a real coffee-ordering app.

**Tiers are ordered by need: Tier 3 = most needed, Tier 1 = least needed.**

Decisions locked in: extend the existing app (no rewrite) · stay Java + XML (no ViewModel/Navigation/Kotlin/Compose) · guest ordering by default, optional accounts · **Room for storage, but not until Tier 2**.

---

## Tier 3 — Most needed

The core ordering journey has to work and be correct. Everything here builds with the current dependency set.

Work these in order — the app stays runnable at every step.

### Step 1 — Fix the build first

- [ ] Declare `androidx.recyclerview` explicitly in `gradle/libs.versions.toml` and add it to `app/build.gradle.kts`
  - It is **not declared today** — the app only compiles because `material` pulls it in transitively. One Material bump and the build breaks.
- [ ] Run `./gradlew assembleDebug` to confirm it still builds

### Step 2 — Money as `long` cents (+ unit tests)

Currency is `double` today. Fix this before anything else depends on it.

- [ ] Change `CoffeeItem.price` (double) → `basePriceCents` (long)
- [ ] **Delete `getPrice()` outright** — don't leave a delegate. Let the compiler find every stale call site (`MainActivity` ×2, `CoffeeAdapter` ×1)
- [ ] Add `String id` to `CoffeeItem` — future Room primary key and cart merge key
- [ ] Replace `int imageID` with `String imageKey` + a static key→drawable map
  - R ids are **not stable across builds**; persisting one into Room later is a live trap
- [ ] Add `Money.java` — `format(long cents)`, passing `Locale.US`
  - Every existing `String.format` omits Locale, which breaks in comma-decimal locales
- [ ] Add `Pricing.java` — statics: `unitPriceCents(CoffeeItem, Size)`, `lineTotalCents`, `subtotalCents`, `taxCents` (half-up: `(subtotal*bp + 5000)/10000`), `totalCents`
- [ ] Write unit tests for `Money` + `Pricing` in `app/src/test/`
- [ ] `./gradlew test` passes, app still runs with unchanged behavior

> Keep `Money`, `Pricing`, `Cart`, `CartLine` free of `android.*` imports so they're testable without an emulator.

### Step 3 — Data layer (no new UI yet)

- [ ] `Size` enum — `SMALL(0)`, `MEDIUM(50)`, `LARGE(100)` carrying `surchargeCents`
- [ ] **Delete `CoffeeItem.sizes`** — the `"S/M/L"` string is not a data model
- [ ] `CartLine` — `{CoffeeItem item, Size size, int quantity}`, merge key = `item.getId() + size`
  - Not the same thing as a `CoffeeItem`: catalog data is immutable, a cart line is a mutable order fact, and one drink can produce several lines
- [ ] `Cart` (Serializable) — `add` (merges matching lines), `setQuantity` (0 removes), `remove`, `clear`, `getLines`
- [ ] `MenuRepository` interface + `StaticMenuRepository` — move the hardcoded list out of `MainActivity.onCreate`
- [ ] `CartRepository` interface + `InMemoryCartRepository`
- [ ] `CoffeeApp extends Application` owning the single `CartRepository` — register it in the manifest
  - This is the rotation fix. Chosen over `onSaveInstanceState` because the cart is now shared across three Activities, and Bundle round-tripping would create diverging copies
  - Known limit: survives rotation, **not** process death. Durable storage is Tier 2's job
- [ ] `MainActivity` delegates to the repositories
- [ ] `./gradlew assembleDebug` — app runs, behavior unchanged

### Step 4 — Size + quantity selection

- [ ] `dialog_size_quantity.xml` — S/M/L `RadioGroup` (each option shows its actual price) + `−` / qty / `+` stepper
- [ ] `SizeQuantityDialogFragment` returning the choice via a listener interface
- [ ] Change `CoffeeAdapter.CartListener.onAddToCart` → `onItemSelected(CoffeeItem)` so a tap opens the dialog instead of adding blindly
- [ ] Fix `coffee_item.xml`: inner vertical `LinearLayout` is `width="match_parent"`, leaving no room for a trailing control → change to `width="0dp"` + `layout_weight="1"`, and lift `btnAddToCart` out to a row-level sibling
- [ ] Verify: adding the same drink in two sizes creates **two lines**; same size twice **merges**

### Step 5 — A real cart screen

The cart is write-only right now — this is the biggest functional hole.

- [ ] `activity_cart.xml` + `cart_line_item.xml` (name/size, unit price, `−` qty `+`, line total, remove)
- [ ] `CartActivity` + `CartLineAdapter` — line items, inline quantity editing, per-line removal, subtotal / tax / total
- [ ] Use `getBindingAdapterPosition()` in this adapter — rows get removed, so `CoffeeAdapter`'s captured-position pattern is unsafe here
- [ ] `MainActivity` checkout button becomes "View Cart (n) — $x"
- [ ] Add `parentActivityName` to `CartActivity` in the manifest

### Step 6 — Confirmation stops being a dead end

- [ ] `Order` (Serializable) — order number, **snapshot** of the lines, subtotal/tax/total, timestamp
- [ ] Pass `Order` as an Intent extra so `ConfirmationActivity` is self-contained and survives process death
- [ ] Rebuild `activity_confirmation.xml`: add `orderNumberText`, a RecyclerView of order lines (`OrderLineAdapter`), subtotal/tax/total rows, and a **"Back to Menu"** button
- [ ] **Clear the cart in the repository at order placement**, not in the button handler — otherwise system Back from confirmation returns you to a stale cart
- [ ] Add `parentActivityName` to `ConfirmationActivity` in the manifest

### Step 7 — Cleanup

- [ ] Extract every hardcoded string into `strings.xml` (it currently holds only `app_name`)

---

## Tier 2 — Should-have

Makes it a credible store rather than a demo.

- [ ] **Room persistence** — database-backed `MenuRepository`/`CartRepository`. Cart survives process death, order history becomes queryable. Depends on the `id` + `imageKey` fixes from Step 2
- [ ] **Optional accounts** — guest stays the default; a local profile (name, phone for pickup) unlocks history and favorites. No auth server — a local identity is honest for a pickup-only store
- [ ] **Order history + one-tap reorder** — the biggest payoff of having a database
- [ ] **Customization beyond size** — milk type, extra shots, syrups, ice level, each with price add-ons. `CartLine` already carries per-line options, so this extends cleanly
- [ ] **Menu categories + search** — hot / iced / food. Three items don't need it; twenty do
- [ ] **Fix dark mode** — currently broken: hardcoded `#FAF0E6` / `#f7f7f7` backgrounds against default-colored text render unreadable under night mode
- [ ] **Resource hygiene** — real brand palette in `colors.xml`; make `button_light_brown.xml` a state selector with pressed/disabled states and a ripple; delete the unreferenced `border.xml`
- [ ] **Store info screen** — hours, the pickup address currently hardcoded in `pickupInfoText`, map intent
- [ ] **Empty and error states** + `contentDescription` on menu images
- [ ] **Espresso test** covering the full order journey

---

## Tier 1 — Least needed

Polish and platform reach. None of this blocks a working app.

- [ ] **Loyalty / rewards** — stamps toward a free drink. The most natural "why sign in?" answer once accounts exist
- [ ] **Firebase Auth + Firestore** — real cross-device accounts, replacing the local profile. Only worth it if orders must follow a customer across devices
- [ ] **Payment** — Google Pay or Stripe. Large scope (PCI, refunds, failure states); pay-in-store is a legitimate permanent answer
- [ ] **Push notifications** — "your order is ready"
- [ ] **Scheduled pickup times + order status tracking**
- [ ] **Promo codes, ratings, tablet/landscape layouts, home-screen widget**
- [ ] **Architecture upgrade** — ViewModel + Navigation, or a Compose rewrite. Deliberately last; Tier 3's repository seam keeps this contained if you ever want it

---

## Verification

Run after each step:

```bash
./gradlew test
```

```bash
./gradlew assembleDebug
```

```bash
./gradlew installDebug
```

Manual pass once Tier 3 lands:

- [ ] Tap a drink → dialog shows S/M/L with correct per-size prices → pick Large ×2 → cart count updates
- [ ] Same drink, different size → **two separate lines**, not a merge
- [ ] Same drink, same size again → **merges** to qty 3
- [ ] In the cart: decrementing to zero removes the line; subtotal/tax/total recompute correctly
- [ ] **Rotate the phone on every screen** — cart contents survive
- [ ] Checkout → confirmation shows itemized lines, an order number, and correct totals
- [ ] **Press system Back from confirmation** → menu shows an empty cart, not a stale one
- [ ] Toggle system dark mode — expect this to look wrong until Tier 2
