/**
 * OrdersTable component - Display orders in table format
 */

'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Eye, Star, Ban, Undo2 } from 'lucide-react';
import { formatPrice } from '@/lib/utils';
import { getOrderStatus } from '@/lib/constants';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { ReviewOrderDialog } from '@/components/features/reviews/ReviewOrderDialog';
import { ReturnRequestDialog } from '@/components/features/orders/ReturnRequestDialog';
import { orderAPI } from '@/lib/api';
import { canReturnOrder } from '@/lib/datetime';
import { toast } from 'sonner';
import type { Order } from '@/types';

interface OrdersTableProps {
  orders: Order[];
  isAdmin?: boolean;
  onViewDetail?: (orderId: number) => void;
  onRefresh?: () => void;
}

export function OrdersTable({ orders, isAdmin = false, onViewDetail, onRefresh }: OrdersTableProps) {
  const router = useRouter();
  const [reviewingOrder, setReviewingOrder] = useState<Order | null>(null);
  const [returningOrder, setReturningOrder] = useState<Order | null>(null);
  const [cancellingId, setCancellingId] = useState<number | null>(null);

  const handleCancel = async (order: Order) => {
    setCancellingId(order.id);
    try {
      await orderAPI.cancel(order.id);
      onRefresh?.();
    } catch (error) {
      toast.error(error instanceof Error ? error.message : 'Không thể gửi yêu cầu hủy');
    } finally {
      setCancellingId(null);
    }
  };

  const handleViewOrder = (order: Order) => {
    if (isAdmin && onViewDetail) {
      // Admin mode: open modal
      onViewDetail(order.id);
    } else {
      // Customer mode: navigate to order page
      router.push(`/orders/${order.id}`);
    }
  };

  // Empty state
  if (orders.length === 0) {
    return (
      <div className="bg-card rounded-xl border border-border p-12 text-center">
        <p className="text-muted-foreground">Không có đơn hàng nào</p>
      </div>
    );
  }

  return (
    <div className="bg-card rounded-xl border border-border overflow-hidden">
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-border bg-secondary/50">
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Mã đơn</th>
              {isAdmin && (
                <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden sm:table-cell">
                  Khách hàng
                </th>
              )}
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Tổng tiền</th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Trạng thái</th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground hidden md:table-cell">
                Ngày đặt
              </th>
              <th className="text-left py-3 px-4 font-semibold text-muted-foreground">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {orders.map((order) => {
              const statusConfig = getOrderStatus(order.status);
              return (
                <tr key={order.id} className="border-b border-border hover:bg-secondary/50">
                  <td className="py-3 px-4 font-medium">#{order.id}</td>
                  {isAdmin && (
                    <td className="py-3 px-4 hidden sm:table-cell">
                      {order.customer || order.recipientName || 'N/A'}
                    </td>
                  )}
                  <td className="py-3 px-4 font-semibold">
                    {formatPrice(order.total || order.totalAmount)}
                  </td>
                  <td className="py-3 px-4">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className={cn("px-2 py-1 rounded-full text-xs font-semibold", statusConfig.class)}>
                        {statusConfig.label}
                      </span>
                      {!isAdmin && order.status === 'DELIVERED' && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-8 gap-1.5"
                          onClick={() => setReviewingOrder(order)}
                        >
                          <Star className="h-4 w-4 text-amber-500" />
                          Đánh giá
                        </Button>
                      )}
                      {!isAdmin && order.cancelRequested && order.status === 'PENDING' && (
                        <span className="rounded-full bg-amber-100 px-2 py-1 text-xs font-semibold text-amber-800">
                          Đã yêu cầu hủy
                        </span>
                      )}
                      {isAdmin && order.cancelRequested && order.status === 'PENDING' && (
                        <span className="rounded-full bg-amber-100 px-2 py-1 text-xs font-semibold text-amber-800">
                          Khách yêu cầu hủy
                        </span>
                      )}
                      {!isAdmin && order.canCancel && !order.cancelRequested && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-8 gap-1.5"
                          disabled={cancellingId === order.id}
                          onClick={() => handleCancel(order)}
                        >
                          <Ban className="h-4 w-4" />
                          Yêu cầu hủy
                        </Button>
                      )}
                      {!isAdmin && order.returnStatus === 'REJECTED' && order.returnAdminNote && (
                        <p className="w-full text-xs text-red-600">Từ chối hoàn: {order.returnAdminNote}</p>
                      )}
                      {!isAdmin && order.returnStatus === 'APPROVED' && (
                        <p className="w-full text-xs text-green-700">Đã hoàn tiền vào ví</p>
                      )}
                      {!isAdmin && order.returnStatus === 'PENDING' && (
                        <p className="w-full text-xs text-amber-700">Đang chờ xét duyệt hoàn hàng</p>
                      )}
                      {!isAdmin && (order.canReturn || canReturnOrder(order.createdAt, order.status, order.returnStatus)) && (
                        <Button
                          size="sm"
                          variant="outline"
                          className="h-8 gap-1.5"
                          onClick={() => setReturningOrder(order)}
                        >
                          <Undo2 className="h-4 w-4" />
                          Hoàn hàng
                        </Button>
                      )}
                    </div>
                  </td>
                  <td className="py-3 px-4 text-muted-foreground hidden md:table-cell">
                    {order.date || new Date(order.createdAt).toLocaleDateString('vi-VN')}
                  </td>
                  <td className="py-3 px-4">
                    <button
                      className="rounded-lg p-2 text-blue-600 hover:bg-secondary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                      aria-label={`Xem chi tiết đơn hàng ${order.id}`}
                      onClick={() => handleViewOrder(order)}
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              );
            })}
          </tbody>
      </table>
      </div>
      {reviewingOrder && (
        <ReviewOrderDialog
          order={reviewingOrder}
          open={Boolean(reviewingOrder)}
          onOpenChange={(open) => {
            if (!open) setReviewingOrder(null);
          }}
        />
      )}
      {returningOrder && (
        <ReturnRequestDialog
          orderId={returningOrder.id}
          open={Boolean(returningOrder)}
          onClose={() => setReturningOrder(null)}
          onSubmitted={() => onRefresh?.()}
        />
      )}
    </div>
  );
}
