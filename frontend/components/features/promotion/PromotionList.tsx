/**
 * PromotionList - Component hiển thị danh sách voucher khả dụng cho khách hàng
 * 
 * Đây là alias cho AvailablePromotionsList, đặt tên theo quy ước trong PHAN_CONG.md
 * Module M04: Cart & Promotion Engine
 * 
 * Chức năng:
 * - Hiển thị danh sách khuyến mãi đang hoạt động
 * - Cho phép khách hàng xem điều kiện áp dụng
 * - Hiển thị thông báo "Voucher không khả thi" khi voucher hết hạn hoặc đơn hàng chưa đủ điều kiện (TC-M04-05)
 * - Tính toán và hiển thị số tiền cần thêm để đủ điều kiện áp dụng
 */
"use client";

import { useState } from "react";
import {
  Ticket,
  Calendar,
  Tag,
  AlertCircle,
  Check,
  Clock,
  Ban,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { useAvailablePromotions } from "@/hooks";
import type { PromotionResponse } from "@/types";
import { cn } from "@/lib/utils";
import { toast } from "sonner";

interface PromotionListProps {
  orderTotal: number;
  onApplyPromotion?: (promotion: PromotionResponse) => void;
  selectedPromotionId?: string;
}

export function PromotionList({
  orderTotal,
  onApplyPromotion,
  selectedPromotionId,
}: PromotionListProps) {
  const { promotions, loading, error, applyPromotion, calculatingDiscount } =
    useAvailablePromotions(orderTotal);

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString("vi-VN", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
    });
  };

  const isExpired = (expirationDate: string) => {
    return new Date(expirationDate) < new Date();
  };

  const handleApply = async (promotion: PromotionResponse) => {
    // Kiểm tra voucher hết hạn phía client trước khi gọi API (TC-M04-05)
    if (promotion.expirationDate && isExpired(promotion.expirationDate)) {
      toast.error("Voucher không khả thi: Khuyến mãi đã hết hạn");
      return;
    }

    // Kiểm tra đơn tối thiểu phía client (TC-M04-05)
    if (
      promotion.minValueToBeApplied &&
      orderTotal < promotion.minValueToBeApplied
    ) {
      toast.error(
        `Voucher không khả thi: Đơn hàng chưa đạt giá trị tối thiểu ${formatCurrency(
          promotion.minValueToBeApplied
        )}`
      );
      return;
    }

    // Gọi API tính discount (sẽ validate lại phía server)
    const result = await applyPromotion(promotion.id);
    if (result.success) {
      onApplyPromotion?.(promotion);
      toast.success(`Đã áp dụng khuyến mãi: ${promotion.title}`);
    } else if (result.error) {
      // Hiển thị lỗi từ server (TC-M04-05: "Voucher không khả thi")
      toast.error(result.error);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600" />
        <span className="ml-3 text-gray-600 dark:text-gray-400">
          Đang tải khuyến mãi...
        </span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
        <p className="text-red-800 dark:text-red-400 flex items-center gap-2">
          <AlertCircle className="w-4 h-4" />
          {error}
        </p>
      </div>
    );
  }

  if (promotions.length === 0) {
    return (
      <div className="text-center p-8 bg-gray-50 dark:bg-gray-800 rounded-lg">
        <Ticket className="w-12 h-12 text-gray-400 mx-auto mb-3" />
        <p className="text-gray-600 dark:text-gray-400">
          {orderTotal > 0
            ? "Không có khuyến mãi phù hợp với đơn hàng của bạn"
            : "Thêm sản phẩm vào giỏ hàng để xem khuyến mãi"}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center gap-2">
        <Tag className="w-5 h-5 text-blue-600" />
        Khuyến mãi có thể áp dụng ({promotions.length})
      </h3>

      <div className="space-y-3">
        {promotions.map((promotion) => {
          const isSelected = selectedPromotionId === promotion.id;
          const expired = promotion.expirationDate
            ? isExpired(promotion.expirationDate)
            : false;
          const canApply =
            !expired &&
            (!promotion.minValueToBeApplied ||
              orderTotal >= promotion.minValueToBeApplied);

          return (
            <div
              key={promotion.id}
              className={cn(
                "relative p-4 rounded-lg border-2 transition-all",
                expired
                  ? "border-red-200 bg-red-50/50 dark:bg-red-900/10 opacity-60"
                  : isSelected
                  ? "border-blue-500 bg-blue-50 dark:bg-blue-900/20"
                  : "border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800 hover:border-blue-300 dark:hover:border-blue-700"
              )}
            >
              {/* Discount Badge */}
              <div className="absolute top-3 right-3">
                {expired ? (
                  <div className="bg-red-500 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg flex items-center gap-1">
                    <Clock className="w-3 h-3" />
                    Hết hạn
                  </div>
                ) : (
                  <div className="bg-gradient-to-r from-red-500 to-pink-500 text-white px-3 py-1 rounded-full text-sm font-bold shadow-lg">
                    -{promotion.percentDiscount}%
                  </div>
                )}
              </div>

              <div className="pr-20">
                {/* Title */}
                <h4
                  className={cn(
                    "font-bold mb-1",
                    expired
                      ? "text-gray-500 line-through"
                      : "text-gray-900 dark:text-white"
                  )}
                >
                  {promotion.title}
                </h4>

                {/* Description */}
                <p className="text-sm text-gray-600 dark:text-gray-400 mb-3 line-clamp-2">
                  {promotion.description}
                </p>

                {/* Details */}
                <div className="space-y-2 text-sm">
                  {promotion.minValueToBeApplied && (
                    <div className="flex items-center gap-2 text-gray-700 dark:text-gray-300">
                      <span className="font-medium">Đơn tối thiểu:</span>
                      <span className="text-blue-600 dark:text-blue-400 font-semibold">
                        {formatCurrency(promotion.minValueToBeApplied)}
                      </span>
                    </div>
                  )}

                  <div
                    className={cn(
                      "flex items-center gap-2",
                      expired
                        ? "text-red-600 dark:text-red-400"
                        : "text-gray-600 dark:text-gray-400"
                    )}
                  >
                    <Calendar className="w-4 h-4" />
                    <span>
                      HSD: {formatDate(promotion.expirationDate)}
                      {expired && " (Đã hết hạn)"}
                    </span>
                  </div>
                </div>

                {/* Action Button */}
                <div className="mt-4">
                  {isSelected ? (
                    <div className="flex items-center gap-2 text-green-600 dark:text-green-400 font-medium">
                      <Check className="w-5 h-5" />
                      <span>Đã áp dụng</span>
                    </div>
                  ) : expired ? (
                    <div className="flex items-center gap-2 text-red-600 font-medium">
                      <Ban className="w-4 h-4" />
                      <span>Voucher không khả thi</span>
                    </div>
                  ) : (
                    <Button
                      onClick={() => handleApply(promotion)}
                      disabled={!canApply || calculatingDiscount}
                      size="sm"
                      className={cn(
                        "w-full",
                        !canApply && "opacity-50 cursor-not-allowed"
                      )}
                    >
                      {calculatingDiscount
                        ? "Đang tính..."
                        : canApply
                        ? "Áp dụng"
                        : "Không đủ điều kiện"}
                    </Button>
                  )}
                </div>

                {/* Not eligible message (TC-M04-05) */}
                {!canApply &&
                  !expired &&
                  promotion.minValueToBeApplied && (
                    <p className="text-xs text-orange-600 dark:text-orange-400 mt-2">
                      Cần thêm{" "}
                      {formatCurrency(
                        promotion.minValueToBeApplied - orderTotal
                      )}{" "}
                      để áp dụng
                    </p>
                  )}

                {/* Expired message (TC-M04-05) */}
                {expired && (
                  <p className="text-xs text-red-600 dark:text-red-400 mt-2 font-medium">
                    Voucher không khả thi: Khuyến mãi đã hết hạn
                  </p>
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
