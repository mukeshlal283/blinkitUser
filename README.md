# Blinkit clone App (For Users)

A fast, modern Android app inspired by Blinkit for ultra‑quick grocery shopping. Users can browse products across many categories, search in real time, add items to cart, and place orders. The cart auto‑calculates subtotal, discounts, and final payable amount.

## Key Features
* Auth: Email/Google Sign‑In (Firebase Authentication)
* Catalog: Product listing by categories with images, price, unit, stock, and discount
* Search: Instant, case‑insensitive search inside RecyclerView
* Cart & Checkout: Add/remove/update quantity; auto total, discount, and final price calculation
* Orders: Place order and persist to backend; basic order details & status
* Offline‑first UX: Cache last seen catalog for quick reloads
* Notifications: Order confirmation/updates

## Tech Stack
* Language: Kotlin
* Architecture: MVVM + Repository pattern
* Networking: Retrofit + OkHttp (Moshi/Gson)
* Async: Kotlin Coroutines + Flow
* ViewModel exposes StateFlow/LiveData for products, categories, cart, and order state.
* Repository coordinates data from API and Firebase and applies pricing/discount rules.
* Storage/Backend: Firebase (Auth, Firestore/Realtime DB, Storage as needed)
* UI: Material Components, RecyclerView with multiple Adapters/ViewHolders