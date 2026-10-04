import { fetchWithAuth, getAuthToken } from '@/lib/api';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081/api/v1';

export interface ProductReview {
  id: number;
  authorName: string;
  rating: number;
  comment: string;
  createdAt: string;
  verifiedPurchase: boolean;
  materialRating?: number | null;
  fitRating?: number | null;
  colorRating?: number | null;
  productColor?: string | null;
  productSize?: string | null;
  imageUrls: string[];
}

export interface ReviewOrderOption {
  orderId: number;
  orderCode: string;
  productColor?: string | null;
  productSize?: string | null;
}

export interface ProductReviewsResponse {
  reviews: ProductReview[];
  averageRating: number;
  totalReviews: number;
  canReview: boolean;
  eligibleOrders: ReviewOrderOption[];
  reviewedOrderIds: number[];
  eligibilityMessage: string;
}

export interface CreateProductReviewPayload {
  orderId: number;
  rating: number;
  materialRating: number;
  fitRating: number;
  colorRating: number;
  comment: string;
  imageUrls: string[];
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

  const response = await fetchWithAuth(url, { ...init, headers, cache: 'no-store' });
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
  payload: CreateProductReviewPayload,
): Promise<ProductReviewsResponse> {
  return request(`${API_BASE_URL}/products/${productId}/reviews`, {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function uploadReviewImages(productId: number, orderId: number, files: File[]): Promise<string[]> {
  const formData = new FormData();
  formData.append('orderId', String(orderId));
  files.forEach((file) => formData.append('files', file));

  const headers = new Headers();
  const token = getAuthToken();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetchWithAuth(`${API_BASE_URL}/products/${productId}/reviews/images`, {
    method: 'POST',
    headers,
    body: formData,
    cache: 'no-store',
  });
  const result = await response.json() as ApiResponse<string[]>;
  if (!response.ok || !result.success) {
    throw new Error(result.message || 'Không thể tải ảnh đánh giá.');
  }
  return result.data;
}

const API_ORIGIN = API_BASE_URL.replace(/\/api\/v1\/?$/, '');

export function getReviewImageUrl(url: string): string {
  return /^https?:\/\//i.test(url) ? url : `${API_ORIGIN}${url}`;
}
