import { useEffect, useState } from 'react'
import type { FormEvent, InputHTMLAttributes } from 'react'
import { BrowserRouter, Link, Route, Routes } from 'react-router-dom'
import './App.css'

type ApiStatus = 'idle' | 'checking' | 'online' | 'offline'

type RegistrationForm = {
  fullName: string
  phone: string
  email: string
  password: string
}

type UserAddress = {
  id: number
  recipientName: string
  phone: string
  addressLine: string
  defaultAddress: boolean
}

type AdministrativeWard = {
  id: string
  name: { local: string }
}

type AdministrativeProvince = {
  id: string
  name: { local: string }
  ward: AdministrativeWard[]
}

type LoginApiResponse = {
  accessToken: string
  expiresIn: number
  user: {
    id: number
    fullName: string
    email: string
    role: string
  }
}

type PasswordFieldProps = Omit<InputHTMLAttributes<HTMLInputElement>, 'type'> & {
  label: string
}

function PasswordField({ label, ...inputProps }: PasswordFieldProps) {
  const [visible, setVisible] = useState(false)

  return (
    <label>
      {label}
      <span className="auth-password-control">
        <input {...inputProps} type={visible ? 'text' : 'password'} />
        <button
          className="auth-password-toggle"
          type="button"
          aria-label={visible ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
          aria-pressed={visible}
          onClick={() => setVisible((current) => !current)}
        >
          {visible ? 'Ẩn' : 'Hiện'}
        </button>
      </span>
    </label>
  )
}

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

function RegisterPage() {
  const [form, setForm] = useState<RegistrationForm>({
    fullName: '',
    phone: '',
    email: '',
    password: '',
  })
  const [confirmPassword, setConfirmPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [step, setStep] = useState<'details' | 'verify' | 'complete'>('details')
  const [secondsLeft, setSecondsLeft] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (step !== 'verify' || secondsLeft <= 0) return
    const timer = window.setTimeout(() => setSecondsLeft((seconds) => seconds - 1), 1000)
    return () => window.clearTimeout(timer)
  }, [step, secondsLeft])

  function updateForm(field: keyof RegistrationForm, value: string) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  async function sendOtp(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()
    setError('')

    if (form.password !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.')
      return
    }
    if (!/^0\d{9}$/.test(form.phone)) {
      setError('Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0.')
      return
    }
    if (!/^[^@\s]+@gmail\.com$/i.test(form.email)) {
      setError('Vui lòng nhập địa chỉ Gmail có dạng @gmail.com.')
      return
    }
    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).{8,32}$/.test(form.password)) {
      setError('Mật khẩu cần 8–32 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.')
      return
    }

    setBusy(true)
    try {
      const response = await fetch('/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: form.fullName,
          email: form.email,
          password: form.password,
          phone: form.phone,
        }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) throw new Error(result.detail ?? result.message ?? 'Không gửi được OTP. Vui lòng thử lại.')
      setStep('verify')
      setOtp('')
      setSecondsLeft(result.expiresInSeconds ?? 60)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không gửi được OTP. Vui lòng thử lại.')
    } finally {
      setBusy(false)
    }
  }

  async function verifyOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setBusy(true)
    try {
      const response = await fetch('/api/auth/verify-email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, otp }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) throw new Error(result.detail ?? result.message ?? 'OTP không hợp lệ hoặc đã hết hạn.')
      setStep('complete')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không xác minh được OTP.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell auth-page">
      <section className="auth-card">
        <p className="eyebrow">TÀI KHOẢN KHÁCH HÀNG</p>
        {step === 'complete' ? (
          <div className="auth-success" role="status">
            <h1>Đăng ký thành công</h1>
            <p>Tài khoản của bạn đã được xác minh và tạo thành công.</p>
            <Link className="button button-primary" to="/">Về trang chính</Link>
          </div>
        ) : step === 'details' ? (
          <>
            <h1>Tạo tài khoản</h1>
            <p className="auth-intro">Nhập thông tin để nhận mã xác minh qua Gmail.</p>
            <form className="auth-form" onSubmit={sendOtp}>
              <label>Họ tên
                <input autoComplete="name" required maxLength={150} value={form.fullName} onChange={(event) => updateForm('fullName', event.target.value)} />
              </label>
              <label>Số điện thoại
                <input autoComplete="tel" inputMode="numeric" required maxLength={10} pattern="0[0-9]{9}" title="Nhập 10 chữ số, bắt đầu bằng 0" value={form.phone} onChange={(event) => updateForm('phone', event.target.value.replace(/\D/g, '').slice(0, 10))} />
              </label>
              <label>Gmail
                <input autoComplete="email" type="email" required maxLength={254} value={form.email} onChange={(event) => updateForm('email', event.target.value)} placeholder="ban@gmail.com" />
              </label>
              <label>Mật khẩu
                <input autoComplete="new-password" type="password" required minLength={8} maxLength={32} value={form.password} onChange={(event) => updateForm('password', event.target.value)} />
              </label>
              <label>Nhập lại mật khẩu
                <input
                  autoComplete="new-password"
                  type="password"
                  required
                  minLength={8}
                  maxLength={32}
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                />
              </label>
              <p className="field-hint">8–32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.</p>
              {error && <p className="auth-error" role="alert">{error}</p>}
              <button className="button button-primary auth-submit" type="submit" disabled={busy}>
                {busy ? 'Đang gửi…' : 'Gửi mã OTP'}
              </button>
            </form>
          </>
        ) : (
          <>
            <h1>Xác minh Gmail</h1>
            <p className="auth-intro">Nhập mã 6 chữ số đã gửi đến <strong>{form.email}</strong>. Mã có hiệu lực trong 60 giây.</p>
            <form className="auth-form" onSubmit={verifyOtp}>
              <label>Mã OTP
                <input autoComplete="one-time-code" inputMode="numeric" required pattern="[0-9]{6}" maxLength={6} value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))} />
              </label>
              <p className="field-hint" aria-live="polite">
                {secondsLeft > 0 ? `Bạn có thể yêu cầu mã mới sau ${secondsLeft} giây.` : 'Mã đã hết hạn. Bạn có thể yêu cầu gửi mã mới.'}
              </p>
              {error && <p className="auth-error" role="alert">{error}</p>}
              <button className="button button-primary auth-submit" type="submit" disabled={busy || secondsLeft === 0}>
                {busy ? 'Đang xác minh…' : 'Xác nhận OTP'}
              </button>
              <button className="button button-secondary auth-submit" type="button" disabled={busy || secondsLeft > 0} onClick={() => void sendOtp()}>
                {busy ? 'Đang gửi…' : 'Gửi lại OTP'}
              </button>
              <button className="text-button" type="button" onClick={() => { setStep('details'); setError('') }}>Sửa thông tin đăng ký</button>
            </form>
          </>
        )}
      </section>
    </main>
  )
}

function LoginPage() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')
    setBusy(true)

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      const result = await response.json() as LoginApiResponse

      if (!response.ok) {
        throw new Error('Email hoặc mật khẩu không chính xác.')
      }

      localStorage.setItem('fashionAccessToken', result.accessToken)
      localStorage.setItem('fashionUser', JSON.stringify(result.user))
      window.location.href = '/account'
    } catch (loginError) {
      setError(loginError instanceof Error ? loginError.message : 'Đăng nhập thất bại.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell auth-page">
      <section className="auth-card">
        <p className="eyebrow">TÀI KHOẢN KHÁCH HÀNG</p>
        <h1>Đăng nhập</h1>
        <form className="auth-form" onSubmit={handleLogin}>
          <label>Gmail
            <input
              type="email"
              required
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="ban@gmail.com"
            />
          </label>
          <PasswordField
            label="Mật khẩu"
            autoComplete="current-password"
            required
            value={password}
            onChange={(event) => setPassword(event.target.value)}
          />
          <Link to="/forgot-password">Quên mật khẩu?</Link>
          {error && <p className="auth-error" role="alert">{error}</p>}
          <button className="button button-primary auth-submit" type="submit" disabled={busy}>
            {busy ? 'Đang đăng nhập…' : 'Đăng nhập'}
          </button>
        </form>
      </section>
    </main>
  )
}

function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [step, setStep] = useState<'email' | 'reset' | 'done'>('email')
  const [secondsLeft, setSecondsLeft] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (step !== 'reset' || secondsLeft <= 0) return
    const timer = window.setTimeout(() => setSecondsLeft((seconds) => seconds - 1), 1000)
    return () => window.clearTimeout(timer)
  }, [step, secondsLeft])

  async function requestOtp(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()
    setError('')
    setBusy(true)

    try {
      const response = await fetch('/api/auth/password-reset/request', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(result.detail ?? result.message ?? 'Không gửi được OTP.')
      }

      setStep('reset')
      setSecondsLeft(result.expiresInSeconds ?? 60)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không gửi được OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function submitReset(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,32}$/.test(newPassword)) {
      setError('Mật khẩu cần 8–32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.')
      return
    }

    setBusy(true)
    try {
      const response = await fetch('/api/auth/password-reset/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, otp, newPassword }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(result.detail ?? result.message ?? 'Không đặt lại được mật khẩu.')
      }
      setStep('done')
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không đặt lại được mật khẩu.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell auth-page">
      <section className="auth-card">
        <p className="eyebrow">TÀI KHOẢN KHÁCH HÀNG</p>

        {step === 'done' ? (
          <div className="auth-success">
            <h1>Đổi mật khẩu thành công</h1>
            <p>Bạn có thể đăng nhập bằng mật khẩu mới.</p>
            <Link className="button button-primary" to="/login">Đăng nhập</Link>
          </div>
        ) : step === 'email' ? (
          <>
            <h1>Quên mật khẩu</h1>
            <p className="auth-intro">Nhập Gmail đã đăng ký để nhận mã OTP.</p>
            <form className="auth-form" onSubmit={requestOtp}>
              <label>Gmail
                <input
                  type="email"
                  required
                  value={email}
                  onChange={(event) => setEmail(event.target.value)}
                  placeholder="ban@gmail.com"
                />
              </label>
              {error && <p className="auth-error" role="alert">{error}</p>}
              <button className="button button-primary auth-submit" type="submit" disabled={busy}>
                {busy ? 'Đang gửi…' : 'Gửi mã OTP'}
              </button>
            </form>
          </>
        ) : (
          <>
            <h1>Tạo mật khẩu mới</h1>
            <p className="auth-intro">Nhập OTP gửi đến {email}. Mã có hiệu lực 60 giây.</p>
            <form className="auth-form" onSubmit={submitReset}>
              <label>Mã OTP
                <input
                  inputMode="numeric"
                  required
                  pattern="[0-9]{6}"
                  maxLength={6}
                  value={otp}
                  onChange={(event) => setOtp(event.target.value.replace(/\\D/g, '').slice(0, 6))}
                />
              </label>
              <PasswordField
                label="Mật khẩu mới"
                autoComplete="new-password"
                required
                minLength={8}
                maxLength={32}
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
              />
              <PasswordField
                label="Nhập lại mật khẩu mới"
                autoComplete="new-password"
                required
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
              />
              {error && <p className="auth-error" role="alert">{error}</p>}
              <button className="button button-primary auth-submit" type="submit" disabled={busy}>
                {busy ? 'Đang xác nhận…' : 'Xác nhận OTP và đổi mật khẩu'}
              </button>
              <button
                className="button button-secondary auth-submit"
                type="button"
                disabled={busy || secondsLeft > 0}
                onClick={() => void requestOtp()}
              >
                {secondsLeft > 0 ? `Gửi lại sau ${secondsLeft} giây` : 'Gửi lại OTP'}
              </button>
            </form>
          </>
        )}
      </section>
    </main>
  )
}

function AccountPage() {
  const [step, setStep] = useState<'details' | 'otp' | 'done'>('details')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [otp, setOtp] = useState('')
  const [secondsLeft, setSecondsLeft] = useState(0)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [addresses, setAddresses] = useState<UserAddress[]>([])
  const [addressError, setAddressError] = useState('')
  const [provinces, setProvinces] = useState<AdministrativeProvince[]>([])
  const [selectedProvinceId, setSelectedProvinceId] = useState('')
  const [selectedWardId, setSelectedWardId] = useState('')
  const [streetAddress, setStreetAddress] = useState('')
  const [recipientName, setRecipientName] = useState('')
  const [addressPhone, setAddressPhone] = useState('')
  const [addressBusy, setAddressBusy] = useState(false)
  const [fullName] = useState(() => {
    const user = localStorage.getItem('fashionUser')
    return user ? (JSON.parse(user) as { fullName?: string }).fullName ?? '' : ''
  })

  useEffect(() => {
    const token = localStorage.getItem('fashionAccessToken')
    if (!token) {
      window.location.href = '/login'
      return
    }

    let cancelled = false

    fetch('/api/users/me/addresses', {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then(async (response) => {
        const result = await response.json().catch(() => [])
        if (!response.ok) throw new Error(result.detail ?? 'Không tải được địa chỉ.')
        return result as UserAddress[]
      })
      .then((result) => {
        if (!cancelled) setAddresses(result)
      })
      .catch((loadError) => {
        if (!cancelled) {
          setAddressError(
            loadError instanceof Error ? loadError.message : 'Không tải được địa chỉ.',
          )
        }
      })

    return () => {
      cancelled = true
    }
  }, [])
  useEffect(() => {
    let cancelled = false

    fetch('/vietnamAdministrativeUnits.json')
      .then((response) => {
        if (!response.ok) throw new Error('Không tải được dữ liệu tỉnh thành')
        return response.json()
      })
      .then((result: { data: AdministrativeProvince[] }) => {
        if (!cancelled) setProvinces(result.data)
      })
      .catch(() => {
        if (!cancelled) setAddressError('Không tải được danh sách tỉnh/thành.')
      })

    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (step !== 'otp' || secondsLeft <= 0) return
    const timer = window.setTimeout(() => setSecondsLeft((seconds) => seconds - 1), 1000)
    return () => window.clearTimeout(timer)
  }, [step, secondsLeft])

  function logout() {
    localStorage.removeItem('fashionAccessToken')
    localStorage.removeItem('fashionUser')
    window.location.href = '/login'
  }

  async function handleAddAddress(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setAddressError('')

    const province = provinces.find((item) => item.id === selectedProvinceId)
    const ward = province?.ward.find((item) => item.id === selectedWardId)

    if (!province || !ward) {
      setAddressError('Vui lòng chọn tỉnh/thành phố và phường/xã.')
      return
    }

    setAddressBusy(true)

    try {
      const token = localStorage.getItem('fashionAccessToken')
      if (!token) throw new Error('Vui lòng đăng nhập lại.')

      const response = await fetch('/api/users/me/addresses', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          recipientName,
          phone: addressPhone,
          addressLine: [streetAddress.trim(), ward.name.local, province.name.local]
            .filter(Boolean)
            .join(', '),
          defaultAddress: addresses.length === 0,
        }),
      })

      const result = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(result.detail ?? result.message ?? 'Không thêm được địa chỉ.')
      }

      setAddresses((current) => [...current, result as UserAddress])
      setRecipientName('')
      setAddressPhone('')
      setStreetAddress('')
      setSelectedProvinceId('')
      setSelectedWardId('')
    } catch (error) {
      setAddressError(error instanceof Error ? error.message : 'Không thêm được địa chỉ.')
    } finally {
      setAddressBusy(false)
    }
  }

  async function requestOtp(event?: FormEvent<HTMLFormElement>) {
    event?.preventDefault()
    setError('')

    if (!/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9\s]).{8,32}$/.test(newPassword)) {
      setError('Mật khẩu cần 8–32 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt.')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('Mật khẩu xác nhận không khớp.')
      return
    }

    const token = localStorage.getItem('fashionAccessToken')
    if (!token) {
      setError('Phiên đăng nhập không còn. Vui lòng đăng nhập lại.')
      return
    }

    setBusy(true)
    try {
      const response = await fetch('/api/auth/change-password/request-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ currentPassword, newPassword }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(result.detail ?? result.message ?? 'Không gửi được OTP.')
      }
      setStep('otp')
      setSecondsLeft(result.expiresInSeconds ?? 60)
    } catch (requestError) {
      setError(requestError instanceof Error ? requestError.message : 'Không gửi được OTP.')
    } finally {
      setBusy(false)
    }
  }

  async function verifyOtp(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    const token = localStorage.getItem('fashionAccessToken')
    if (!token) {
      setError('Phiên đăng nhập không còn. Vui lòng đăng nhập lại.')
      return
    }

    setBusy(true)
    try {
      const response = await fetch('/api/auth/change-password/verify-otp', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ otp }),
      })
      const result = await response.json().catch(() => ({}))
      if (!response.ok) {
        throw new Error(result.detail ?? result.message ?? 'OTP không hợp lệ hoặc đã hết hạn.')
      }
      setStep('done')
    } catch (verifyError) {
      setError(verifyError instanceof Error ? verifyError.message : 'Không xác nhận được OTP.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="page-shell auth-page">
      <section className="auth-card">
        <p className="eyebrow">TÀI KHOẢN KHÁCH HÀNG</p>
        <h1>Tài khoản của tôi</h1>
        {fullName && <p>Xin chào, {fullName}</p>}
        <button className="button button-secondary" type="button" onClick={logout}>
          Đăng xuất
        </button>

        <section className="auth-form">
          <h2>Địa chỉ giao hàng</h2>

          {addressError && (
            <p className="auth-error" role="alert">{addressError}</p>
          )}

          {addresses.length === 0 && !addressError && (
            <p>Bạn chưa có địa chỉ giao hàng.</p>
          )}

          <form className="auth-form" onSubmit={handleAddAddress}>
            <label>
              Tên người nhận
              <input
                required
                value={recipientName}
                onChange={(event) => setRecipientName(event.target.value)}
              />
            </label>

            <label>
              Số điện thoại
              <input
                required
                value={addressPhone}
                onChange={(event) => setAddressPhone(event.target.value)}
              />
            </label>

            <label>
              Số nhà, tên đường
              <input
                required
                value={streetAddress}
                onChange={(event) => setStreetAddress(event.target.value)}
              />
            </label>

            <label>
              Tỉnh/thành phố
              <select
                required
                value={selectedProvinceId}
                onChange={(event) => {
                  setSelectedProvinceId(event.target.value)
                  setSelectedWardId('')
                }}
              >
                <option value="">Chọn tỉnh/thành phố</option>
                {provinces.map((province) => (
                  <option key={province.id} value={province.id}>
                    {province.name.local}
                  </option>
                ))}
              </select>
            </label>

            <label>
              Phường/xã
              <select
                required
                value={selectedWardId}
                disabled={!selectedProvinceId}
                onChange={(event) => setSelectedWardId(event.target.value)}
              >
                <option value="">Chọn phường/xã</option>
                {(provinces.find((province) => province.id === selectedProvinceId)?.ward ?? []).map(
                  (ward) => (
                    <option key={ward.id} value={ward.id}>
                      {ward.name.local}
                    </option>
                  ),
                )}
              </select>
            </label>

            <button type="submit" disabled={addressBusy}>
              {addressBusy ? 'Đang lưu...' : 'Thêm địa chỉ'}
            </button>
          </form>

          {addresses.map((address) => (
            <p key={address.id}>
              {address.recipientName} · {address.phone} · {address.addressLine}
              {address.defaultAddress && ' · Mặc định'}
            </p>
          ))}
        </section>

        {step === 'done' ? (
          <div className="auth-success">
            <h2>Đổi mật khẩu thành công</h2>
          </div>
        ) : step === 'otp' ? (
          <form className="auth-form" onSubmit={verifyOtp}>
            <h2>Xác nhận đổi mật khẩu</h2>
            <p className="auth-intro">Nhập OTP gửi đến Gmail đăng ký. Mã có hiệu lực 60 giây.</p>
            <label>Mã OTP
              <input
                inputMode="numeric"
                required
                pattern="[0-9]{6}"
                maxLength={6}
                value={otp}
                onChange={(event) => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))}
              />
            </label>
            {error && <p className="auth-error" role="alert">{error}</p>}
            <button className="button button-primary auth-submit" type="submit" disabled={busy}>
              {busy ? 'Đang xác nhận…' : 'Xác nhận OTP'}
            </button>
            <button
              className="button button-secondary auth-submit"
              type="button"
              disabled={busy || secondsLeft > 0}
              onClick={() => void requestOtp()}
            >
              {secondsLeft > 0 ? `Gửi lại sau ${secondsLeft} giây` : 'Gửi lại OTP'}
            </button>
          </form>
        ) : (
          <form className="auth-form" onSubmit={requestOtp}>
            <h2>Đổi mật khẩu</h2>
            <PasswordField
              label="Mật khẩu hiện tại"
              autoComplete="current-password"
              required
              value={currentPassword}
              onChange={(event) => setCurrentPassword(event.target.value)}
            />
            <PasswordField
              label="Mật khẩu mới"
              autoComplete="new-password"
              required
              minLength={8}
              maxLength={32}
              value={newPassword}
              onChange={(event) => setNewPassword(event.target.value)}
            />
            <PasswordField
              label="Nhập lại mật khẩu mới"
              autoComplete="new-password"
              required
              value={confirmPassword}
              onChange={(event) => setConfirmPassword(event.target.value)}
            />
            {error && <p className="auth-error" role="alert">{error}</p>}
            <button className="button button-primary auth-submit" type="submit" disabled={busy}>
              {busy ? 'Đang gửi OTP…' : 'Gửi mã OTP đổi mật khẩu'}
            </button>
          </form>
        )}
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
            <Link to="/register">Đăng ký</Link>
            <Link to="/login">Đăng nhập</Link>
            <Link to="/admin">Quản trị</Link>

          </nav>
          <span className="header-note">Đồ án CNPM · 2026</span>
        </header>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/account" element={<AccountPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/products/*" element={<ComingSoon title="Danh mục sản phẩm" />} />
          <Route path="/admin/*" element={<ComingSoon title="Khu vực quản trị" />} />
          <Route path="*" element={<ComingSoon title="Không tìm thấy trang" />} />
        </Routes>
        <footer className="site-footer"><span>HCMUTE · Công nghệ Phần mềm</span><span>Nhóm 6</span></footer>
      </div>
    </BrowserRouter>
  )
}

export default App
