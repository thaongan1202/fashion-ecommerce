export type ProductImage = { id: number; imageUrl: string; primary: boolean }
export type ProductVariant = { id: number; size: string | null; color: string | null; price: number; stockQty: number }
export type Product = {
  id: number
  name: string
  description: string | null
  categoryId: number
  categoryName: string
  brandId: number
  brandName: string
  material: string | null
  minPrice: number
  maxPrice: number
  averageRating: number
  reviewCount: number
  images: ProductImage[]
  variants: ProductVariant[]
}
export type CategoryOption = { id: number; name: string }
export type PriceRange = { min: number; max: number }
export type ReviewReminder = { productId: number; productName: string; deliveredAt: string }
export type PageResult<T> = { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
export type Review = {
  id: number; userName: string; rating: number; comment: string; imageUrl?: string | null; status: string
  variantId: number | null; size: string | null; color: string | null; heightCm: number | null; weightKg: number | null; createdAt: string
}
export type PurchasedReviewVariant = { id: number; size: string | null; color: string | null }
export type MineReview = { eligible: boolean; review: Review | null; purchasedVariants: PurchasedReviewVariant[] }
