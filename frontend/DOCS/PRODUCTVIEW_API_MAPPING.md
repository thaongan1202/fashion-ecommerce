# ProductView API Mapping

Tài liệu mapping giữa Backend ProductView API và Frontend Service.

## 📋 Danh sách API đã implement

### ✅ Core APIs

| API Endpoint | Method | Frontend Service | Status | Ghi chú |
|-------------|--------|-----------------|--------|---------|
| `/api/v1/products/all` | GET | `getAllProducts()` | ✅ | Lấy tất cả sản phẩm với phân trang |
| `/api/v1/products/search` | GET | `searchProducts()` | ✅ | Tìm kiếm và lọc sản phẩm |
| `/api/v1/products/{id}` | GET | `getProductById()` | ✅ | Xem chi tiết sản phẩm |
| `/api/v1/products/{id}/detail-with-sold` | GET | `getProductDetailWithSoldCount()` | ✅ | Chi tiết SP kèm số lượng đã bán |
| `/api/v1/products/{id}/related` | GET | `getRelatedProducts()` | ✅ | Sản phẩm liên quan |
| `/api/v1/products/category/{categoryId}` | GET | `getProductsByCategory()` | ✅ | Sản phẩm theo danh mục |

### ✅ Featured Lists APIs

| API Endpoint | Method | Frontend Service | Status | Ghi chú |
|-------------|--------|-----------------|--------|---------|
| `/api/v1/products/best-selling` | GET | `getBestSellingProducts()` | ✅ | Sản phẩm bán chạy |
| `/api/v1/products/new-arrivals` | GET | `getNewArrivals()` | ✅ | Sản phẩm mới nhất |
| `/api/v1/products/featured` | GET | `getFeaturedProducts()` | ✅ | Sản phẩm nổi bật |

### ✅ Filter APIs

| API Endpoint | Method | Frontend Service | Status | Ghi chú |
|-------------|--------|-----------------|--------|---------|
| `/api/v1/products/filter/ram` | GET | `filterByRam()` | ✅ | Lọc theo RAM |
| `/api/v1/products/filter/storage` | GET | `filterByStorage()` | ✅ | Lọc theo bộ nhớ |
| `/api/v1/products/filter/battery` | GET | `filterByBattery()` | ✅ | Lọc theo pin |
| `/api/v1/products/filter/screen` | GET | `filterByScreenSize()` | ✅ | Lọc theo màn hình |
| `/api/v1/products/filter/os` | GET | `filterByOS()` | ✅ | Lọc theo hệ điều hành |
| `/api/v1/products/filter/rating` | GET | `filterByRating()` | ✅ | Lọc theo đánh giá |
| `/api/v1/products/filter/sold-count` | GET | `filterBySoldCount()` | ✅ | Lọc theo số lượng bán |

### ✅ Other APIs

| API Endpoint | Method | Frontend Service | Status | Ghi chú |
|-------------|--------|-----------------|--------|---------|
| `/api/v1/products/compare` | POST | `compareProducts()` | ✅ | So sánh sản phẩm |

## 🔄 Response Mapping

### Backend Response Structure
```json
{
  "success": true,
  "status": 0,
  "message": "string",
  "data": {
    "id": 0,
    "name": "string",
    "category": { "id": 0, "name": "string", "slug": "string" },
    "brand": { "id": 0, "name": "string", "logoUrl": "string" },
    "variants": [...],
    "technicalSpecs": {...},
    "images": [...]
  }
}
```

### Frontend Internal Structure
```typescript
{
  id: number,
  name: string,
  categoryId: number,      // mapped from category.id
  categoryName: string,    // mapped from category.name
  brandId: number,         // mapped from brand.id
  brandName: string,       // mapped from brand.name
  templates: [...],        // mapped from variants
  specifications: {...},   // mapped from technicalSpecs
  images: [...]
}
```

## 📝 Mapping Details

### 1. Category & Brand Mapping
```typescript
// Backend
category: { id: 1, name: "Điện thoại", slug: "dien-thoai" }

// Frontend
categoryId: 1
categoryName: "Điện thoại"
```

### 2. Variants → Templates Mapping
```typescript
// Backend
variants: [{
  id: 1,
  sku: "IP15PM-256-BLK",
  color: "Titan Black",
  storage: "256GB",
  ram: "8GB",
  price: 32990000,
  stockQuantity: 50,
  status: true
}]

// Frontend
templates: [{
  id: 1,
  sku: "IP15PM-256-BLK",
  color: "Titan Black",
  storage: "256GB",
  ram: "8GB",
  price: 32990000,
  stockQuantity: 50,
  status: true
}]
```

### 3. TechnicalSpecs → Specifications Mapping
```typescript
// Backend
technicalSpecs: {
  screen: "6.7 inch Super Retina XDR OLED",
  os: "iOS 17",
  cpu: "Apple A17 Pro",
  ram: "8GB",
  battery: "4422 mAh"
}

// Frontend
specifications: {
  screen: "6.7 inch Super Retina XDR OLED",
  os: "iOS 17",
  cpu: "Apple A17 Pro",
  ram: "8GB",
  battery: "4422 mAh"
}
```

## 🎯 Usage Examples

### Example 1: Get Product Detail
```typescript
import { productViewService } from '@/services/product-view.service';

// Lấy chi tiết sản phẩm
const product = await productViewService.getProductById(1);

console.log(product.name); // "iPhone 15 Pro Max"
console.log(product.templates[0].price); // 32990000
console.log(product.specifications.screen); // "6.7 inch..."
```

### Example 2: Search with Filters
```typescript
// Tìm kiếm sản phẩm với nhiều bộ lọc
const results = await productViewService.searchProducts({
  keyword: "iPhone",
  categoryId: 1,
  brandIds: [1],
  minPrice: 20000000,
  maxPrice: 35000000,
  ramOptions: ["8GB"],
  storageOptions: ["256GB", "512GB"],
  page: 0,
  size: 20,
  sortBy: "price",
  sortDirection: "asc"
});
```

### Example 3: Get Featured Products
```typescript
// Lấy 10 sản phẩm nổi bật
const featured = await productViewService.getFeaturedProducts(10);
```

### Example 4: Filter by Multiple Criteria
```typescript
// Lọc theo RAM
const ramFiltered = await productViewService.filterByRam(
  ["8GB", "12GB"],
  { page: 0, size: 20 }
);

// Lọc theo battery
const batteryFiltered = await productViewService.filterByBattery(
  4000, // minBattery
  5000, // maxBattery
  { page: 0, size: 20 }
);
```

## 📊 Response Types

### ProductViewResponse
```typescript
interface ProductViewResponse {
  id: number;
  name: string;
  thumbnailUrl?: string;
  categoryId: number;
  categoryName: string;
  brandId: number;
  brandName: string;
  minPrice: number;
  maxPrice: number;
  averageRating: number;
  totalReviews: number;
  inStock: boolean;
  totalStock: number;
  soldCount: number;
  images: ProductImageInfo[];
  variantsCount: number;
  ram?: string;
  storage?: string;
  // ... more fields
}
```

### ProductDetailViewResponse
```typescript
interface ProductDetailViewResponse {
  id: number;
  name: string;
  description?: string;
  thumbnailUrl?: string;
  categoryId: number;
  categoryName: string;
  brandId: number;
  brandName: string;
  templates: ProductTemplateInfo[];
  averageRating: number;
  totalReviews: number;
  totalStock: number;
  soldCount: number;
  images: ProductImageInfo[];
  specifications: Record<string, any>;
  createdAt: string;
  updatedAt?: string;
}
```

## 🔧 Debugging

### Enable Logging
Tất cả service methods đều có console.log để debug:

```typescript
// Trong getProductById()
console.log('📦 Raw API response:', apiData);
console.log('✅ Mapped product data:', mapped);
```

### Check Console
Khi gọi API, check browser console để xem:
- Raw API response
- Mapped data
- Any errors

## ⚠️ Known Issues & Notes

1. **Price = 0**: Nếu giá = 0, check:
   - Backend có trả về `variants[0].price` không?
   - Database có data trong `product_templates` không?

2. **Specifications Empty**: Nếu specifications = {}, check:
   - Backend có trả về `technicalSpecs` không?
   - Database có data trong `product_metadata` không?

3. **Images 404**: 
   - Đã thay đổi sang placeholder.com URLs
   - Nếu vẫn 404, check `init.sql`

## 🚀 Next Steps

1. Implement product comparison page
2. Add advanced filtering UI
3. Optimize API calls with caching
4. Add error boundaries
5. Implement retry logic

## 📚 Related Files

- Service: `frontend/services/product-view.service.ts`
- Types: `frontend/types/product-view.d.ts`
- Hooks: `frontend/hooks/useProductView.ts`
- Components: 
  - `frontend/app/(main)/products/page.tsx`
  - `frontend/app/(main)/products/[id]/page.tsx`
  - `frontend/components/features/products/ProductCard.tsx`
  - `frontend/components/features/products/ProductFilterSidebar.tsx`
