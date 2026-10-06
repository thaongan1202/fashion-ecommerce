/**
 * RegisterForm component - Registration form with validation
 */

'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { User, Mail, Lock, AlertCircle, CheckCircle, Loader2, Eye, EyeOff, Phone } from 'lucide-react';
import { authAPI } from '@/lib/api';
import { useFormValidation } from '@/hooks';
import { ROUTES } from '@/lib/constants';
import { validateVietnameseMobilePhone } from '@/lib/utils/validators';

export function RegisterForm() {
  const router = useRouter();
  const { errors, validate, validatePasswordConfirmation, clearError, clearAllErrors } = useFormValidation();
  
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    email: '',
    phoneNumber: '',
    gender: '' as 'MALE' | 'FEMALE' | 'OTHER' | '',
    dateOfBirth: '',
    password: '',
    confirmPassword: '',
  });
  const [error, setError] = useState('');
  const [phoneError, setPhoneError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [otpStep, setOtpStep] = useState(false);
  const [otp, setOtp] = useState('');
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [attemptsLeft, setAttemptsLeft] = useState(5);
  const [locked, setLocked] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setError('');
    if (name === 'phoneNumber') setPhoneError('');
    clearError(name);
  };

  const validateForm = (): boolean => {
    clearAllErrors();
    let isValid = true;

    // Validate all required fields
    if (!validate('username', formData.username, 'username')) isValid = false;
    if (!validate('fullName', formData.fullName, 'required', { fieldName: 'Họ và tên' })) isValid = false;
    if (!validate('email', formData.email, 'email')) isValid = false;
    const phoneValidation = validateVietnameseMobilePhone(formData.phoneNumber);
    setPhoneError(phoneValidation.error ?? '');
    if (!phoneValidation.isValid) isValid = false;
    if (!validate('password', formData.password, 'password')) isValid = false;
    if (!validatePasswordConfirmation(formData.password, formData.confirmPassword)) isValid = false;

    if (!agreeTerms) {
      setError('Bạn cần đồng ý với điều khoản dịch vụ');
      return false;
    }

    return isValid;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      const response = await authAPI.register({
        username: formData.username,
        fullName: formData.fullName,
        email: formData.email,
        phoneNumber: formData.phoneNumber.trim() || undefined,
        gender: formData.gender || undefined,
        dateOfBirth: formData.dateOfBirth || undefined,
        password: formData.password,
        confirmPassword: formData.confirmPassword,
      });

      if (response.success && response.data) {
        setOtpStep(true);
        setSecondsLeft(response.data.expiresInSeconds || 60);
        setAttemptsLeft(response.data.remainingAttempts ?? 5);
        setLocked(false);
        setSuccess(`Mã OTP đã gửi tới ${response.data.email}. Mã hết hạn sau 60 giây.`);
      } else {
        setError(response.message || 'Đăng ký thất bại');
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'Có lỗi xảy ra khi đăng ký';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!otpStep || secondsLeft <= 0) return;
    const timer = window.setInterval(() => {
      setSecondsLeft((current) => (current > 0 ? current - 1 : 0));
    }, 1000);
    return () => window.clearInterval(timer);
  }, [otpStep, secondsLeft > 0]);

  const handleVerifyOtp = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    if (!/^\d{6}$/.test(otp)) {
      setError('OTP phải gồm 6 chữ số');
      return;
    }
    setLoading(true);
    try {
      const response = await authAPI.verifyRegistrationOtp({
        email: formData.email,
        otp,
      });
      if (response.success) {
        setSuccess('Xác thực email thành công. Đang chuyển đến trang đăng nhập...');
        setTimeout(() => router.push(ROUTES.LOGIN), 1500);
      } else {
        setError(response.message || 'OTP không hợp lệ');
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : 'OTP không hợp lệ';
      setError(errorMessage);
      if (errorMessage.includes('5 lần')) {
        setLocked(true);
        setAttemptsLeft(0);
      }
    } finally {
      setLoading(false);
    }
  };

  const handleResendOtp = async () => {
    setError('');
    setLoading(true);
    try {
      const response = await authAPI.resendRegistrationOtp(formData.email);
      if (response.success && response.data) {
        setSecondsLeft(response.data.expiresInSeconds || 60);
        setAttemptsLeft(response.data.remainingAttempts ?? 5);
        setLocked(false);
        setOtp('');
        setSuccess('Đã gửi OTP mới. Mã có hiệu lực 60 giây.');
      }
    } catch (err: unknown) {
      setError(err instanceof Error ? err.message : 'Không thể cấp lại OTP');
    } finally {
      setLoading(false);
    }
  };

  if (otpStep) {
    return (
      <form onSubmit={handleVerifyOtp} className="p-6 space-y-4">
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-3 flex gap-3">
            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}
        {success && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-3 flex gap-3">
            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0" />
            <p className="text-sm text-green-700">{success}</p>
          </div>
        )}
        <div>
          <p className="text-sm text-muted-foreground">
            Nhập mã OTP đã gửi tới <strong>{formData.email}</strong>. Mã hết hạn sau{' '}
            <strong>{secondsLeft}s</strong>. Còn {attemptsLeft} lần nhập.
          </p>
          <input
            inputMode="numeric"
            maxLength={6}
            value={otp}
            onChange={(event) => setOtp(event.target.value.replace(/\D/g, '').slice(0, 6))}
            className="mt-3 w-full rounded-lg border border-input bg-background px-4 py-3 text-center text-2xl tracking-[0.4em]"
            placeholder="000000"
            disabled={loading || locked}
          />
        </div>
        <Button type="submit" className="w-full" disabled={loading || locked || secondsLeft <= 0}>
          {loading ? <Loader2 className="w-4 h-4 animate-spin" /> : 'Xác nhận OTP'}
        </Button>
        <Button
          type="button"
          variant="outline"
          className="w-full"
          disabled={loading || secondsLeft > 0}
          onClick={handleResendOtp}
        >
          {secondsLeft > 0 ? `Yêu cầu OTP mới sau ${secondsLeft}s` : 'Gửi lại OTP'}
        </Button>
      </form>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="p-6 space-y-4">
      {/* Error Message */}
      {error && (
        <div className="bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg p-3 flex gap-3">
          <AlertCircle className="w-5 h-5 text-red-600 dark:text-red-400 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-red-700 dark:text-red-300">{error}</p>
        </div>
      )}

      {/* Success Message */}
      {success && (
        <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-3 flex gap-3">
          <CheckCircle className="w-5 h-5 text-green-600 dark:text-green-400 flex-shrink-0 mt-0.5" />
          <p className="text-sm text-green-700 dark:text-green-300">{success}</p>
        </div>
      )}

      {/* Username Field */}
      <div>
        <label htmlFor="username" className="block text-sm font-medium text-foreground mb-1.5">
          Tên đăng nhập <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="text"
            id="username"
            name="username"
            value={formData.username}
            onChange={handleChange}
            placeholder="nguoidung123"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.username ? (
          <p className="text-xs text-red-600 mt-1">{errors.username}</p>
        ) : (
          <p className="text-xs text-muted-foreground mt-1">Tối thiểu 3 ký tự</p>
        )}
      </div>

      {/* Full Name Field */}
      <div>
        <label htmlFor="fullName" className="block text-sm font-medium text-foreground mb-1.5">
          Họ và tên <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="text"
            id="fullName"
            name="fullName"
            value={formData.fullName}
            onChange={handleChange}
            placeholder="Nguyễn Văn A"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.fullName && (
          <p className="text-xs text-red-600 mt-1">{errors.fullName}</p>
        )}
      </div>

      {/* Email Field */}
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-foreground mb-1.5">
          Email <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="you@example.com"
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {errors.email && (
          <p className="text-xs text-red-600 mt-1">{errors.email}</p>
        )}
      </div>

      {/* Phone Number Field */}
      <div>
        <label htmlFor="phoneNumber" className="block text-sm font-medium text-foreground mb-1.5">
          Số điện thoại
        </label>
        <div className="relative">
          <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type="tel"
            id="phoneNumber"
            name="phoneNumber"
            value={formData.phoneNumber}
            onChange={handleChange}
            placeholder="0912345678"
            aria-invalid={!!phoneError}
            className="w-full pl-10 pr-4 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
        {phoneError && (
          <p className="text-xs text-red-600 mt-1">{phoneError}</p>
        )}
      </div>

      {/* Gender and Date of Birth - Grid Layout */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Gender Field */}
        <div>
          <label htmlFor="gender" className="block text-sm font-medium text-foreground mb-1.5">
            Giới tính
          </label>
          <select
            id="gender"
            name="gender"
            value={formData.gender}
            onChange={handleChange}
            className="w-full px-4 py-2.5 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          >
            <option value="">Chọn giới tính</option>
            <option value="MALE">Nam</option>
            <option value="FEMALE">Nữ</option>
            <option value="OTHER">Khác</option>
          </select>
        </div>

        {/* Date of Birth Field */}
        <div>
          <label htmlFor="dateOfBirth" className="block text-sm font-medium text-foreground mb-1.5">
            Ngày sinh
          </label>
          <input
            type="date"
            id="dateOfBirth"
            name="dateOfBirth"
            value={formData.dateOfBirth}
            onChange={handleChange}
            max={new Date().toISOString().split('T')[0]}
            className="w-full px-4 py-2.5 border border-input rounded-lg bg-background text-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
        </div>
      </div>

      {/* Password Field */}
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-foreground mb-1.5">
          Mật khẩu <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type={showPassword ? 'text' : 'password'}
            id="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="••••••••"
            className="w-full pl-10 pr-12 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
          <button
            type="button"
            onClick={() => setShowPassword(!showPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            disabled={loading}
          >
            {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
        {errors.password ? (
          <p className="text-xs text-red-600 mt-1">{errors.password}</p>
        ) : (
          <p className="text-xs text-muted-foreground mt-1">
            Tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường và số
          </p>
        )}
      </div>

      {/* Confirm Password Field */}
      <div>
        <label htmlFor="confirmPassword" className="block text-sm font-medium text-foreground mb-1.5">
          Xác nhận mật khẩu <span className="text-destructive">*</span>
        </label>
        <div className="relative">
          <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
          <input
            type={showConfirmPassword ? 'text' : 'password'}
            id="confirmPassword"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="••••••••"
            className="w-full pl-10 pr-12 py-2.5 border border-input rounded-lg bg-background text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent transition-all"
            disabled={loading}
          />
          <button
            type="button"
            onClick={() => setShowConfirmPassword(!showConfirmPassword)}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground transition-colors"
            disabled={loading}
          >
            {showConfirmPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
        {errors.confirmPassword && (
          <p className="text-xs text-red-600 mt-1">{errors.confirmPassword}</p>
        )}
      </div>

      {/* Terms & Conditions */}
      <label className="flex items-start gap-2 cursor-pointer">
        <input
          type="checkbox"
          checked={agreeTerms}
          onChange={(e) => setAgreeTerms(e.target.checked)}
          className="w-4 h-4 rounded border-input text-primary focus:ring-primary mt-0.5"
          disabled={loading}
        />
        <span className="text-sm text-muted-foreground">
          Tôi đồng ý với{' '}
          <Link href="#" className="text-primary hover:underline font-medium">
            Điều khoản dịch vụ
          </Link>
          {' '}và{' '}
          <Link href="#" className="text-primary hover:underline font-medium">
            Chính sách bảo mật
          </Link>
        </span>
      </label>

      {/* Submit Button */}
      <Button
        type="submit"
        disabled={loading}
        className="w-full py-3 text-base font-semibold"
        size="lg"
      >
        {loading ? (
          <>
            <Loader2 className="w-5 h-5 animate-spin mr-2" />
            Đang đăng ký...
          </>
        ) : (
          'Đăng ký'
        )}
      </Button>
    </form>
  );
}
