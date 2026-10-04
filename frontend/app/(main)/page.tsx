"use client";

import {
  HeroBanner,
  FlashSaleSection,
  FeaturedProducts,
  BestSellingSection,
  NewArrivalsSection,
  ViewAllProductsSection,
  QuickLinksBar,
} from "@/components/features";

export default function HomePage() {
  return (
    <>
      <HeroBanner
        productId={1}
        productName="Áo thun basic cotton"
        productImage="https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=600"
        description="Cotton thoáng, form dễ mặc, phối được quần jeans hoặc kaki cả tuần."
        salePrice={169000}
        originalPrice={199000}
        badge="GIẢM 15%"
      />

      {/* Quick Navigation Bar - Sticky */}
      <section className="border-b bg-background/95 sticky top-16 z-40 backdrop-blur-sm shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <QuickLinksBar />
        </div>
      </section>

      {/* Flash Sale - Ưu tiên cao vì urgency */}
      <FlashSaleSection />

      {/* Sản phẩm nổi bật */}
      <FeaturedProducts />

      {/* Sản phẩm bán chạy */}
      <BestSellingSection />

      {/* Sản phẩm mới nhất */}
      <NewArrivalsSection />

      {/* Nút xem tất cả sản phẩm */}
      <ViewAllProductsSection />
    </>
  );
}
