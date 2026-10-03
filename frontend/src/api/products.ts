import type { CategoryOption, MineReview, PageResult, PriceRange, Product, Review, ReviewReminder } from '../types/product'
import { apiJson } from './client'

export function getProducts(params: URLSearchParams) {
  return apiJson<PageResult<Product>>(`/api/products?${params.toString()}`)
}

export function getCategories() {
  return apiJson<CategoryOption[]>('/api/products/categories')
}

export function getPriceRange() {
  return apiJson<PriceRange>('/api/products/price-range')
}

export function getReviewReminders() {
  return apiJson<ReviewReminder[]>('/api/reviews/to-review')
}

export function getProduct(id: number) {
  return apiJson<Product>(`/api/products/${id}`)
}

export function getRelatedProducts(id: number) {
  return apiJson<Product[]>(`/api/products/${id}/related`)
}

export function getReviews(id: number, page = 0) {
  return apiJson<PageResult<Review>>(`/api/products/${id}/reviews?page=${page}&size=10&sort=createdAt,desc`)
}

export type ReviewInput = { rating: number; comment: string; variantId: number; heightCm: number | null; weightKg: number | null; imageUrl: string | null }

export async function uploadReviewImage(productId: number, file: File) {
  const body = new FormData()
  body.append('file', file)
  return apiJson<{ imageUrl: string }>(`/api/products/${productId}/reviews/image`, { method: 'POST', body, requireAuth: true })
}

export function postReview(id: number, input: ReviewInput) {
  return apiJson<Review>(`/api/products/${id}/reviews`, { method: 'POST', body: JSON.stringify(input), requireAuth: true })
}

export function getMyReview(id: number) {
  return apiJson<MineReview>(`/api/products/${id}/reviews/mine`, { requireAuth: true })
}

export function updateReview(productId: number, reviewId: number, input: ReviewInput) {
  return apiJson<Review>(`/api/products/${productId}/reviews/${reviewId}`, { method: 'PUT', body: JSON.stringify(input), requireAuth: true })
}
