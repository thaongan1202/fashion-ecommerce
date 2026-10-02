import { useCallback, useEffect, useState } from 'react'
import { getPendingReviews, setReviewStatus } from '../api/adminReviews'
import type { AdminReview } from '../api/adminReviews'

export function AdminReviewsPage() {
  const [reviews, setReviews] = useState<AdminReview[]>([])
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState<number | null>(null)

  const refresh = useCallback(() => getPendingReviews().then(setReviews).catch((reason: unknown) => {
    setError(reason instanceof Error ? reason.message : 'Không tải được danh sách review.')
  }), [])

  useEffect(() => { void refresh() }, [refresh])

  async function moderate(id: number, status: 'APPROVED' | 'HIDDEN') {
    setBusyId(id); setError('')
    try {
      await setReviewStatus(id, status)
      setReviews((current) => current.filter((review) => review.id !== id))
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Không cập nhật được trạng thái review.')
    } finally { setBusyId(null) }
  }

  return <main className="page-shell admin-review-page">
    <p className="eyebrow">QUẢN TRỊ</p>
    <h1>Duyệt đánh giá</h1>
    <p>Review mới và review vừa chỉnh sửa cần được duyệt trước khi hiển thị công khai.</p>
    {error && <p className="notice notice-error" role="alert">{error}</p>}
    {!reviews.length && !error && <p className="notice">Không có review đang chờ duyệt.</p>}
    <div className="admin-review-list">
      {reviews.map((review) => <article className="admin-review-card" key={review.id}>
        <div className="admin-review-heading"><strong>{review.productName}</strong><span>{review.userName} · {review.rating}/5 sao</span></div>
        <small>{new Date(review.createdAt).toLocaleString('vi-VN')}</small>
        <p>{review.comment}</p>
        <div className="review-fit-details">{[review.size, review.color, review.heightCm && `${review.heightCm} cm`, review.weightKg && `${review.weightKg} kg`].filter((detail): detail is string => Boolean(detail)).map((detail, index) => <span key={`${review.id}-${index}`}>{detail}</span>)}</div>
        {review.imageUrl && <img className="review-photo" src={review.imageUrl} alt="Ảnh khách gửi kèm review" />}
        <div className="admin-review-actions">
          <button className="button button-primary" disabled={busyId === review.id} onClick={() => void moderate(review.id, 'APPROVED')}>Duyệt</button>
          <button disabled={busyId === review.id} onClick={() => void moderate(review.id, 'HIDDEN')}>Ẩn review</button>
        </div>
      </article>)}
    </div>
  </main>
}
