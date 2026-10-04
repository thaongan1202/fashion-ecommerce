'use client';

import { useCallback, useEffect, useMemo, useRef, useState, type FormEvent, type ChangeEvent } from 'react';
import Link from 'next/link';
import { BadgeCheck, ImagePlus, Loader2, MessageSquare, Send, Star, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { useAuth } from '@/hooks/useAuth';
import {
  getProductReviews,
  getReviewImageUrl,
  submitProductReview,
  uploadReviewImages,
  type ProductReviewsResponse,
} from '@/services/review.service';

interface ProductReviewsProps {
  productId: number;
  onSummaryChange?: (summary: { averageRating: number; totalReviews: number }) => void;
  fixedOrderId?: number;
  variantColor?: string | null;
  variantSize?: string | null;
  compact?: boolean;
}

const MAX_REVIEW_IMAGES = 5;
const MAX_IMAGE_BYTES = 5 * 1024 * 1024;
const IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

function RatingInput({
  label,
  value,
  disabled,
  onChange,
}: {
  label: string;
  value: number;
  disabled: boolean;
  onChange: (rating: number) => void;
}) {
  return (
    <fieldset>
      <legend className="mb-2 text-sm font-medium">{label}</legend>
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <button
            key={star}
            type="button"
            aria-label={`${star} sao`}
            aria-pressed={value === star}
            onClick={() => onChange(star)}
            disabled={disabled}
            className="rounded p-1 text-amber-500 transition hover:scale-110 disabled:opacity-50"
          >
            <Star className={`h-6 w-6 ${star <= value ? 'fill-current' : ''}`} />
          </button>
        ))}
        {value > 0 && <span className="ml-2 text-sm text-muted-foreground">{value}/5</span>}
      </div>
    </fieldset>
  );
}

export function ProductReviews({
  productId,
  onSummaryChange,
  fixedOrderId,
  variantColor,
  variantSize,
  compact = false,
}: ProductReviewsProps) {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [data, setData] = useState<ProductReviewsResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [rating, setRating] = useState(0);
  const [materialRating, setMaterialRating] = useState(0);
  const [fitRating, setFitRating] = useState(0);
  const [colorRating, setColorRating] = useState(0);
  const [comment, setComment] = useState('');
  const [orderId, setOrderId] = useState('');
  const [images, setImages] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const imagePreviewsRef = useRef(imagePreviews);
  imagePreviewsRef.current = imagePreviews;

  const loadReviews = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const response = await getProductReviews(productId);
      setData(response);
      setOrderId((current) => {
        if (fixedOrderId !== undefined) return String(fixedOrderId);
        const stillEligible = response.eligibleOrders.some((order) => String(order.orderId) === current);
        return stillEligible ? current : String(response.eligibleOrders[0]?.orderId ?? '');
      });
    } catch (loadError) {
      setError(loadError instanceof Error ? loadError.message : 'Không thể tải đánh giá.');
    } finally {
      setLoading(false);
    }
  }, [fixedOrderId, productId]);

  useEffect(() => {
    void loadReviews();
  }, [loadReviews]);

  useEffect(() => () => imagePreviewsRef.current.forEach((preview) => URL.revokeObjectURL(preview)), []);

  const selectedOrder = useMemo(
    () => data?.eligibleOrders.find((order) => order.orderId === (fixedOrderId ?? Number(orderId))),
    [data?.eligibleOrders, fixedOrderId, orderId],
  );

  const handleImageSelection = (event: ChangeEvent<HTMLInputElement>) => {
    const selected = Array.from(event.target.files ?? []);
    event.target.value = '';
    if (selected.length + images.length > MAX_REVIEW_IMAGES) {
      setError(`Bạn chỉ có thể tải tối đa ${MAX_REVIEW_IMAGES} ảnh.`);
      return;
    }
    const invalidImage = selected.find((file) => !IMAGE_TYPES.includes(file.type) || file.size > MAX_IMAGE_BYTES);
    if (invalidImage) {
      setError('Chỉ nhận ảnh JPG, PNG hoặc WebP, tối đa 5 MB mỗi ảnh.');
      return;
    }
    setError('');
    setImages((current) => [...current, ...selected]);
    setImagePreviews((current) => [...current, ...selected.map((file) => URL.createObjectURL(file))]);
  };

  const removeImage = (index: number) => {
    const preview = imagePreviews[index];
    if (preview) URL.revokeObjectURL(preview);
    setImages((current) => current.filter((_, itemIndex) => itemIndex !== index));
    setImagePreviews((current) => current.filter((_, itemIndex) => itemIndex !== index));
  };

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const selectedOrderId = fixedOrderId ?? Number(orderId);
    const orderIsEligible = data?.eligibleOrders.some((order) => order.orderId === selectedOrderId);
    if (!selectedOrderId || !orderIsEligible) {
      setError('Đơn hàng này chưa đủ điều kiện đánh giá hoặc bạn đã đánh giá sản phẩm.');
      return;
    }
    if (rating < 1 || materialRating < 1 || fitRating < 1 || colorRating < 1 || comment.trim().length === 0) {
      setError('Vui lòng chấm sao cho các tiêu chí và nhập nội dung đánh giá.');
      return;
    }

    setSubmitting(true);
    setError('');
    try {
      const imageUrls = images.length > 0 ? await uploadReviewImages(productId, selectedOrderId, images) : [];
      const updated = await submitProductReview(productId, {
        orderId: selectedOrderId,
        rating,
        materialRating,
        fitRating,
        colorRating,
        comment: comment.trim(),
        imageUrls,
      });
      setData(updated);
      setOrderId(String(updated.eligibleOrders[0]?.orderId ?? ''));
      setRating(0);
      setMaterialRating(0);
      setFitRating(0);
      setColorRating(0);
      setComment('');
      imagePreviews.forEach((preview) => URL.revokeObjectURL(preview));
      setImages([]);
      setImagePreviews([]);
      onSummaryChange?.({ averageRating: updated.averageRating, totalReviews: updated.totalReviews });
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : 'Không thể gửi đánh giá.');
    } finally {
      setSubmitting(false);
    }
  };

  const formOrder = fixedOrderId === undefined ? selectedOrder : undefined;
  const canSubmitForOrder = fixedOrderId === undefined
    ? Boolean(data?.canReview)
    : Boolean(data?.eligibleOrders.some((order) => order.orderId === fixedOrderId));
  const alreadyReviewed = fixedOrderId !== undefined
    && Boolean(data?.reviewedOrderIds?.includes(fixedOrderId));

  if (loading && !data) {
    return <div className="py-8 text-center text-sm text-muted-foreground">Đang tải đánh giá...</div>;
  }

  if (error && !data) {
    return <div className="py-8 text-center text-sm text-destructive">{error}</div>;
  }

  const reviewForm = canSubmitForOrder ? (
    <form onSubmit={handleSubmit} className="space-y-5 rounded-xl border p-5">
      <div>
        <h4 className="font-semibold">Viết đánh giá của bạn</h4>
        <p className="mt-1 text-sm text-muted-foreground">Chỉ đơn hàng đã giao thành công mới được đánh giá.</p>
      </div>

      {fixedOrderId === undefined && data?.eligibleOrders.length ? (
        <div>
          <label htmlFor={`review-order-${productId}`} className="mb-2 block text-sm font-medium">Đơn hàng đã nhận</label>
          <select
            id={`review-order-${productId}`}
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
      ) : null}

      {(variantColor || variantSize || formOrder?.productColor || formOrder?.productSize) && (
        <div className="rounded-lg bg-muted/50 p-3 text-sm">
          <p className="mb-1 font-medium">Biến thể trong đơn hàng</p>
          <div className="flex flex-wrap gap-x-4 gap-y-1 text-muted-foreground">
            {(variantColor || formOrder?.productColor) && <span>Màu sắc: {variantColor || formOrder?.productColor}</span>}
            {(variantSize || formOrder?.productSize) && <span>Kích cỡ: {variantSize || formOrder?.productSize}</span>}
          </div>
        </div>
      )}

      <RatingInput label="Đánh giá chung" value={rating} disabled={submitting} onChange={setRating} />
      <div className="grid gap-4 sm:grid-cols-3">
        <RatingInput label="Chất liệu" value={materialRating} disabled={submitting} onChange={setMaterialRating} />
        <RatingInput label="Form dáng, kích cỡ" value={fitRating} disabled={submitting} onChange={setFitRating} />
        <RatingInput label="Màu sắc" value={colorRating} disabled={submitting} onChange={setColorRating} />
      </div>

      <div>
        <label htmlFor={`review-comment-${productId}`} className="mb-2 block text-sm font-medium">Nội dung đánh giá</label>
        <textarea
          id={`review-comment-${productId}`}
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

      <div>
        <label className="mb-2 block text-sm font-medium">Ảnh thực tế (tối đa 5 ảnh, 5 MB mỗi ảnh)</label>
        <div className="flex flex-wrap gap-3">
          {imagePreviews.map((preview, index) => (
            <div key={preview} className="relative h-20 w-20 overflow-hidden rounded-lg border">
              <img src={preview} alt={`Ảnh đánh giá ${index + 1}`} className="h-full w-full object-cover" />
              <button
                type="button"
                onClick={() => removeImage(index)}
                disabled={submitting}
                aria-label={`Xóa ảnh ${index + 1}`}
                className="absolute right-1 top-1 rounded-full bg-black/70 p-1 text-white"
              >
                <X className="h-3 w-3" />
              </button>
            </div>
          ))}
          {images.length < MAX_REVIEW_IMAGES && (
            <label className="flex h-20 w-20 cursor-pointer flex-col items-center justify-center gap-1 rounded-lg border border-dashed text-muted-foreground hover:bg-muted/50">
              <ImagePlus className="h-5 w-5" />
              <span className="text-xs">Thêm ảnh</span>
              <input
                type="file"
                accept="image/jpeg,image/png,image/webp"
                multiple
                className="sr-only"
                disabled={submitting}
                onChange={handleImageSelection}
              />
            </label>
          )}
        </div>
      </div>

      {error && <p role="alert" className="text-sm text-destructive">{error}</p>}
      <Button type="submit" disabled={submitting || !orderId && fixedOrderId === undefined}>
        {submitting ? <Loader2 className="mr-2 h-4 w-4 animate-spin" /> : <Send className="mr-2 h-4 w-4" />}
        Gửi đánh giá
      </Button>
    </form>
  ) : alreadyReviewed ? (
    <div className="rounded-xl border border-emerald-200 bg-emerald-50 p-5 text-sm text-emerald-800">
      Bạn đã đánh giá sản phẩm này trong đơn hàng.
    </div>
  ) : (
    <div className="rounded-xl border border-dashed p-5 text-sm text-muted-foreground">
      <p>{data?.eligibilityMessage}</p>
      {!authLoading && !isAuthenticated && (
        <Link className="mt-3 inline-block font-medium text-primary hover:underline" href={`/login?redirect=/products/${productId}`}>
          Đăng nhập để đánh giá
        </Link>
      )}
    </div>
  );

  if (compact) {
    return <div className="space-y-4">{reviewForm}</div>;
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
            {[1, 2, 3, 4, 5].map((star) => (
              <Star key={star} className={`h-5 w-5 ${star <= Math.round(data?.averageRating ?? 0) ? 'fill-current' : ''}`} />
            ))}
          </div>
          <span className="font-semibold">{(data?.averageRating ?? 0).toFixed(1)}/5</span>
        </div>
      </section>

      {reviewForm}
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
              {[1, 2, 3, 4, 5].map((star) => (
                <Star key={star} className={`h-4 w-4 ${star <= review.rating ? 'fill-current' : ''}`} />
              ))}
            </div>
            {(review.productColor || review.productSize) && (
              <p className="mt-2 text-xs text-muted-foreground">
                Biến thể đã mua: {[review.productColor && `Màu ${review.productColor}`, review.productSize && `Size ${review.productSize}`].filter(Boolean).join(' · ')}
              </p>
            )}
            {(review.materialRating || review.fitRating || review.colorRating) && (
              <div className="mt-2 flex flex-wrap gap-2 text-xs text-muted-foreground">
                {review.materialRating && <span className="rounded-full bg-muted px-2 py-1">Chất liệu {review.materialRating}/5</span>}
                {review.fitRating && <span className="rounded-full bg-muted px-2 py-1">Form dáng {review.fitRating}/5</span>}
                {review.colorRating && <span className="rounded-full bg-muted px-2 py-1">Màu sắc {review.colorRating}/5</span>}
              </div>
            )}
            <p className="mt-3 whitespace-pre-wrap text-sm leading-6 text-foreground/90">{review.comment}</p>
            {review.imageUrls?.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {review.imageUrls.map((imageUrl) => (
                  <a key={imageUrl} href={getReviewImageUrl(imageUrl)} target="_blank" rel="noreferrer">
                    <img src={getReviewImageUrl(imageUrl)} alt="Ảnh khách hàng đánh giá" loading="lazy" className="h-20 w-20 rounded-lg border object-cover" />
                  </a>
                ))}
              </div>
            )}
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
