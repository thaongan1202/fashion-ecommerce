import { useCallback, useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { apiJson } from '../api/client'

type Voucher = { id: number; code: string; discountType: 'PERCENT' | 'FIXED'; discountValue: number; minOrderValue: number; expiryDate: string; usageLimit: number; usedCount: number; active: boolean; createdAt: string }
type User = { id: number; fullName: string; email: string; phone?: string; role: 'CUSTOMER' | 'ADMIN'; status: 'ACTIVE' | 'LOCKED'; createdAt: string }
type Review = { id: number; productId: number; productName: string; userId: number; customerName: string; customerEmail: string; rating: number; comment: string; imageUrl?: string; status: 'PENDING' | 'APPROVED' | 'HIDDEN'; createdAt: string }
type Dashboard = { activeCustomers: number; activeProducts: number; totalOrders: number; pendingOrders: number; deliveredOrders: number; lowStockVariants: number; deliveredRevenue: number; lastSevenDays: { date: string; revenue: number; deliveredOrders: number }[] }
type VoucherForm = { code: string; discountType: 'PERCENT' | 'FIXED'; discountValue: string; minOrderValue: string; expiryDate: string; usageLimit: string; active: boolean }

async function api<T>(path: string, init?: RequestInit): Promise<T> {
  return apiJson<T>(path, { ...init, requireAuth: true })
}

const money = (value: number) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(value)
const date = (value: string) => new Date(`${value.slice(0, 10)}T12:00:00`).toLocaleDateString('vi-VN')
const errorText = (error: unknown) => error instanceof Error ? error.message : 'Có lỗi xảy ra, vui lòng thử lại.'

export function AdminModule() {
  const { pathname } = useLocation()
  const page = pathname.startsWith('/admin/vouchers') ? <VouchersPage />
    : pathname.startsWith('/admin/users') ? <UsersPage />
      : pathname.startsWith('/admin/reviews') ? <ReviewsPage /> : <DashboardPage />
  return <main className="admin-shell">
    <aside className="admin-sidebar"><p className="eyebrow">KHU VỰC QUẢN TRỊ</p><h1>Điều hành cửa hàng</h1><nav aria-label="Điều hướng quản trị">
      <Link className={pathname === '/admin' ? 'admin-link selected' : 'admin-link'} to="/admin">Tổng quan</Link>
      <Link className={pathname.startsWith('/admin/vouchers') ? 'admin-link selected' : 'admin-link'} to="/admin/vouchers">Voucher</Link>
      <Link className={pathname.startsWith('/admin/users') ? 'admin-link selected' : 'admin-link'} to="/admin/users">Người dùng</Link>
      <Link className={pathname.startsWith('/admin/reviews') ? 'admin-link selected' : 'admin-link'} to="/admin/reviews">Duyệt đánh giá</Link>
      <Link className="admin-link" to="/">Về cửa hàng</Link>
    </nav></aside>
    <section className="admin-content">{page}</section>
  </main>
}

function ErrorMessage({ children }: { children: string }) { return children ? <p className="admin-error" role="alert">{children}</p> : null }

function DashboardPage() {
  const [data, setData] = useState<Dashboard>()
  const [error, setError] = useState('')
  const refresh = useCallback(() => { void api<Dashboard>('/api/admin/dashboard').then(setData).catch(e => setError(errorText(e))) }, [])
  useEffect(refresh, [refresh])
  const max = Math.max(1, ...(data?.lastSevenDays.map(day => day.revenue) ?? []))
  return <>
    <div className="admin-page-heading"><div><p className="eyebrow">CỬA HÀNG</p><h2>Tổng quan</h2></div><button className="admin-button" onClick={refresh}>Tải lại</button></div>
    <ErrorMessage>{error}</ErrorMessage>
    {!data ? <p className="admin-muted">Đang tải số liệu…</p> : <>
      <div className="metric-grid">
        <Metric title="Khách đang hoạt động" value={data.activeCustomers} />
        <Metric title="Sản phẩm đang bán" value={data.activeProducts} />
        <Metric title="Tổng đơn hàng" value={data.totalOrders} />
        <Metric title="Đơn chờ xử lý" value={data.pendingOrders} />
        <Metric title="Biến thể sắp hết hàng" value={data.lowStockVariants} />
        <Metric title="Doanh thu đơn đã giao" value={money(data.deliveredRevenue)} />
      </div>
      <section className="admin-card"><div className="admin-card-heading"><div><h3>Doanh thu 7 ngày gần nhất</h3><p>Chỉ tính các đơn đã giao thành công.</p></div></div>
        <div className="revenue-chart" role="img" aria-label="Biểu đồ doanh thu theo ngày">
          {data.lastSevenDays.map(day => <div className="revenue-column" key={day.date} title={`${date(day.date)}: ${money(day.revenue)}, ${day.deliveredOrders} đơn`}>
            <span className="revenue-value">{day.revenue ? money(day.revenue) : '0₫'}</span><div className="revenue-bar-track"><div className="revenue-bar" style={{ height: `${Math.max(day.revenue ? 8 : 0, day.revenue / max * 100)}%` }} /></div><span className="revenue-date">{new Date(`${day.date}T12:00:00`).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' })}</span>
          </div>)}
        </div>
      </section>
    </>}
  </>
}

function Metric({ title, value }: { title: string; value: string | number }) {
  return <article className="metric-card"><p>{title}</p><strong>{value}</strong></article>
}

function VouchersPage() {
  const blank: VoucherForm = { code: '', discountType: 'PERCENT', discountValue: '', minOrderValue: '0', expiryDate: '', usageLimit: '100', active: true }
  const [items, setItems] = useState<Voucher[]>([])
  const [form, setForm] = useState<VoucherForm>(blank)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const load = useCallback(async () => { try { setItems(await api<Voucher[]>('/api/admin/vouchers')); setError('') } catch (e) { setError(errorText(e)) } }, [])
  useEffect(() => { void load() }, [load])
  function reset() { setEditingId(null); setForm(blank) }
  function edit(item: Voucher) { setEditingId(item.id); setForm({ code: item.code, discountType: item.discountType, discountValue: String(item.discountValue), minOrderValue: String(item.minOrderValue), expiryDate: item.expiryDate, usageLimit: String(item.usageLimit), active: item.active }) }
  async function submit(event: FormEvent) {
    event.preventDefault(); setBusy(true); setError('')
    const body = { ...form, code: form.code.trim().toUpperCase(), discountValue: Number(form.discountValue), minOrderValue: Number(form.minOrderValue), usageLimit: Number(form.usageLimit) }
    try {
      await api(editingId ? `/api/admin/vouchers/${editingId}` : '/api/admin/vouchers', { method: editingId ? 'PUT' : 'POST', body: JSON.stringify(body) })
      reset(); await load()
    } catch (e) { setError(errorText(e)) } finally { setBusy(false) }
  }
  async function remove(id: number) { if (!window.confirm('Xóa hoặc tắt voucher này? Voucher đã dùng sẽ được tắt để giữ lịch sử đơn hàng.')) return; try { await api(`/api/admin/vouchers/${id}`, { method: 'DELETE' }); await load() } catch (e) { setError(errorText(e)) } }
  return <>
    <div className="admin-page-heading"><div><p className="eyebrow">KHUYẾN MÃI</p><h2>Voucher</h2></div><button className="admin-button" onClick={load}>Tải lại</button></div>
    <ErrorMessage>{error}</ErrorMessage>
    <form className="admin-card admin-form" onSubmit={submit}><div className="admin-card-heading"><div><h3>{editingId ? 'Cập nhật voucher' : 'Tạo voucher'}</h3><p>Voucher phần trăm hoặc giảm số tiền cố định.</p></div></div>
      <div className="admin-form-grid"><label>Mã voucher<input required maxLength={60} pattern="[A-Za-z0-9_-]+" value={form.code} onChange={e => setForm({ ...form, code: e.target.value })} placeholder="WELCOME10" /></label>
        <label>Loại giảm<select value={form.discountType} onChange={e => setForm({ ...form, discountType: e.target.value as 'PERCENT' | 'FIXED' })}><option value="PERCENT">Phần trăm</option><option value="FIXED">Số tiền cố định</option></select></label>
        <label>Giá trị giảm<input required type="number" min="0.01" step="0.01" value={form.discountValue} onChange={e => setForm({ ...form, discountValue: e.target.value })} /></label>
        <label>Đơn tối thiểu (₫)<input required type="number" min="0" step="1000" value={form.minOrderValue} onChange={e => setForm({ ...form, minOrderValue: e.target.value })} /></label>
        <label>Hạn sử dụng<input required type="date" value={form.expiryDate} onChange={e => setForm({ ...form, expiryDate: e.target.value })} /></label>
        <label>Giới hạn lượt<input required type="number" min={items.find(v => v.id === editingId)?.usedCount ?? 1} step="1" value={form.usageLimit} onChange={e => setForm({ ...form, usageLimit: e.target.value })} /></label>
      </div>
      <label className="admin-checkbox"><input type="checkbox" checked={form.active} onChange={e => setForm({ ...form, active: e.target.checked })} /> Đang hoạt động</label>
      <div className="admin-form-actions"><button className="admin-button primary" disabled={busy}>{busy ? 'Đang lưu…' : editingId ? 'Lưu thay đổi' : 'Tạo voucher'}</button>{editingId && <button type="button" className="admin-button" onClick={reset}>Hủy sửa</button>}</div>
    </form>
    <section className="admin-card"><div className="admin-card-heading"><div><h3>Danh sách voucher</h3><p>{items.length} mã</p></div></div>
      <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Mã</th><th>Mức giảm</th><th>Hạn</th><th>Lượt dùng</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
        {items.map(item => <tr key={item.id}><td><strong>{item.code}</strong><small>Đơn tối thiểu {money(item.minOrderValue)}</small></td><td>{item.discountType === 'PERCENT' ? `${item.discountValue}%` : money(item.discountValue)}</td><td>{date(item.expiryDate)}</td><td>{item.usedCount} / {item.usageLimit}</td><td><span className={item.active ? 'status-pill good' : 'status-pill muted'}>{item.active ? 'Đang bật' : 'Đã tắt'}</span></td><td><div className="table-actions"><button className="admin-text-button" onClick={() => edit(item)}>Sửa</button><button className="admin-text-button danger" onClick={() => void remove(item.id)}>Xóa/tắt</button></div></td></tr>)}
        {items.length === 0 && <tr><td colSpan={6} className="admin-empty">Chưa có voucher.</td></tr>}
      </tbody></table></div>
    </section>
  </>
}

function UsersPage() {
  const [items, setItems] = useState<User[]>([])
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const [error, setError] = useState('')
  const load = useCallback(async () => { try { const query = new URLSearchParams({ page: '0', size: '100' }); if (keyword.trim()) query.set('keyword', keyword.trim()); if (status) query.set('status', status); const result = await api<{ content: User[] }>(`/api/admin/users?${query}`); setItems(result.content); setError('') } catch (e) { setError(errorText(e)) } }, [keyword, status])
  useEffect(() => { void load() }, [load])
  async function toggle(user: User) { const next = user.status === 'ACTIVE' ? 'LOCKED' : 'ACTIVE'; if (!window.confirm(`${next === 'LOCKED' ? 'Khóa' : 'Mở khóa'} tài khoản ${user.email}?`)) return; try { await api(`/api/admin/users/${user.id}/status`, { method: 'PUT', body: JSON.stringify({ status: next }) }); await load() } catch (e) { setError(errorText(e)) } }
  return <>
    <div className="admin-page-heading"><div><p className="eyebrow">TÀI KHOẢN</p><h2>Quản lý người dùng</h2></div><button className="admin-button" onClick={load}>Tải lại</button></div>
    <ErrorMessage>{error}</ErrorMessage>
    <section className="admin-card"><div className="admin-toolbar"><input aria-label="Tìm người dùng" placeholder="Tìm theo tên hoặc email" value={keyword} onChange={e => setKeyword(e.target.value)} onKeyDown={e => { if (e.key === 'Enter') void load() }} /><select aria-label="Lọc trạng thái" value={status} onChange={e => setStatus(e.target.value)}><option value="">Tất cả trạng thái</option><option value="ACTIVE">Đang hoạt động</option><option value="LOCKED">Đã khóa</option></select><button className="admin-button" onClick={load}>Tìm</button></div>
      <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Người dùng</th><th>Điện thoại</th><th>Vai trò</th><th>Ngày tạo</th><th>Trạng thái</th><th>Thao tác</th></tr></thead><tbody>
        {items.map(user => <tr key={user.id}><td><strong>{user.fullName}</strong><small>{user.email}</small></td><td>{user.phone || '—'}</td><td>{user.role}</td><td>{date(user.createdAt)}</td><td><span className={user.status === 'ACTIVE' ? 'status-pill good' : 'status-pill muted'}>{user.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}</span></td><td><button className={user.status === 'ACTIVE' ? 'admin-text-button danger' : 'admin-text-button'} onClick={() => void toggle(user)}>{user.status === 'ACTIVE' ? 'Khóa' : 'Mở khóa'}</button></td></tr>)}
        {items.length === 0 && <tr><td colSpan={6} className="admin-empty">Không tìm thấy người dùng.</td></tr>}
      </tbody></table></div>
    </section>
  </>
}

function ReviewsPage() {
  const [items, setItems] = useState<Review[]>([])
  const [status, setStatus] = useState('PENDING')
  const [error, setError] = useState('')
  const load = useCallback(async () => { try { const result = await api<{ content: Review[] }>(`/api/admin/reviews?status=${status}&page=0&size=100`); setItems(result.content); setError('') } catch (e) { setError(errorText(e)) } }, [status])
  useEffect(() => { void load() }, [load])
  async function moderate(reviewId: number, next: 'APPROVED' | 'HIDDEN') { try { await api(`/api/admin/reviews/${reviewId}/status`, { method: 'PUT', body: JSON.stringify({ status: next }) }); await load() } catch (e) { setError(errorText(e)) } }
  return <>
    <div className="admin-page-heading"><div><p className="eyebrow">NỘI DUNG</p><h2>Kiểm duyệt đánh giá</h2></div><button className="admin-button" onClick={load}>Tải lại</button></div>
    <ErrorMessage>{error}</ErrorMessage>
    <section className="admin-card"><div className="admin-toolbar"><label>Lọc trạng thái <select value={status} onChange={e => setStatus(e.target.value)}><option value="PENDING">Chờ duyệt</option><option value="APPROVED">Đã duyệt</option><option value="HIDDEN">Đã ẩn</option><option value="ALL">Tất cả</option></select></label></div>
      <div className="review-list">{items.map(review => <article className="review-card" key={review.id}><div className="review-heading"><div><strong>{review.productName}</strong><small>{review.customerName} · {review.customerEmail} · {date(review.createdAt)}</small></div><span className="status-pill muted">{review.status}</span></div><p className="review-stars" aria-label={`${review.rating} trên 5 sao`}>{'★'.repeat(review.rating)}<span>{'★'.repeat(5 - review.rating)}</span></p><p className="review-comment">{review.comment}</p>{review.imageUrl && <a href={review.imageUrl} target="_blank" rel="noreferrer">Xem ảnh khách gửi</a>}
        <div className="table-actions">{review.status !== 'APPROVED' && <button className="admin-button primary" onClick={() => void moderate(review.id, 'APPROVED')}>Duyệt</button>}{review.status !== 'HIDDEN' && <button className="admin-button" onClick={() => void moderate(review.id, 'HIDDEN')}>Ẩn</button>}</div>
      </article>)}{items.length === 0 && <p className="admin-empty">Không có đánh giá ở trạng thái này.</p>}</div>
    </section>
  </>
}
