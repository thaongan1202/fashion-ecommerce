import type { CategoryOption, MineReview, PageResult, PriceRange, Product, Review, ReviewReminder } from '../types/product'

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, { ...init, headers: { 'Content-Type': 'application/json', ...init?.headers } })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.message ?? 'Không thể tải dữ liệu. Vui lòng thử lại.')
  }
  return response.json() as Promise<T>
}

export function getProducts(params: URLSearchParams) {
  return request<PageResult<Product>>(`/api/products?${params.toString()}`)
}

export function getCategories() {
  return request<CategoryOption[]>('/api/products/categories')
}

export function getPriceRange() {
  return request<PriceRange>('/api/products/price-range')
}

export function getReviewReminders() {
  return request<ReviewReminder[]>('/api/reviews/to-review')
}

export function getProduct(id: number) {
  return request<Product>(`/api/products/${id}`)
}

export function getRelatedProducts(id: number) {
  return request<Product[]>(`/api/products/${id}/related`)
}

export function getReviews(id: number, page = 0) {
  return request<PageResult<Review>>(`/api/products/${id}/reviews?page=${page}&size=10&sort=createdAt,desc`)
}

export type ReviewInput = { rating: number; comment: string; variantId: number; heightCm: number | null; weightKg: number | null; imageUrl: string | null }

export async function uploadReviewImage(productId: number, file: File) {
  const body = new FormData()
  body.append('file', file)
  const response = await fetch(`/api/products/${productId}/reviews/image`, { method: 'POST', body })
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.message ?? 'Không thể tải ảnh đánh giá lên.')
  }
  return response.json() as Promise<{ imageUrl: string }>
}

export function postReview(id: number, input: ReviewInput) {
  return request<Review>(`/api/products/${id}/reviews`, { method: 'POST', body: JSON.stringify(input) })
}

export function getMyReview(id: number) {
  return request<MineReview>(`/api/products/${id}/reviews/mine`)
}

export function updateReview(productId: number, reviewId: number, input: ReviewInput) {
  return request<Review>(`/api/products/${productId}/reviews/${reviewId}`, { method: 'PUT', body: JSON.stringify(input) })
}
