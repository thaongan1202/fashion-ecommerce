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

export function ReturnRequestDialog({ orderId, open, onClose, onSubmitted }: ReturnRequestDialogProps) {
  const [reason, setReason] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);

  if (!open) return null;

  const submit = async () => {
    if (reason.trim().length < 10) {
      toast.error('Lý do hoàn hàng phải có ít nhất 10 ký tự');
      return;
    }
    if (!file) {
      toast.error('Vui lòng tải ảnh hoặc video minh chứng');
      return;
    }
    setLoading(true);
    try {
      const response = await orderAPI.createReturn(orderId, reason.trim(), file);
      if (response.success) {
        toast.success('Đã gửi yêu cầu hoàn hàng');
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
        <h3 className="text-lg font-semibold">Yêu cầu hoàn hàng</h3>
        <p className="mt-1 text-sm text-muted-foreground">
          Hoàn trong vòng 3 ngày kể từ ngày đặt, sau khi giao thành công. Ảnh hoặc video minh chứng là bắt buộc.
        </p>
        <label className="mt-4 block text-sm font-medium">Lý do hoàn</label>
        <textarea
          value={reason}
          onChange={(event) => setReason(event.target.value)}
          className="mt-1 w-full rounded-lg border border-input bg-background p-3 text-sm"
          rows={4}
          placeholder="Mô tả lý do hoàn hàng"
        />
        <label className="mt-4 block text-sm font-medium">Ảnh hoặc video minh chứng</label>
        <input
          type="file"
          accept="image/jpeg,image/png,image/webp,image/gif,video/mp4,video/webm,video/quicktime"
          className="mt-1 block w-full text-sm"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
        />
        <div className="mt-5 flex justify-end gap-2">
          <Button variant="outline" onClick={onClose} disabled={loading}>Đóng</Button>
          <Button onClick={submit} disabled={loading}>{loading ? 'Đang gửi...' : 'Gửi yêu cầu'}</Button>
        </div>
      </div>
    </div>
  );
}
