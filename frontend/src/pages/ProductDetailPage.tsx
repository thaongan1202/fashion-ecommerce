import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProduct, getMyReview, getRelatedProducts, getReviews, postReview, updateReview, uploadReviewImage } from '../api/products'
import type { PurchasedReviewVariant } from '../types/product'
import type { DemoSession } from '../types/auth'
import type { Product, Review } from '../types/product'

export function ProductDetailPage({ demoUser }: { demoUser: DemoSession | null }) {
  const { productId } = useParams()
  const id = Number(productId)
  const [product, setProduct] = useState<Product | null>(null)
  const [related, setRelated] = useState<Product[]>([])
  const [reviews, setReviews] = useState<Review[]>([])
  const [myReview, setMyReview] = useState<Review | null>(null)
  const [purchasedVariants, setPurchasedVariants] = useState<PurchasedReviewVariant[]>([])
  const [selectedVariantId, setSelectedVariantId] = useState('')
  const [heightCm, setHeightCm] = useState('')
  const [weightKg, setWeightKg] = useState('')
  const [imageFile, setImageFile] = useState<File | null>(null)
  const [imageInputKey, setImageInputKey] = useState(0)
  const [eligible, setEligible] = useState<boolean | null>(null)
  const [rating, setRating] = useState(5)
  const [comment, setComment] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (!Number.isSafeInteger(id) || id <= 0) { setError('Sản phẩm không hợp lệ.'); return }
    Promise.all([getProduct(id), getRelatedProducts(id), getReviews(id)])
      .then(([item, similar, reviewPage]) => { setProduct(item); setRelated(similar); setReviews(reviewPage.content) })
      .catch((reason: Error) => setError(reason.message))
  }, [id])

  useEffect(() => {
    setMyReview(null); setEligible(null)
    setImageFile(null); setImageInputKey((key) => key + 1)
    setPurchasedVariants([]); setSelectedVariantId(''); setHeightCm(''); setWeightKg('')
    if (!demoUser?.userId || !Number.isSafeInteger(id) || id <= 0) return
    getMyReview(id).then((mine) => {
      setMyReview(mine.review); setEligible(mine.eligible)
      setPurchasedVariants(mine.purchasedVariants)
      setSelectedVariantId(String(mine.review?.variantId ?? mine.purchasedVariants[0]?.id ?? ''))
      setRating(mine.review?.rating ?? 5); setComment(mine.review?.comment ?? '')
      setHeightCm(mine.review?.heightCm?.toString() ?? '')
      setWeightKg(mine.review?.weightKg?.toString() ?? '')
    })
      .catch((reason: Error) => setError(reason.message))
  }, [id, demoUser?.userId])

  async function submitReview(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setMessage(''); setError('')
    try {
      const imageUrl = imageFile
        ? (await uploadReviewImage(id, imageFile)).imageUrl
        : myReview?.imageUrl ?? null
      const input = {
        rating,
        comment,
        variantId: Number(selectedVariantId),
        heightCm: heightCm ? Number(heightCm) : null,
        weightKg: weightKg ? Number(weightKg) : null,
        imageUrl,
      }
      const saved = myReview
        ? await updateReview(id, myReview.id, input)
        : await postReview(id, input)
      setMyReview(saved); setRating(saved.rating); setComment(saved.comment)
      setSelectedVariantId(String(saved.variantId ?? selectedVariantId))
      setHeightCm(saved.heightCm?.toString() ?? ''); setWeightKg(saved.weightKg?.toString() ?? '')
      setImageFile(null); setImageInputKey((key) => key + 1)
      const [updatedProduct, updatedReviews] = await Promise.all([getProduct(id), getReviews(id)])
      setProduct(updatedProduct); setReviews(updatedReviews.content)
      setMessage('Đánh giá của bạn đã được đăng.')
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Không gửi được đánh giá.') }
  }

  if (error && !product) return <main className="page-shell"><p className="notice notice-error" role="alert">{error}</p><Link to="/products">Quay lại danh sách</Link></main>
  if (!product) return <main className="page-shell">Đang tải sản phẩm...</main>
  const image = product.images.find((item) => item.primary)?.imageUrl ?? product.images[0]?.imageUrl

  return <main className="page-shell detail-page">
    <Link to="/products" className="back-link">← Tất cả sản phẩm</Link>
    <section className="detail-layout">
      <div className="detail-image">{image ? <img src={image} alt={product.name} /> : <span>FASHION / STORE</span>}</div>
      <div className="detail-copy"><p className="eyebrow">{product.brandName} · {product.categoryName}</p><h1>{product.name}</h1><p className="detail-price">{product.minPrice.toLocaleString('vi-VN')} ₫{product.maxPrice !== product.minPrice && ` – ${product.maxPrice.toLocaleString('vi-VN')} ₫`}</p><p>{product.description}</p>{product.material && <p>Chất liệu: {product.material}</p>}<p className="rating-summary"><span>{'★'.repeat(Math.round(product.averageRating))}{'☆'.repeat(5 - Math.round(product.averageRating))}</span> {product.averageRating.toFixed(1)} / 5 · {product.reviewCount} đánh giá</p>
        <div className="variant-list">{product.variants.map((variant) => <span key={variant.id}>{[variant.color, variant.size].filter(Boolean).join(' / ') || 'Mặc định'} · {variant.stockQty > 0 ? 'Còn hàng' : 'Hết hàng'}</span>)}</div>
      </div>
    </section>
    <section className="review-section"><div><p className="eyebrow">CỘNG ĐỒNG</p><h2>Đánh giá sản phẩm</h2></div>
      {!demoUser?.userId && <p className="notice">Đăng nhập bằng tài khoản demo ở thanh trên để đánh giá sản phẩm.</p>}
      {demoUser?.userId && eligible === false && <p className="notice notice-error">Tài khoản này chưa có đơn hàng đã giao chứa sản phẩm này nên chưa thể đánh giá.</p>}
      {demoUser?.userId && eligible === null && <p>Đang kiểm tra điều kiện đánh giá…</p>}
      {demoUser?.userId && eligible && <>
        {myReview && <p className="notice">Đánh giá của bạn đã được đăng. Bạn có thể chỉnh sửa đánh giá này.</p>}
        <form className="review-form review-form-quick" onSubmit={submitReview}>
          <label>Điểm đánh giá<select value={rating} onChange={(event) => setRating(Number(event.target.value))}>{[5, 4, 3, 2, 1].map((value) => <option key={value} value={value}>{value} sao</option>)}</select></label>
          <label>Phân loại đã mua<select required value={selectedVariantId} onChange={(event) => setSelectedVariantId(event.target.value)}>{purchasedVariants.map((variant) => <option value={variant.id} key={variant.id}>{[variant.size, variant.color].filter(Boolean).join(' · ') || 'Phân loại mặc định'}</option>)}</select></label>
          <label>Chiều cao (cm)<input type="number" min="100" max="250" value={heightCm} onChange={(event) => setHeightCm(event.target.value)} placeholder="Không bắt buộc" /></label>
          <label>Cân nặng (kg)<input type="number" min="20" max="300" step="0.1" value={weightKg} onChange={(event) => setWeightKg(event.target.value)} placeholder="Không bắt buộc" /></label>
          <label className="review-image-field">Ảnh đánh giá<input key={imageInputKey} type="file" accept="image/jpeg,image/png,image/webp" onChange={(event) => { const file = event.target.files?.[0] ?? null; if (file && file.size > 5 * 1024 * 1024) { setError('Ảnh đánh giá tối đa 5 MB.'); event.target.value = ''; return } setError(''); setImageFile(file) }} /><small>JPEG, PNG hoặc WebP; tối đa 5 MB.</small></label>
          {!imageFile && myReview?.imageUrl && <img className="review-photo-preview" src={myReview.imageUrl} alt="Review image" />}
          {imageFile && <small className="review-image-name">Selected: {imageFile.name}</small>}
          <label className="review-comment-field">Nhận xét<textarea required minLength={1} maxLength={2000} value={comment} onChange={(event) => setComment(event.target.value)} placeholder="Chia sẻ trải nghiệm của bạn" /></label>
          <button className="button button-primary" type="submit" disabled={!selectedVariantId}>{myReview ? 'Lưu chỉnh sửa' : 'Gửi đánh giá'}</button>
        </form>
      </>}
      {message && <p className="notice notice-success" role="status">{message}</p>}{error && <p className="notice notice-error" role="alert">{error}</p>}
      <div className="review-list">{reviews.map((review) => <article key={review.id}><div className="review-author"><strong>{review.userName}{review.id === myReview?.id ? ' (Bạn)' : ''}</strong><span className="review-stars">{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</span></div><p>{review.comment}</p>{review.imageUrl && <img className="review-photo" src={review.imageUrl} alt={`Ảnh đánh giá from ${review.userName}`} />}<div className="review-fit-details">{[review.size && `Phân loại: ${review.size}`, review.color && `Màu: ${review.color}`, review.heightCm && `Cao ${review.heightCm} cm`, review.weightKg && `Nặng ${review.weightKg} kg`].filter(Boolean).map((detail) => <span key={detail}>{detail}</span>)}</div><time>{new Date(review.createdAt).toLocaleDateString('vi-VN')}</time></article>)}{reviews.length === 0 && <p>Chưa có đánh giá được duyệt.</p>}</div>
    </section>
    {related.length > 0 && <section className="related-section"><p className="eyebrow">CÓ THỂ BẠN SẼ THÍCH</p><h2>Sản phẩm liên quan</h2><div className="product-grid">{related.map((item) => <Link className="product-card" to={`/products/${item.id}`} key={item.id}><div className="product-image">{item.images[0] ? <img src={item.images[0].imageUrl} alt={item.name} /> : <span>FASHION / STORE</span>}</div><div className="product-info"><h3>{item.name}</h3><span>{item.minPrice.toLocaleString('vi-VN')} ₫</span></div></Link>)}</div></section>}
  </main>
}
