import { getAuthToken } from '@/lib/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

export interface ProductReview {
  id: number;
  authorName: string;
  rating: number;
  comment: string;
  createdAt: string;
  verifiedPurchase: boolean;
}

export interface ReviewOrderOption {
  orderId: number;
  orderCode: string;
}

export interface ProductReviewsResponse {
  reviews: ProductReview[];
  averageRating: number;
  totalReviews: number;
  canReview: boolean;
  eligibleOrders: ReviewOrderOption[];
  eligibilityMessage: string;
}

interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set('Content-Type', 'application/json');

  const token = getAuthToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(url, { ...init, headers, cache: 'no-store' });
  const result = await response.json() as ApiResponse<T>;

  if (!response.ok || !result.success) {
    throw new Error(result.message || 'Không thể tải đánh giá sản phẩm.');
  }

  return result.data;
}

export function getProductReviews(productId: number): Promise<ProductReviewsResponse> {
  return request(`${API_BASE_URL}/products/${productId}/reviews`);
}

export function submitProductReview(
  productId: number,
  payload: { orderId: number; rating: number; comment: string },
): Promise<ProductReviewsResponse> {
  return request(`${API_BASE_URL}/products/${productId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}
