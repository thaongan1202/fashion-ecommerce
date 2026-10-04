/**
 * Centralized mock data for development
 * TODO: Replace with real API calls
 */

export interface Product {
  id: number;
  name: string;
  image: string;
  originalPrice: number; // For mock data compatibility
  salePrice: number; // For mock data compatibility
  // Backend fields
  price?: number; // Original price from backend
  discountPercent?: number; // Discount % from active DISCOUNT promotions (0-100)
  discountedPrice?: number; // Price after discount
  rating: number;
  reviews: number;
  discount: number; // For mock data compatibility
  isNew?: boolean;
  stock?: number;
  category?: string;
  sales?: number;
}

export interface Order {
  id: number;
  customer: string;
  total: number;
  status: "pending" | "processing" | "shipped" | "delivered" | "cancelled";
  date: string;
  items: number;
}

export interface UserData {
  id: number;
  name: string;
  email: string;
  role: string;
  status: "ACTIVE" | "INACTIVE" | "BANNED";
  joinDate: string;
}

/**
 * Mock featured products
 */
export const MOCK_FEATURED_PRODUCTS: Product[] = [
  {
    id: 1,
    name: "Áo thun basic cotton",
    image: "👕",
    originalPrice: 249000,
    salePrice: 199000,
    rating: 4.9,
    reviews: 256,
    discount: 20,
    isNew: true,
  },
  {
    id: 2,
    name: "Quần jeans ống rộng",
    image: "👖",
    originalPrice: 599000,
    salePrice: 549000,
    rating: 4.8,
    reviews: 189,
    discount: 8,
    isNew: true,
  },
  {
    id: 3,
    name: "Áo sơ mi oversize",
    image: "👔",
    originalPrice: 399000,
    salePrice: 349000,
    rating: 4.7,
    reviews: 124,
    discount: 13,
    isNew: false,
  },
  {
    id: 4,
    name: "Sneaker trắng basic",
    image: "👟",
    originalPrice: 799000,
    salePrice: 699000,
    rating: 4.6,
    reviews: 98,
    discount: 13,
    isNew: false,
  },
];

/**
 * Mock flash sale products
 */
export const MOCK_FLASH_SALE_PRODUCTS: Product[] = [
  {
    id: 5,
    name: "Áo polo pique",
    image: "👕",
    originalPrice: 349000,
    salePrice: 299000,
    rating: 4.8,
    reviews: 512,
    discount: 22,
  },
  {
    id: 6,
    name: "Chân váy chữ A",
    image: "👗",
    originalPrice: 389000,
    salePrice: 329000,
    rating: 4.5,
    reviews: 324,
    discount: 23,
  },
  {
    id: 7,
    name: "Túi tote canvas",
    image: "👜",
    originalPrice: 229000,
    salePrice: 189000,
    rating: 4.4,
    reviews: 156,
    discount: 23,
  },
  {
    id: 8,
    name: "Mũ lưỡi trai basic",
    image: "🧢",
    originalPrice: 159000,
    salePrice: 129000,
    rating: 4.3,
    reviews: 87,
    discount: 20,
  },
];

/**
 * Mock products for admin management
 */
export const MOCK_PRODUCTS: Product[] = [
  {
    id: 1,
    name: "Áo thun basic cotton",
    image: "👕",
    originalPrice: 249000,
    salePrice: 199000,
    rating: 4.9,
    reviews: 234,
    discount: 6,
    stock: 45,
    category: "Quần áo",
    sales: 234,
  },
  {
    id: 2,
    name: "Quần jeans ống rộng",
    image: "👖",
    originalPrice: 599000,
    salePrice: 549000,
    rating: 4.8,
    reviews: 189,
    discount: 12,
    stock: 32,
    category: "Quần áo",
    sales: 189,
  },
  {
    id: 3,
    name: "Áo sơ mi oversize",
    image: "👔",
    originalPrice: 399000,
    salePrice: 349000,
    rating: 4.7,
    reviews: 156,
    discount: 8,
    stock: 28,
    category: "Quần áo",
    sales: 156,
  },
  {
    id: 4,
    name: "Sneaker trắng basic",
    image: "👟",
    originalPrice: 799000,
    salePrice: 699000,
    rating: 4.6,
    reviews: 142,
    discount: 10,
    stock: 50,
    category: "Giày dép",
    sales: 142,
  },
  {
    id: 5,
    name: "Đầm nữ cổ vuông",
    image: "👗",
    originalPrice: 529000,
    salePrice: 459000,
    rating: 4.5,
    reviews: 87,
    discount: 7,
    stock: 15,
    category: "Quần áo",
    sales: 87,
  },
];

/**
 * Mock orders
 */
export const MOCK_ORDERS: Order[] = [
  {
    id: 1001,
    customer: "Nguyễn Văn A",
    total: 199000,
    status: "delivered",
    date: "2024-01-15",
    items: 1,
  },
  {
    id: 1002,
    customer: "Trần Thị B",
    total: 549000,
    status: "shipped",
    date: "2024-01-14",
    items: 1,
  },
  {
    id: 1003,
    customer: "Lê Văn C",
    total: 748000,
    status: "processing",
    date: "2024-01-13",
    items: 2,
  },
  {
    id: 1004,
    customer: "Phạm Thị D",
    total: 349000,
    status: "pending",
    date: "2024-01-12",
    items: 1,
  },
  {
    id: 1005,
    customer: "Hoàng Văn E",
    total: 888000,
    status: "delivered",
    date: "2024-01-11",
    items: 2,
  },
];

/**
 * Mock users
 */
export const MOCK_USERS: UserData[] = [
  {
    id: 1,
    name: "Nguyễn Văn A",
    email: "nguyenvana@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-01",
  },
  {
    id: 2,
    name: "Trần Thị B",
    email: "tranthib@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-02",
  },
  {
    id: 3,
    name: "Lê Văn C",
    email: "levanc@example.com",
    role: "CUSTOMER",
    status: "INACTIVE",
    joinDate: "2024-01-03",
  },
  {
    id: 4,
    name: "Phạm Thị D",
    email: "phamthid@example.com",
    role: "CUSTOMER",
    status: "ACTIVE",
    joinDate: "2024-01-04",
  },
  {
    id: 5,
    name: "Admin User",
    email: "admin@utefashionhub.com",
    role: "ADMIN",
    status: "ACTIVE",
    joinDate: "2024-01-05",
  },
];

/**
 * Mock dashboard stats
 */
export const MOCK_STATS = [
  {
    label: "Doanh thu",
    value: "1.234.567.890₫",
    change: "+20.1%",
    colorClass: "text-green-500",
  },
  {
    label: "Đơn hàng",
    value: "1,234",
    change: "+15.3%",
    colorClass: "text-blue-500",
  },
  {
    label: "Người dùng",
    value: "5,678",
    change: "+8.2%",
    colorClass: "text-purple-500",
  },
  {
    label: "Sản phẩm",
    value: "256",
    change: "+4.3%",
    colorClass: "text-orange-500",
  },
];
