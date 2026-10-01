import { FormEvent, useEffect, useState } from 'react'

type CartItem = { itemId: number; variantId: number; productName: string; size?: string; color?: string; imageUrl?: string; unitPrice: number; quantity: number; availableStock: number; lineTotal: number }
type Cart = { items: CartItem[]; subtotal: number; itemCount: number }
type Order = { id: number; orderCode: string; status: string; totalAmount: number; recipientName: string; phone: string; address: string; createdAt: string; itemCount?: number; items?: { id: number; productName: string; size?: string; color?: string; quantity: number; unitPrice: number; lineTotal: number }[]; history?: { status: string; changedAt: string; note?: string }[] }
type Address = { id: number; recipientName: string; phone: string; addressLine: string; isDefault: boolean }

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  const token = localStorage.getItem('accessToken') ?? localStorage.getItem('token')
  const response = await fetch(path, { ...init, headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...init?.headers } })
  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message ?? `Yêu cầu thất bại (${response.status})`)
  }
  return response.status === 204 ? undefined as T : response.json()
}
const money = (amount: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(amount)
const messageOf = (error: unknown) => error instanceof Error ? error.message : 'Có lỗi xảy ra. Vui lòng thử lại.'

export function CartPage() {
  const [cart, setCart] = useState<Cart>()
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  async function refresh() { try { setCart(await api<Cart>('/api/cart')); setError('') } catch (e) { setError(messageOf(e)) } }
  useEffect(() => { void refresh() }, [])
  async function update(itemId: number, quantity: number) {
    setBusy(true); try { setCart(await api<Cart>(`/api/cart/items/${itemId}`, { method: 'PUT', body: JSON.stringify({ quantity }) })); setError('') } catch (e) { setError(messageOf(e)) } finally { setBusy(false) }
  }
  async function remove(itemId: number) {
    setBusy(true); try { setCart(await api<Cart>(`/api/cart/items/${itemId}`, { method: 'DELETE' })); setError('') } catch (e) { setError(messageOf(e)) } finally { setBusy(false) }
  }
  return <main className="page-shell order-page"><p className="eyebrow">MUA SẮM</p><h1>Giỏ hàng</h1>
    {error && <p className="notice notice-error" role="alert">{error}</p>}
    {!cart ? <p>Đang tải giỏ hàng…</p> : cart.items.length === 0 ? <section className="order-card"><h2>Giỏ hàng đang trống</h2><p>Hãy chọn sản phẩm bạn yêu thích để bắt đầu mua sắm.</p><a className="button button-primary" href="/products">Xem sản phẩm</a></section> : <div className="order-layout"><section className="order-card cart-lines">{cart.items.map(item => <article className="cart-line" key={item.itemId}>
      {item.imageUrl ? <img src={item.imageUrl} alt="" /> : <div className="product-placeholder">F</div>}
      <div className="cart-product"><h2>{item.productName}</h2><p>{[item.size, item.color].filter(Boolean).join(' · ') || 'Sản phẩm thời trang'}</p><span>{money(item.unitPrice)} / sản phẩm</span><small>Còn {item.availableStock} sản phẩm</small></div>
      <div className="quantity-control"><button disabled={busy || item.quantity <= 1} onClick={() => void update(item.itemId, item.quantity - 1)} aria-label="Giảm số lượng">−</button><span>{item.quantity}</span><button disabled={busy || item.quantity >= item.availableStock} onClick={() => void update(item.itemId, item.quantity + 1)} aria-label="Tăng số lượng">+</button></div>
      <strong>{money(item.lineTotal)}</strong><button className="text-button" disabled={busy} onClick={() => void remove(item.itemId)}>Xóa</button>
    </article>)}</section><aside className="order-card summary-card"><h2>Tóm tắt đơn hàng</h2><p><span>{cart.itemCount} sản phẩm</span><strong>{money(cart.subtotal)}</strong></p><p className="summary-total"><span>Tạm tính</span><strong>{money(cart.subtotal)}</strong></p><a className="button button-primary full-button" href="/checkout">Tiến hành đặt hàng</a><small>Phí vận chuyển sẽ được thông báo khi xác nhận đơn.</small></aside></div>}
  </main>
}

export function CheckoutPage() {
  const [cart, setCart] = useState<Cart>()
  const [addresses, setAddresses] = useState<Address[]>([])
  const [addressId, setAddressId] = useState('')
  const [voucherCode, setVoucherCode] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [order, setOrder] = useState<Order>()
  useEffect(() => {
    void api<Cart>('/api/cart').then(setCart).catch(e => setError(messageOf(e)))
    void api<Address[]>('/api/users/me/addresses').then(result => {
      setAddresses(result)
      const preferred = result.find(address => address.isDefault) ?? result[0]
      if (preferred) setAddressId(String(preferred.id))
    }).catch(e => setError(messageOf(e)))
  }, [])
  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('')
    try { setOrder(await api<Order>('/api/orders', { method: 'POST', body: JSON.stringify({ addressId: Number(addressId), voucherCode: voucherCode.trim() || null }) })) }
    catch (e) { setError(messageOf(e)) } finally { setBusy(false) }
  }
  if (order) return <main className="page-shell order-page"><section className="order-card success-card"><p className="eyebrow">ĐẶT HÀNG THÀNH CÔNG</p><h1>Cảm ơn bạn đã đặt hàng.</h1><p>Mã đơn: <strong>{order.orderCode}</strong></p><p>Tổng thanh toán COD: <strong>{money(order.totalAmount)}</strong></p><a className="button button-primary" href={`/orders/${order.id}`}>Xem chi tiết đơn hàng</a></section></main>
  return <main className="page-shell order-page"><p className="eyebrow">THANH TOÁN</p><h1>Hoàn tất đơn hàng</h1>
    {error && <p className="notice notice-error" role="alert">{error}</p>}
    <div className="order-layout"><form className="order-card checkout-form" onSubmit={submit}><label>Địa chỉ giao hàng<select required value={addressId} onChange={e => setAddressId(e.target.value)}><option value="">Chọn địa chỉ</option>{addresses.map(address => <option key={address.id} value={address.id}>{address.recipientName} · {address.phone} · {address.addressLine}{address.isDefault ? ' (Mặc định)' : ''}</option>)}</select></label>{addresses.length === 0 && <small>Hãy thêm địa chỉ trong Sổ địa chỉ trước khi đặt hàng.</small>}<label>Mã voucher (không bắt buộc)<input value={voucherCode} onChange={e => setVoucherCode(e.target.value)} placeholder="Nhập mã giảm giá" /></label><label>Phương thức thanh toán<input value="Thanh toán khi nhận hàng (COD)" readOnly /></label><button className="button button-primary" disabled={busy || !cart?.items.length || !addressId}>{busy ? 'Đang tạo đơn…' : 'Đặt hàng'}</button></form>
      <aside className="order-card summary-card"><h2>Đơn hàng của bạn</h2>{cart?.items.map(item => <p key={item.itemId}><span>{item.productName} × {item.quantity}</span><strong>{money(item.lineTotal)}</strong></p>)}<p className="summary-total"><span>Tạm tính</span><strong>{money(cart?.subtotal ?? 0)}</strong></p><small>Voucher được xác thực và áp dụng khi tạo đơn.</small></aside></div>
  </main>
}

export function OrdersPage({ orderId }: { orderId?: string }) {
  const [orders, setOrders] = useState<Order[]>([])
  const [detail, setDetail] = useState<Order>()
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  useEffect(() => { if (orderId) void api<Order>(`/api/orders/${orderId}`).then(setDetail).catch(e => setError(messageOf(e))); else void api<Order[]>('/api/orders').then(setOrders).catch(e => setError(messageOf(e))) }, [orderId, refreshKey])
  async function cancel(id: number) { try { await api(`/api/orders/${id}/cancel`, { method: 'PUT' }); setRefreshKey(k => k + 1) } catch (e) { setError(messageOf(e)) } }
  return <main className="page-shell order-page"><p className="eyebrow">TÀI KHOẢN</p><h1>{detail ? `Đơn ${detail.orderCode}` : 'Lịch sử đơn hàng'}</h1>{error && <p className="notice notice-error" role="alert">{error}</p>}
    {detail ? <section className="order-card"><p>Trạng thái: <strong>{detail.status}</strong> · {money(detail.totalAmount)}</p><p>{detail.recipientName} · {detail.phone}<br />{detail.address}</p><div className="history-list">{detail.items?.map(item => <p key={item.id}>{item.productName} × {item.quantity} <strong>{money(item.lineTotal)}</strong></p>)}</div><h2>Lịch sử trạng thái</h2>{detail.history?.map((entry, index) => <p key={`${entry.status}-${index}`}>{entry.status} · {new Date(entry.changedAt).toLocaleString('vi-VN')} {entry.note}</p>)}{detail.status === 'PENDING' && <button className="button button-secondary" onClick={() => void cancel(detail.id)}>Hủy đơn hàng</button>}</section> : <section className="order-card">{orders.length === 0 ? <p>Bạn chưa có đơn hàng.</p> : orders.map(order => <article className="order-row" key={order.id}><div><strong>{order.orderCode}</strong><p>{new Date(order.createdAt).toLocaleString('vi-VN')} · {order.itemCount} sản phẩm</p></div><div><strong>{money(order.totalAmount)}</strong><p>{order.status}</p></div><a href={`/orders/${order.id}`}>Chi tiết</a>{order.status === 'PENDING' && <button className="text-button" onClick={() => void cancel(order.id)}>Hủy</button>}</article>)}</section>}
  </main>
}

export function AdminOrdersPage() {
  const [orders, setOrders] = useState<Order[]>([])
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  useEffect(() => { void api<Order[]>('/api/admin/orders').then(setOrders).catch(e => setError(messageOf(e))) }, [refreshKey])
  async function changeStatus(id: number, status: string) { try { await api(`/api/admin/orders/${id}/status`, { method: 'PUT', body: JSON.stringify({ status, note: 'Cập nhật bởi quản trị viên' }) }); setRefreshKey(k => k + 1) } catch (e) { setError(messageOf(e)) } }
  return <main className="page-shell order-page"><p className="eyebrow">QUẢN TRỊ</p><h1>Quản lý đơn hàng</h1>{error && <p className="notice notice-error" role="alert">{error}</p>}<section className="order-card">{orders.length === 0 ? <p>Chưa có đơn hàng hoặc cần quyền Admin để xem.</p> : orders.map(order => <article className="order-row" key={order.id}><div><strong>{order.orderCode}</strong><p>{order.recipientName} · {order.phone}</p><p>{order.address}</p></div><div><strong>{money(order.totalAmount)}</strong><p>{order.status}</p></div><div className="admin-actions">{order.status === 'PENDING' && <button onClick={() => void changeStatus(order.id, 'PROCESSING')}>Xác nhận</button>}{order.status === 'PROCESSING' && <button onClick={() => void changeStatus(order.id, 'SHIPPING')}>Giao vận chuyển</button>}{order.status === 'SHIPPING' && <button onClick={() => void changeStatus(order.id, 'DELIVERED')}>Đã giao</button>}{['PENDING','PROCESSING'].includes(order.status) && <button className="danger-button" onClick={() => void changeStatus(order.id, 'CANCELLED')}>Hủy đơn</button>}</div></article>)}</section></main>
}
