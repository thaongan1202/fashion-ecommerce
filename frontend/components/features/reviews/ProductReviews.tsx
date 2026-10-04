'use client';

import { useCallback, useEffect, useState, type FormEvent } from 'react';
import Link from 'next/link';
import { BadgeCheck, Loader2, MessageSquare, Send, Star } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/hooks/useAuth';
import {
  getProductReviews,
  submitProductReview,
  type ProductReviewsResponse,
} from '@/services/review.service';

interface ProductReviewsProps {
  productId: number;
  onSummaryChange?: (summary: { averageRating: number; totalReviews: number }) => void;
}

export function ProductReviews({ productId, onSummaryChange }: ProductReviewsProps) {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [data, setData] = useState<ProductReviewsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [rating, setRating] = useState(0);
  const [comment, setComment] = useState('');
  const [orderId, setOrderId] = useState('');

  const loadReviews = useCallback(async () => {
    setError('');
    try {
      const response = await getProductReviews(productId);
      setData(response);
      setOrderId((current) => {
        const stillEligible = response.eligibleOrders.some((order) => String(order.orderId) === current);
        return stillEligible ? current : String(response.eligibleOrders[0]?.orderId ?? '');
      });
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Không thể tải đánh giá.');
    } finally {
      setLoading(false);
    }
  }, [productId]);

  useEffect(() => {
    void loadReviews();
  }, [loadReviews]);

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (rating < 1 || !orderId || comment.trim().length === 0) {
      setError('Vui lòng chọn đơn hàng, số sao và nhập nội dung đánh giá.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const updated = await submitProductReview(productId, {
        orderId: Number(orderId),
        rating,
        comment: comment.trim(),
      });
      setData(updated);
      setOrderId(String(updated.eligibleOrders[0]?.orderId ?? ''));
      setRating(0);
      setComment('');
      onSummaryChange?.({
        averageRating: updated.averageRating,
        totalReviews: updated.totalReviews,
      });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Không thể gửi đánh giá.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="py-10 text-center text-muted-foreground">Đang tải đánh giá...</div>;
  }

  if (error && !data) {
    return <div className="py-8 text-center text-sm text-destructive">{error}</div>;
  }

  return (
    <div className="space-y-8">
      <section className="flex flex-col gap-3 rounded-xl bg-muted/40 p-5 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-lg font-semibold">Đánh giá sản phẩm</h3>
          <p className="mt-1 text-sm text-muted-foreground">
            {data?.totalReviews ? `${data.totalReviews} đánh giá` : 'Sản phẩm chưa có đánh giá'}
          </p>
        </div>
        <div className="flex items-center gap-2" aria-label={`Điểm trung bình ${data?.averageRating.toFixed(1) ?? '0.0'} trên 5`}>
          <div className="flex text-amber-500">
            {[1, 2, 3, 4, 5].map((value) => (
              <Star key={value} className={`h-5 w-5 ${value <= Math.round(data?.averageRating ?? 0) ? 'fill-current' : ''}`} />
            ))}
          </div>
          <span className="font-semibold">{(data?.averageRating ?? 0).toFixed(1)}/5</span>
        </div>
      </section>

      {data?.canReview ? (
        <form onSubmit={handleSubmit} className="space-y-4 rounded-xl border p-5">
          <div>
            <h4 className="font-semibold">Viết đánh giá của bạn</h4>
            <p className="mt-1 text-sm text-muted-foreground">Chỉ khách đã nhận hàng mới có thể đánh giá.</p>
          </div>

          <div>
            <label htmlFor="review-order" className="mb-2 block text-sm font-medium">Đơn hàng đã nhận</label>
            <select
              id="review-order"
              value={orderId}
              onChange={(event) => setOrderId(event.target.value)}
              className="w-full rounded-lg border border-input bg-background px-3 py-2 text-sm"
              disabled={submitting}
            >
              {data.eligibleOrders.map((order) => (
                <option key={order.orderId} value={order.orderId}>Đơn {order.orderCode}</option>
              ))}
            </select>
          </div>

          <fieldset>
            <legend className="mb-2 text-sm font-medium">Bạn chấm sản phẩm bao nhiêu sao?</legend>
            <div className="flex items-center gap-1">
              {[1, 2, 3, 4, 5].map((value) => (
                <button
                  key={value}
                  type="button"
                  aria-label={`${value} sao`}
                  aria-pressed={rating === value}
                  onClick={() => setRating(value)}
                  disabled={submitting}
                  className="rounded p-1 text-amber-500 transition hover:scale-110 disabled:opacity-50"
                >
                  <Star className={`h-7 w-7 ${value <= rating ? 'fill-current' : ''}`} />
                </button>
              ))}
              {rating > 0 && <span className="ml-2 text-sm text-muted-foreground">{rating}/5</span>}
            </div>
          </fieldset>

          <div>
            <label htmlFor="review-comment" className="mb-2 block text-sm font-medium">Nội dung đánh giá</label>
            <textarea
              id="review-comment"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              maxLength={2000}
              rows={4}
              placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm..."
              className="w-full resize-y rounded-lg border border-input bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-primary"
              disabled={submitting}
            />
            <p className="mt-1 text-right text-xs text-muted-foreground">{comment.length}/2000</p>
          </div>

          {error && <p role="alert" className="text-sm text-destructive">{error}</p>}

          <Button type="submit" disabled={submitting || !orderId}>
            {submitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Send className="mr-2 h-4 w-4" />}
            Gửi đánh giá
          </Button>
        </form>
      ) : (
        <div className="rounded-xl border border-dashed p-5 text-sm text-muted-foreground">
          <p>{data?.eligibilityMessage}</p>
          {!authLoading && !isAuthenticated && (
            <Link className="mt-3 inline-block font-medium text-primary hover:underline" href={`/login?redirect=/products/${productId}`}>
              Đăng nhập để đánh giá
            </Link>
          )}
        </div>
      )}

      {error && data && <p role="alert" className="text-sm text-destructive">{error}</p>}

      <section aria-label="Danh sách đánh giá" className="space-y-5">
        {data?.reviews.length ? data.reviews.map((review) => (
          <article key={review.id} className="border-b pb-5 last:border-b-0">
            <div className="flex flex-wrap items-center gap-3">
              <span className="font-semibold">{review.authorName}</span>
              {review.verifiedPurchase && (
                <span className="inline-flex items-center gap-1 text-xs text-emerald-700">
                  <BadgeCheck className="h-4 w-4" /> Đã mua hàng
                </span>
              )}
              <time className="text-xs text-muted-foreground" dateTime={review.createdAt}>
                {new Intl.DateTimeFormat('vi-VN', { dateStyle: 'medium' }).format(new Date(review.createdAt))}
              </time>
            </div>
            <div className="mt-2 flex text-amber-500" aria-label={`${review.rating} trên 5 sao`}>
              {[1, 2, 3, 4, 5].map((value) => (
                <Star key={value} className={`h-4 w-4 ${value <= review.rating ? 'fill-current' : ''}`} />
              ))}
            </div>
            <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-foreground/90">{review.comment}</p>
          </article>
        )) : (
          <div className="py-8 text-center text-sm text-muted-foreground">
            <MessageSquare className="mx-auto mb-2 h-8 w-8 opacity-50" />
            Chưa có đánh giá nào cho sản phẩm này.
          </div>
        )}
      </section>
    </div>
  );
}
