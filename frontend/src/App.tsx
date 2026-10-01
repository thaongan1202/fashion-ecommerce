import { useState } from 'react'
import { BrowserRouter, Link, Route, Routes, useParams } from 'react-router-dom'
import { AdminOrdersPage, CartPage, CheckoutPage, OrdersPage } from './OrderPages'
import './App.css'

type ApiStatus = 'idle' | 'checking' | 'online' | 'offline'

function HomePage() {
  const [apiStatus, setApiStatus] = useState<ApiStatus>('idle')

  async function checkApi() {
    setApiStatus('checking')
    try {
      const response = await fetch('/api/health')
      if (!response.ok) throw new Error('Backend returned an error')
      setApiStatus('online')
    } catch {
      setApiStatus('offline')
    }
  }

  return (
    <main className="page-shell">
      <section className="hero-card">
        <p className="eyebrow">THỜI TRANG · HCMUTE</p>
        <h1>Mặc đẹp mỗi ngày.</h1>
        <p className="hero-copy">
          Không gian dự án đã sẵn sàng. Nhóm có thể bắt đầu phát triển danh mục,
          giỏ hàng, đặt hàng và trang quản trị theo hợp đồng API chung.
        </p>
        <div className="hero-actions">
          <Link className="button button-primary" to="/products">Xem sản phẩm</Link>
          <button className="button button-secondary" onClick={checkApi} disabled={apiStatus === 'checking'}>
            {apiStatus === 'checking' ? 'Đang kiểm tra…' : 'Kiểm tra backend'}
          </button>
        </div>
        <p className={`api-status api-status-${apiStatus}`} aria-live="polite">
          {apiStatus === 'idle' && 'Chưa kiểm tra trạng thái backend.'}
          {apiStatus === 'checking' && 'Đang kết nối tới API…'}
          {apiStatus === 'online' && 'Backend đang hoạt động.'}
          {apiStatus === 'offline' && 'Chưa kết nối được. Hãy chạy Spring Boot ở cổng 8080.'}
        </p>
      </section>
      <section className="starter-grid" aria-label="Project modules">
        <article><span>01</span><h2>Khám phá</h2><p>Danh mục, tìm kiếm và bộ lọc sản phẩm.</p></article>
        <article><span>02</span><h2>Mua sắm</h2><p>Biến thể, giỏ hàng và thanh toán COD.</p></article>
        <article><span>03</span><h2>Quản trị</h2><p>Công cụ vận hành dành cho Admin.</p></article>
      </section>
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
            <Link to="/cart">Giỏ hàng</Link>
            <Link to="/orders">Đơn hàng</Link>
            <Link to="/admin/orders">Quản trị đơn hàng</Link>
          </nav>
          <span className="header-note">Đồ án CNPM · 2026</span>
        </header>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:orderId" element={<OrderDetailRoute />} />
          <Route path="/admin/orders" element={<AdminOrdersPage />} />
          <Route path="/products/*" element={<ComingSoon title="Danh mục sản phẩm" />} />
          <Route path="/admin/*" element={<ComingSoon title="Khu vực quản trị" />} />
          <Route path="*" element={<ComingSoon title="Không tìm thấy trang" />} />
        </Routes>
        <footer className="site-footer"><span>HCMUTE · Công nghệ Phần mềm</span><span>Nhóm 6</span></footer>
      </div>
    </BrowserRouter>
  )
}

function OrderDetailRoute() {
  const { orderId } = useParams()
  return <OrdersPage orderId={orderId} />
}

export default App
