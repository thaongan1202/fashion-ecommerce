import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import './App.css'
import { ProductListPage } from './pages/ProductListPage'
import { ProductDetailPage } from './pages/ProductDetailPage'
import { getDemoSession, getDemoUsers, loginDemo, logoutDemo } from './api/demoAuth'
import { getReviewReminders } from './api/products'
import type { DemoSession, DemoUser } from './types/auth'
import type { ReviewReminder } from './types/product'

function HomePage() {
  return (
    <main className="page-shell">
      <section className="hero-card">
        <p className="eyebrow">THỜI TRANG · HCMUTE</p>
        <h1>Mặc đẹp mỗi ngày.</h1>
        <p className="hero-copy">Khám phá những thiết kế được tuyển chọn cho phong cách mỗi ngày.</p>
        <div className="hero-actions">
          <Link className="button button-primary" to="/products">Xem sản phẩm</Link>
        </div>
      </section>
      <ProductListPage featured />
    </main>
  )
}

function ComingSoon({ title }: { title: string }) {
  return (
    <main className="page-shell">
      <section className="placeholder-card">
        <p className="eyebrow">KHU VỰC MODULE</p>
        <h1>{title}</h1>
        <p>Khu vực này dành cho thành viên phụ trách module. Hãy theo hợp đồng API và database chung.</p>
        <Link to="/">Về trang chính</Link>
      </section>
    </main>
  )
}

function App() {
  const [demoUser, setDemoUser] = useState<DemoSession | null>(null)
  const [demoUsers, setDemoUsers] = useState<DemoUser[]>([])
  const [selectedEmail, setSelectedEmail] = useState('')
  const [showLogin, setShowLogin] = useState(false)
  const [authError, setAuthError] = useState('')
  const [reviewReminders, setReviewReminders] = useState<ReviewReminder[]>([])

  useEffect(() => {
    getDemoUsers().then((users) => { setDemoUsers(users); setSelectedEmail(users[0]?.email ?? '') }).catch(() => setDemoUsers([]))
    getDemoSession().then((session) => setDemoUser(session.userId ? session : null)).catch(() => setDemoUser(null))
  }, [])

  useEffect(() => {
    if (!demoUser?.userId) { setReviewReminders([]); return }
    let active = true
    const refreshReminders = () => getReviewReminders()
      .then((reminders) => { if (active) setReviewReminders(reminders) })
      .catch(() => { if (active) setReviewReminders([]) })
    refreshReminders()
    const timer = window.setInterval(refreshReminders, 15000)
    return () => { active = false; window.clearInterval(timer) }
  }, [demoUser?.userId])

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setAuthError('')
    try { setDemoUser(await loginDemo(selectedEmail)); setShowLogin(false) }
    catch (reason) { setAuthError(reason instanceof Error ? reason.message : 'Đăng nhập demo thất bại.') }
  }

  async function handleLogout() {
    await logoutDemo(); setDemoUser(null)
  }

  return (
    <BrowserRouter>
      <div className="app-frame">
        <header className="site-header">
          <Link className="brand" to="/" aria-label="Fashion E-commerce home">
            <span className="brand-mark">F</span>
            <span>THỜI TRANG<span className="brand-light"> / STORE</span></span>
          </Link>
          <nav className="main-nav" aria-label="Main navigation">
            <Link to="/products">Sản phẩm</Link>
          </nav>
          <div className="demo-auth">
            {demoUser ? <><span>Xin chào, {demoUser.fullName}</span><button onClick={handleLogout}>Đăng xuất</button></> : <>
              <button onClick={() => { setShowLogin((value) => !value); setAuthError('') }}>Đăng nhập demo</button>
              {showLogin && <form className="demo-login-panel" onSubmit={handleLogin}>
                <strong>Chọn tài khoản kiểm thử</strong>
                <select required value={selectedEmail} onChange={(event) => setSelectedEmail(event.target.value)}>
                  {demoUsers.map((user) => <option value={user.email} key={user.id}>{user.fullName} — {user.email}</option>)}
                </select>
                <small>Tài khoản Buyer có đơn đã giao sản phẩm mẫu; tài khoản Visitor/Without Purchase chưa có đơn.</small>
                {authError && <span className="auth-error" role="alert">{authError}</span>}
                <button type="submit" disabled={!selectedEmail}>Đăng nhập</button>
              </form>}
            </>}
          </div>
          <span className="header-note">Đồ án CNPM · 2026</span>
        </header>
        {reviewReminders.length > 0 && <aside className="review-reminder" role="status" aria-live="polite">
          <strong>Đơn hàng đã giao thành công. Mời bạn đánh giá sản phẩm:</strong>
          <div>{reviewReminders.map((item) => <Link to={`/products/${item.productId}`} key={item.productId}>{item.productName}</Link>)}</div>
        </aside>}
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/products" element={<ProductListPage />} />
          <Route path="/products/:productId" element={<ProductDetailPage demoUser={demoUser} />} />
          <Route path="/products/*" element={<ProductListPage />} />
          <Route path="*" element={<ComingSoon title="Không tìm thấy trang" />} />
        </Routes>
        <footer className="site-footer"><span>HCMUTE · Công nghệ Phần mềm</span><span>Nhóm 6</span></footer>
      </div>
    </BrowserRouter>
  )
}

export default App
