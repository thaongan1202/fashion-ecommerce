export interface AdminReview {
  id: number;
  productId: number;
  productName: string;
  productThumbnailUrl?: string | null;
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

export interface ReviewedProductOption {
  id: number;
  name: string;
  reviewCount: number;
}

export interface AdminReviewPage {
  content: AdminReview[];
  number: number;
  totalPages: number;
  totalElements: number;
  size: number;
}
