# Member 4 integration contract

## Authentication handoff (Member 1)

Controllers read the authenticated user's numeric ID from the verified JWT `sub`. Auth currently issues `sub = userId`; Cart/Order services validate the active account by that ID and do not resolve the principal through email. Required access rules:

- `GET/POST /api/cart/**`, `PUT/DELETE /api/cart/items/**`: authenticated `CUSTOMER`.
- `/api/orders/**`: authenticated `CUSTOMER`.
- `/api/admin/orders/**`: authenticated `ADMIN`.

The service validates the active user by `users.id` and checks the Admin role in the database for admin operations. `SecurityConfiguration` requires role `CUSTOMER` for `/api/cart/**` and `/api/orders/**`; `/api/admin/**` requires `ADMIN`.

## Voucher handoff (Member 5)

Implement `CheckoutVoucherPort` as a Spring bean. `redeem(code, subtotal)` must validate active state, expiration in the agreed Vietnam-local date, minimum order amount, usage limit and discount rules, then return the discount amount rounded to two decimal places (HALF_UP). It must increment `used_count` in the caller's transaction; do not mark its method `REQUIRES_NEW`. Throw an `OrderException` (or a mapped business exception) for an invalid voucher. The order stores the matching `vouchers.id`; all voucher usage and order/stock/cart updates then commit or roll back together. Canceling an order does not decrement `used_count`.

Until the real bean exists, a fallback adapter allows the application context to start and returns `VOUCHER_UNAVAILABLE` only when checkout includes a voucher code.

## Catalog handoff (Member 2)

Cart and checkout read these V1 fields: `product_variants(id, product_id, size, color, sku, price, stock_qty)`, `products(name,status)`, and `product_images(image_url,is_primary)`. Checkout locks variant rows in ascending ID order, verifies product status is `ACTIVE`, calculates the total from database prices and decrements stock conditionally. Coordinate any rename or schema migration before changing these queries.

## Frontend handoff

Checkout reads `GET /api/users/me/addresses`, expecting `id`, `recipientName`, `phone`, `addressLine`, and `defaultAddress`. The shared frontend API helper reads the JWT from `localStorage.fashionAccessToken` and sends it as a Bearer token. Product detail pages add items with `{ "variantId": 123, "quantity": 1 }`.

## State transitions implemented

`PENDING → PROCESSING → SHIPPING → DELIVERED`; `PENDING` and `PROCESSING` may transition to `CANCELLED`. `SHIPPING`, `DELIVERED`, and `CANCELLED` are terminal for the current scope. Customer cancellation is limited to the owner's `PENDING` order. Every accepted status update is recorded in `order_status_history`.
