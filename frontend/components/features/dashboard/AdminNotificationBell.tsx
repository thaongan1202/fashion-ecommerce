'use client';

import { useCallback, useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { notificationAPI, type AdminNotificationItem } from '@/lib/api';
import { formatVietnamDateTime } from '@/lib/datetime';

interface AdminNotificationBellProps {
  onOpenReturns: () => void;
  onUnreadChange?: (count: number) => void;
}

export function AdminNotificationBell({ onOpenReturns, onUnreadChange }: AdminNotificationBellProps) {
  const [open, setOpen] = useState(false);
  const [items, setItems] = useState<AdminNotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const load = useCallback(async () => {
    try {
      const response = await notificationAPI.list();
      const payload = response.data;
      const nextItems = payload?.items || [];
      const nextUnread = payload?.unreadCount || 0;
      setItems(nextItems);
      setUnreadCount(nextUnread);
      onUnreadChange?.(nextUnread);
    } catch {
      // Keep the last list if the poll fails.
    }
  }, [onUnreadChange]);

  useEffect(() => {
    load();
    const timer = window.setInterval(load, 15000);
    return () => window.clearInterval(timer);
  }, [load]);

  const openReturn = async (item: AdminNotificationItem) => {
    if (!item.read) {
      try {
        await notificationAPI.markRead(item.id);
      } catch {
        // Still open the return list.
      }
    }
    setOpen(false);
    onOpenReturns();
    await load();
  };

  const markAll = async () => {
    try {
      await notificationAPI.markAllRead();
      await load();
    } catch {
      // Ignore; the next poll will retry.
    }
  };

  return (
    <div className="relative">
      <button
        type="button"
        className="p-2 hover:bg-secondary rounded-lg relative"
        aria-label="Thông báo hoàn tiền"
        onClick={() => {
          setOpen((current) => !current);
          load();
        }}
      >
        <Bell className="w-5 h-5 text-muted-foreground" />
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-4 rounded-full bg-destructive px-1 text-[10px] font-semibold leading-4 text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 z-50 mt-2 w-80 max-w-[calc(100vw-2rem)] rounded-xl border bg-card shadow-lg">
          <div className="flex items-center justify-between border-b px-3 py-2">
            <p className="text-sm font-semibold">Thông báo hoàn tiền</p>
            <button type="button" className="text-xs text-primary" onClick={markAll}>
              Đọc tất cả
            </button>
          </div>
          <div className="max-h-80 overflow-y-auto">
            {items.length === 0 ? (
              <p className="px-3 py-6 text-center text-sm text-muted-foreground">Chưa có thông báo</p>
            ) : (
              items.map((item) => (
                <button
                  key={item.id}
                  type="button"
                  onClick={() => openReturn(item)}
                  className="block w-full border-b px-3 py-2 text-left last:border-b-0 hover:bg-secondary"
                >
                  <p className={`text-sm ${item.read ? 'text-muted-foreground' : 'font-semibold text-foreground'}`}>
                    {item.title}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">{item.message}</p>
                  <p className="mt-1 text-[11px] text-muted-foreground">{formatVietnamDateTime(item.createdAt)}</p>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
