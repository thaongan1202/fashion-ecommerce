'use client';

import { useEffect, useMemo, useState } from 'react';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { ProductReviews } from './ProductReviews';
import type { Order } from '@/types';

interface ReviewOrderDialogProps {
  order: Order;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function ReviewOrderDialog({ order, open, onOpenChange }: ReviewOrderDialogProps) {
  const items = order.items ?? [];
  const [selectedItemKey, setSelectedItemKey] = useState('');

  useEffect(() => {
    if (!open) return;
    setSelectedItemKey(String(items[0]?.id ?? items[0]?.productId ?? ''));
  }, [open, order.id]);

  const selectedItem = useMemo(
    () => items.find((item, index) => String(item.id ?? item.productId ?? index) === selectedItemKey),
    [items, selectedItemKey],
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-h-[90vh] max-w-2xl overflow-y-auto">
        <DialogHeader>
          <DialogTitle>Đánh giá đơn hàng {order.orderCode || `#${order.id}`}</DialogTitle>
          <DialogDescription>Đánh giá từng sản phẩm bạn đã nhận. Màu sắc và kích cỡ được lấy từ đơn hàng.</DialogDescription>
        </DialogHeader>

        {items.length === 0 ? (
          <p className="rounded-lg border border-dashed p-5 text-sm text-muted-foreground">Đơn hàng không có sản phẩm để đánh giá.</p>
        ) : (
          <div className="space-y-5">
            {items.length > 1 && (
              <div>
                <label htmlFor={`review-item-${order.id}`} className="mb-2 block text-sm font-medium">Sản phẩm</label>
                <select
                  id={`review-item-${order.id}`}
                  value={selectedItemKey}
                  onChange={(event) => setSelectedItemKey(event.target.value)}
                  className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm"
                >
                  {items.map((item, index) => {
                    const itemKey = String(item.id ?? item.productId ?? index);
                    const variant = [item.color && `Màu ${item.color}`, item.size && `Size ${item.size}`].filter(Boolean).join(' · ');
                    return (
                      <option key={`${itemKey}-${index}`} value={itemKey}>
                        {item.productName}{variant ? ` — ${variant}` : ''}
                      </option>
                    );
                  })}
                </select>
              </div>
            )}

            {selectedItem && (
              <ProductReviews
                key={`${order.id}-${selectedItem.productId}-${selectedItem.id}`}
                productId={selectedItem.productId}
                fixedOrderId={order.id}
                variantColor={selectedItem.color}
                variantSize={selectedItem.size}
                compact
              />
            )}
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
}
