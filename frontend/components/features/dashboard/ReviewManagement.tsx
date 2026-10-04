'use client';

import { useCallback, useEffect, useState } from 'react';
import { BadgeCheck, ChevronLeft, ChevronRight, Package, RefreshCw, Star, Trash2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/components/features/users';
import { adminReviewAPI } from '@/lib/api';
import { getReviewImageUrl } from '@/services/review.service';
import { toast } from 'sonner';
import type { AdminReview, ReviewedProductOption } from '@/types';

const PAGE_SIZE = 15;

function ReviewStars({ rating }: { rating: number }) {
  return (
    <span className="inline-flex items-center gap-0.5" aria-label={`${rating} trên 5 sao`}>
      {[1, 2, 3, 4, 5].map((star) => (
        <Star
          key={star}
          className={`h-4 w-4 ${star <= rating ? 'fill-amber-400 text-amber-400' : 'text-muted-foreground/30'}`}
        />
      ))}
    </span>
  );
}

function ReviewCriteria({ review }: { review: AdminReview }) {
  const criteria = [
    ['Chất liệu', review.materialRating],
    ['Độ vừa vặn', review.fitRating],
    ['Màu sắc', review.colorRating],
  ].filter((item): item is [string, number] => typeof item[1] === 'number');

  if (!criteria.length && !review.productColor && !review.productSize) return null;

  return (
    <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-xs text-muted-foreground">
      {criteria.map(([label, score]) => <span key={label}>{label}: {score}/5</span>)}
      {review.productColor && <span>Màu đã mua: {review.productColor}</span>}
      {review.productSize && <span>Kích cỡ: {review.productSize}</span>}
    </div>
  );
}

export function ReviewManagement() {
  const [reviews, setReviews] = useState<AdminReview[]>([]);
  const [products, setProducts] = useState<ReviewedProductOption[]>([]);
  const [page, setPage] = useState(0);
  const [productFilter, setProductFilter] = useState('');
  const [ratingFilter, setRatingFilter] = useState('');
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const [reviewToDelete, setReviewToDelete] = useState<AdminReview | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchReviews = useCallback(async () => {
    setIsLoading(true);
    setError('');
    try {
      const response = await adminReviewAPI.getReviews({
        page,
        size: PAGE_SIZE,
        productId: productFilter ? Number(productFilter) : undefined,
        rating: ratingFilter ? Number(ratingFilter) : undefined,
      });
      if (!response.success || !response.data) {
        throw new Error(response.message || 'Không thể tải danh sách đánh giá');
      }
      setReviews(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
    } catch (fetchError) {
      setError(fetchError instanceof Error ? fetchError.message : 'Không thể tải danh sách đánh giá');
      setReviews([]);
      setTotalPages(0);
      setTotalElements(0);
    } finally {
      setIsLoading(false);
    }
  }, [page, productFilter, ratingFilter]);

  useEffect(() => {
    void adminReviewAPI.getReviewedProducts()
      .then((response) => {
        if (response.success && response.data) setProducts(response.data);
      })
      .catch(() => setProducts([]));
  }, []);

  const handleDeleteReview = async () => {
    if (!reviewToDelete) return;
    setIsDeleting(true);
    try {
      const response = await adminReviewAPI.deleteReview(reviewToDelete.id);
      if (!response.success) throw new Error(response.message || 'Không thể xóa đánh giá');
      toast.success('Đã xóa đánh giá');
      setReviewToDelete(null);

      const productResponse = await adminReviewAPI.getReviewedProducts();
      const updatedProducts = productResponse.success ? productResponse.data || [] : products;
      setProducts(updatedProducts);
      const selectedProductWasRemoved = productFilter && !updatedProducts.some(
        (product) => product.id === Number(productFilter),
      );

      if (selectedProductWasRemoved) {
        setProductFilter('');
        setPage(0);
      } else if (reviews.length === 1 && page > 0) {
        setPage((current) => current - 1);
      } else {
        await fetchReviews();
      }
    } catch (deleteError) {
      toast.error(deleteError instanceof Error ? deleteError.message : 'Không thể xóa đánh giá');
    } finally {
      setIsDeleting(false);
    }
  };

  useEffect(() => {
    void fetchReviews();
  }, [fetchReviews]);

  const changeProduct = (value: string) => {
    setProductFilter(value);
    setPage(0);
  };

  const changeRating = (value: string) => {
    setRatingFilter(value);
    setPage(0);
  };

  return (
    <section className="space-y-5">
      <div className="flex flex-col gap-4 rounded-xl border border-border bg-card p-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="grid flex-1 gap-3 sm:grid-cols-2">
          <label className="space-y-1.5 text-sm font-medium">
            <span>Lọc theo sản phẩm</span>
            <select
              value={productFilter}
              onChange={(event) => changeProduct(event.target.value)}
              className="h-10 w-full rounded-md border border-input bg-background px-3 font-normal outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="">Tất cả sản phẩm đã được đánh giá</option>
              {products.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.name} ({product.reviewCount})
                </option>
              ))}
            </select>
          </label>
          <label className="space-y-1.5 text-sm font-medium">
            <span>Lọc theo số sao</span>
            <select
              value={ratingFilter}
              onChange={(event) => changeRating(event.target.value)}
              className="h-10 w-full rounded-md border border-input bg-background px-3 font-normal outline-none focus:ring-2 focus:ring-ring"
            >
              <option value="">Tất cả mức sao</option>
              {[5, 4, 3, 2, 1].map((rating) => (
                <option key={rating} value={rating}>{rating} sao</option>
              ))}
            </select>
          </label>
        </div>
        <Button variant="outline" onClick={() => void fetchReviews()} disabled={isLoading}>
          <RefreshCw className={`mr-2 h-4 w-4 ${isLoading ? 'animate-spin' : ''}`} />
          Làm mới
        </Button>
      </div>

      <div className="flex items-center justify-between text-sm text-muted-foreground">
        <span>{totalElements} đánh giá</span>
        {(productFilter || ratingFilter) && (
          <button
            type="button"
            className="underline underline-offset-4 hover:text-foreground"
            onClick={() => { setProductFilter(''); setRatingFilter(''); setPage(0); }}
          >
            Xóa bộ lọc
          </button>
        )}
      </div>

      {error && (
        <div className="rounded-lg border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="space-y-3" aria-label="Đang tải đánh giá">
          {[1, 2, 3].map((item) => <div key={item} className="h-36 animate-pulse rounded-xl border border-border bg-card" />)}
        </div>
      ) : !error && reviews.length === 0 ? (
        <div className="rounded-xl border border-border bg-card p-10 text-center text-sm text-muted-foreground">
          Chưa có đánh giá phù hợp với bộ lọc.
        </div>
      ) : !error ? (
        <div className="space-y-3">
          {reviews.map((review) => (
            <article key={review.id} className="rounded-xl border border-border bg-card p-4 sm:p-5">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="min-w-0">
                  <div className="mb-2 flex items-center gap-2 text-sm font-semibold text-foreground">
                    <Package className="h-4 w-4 shrink-0 text-primary" />
                    <span className="truncate">Sản phẩm: {review.productName}</span>
                    <span className="shrink-0 text-xs font-normal text-muted-foreground">#{review.productId}</span>
                  </div>
                  <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
                    <span className="font-medium">{review.authorName || 'Khách hàng'}</span>
                    {review.verifiedPurchase && (
                      <span className="inline-flex items-center gap-1 text-xs text-emerald-700">
                        <BadgeCheck className="h-3.5 w-3.5" /> Đã mua hàng
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex shrink-0 items-center gap-2">
                  <ReviewStars rating={review.rating} />
                  <span className="text-sm font-semibold">{review.rating}/5</span>
                </div>
              </div>

              <ReviewCriteria review={review} />
              <p className="mt-3 whitespace-pre-wrap break-words text-sm leading-6 text-foreground/90">
                {review.comment || 'Khách hàng không để lại nội dung.'}
              </p>

              {!!review.imageUrls?.length && (
                <div className="mt-3 flex flex-wrap gap-2">
                  {review.imageUrls.map((imageUrl) => (
                    <a key={imageUrl} href={getReviewImageUrl(imageUrl)} target="_blank" rel="noreferrer" aria-label="Mở ảnh đánh giá">
                      <img
                        src={getReviewImageUrl(imageUrl)}
                        alt="Ảnh khách hàng gửi trong đánh giá"
                        loading="lazy"
                        className="h-16 w-16 rounded-md border border-border object-cover"
                      />
                    </a>
                  ))}
                </div>
              )}

              <div className="mt-3 text-xs text-muted-foreground">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <span>{review.createdAt ? new Date(review.createdAt).toLocaleString('vi-VN') : 'Không rõ thời gian'}</span>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="h-8 gap-1.5 text-destructive hover:text-destructive"
                    onClick={() => setReviewToDelete(review)}
                  >
                    <Trash2 className="h-4 w-4" /> Xóa đánh giá
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      ) : null}

      {totalPages > 1 && (
        <div className="flex items-center justify-between rounded-xl border border-border bg-card p-3">
          <span className="text-sm text-muted-foreground">Trang {page + 1} / {totalPages}</span>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={page === 0 || isLoading}>
              <ChevronLeft className="mr-1 h-4 w-4" /> Trước
            </Button>
            <Button variant="outline" size="sm" onClick={() => setPage((current) => Math.min(totalPages - 1, current + 1))} disabled={page >= totalPages - 1 || isLoading}>
              Sau <ChevronRight className="ml-1 h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={!!reviewToDelete}
        onOpenChange={(open) => { if (!open && !isDeleting) setReviewToDelete(null); }}
        onConfirm={() => { void handleDeleteReview(); }}
        title="Xóa đánh giá sản phẩm?"
        description={reviewToDelete
          ? `Đánh giá ${reviewToDelete.rating} sao cho sản phẩm “${reviewToDelete.productName}” sẽ bị xóa cùng ảnh đính kèm. Thao tác này không thể hoàn tác.`
          : ''}
        confirmText={isDeleting ? 'Đang xóa…' : 'Xóa đánh giá'}
        variant="destructive"
      />
    </section>
  );
}
