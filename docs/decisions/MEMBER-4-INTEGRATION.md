# Member 4 integration contract

## Authentication handoff (Member 1)

Controllers use `Authentication.getName()` as the authenticated user's normalized email. JWT `sub` must therefore contain the user's email, or Member 1 should adapt this handoff before merging. Required access rules:

- `GET/POST /api/cart/**`, `PUT/DELETE /api/cart/items/**`: authenticated `CUSTOMER`.
- `/api/orders/**`: authenticated `CUSTOMER`.
- `/api/admin/orders/**`: authenticated `ADMIN`.

The service independently resolves the active user from `users` and checks Admin role for admin operations. The current shared `SecurityConfiguration` still denies every route except health, so the endpoints remain inaccessible until Member 1 integrates these matchers and JWT authentication.

## Voucher handoff (Member 5)

Implement `CheckoutVoucherPort` as a Spring bean. `redeem(code, subtotal)` must validate active state, expiration in the agreed Vietnam-local date, minimum order amount, usage limit and discount rules, then return the discount amount rounded to two decimal places (HALF_UP). It must increment `used_count` in the caller's transaction; do not mark its method `REQUIRES_NEW`. Throw an `OrderException` (or a mapped business exception) for an invalid voucher. The order stores the matching `vouchers.id`; all voucher usage and order/stock/cart updates then commit or roll back together. Canceling an order does not decrement `used_count`.

Until the real bean exists, a fallback adapter allows the application context to start and returns `VOUCHER_UNAVAILABLE` only when checkout includes a voucher code.

## Catalog handoff (Member 2)

Cart and checkout read these V1 fields: `product_variants(id, product_id, size, color, sku, price, stock_qty)`, `products(name,status)`, and `product_images(image_url,is_primary)`. Checkout locks variant rows in ascending ID order, verifies product status is `ACTIVE`, calculates the total from database prices and decrements stock conditionally. Coordinate any rename or schema migration before changing these queries.

## Frontend handoff

Checkout reads `GET /api/users/me/addresses`, expecting `id`, `recipientName`, `phone`, `addressLine`, and `isDefault`. The frontend reads a bearer token from `localStorage.accessToken` or `localStorage.token`; align that key with Member 1's login response before integration. Product detail pages should add items with `{ "variantId": 123, "quantity": 1 }`.

## State transitions implemented

`PENDING → PROCESSING → SHIPPING → DELIVERED`; `PENDING` and `PROCESSING` may transition to `CANCELLED`. `SHIPPING`, `DELIVERED`, and `CANCELLED` are terminal for the current scope. Customer cancellation is limited to the owner's `PENDING` order. Every accepted status update is recorded in `order_status_history`.
