'use client';

import { useCallback, useEffect, useState } from 'react';
import { returnAPI } from '@/lib/api';
import { Button } from '@/components/ui/button';
import { formatVietnamDateTime } from '@/lib/datetime';
import type { OrderReturn, ReturnStatistics } from '@/types';
import { toast } from 'sonner';

const FILTERS = [
  { id: 'ALL', label: 'Tất cả' },
  { id: 'PENDING', label: 'Chờ xử lý' },
  { id: 'APPROVED', label: 'Đã xác nhận hoàn' },
  { id: 'REJECTED', label: 'Đã từ chối' },
] as const;

function mediaUrl(path?: string) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  const api = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';
  return `${api.replace(/\/api\/v1\/?$/, '')}${path}`;
}

function isVideo(url: string) {
  return /\.(mp4|webm|mov)(\?|$)/i.test(url);
}

export function ReturnManagement() {
  const [filter, setFilter] = useState<(typeof FILTERS)[number]['id']>('ALL');
  const [items, setItems] = useState<OrderReturn[]>([]);
  const [stats, setStats] = useState<ReturnStatistics | null>(null);
  const [loading, setLoading] = useState(true);
  const [note, setNote] = useState('');
  const [actingId, setActingId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const [listResponse, statsResponse] = await Promise.all([
        returnAPI.list(filter),
        returnAPI.statistics(),
      ]);
      setItems(listResponse.data?.content || []);
      setStats(statsResponse.data || null);
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không tải được danh sách hoàn hàng');
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => {
    load();
  }, [load]);

  const approve = async (id: number) => {
    setActingId(id);
    try {
      await returnAPI.approve(id);
      toast.success('Đã xác nhận hoàn và hoàn tiền vào ví');
      await load();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không thể xác nhận hoàn');
    } finally {
      setActingId(null);
    }
  };

  const reject = async (id: number) => {
    if (note.trim().length < 5) {
      toast.error('Nhập lý do từ chối trước khi từ chối yêu cầu');
      return;
    }
    setActingId(id);
    try {
      await returnAPI.reject(id, note.trim());
      toast.success('Đã từ chối yêu cầu hoàn');
      setNote('');
      await load();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không thể từ chối');
    } finally {
      setActingId(null);
    }
  };

  const money = (value?: number) => `${Number(value || 0).toLocaleString('vi-VN')}₫`;

  return (
    <div className="space-y-4">
      {stats && (
        <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
          <Stat label="Đơn đã xác nhận hoàn" value={String(stats.refundedOrderCount)} />
          <Stat label="Đã từ chối" value={String(stats.rejectedCount)} />
          <Stat label="Tiền đã hoàn" value={money(stats.totalRefundAmount)} />
          <Stat label="Doanh thu sau hoàn" value={money(stats.revenueAfterRefund)} />
        </div>
      )}

      <div className="flex flex-wrap gap-2">
        {FILTERS.map((item) => (
          <Button
            key={item.id}
            size="sm"
            variant={filter === item.id ? 'default' : 'outline'}
            onClick={() => setFilter(item.id)}
          >
            {item.label}
          </Button>
        ))}
      </div>

      <div className="rounded-xl border bg-card p-4">
        <label className="text-sm font-medium">Lý do từ chối (dùng khi bấm Từ chối)</label>
        <textarea
          value={note}
          onChange={(event) => setNote(event.target.value)}
          className="mt-2 w-full rounded-lg border bg-background p-3 text-sm"
          rows={2}
        />
      </div>

      {loading ? (
        <div className="rounded-xl border bg-card p-8 text-center text-muted-foreground">Đang tải...</div>
      ) : items.length === 0 ? (
        <div className="rounded-xl border bg-card p-8 text-center text-muted-foreground">Không có yêu cầu hoàn</div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => {
            const url = mediaUrl(item.evidenceUrl);
            return (
              <article key={item.id} className="rounded-xl border bg-card p-4">
                <div className="flex flex-wrap items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold">{item.orderCode} · {item.customerName}</p>
                    <p className="text-sm text-muted-foreground">{item.customerEmail}</p>
                    <p className="mt-2 text-sm">Lý do: {item.reason}</p>
                    <p className="text-sm text-muted-foreground">
                      Đặt lúc {formatVietnamDateTime(item.orderCreatedAt)} · Yêu cầu {formatVietnamDateTime(item.createdAt)}
                    </p>
                    {item.adminNote && <p className="mt-1 text-sm">Ghi chú admin: {item.adminNote}</p>}
                  </div>
                  <span className="rounded-full bg-secondary px-3 py-1 text-xs font-semibold">{item.status}</span>
                </div>
                <div className="mt-3">
                  {isVideo(url) ? (
                    <video src={url} controls className="max-h-64 rounded-lg" />
                  ) : (
                    <a href={url} target="_blank" rel="noreferrer" className="text-sm text-primary underline">
                      Xem ảnh minh chứng
                    </a>
                  )}
                </div>
                {item.status === 'PENDING' && (
                  <div className="mt-3 flex gap-2">
                    <Button size="sm" disabled={actingId === item.id} onClick={() => approve(item.id)}>
                      Xác nhận hoàn
                    </Button>
                    <Button size="sm" variant="outline" disabled={actingId === item.id} onClick={() => reject(item.id)}>
                      Từ chối
                    </Button>
                  </div>
                )}
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border bg-card p-4">
      <p className="text-sm text-muted-foreground">{label}</p>
      <p className="mt-1 text-xl font-bold">{value}</p>
    </div>
  );
}
