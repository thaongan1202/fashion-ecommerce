'use client';

import { useState } from 'react';
import { Button } from '@/components/ui/button';
import { orderAPI } from '@/lib/api';
import { toast } from 'sonner';

interface ReturnRequestDialogProps {
  orderId: number;
  open: boolean;
  onClose: () => void;
  onSubmitted: () => void;
}

const RETURN_REASONS = [
  { id: 'defect', label: 'Sản phẩm bị lỗi hoặc hư hỏng' },
  { id: 'wrong-variant', label: 'Sai kích cỡ hoặc màu sắc' },
  { id: 'not-as-described', label: 'Không đúng mô tả sản phẩm' },
  { id: 'missing', label: 'Giao thiếu hàng hoặc giao nhầm' },
  { id: 'quality', label: 'Chất lượng không như mong đợi' },
  { id: 'other', label: 'Khác' },
] as const;

const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

export function ReturnRequestDialog({ orderId, open, onClose, onSubmitted }: ReturnRequestDialogProps) {
  const [reasonId, setReasonId] = useState('');
  const [otherReason, setOtherReason] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);

  if (!open) return null;

  const selected = RETURN_REASONS.find((item) => item.id === reasonId);
  const isOther = reasonId === 'other';

  const submit = async () => {
    if (!selected) {
      toast.error('Vui lòng chọn lý do hoàn hàng');
      return;
    }
    const reason = isOther ? otherReason.trim() : selected.label;
    if (reason.length < 5) {
      toast.error('Lý do khác phải có ít nhất 5 ký tự');
      return;
    }
    if (!file) {
      toast.error('Vui lòng tải hình ảnh minh chứng');
      return;
    }
    if (!IMAGE_TYPES.includes(file.type)) {
      toast.error('Chỉ chấp nhận ảnh JPG, PNG, WEBP hoặc GIF');
      return;
    }

    setLoading(true);
    try {
      const response = await orderAPI.createReturn(orderId, reason, file);
      if (response.success) {
        toast.success('Đã gửi yêu cầu hoàn hàng. Admin sẽ kiểm tra và hoàn tiền vào ví nếu được duyệt.');
        setReasonId('');
        setOtherReason('');
        setFile(null);
        onSubmitted();
        onClose();
      } else {
        toast.error(response.message || 'Không thể gửi yêu cầu hoàn');
      }
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không thể gửi yêu cầu hoàn');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="w-full max-w-lg rounded-xl bg-card p-6 shadow-xl">
        <h3 className="text-lg font-semibold">Yêu cầu hoàn tiền / trả hàng</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          Chọn lý do và tải hình ảnh minh chứng. Sau khi admin xác nhận, tiền được hoàn vào số dư ví của bạn.
        </p>

        <label className="mt-4 block text-sm font-medium" htmlFor={`return-reason-${orderId}`}>
          Lý do hoàn hàng
        </label>
        <select
          id={`return-reason-${orderId}`}
          value={reasonId}
          onChange={(event) => setReasonId(event.target.value)}
          className="mt-1 w-full rounded-lg border border-input bg-background px-3 py-2 text-sm"
        >
          <option value="">-- Chọn lý do --</option>
          {RETURN_REASONS.map((item) => (
            <option key={item.id} value={item.id}>{item.label}</option>
          ))}
        </select>

        {isOther && (
          <div className="mt-3">
            <label className="block text-sm font-medium" htmlFor={`return-other-${orderId}`}>
              Lý do khác
            </label>
            <textarea
              id={`return-other-${orderId}`}
              value={otherReason}
              onChange={(event) => setOtherReason(event.target.value)}
              className="mt-1 w-full rounded-lg border border-input bg-background p-3 text-sm"
              rows={3}
              placeholder="Nhập lý do hoàn hàng"
              required
            />
          </div>
        )}

        <label className="mt-4 block text-sm font-medium" htmlFor={`return-image-${orderId}`}>
          Hình ảnh minh chứng
        </label>
        <input
          id={`return-image-${orderId}`}
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif"
          className="mt-1 block w-full text-sm"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
        />

        <div className="mt-5 flex justify-end gap-2">
          <Button variant="outline" onClick={onClose} disabled={loading}>Đóng</Button>
          <Button onClick={submit} disabled={loading}>{loading ? 'Đang gửi...' : 'Xác nhận hoàn hàng'}</Button>
        </div>
      </div>
    </div>
  );
}
