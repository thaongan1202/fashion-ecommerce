'use client';

import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { 
  Star, 
  ShoppingCart, 
  Heart,
  ExternalLink,
  Check,
  X
} from 'lucide-react';
import Image from 'next/image';
import Link from 'next/link';
import { productViewService } from '@/services/product-view.service';
import type { ProductDetailViewResponse } from '@/types/product-view';

interface QuickViewModalProps {
  productId: number | null;
  open: boolean;
  onClose: () => void;
}

export function QuickViewModal({ productId, open, onClose }: QuickViewModalProps) {
  const [product, setProduct] = useState<ProductDetailViewResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string>('');
  const [selectedTemplate, setSelectedTemplate] = useState<number>(0);

  useEffect(() => {
    if (productId && open) {
      fetchProduct();
    }
  }, [productId, open]);

  const fetchProduct = async () => {
    if (!productId) return;
    
    try {
      setLoading(true);
      const data = await productViewService.getProductById(productId);
      setProduct(data);
      setSelectedImage(data.thumbnailUrl || '');
      setSelectedTemplate(0);
    } catch (error) {
      console.error('Quick view error:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const handleAddToCart = () => {
    // Implement add to cart logic
    console.log('Add to cart:', product?.id);
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        {loading || !product ? (
          <QuickViewSkeleton />
        ) : (
          <>
            <DialogHeader>
              <DialogTitle className="text-xl">{product.name}</DialogTitle>
            </DialogHeader>

            <div className="grid md:grid-cols-2 gap-6">
              {/* Left Column - Images */}
              <div>
                <div className="relative aspect-square mb-4 overflow-hidden rounded-lg border">
                  <Image
                    src={selectedImage || product.thumbnailUrl || '/placeholder-product.png'}
                    alt={product.name}
                    fill
                    className="object-cover"
                  />
                </div>
                
                {/* Thumbnail Gallery */}
                {product.images && product.images.length > 1 && (
                  <div className="grid grid-cols-5 gap-2">
                    {product.images.slice(0, 5).map((img, idx) => (
                      <button
                        key={idx}
                        onClick={() => setSelectedImage(img.imageUrl)}
                        className={`relative aspect-square overflow-hidden rounded border-2 transition-colors ${
                          selectedImage === img.imageUrl
                            ? 'border-primary'
                            : 'border-transparent hover:border-gray-300'
                        }`}
                      >
                        <Image
                          src={img.imageUrl}
                          alt={`${product.name} ${idx + 1}`}
                          fill
                          className="object-cover"
                        />
                      </button>
                    ))}
                  </div>
                )}
              </div>

              {/* Right Column - Info */}
              <div className="space-y-4">
                {/* Badges */}
                <div className="flex flex-wrap gap-2">
                  <Badge variant="outline">{product.brandName}</Badge>
                  <Badge variant="secondary">{product.categoryName}</Badge>
                  {product.totalStock > 0 ? (
                    <Badge variant="default" className="bg-green-500">
                      <Check className="w-3 h-3 mr-1" />
                      Còn hàng
                    </Badge>
                  ) : (
                    <Badge variant="secondary">
                      <X className="w-3 h-3 mr-1" />
                      Hết hàng
                    </Badge>
                  )}
                </div>

                {/* Rating */}
                <div className="flex items-center gap-3">
                  <div className="flex items-center">
                    {[1, 2, 3, 4, 5].map((star) => (
                      <Star
                        key={star}
                        className={`w-5 h-5 ${
                          star <= Math.round(product.averageRating)
                            ? 'fill-yellow-400 text-yellow-400'
                            : 'text-gray-300'
                        }`}
                      />
                    ))}
                  </div>
                  <span className="text-sm font-medium">
                    {product.averageRating.toFixed(1)}
                  </span>
                  <span className="text-sm text-muted-foreground">
                    ({product.totalReviews} đánh giá)
                  </span>
                </div>

                {/* Price */}
                <div>
                  <p className="text-3xl font-bold text-primary">
                    {product.variants && product.variants[selectedTemplate]
                      ? formatPrice(product.variants[selectedTemplate].discountedPrice || product.variants[selectedTemplate].originalPrice || 0)
                      : 'Liên hệ'}
                  </p>
                </div>

                {/* Variants */}
                {product.variants && product.variants.length > 1 && (
                  <div>
                    <p className="text-sm font-semibold mb-2">Chọn màu và kích thước:</p>
                    <div className="flex flex-wrap gap-2">
                      {product.variants.map((template, idx) => (
                        <Button
                          key={template.id}
                          variant={selectedTemplate === idx ? 'default' : 'outline'}
                          size="sm"
                          onClick={() => setSelectedTemplate(idx)}
                        >
                          {template.color}{template.size ? ` / ${template.size}` : ''}
                        </Button>
                      ))}
                    </div>
                  </div>
                )}

                {/* Key Specs */}
                {(() => {
                  const info = product.fashionInfo || product.technicalSpecs;
                  if (!info) return null;
                  return (
                  <div className="border rounded-lg p-4 space-y-2">
                    <h4 className="font-semibold text-sm mb-3">Thông tin sản phẩm</h4>
                    {info.material && (
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Chất liệu:</span>
                        <span className="font-medium">{info.material}</span>
                      </div>
                    )}
                    {info.style && (
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Phong cách:</span>
                        <span className="font-medium">{info.style}</span>
                      </div>
                    )}
                    {info.targetAudience && (
                      <div className="flex justify-between text-sm">
                        <span className="text-muted-foreground">Đối tượng:</span>
                        <span className="font-medium">{info.targetAudience}</span>
                      </div>
                    )}
                  </div>
                  );
                })()}

                {/* Description */}
                {product.description && (
                  <div>
                    <p className="text-sm text-muted-foreground line-clamp-3">
                      {product.description}
                    </p>
                  </div>
                )}

                {/* Actions */}
                <div className="flex gap-3 pt-4">
                  <Button
                    onClick={handleAddToCart}
                    disabled={product.totalStock === 0}
                    className="flex-1"
                    size="lg"
                  >
                    <ShoppingCart className="w-5 h-5 mr-2" />
                    Thêm vào giỏ
                  </Button>
                  
                  <Button
                    variant="outline"
                    size="lg"
                  >
                    <Heart className="w-5 h-5" />
                  </Button>
                </div>

                {/* View Full Details Link */}
                <Link href={`/products/${product.id}`}>
                  <Button variant="outline" className="w-full" onClick={onClose}>
                    Xem chi tiết đầy đủ
                    <ExternalLink className="w-4 h-4 ml-2" />
                  </Button>
                </Link>
              </div>
            </div>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}

function QuickViewSkeleton() {
  return (
    <div className="grid md:grid-cols-2 gap-6">
      <div>
        <Skeleton className="aspect-square mb-4" />
        <div className="grid grid-cols-5 gap-2">
          {[1, 2, 3, 4, 5].map((i) => (
            <Skeleton key={i} className="aspect-square" />
          ))}
        </div>
      </div>
      <div className="space-y-4">
        <Skeleton className="h-6 w-full" />
        <Skeleton className="h-6 w-3/4" />
        <Skeleton className="h-10 w-1/2" />
        <Skeleton className="h-32 w-full" />
        <Skeleton className="h-12 w-full" />
      </div>
    </div>
  );
}
