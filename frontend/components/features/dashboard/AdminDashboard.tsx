/**
 * AdminDashboard component - Dashboard for admin users
 * Uses real API for endpoints that exist: /admin/dashboard/overview and /admin/dashboard/recent-orders
 */

'use client';

import { useState, useEffect, useCallback, useRef } from 'react';
import { Button } from '@/components/ui/button';
import { StatsCard } from './StatsCard';
import { RevenueChart } from './RevenueChart';
import { OrderStatusChart } from './OrderStatusChart';
import { UserRegistrationChart } from './UserRegistrationChart';
import { TopProductsChart } from './TopProductsChart';
import { RecentOrdersTable } from './RecentOrdersTable';
import { LowStockProductsTable } from './LowStockProductsTable';
import { useDashboard } from '@/hooks';
import { dashboardAPI } from '@/lib/api';
import { RevenueChartData, OrderStatusChartData, UserRegistrationChartData, TopProduct, RecentOrder, LowStockProduct, DashboardPeriod, RegistrationPeriod } from '@/types';

// ✅ CONSTANTS for pagination
const ORDERS_PER_PAGE = 10;
const TOTAL_ORDERS_TO_FETCH = 100;

export function AdminDashboard() {
  // Using real API - these endpoints exist:
  // GET /api/v1/admin/dashboard/overview
  // GET /api/v1/admin/dashboard/recent-orders
  // GET /api/v1/dashboard/revenue-chart
  const { stats, loading: statsLoading } = useDashboard();
  
  // Revenue Chart State
  const [revenueData, setRevenueData] = useState<RevenueChartData | null>(null);
  const [revenueLoading, setRevenueLoading] = useState(true);
  const [revenuePeriod, setRevenuePeriod] = useState<DashboardPeriod>('THIRTY_DAYS');
  const revenueRequestId = useRef(0);

  // Order Status Chart State
  const [orderStatusData, setOrderStatusData] = useState<OrderStatusChartData | null>(null);
  const [orderStatusLoading, setOrderStatusLoading] = useState(true);
  const [orderStatusError, setOrderStatusError] = useState<string | null>(null);

  // User Registration Chart State
  const [userRegistrationData, setUserRegistrationData] = useState<UserRegistrationChartData | null>(null);
  const [userRegistrationLoading, setUserRegistrationLoading] = useState(true);
  const [userRegistrationError, setUserRegistrationError] = useState<string | null>(null);

  // Top Products State
  const [topProducts, setTopProducts] = useState<TopProduct[]>([]);
  const [topProductsLoading, setTopProductsLoading] = useState(true);
  const [topProductsError, setTopProductsError] = useState<string | null>(null);

  // ✅ UPDATE: Recent Orders State with client-side pagination
  const [allRecentOrders, setAllRecentOrders] = useState<RecentOrder[]>([]); // Store ALL orders
  const [recentOrdersLoading, setRecentOrdersLoading] = useState(true);
  const [recentOrdersPage, setRecentOrdersPage] = useState(0); // Current page (0-indexed)

  // ✅ Calculate paginated data on-the-fly
  const paginatedOrders = allRecentOrders.slice(
    recentOrdersPage * ORDERS_PER_PAGE,
    (recentOrdersPage + 1) * ORDERS_PER_PAGE
  );
  const recentOrdersHasNext = (recentOrdersPage + 1) * ORDERS_PER_PAGE < allRecentOrders.length;

  // Low Stock Products State
  const [lowStockProducts, setLowStockProducts] = useState<LowStockProduct[]>([]);
  const [lowStockProductsLoading, setLowStockProductsLoading] = useState(true);
  const [lowStockThreshold, setLowStockThreshold] = useState(10); // Default threshold

  // Fetch revenue chart data
  const fetchRevenueData = useCallback(async (period: DashboardPeriod = 'THIRTY_DAYS') => {
    const requestId = ++revenueRequestId.current;
    setRevenuePeriod(period);
    setRevenueLoading(true);
    try {
      const response = await dashboardAPI.getRevenueChart(period);
      if (requestId !== revenueRequestId.current) return;

      if (response.success && response.data) {
        setRevenueData(response.data);
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      if (requestId !== revenueRequestId.current) return;
      console.error('Error fetching revenue chart:', error);
    } finally {
      if (requestId === revenueRequestId.current) {
        setRevenueLoading(false);
      }
    }
  }, []);

  // Fetch order status chart data
  const fetchOrderStatusData = useCallback(async () => {
    try {
      setOrderStatusLoading(true);
      setOrderStatusError(null);
      
      console.log('🔄 [AdminDashboard] Fetching order status chart');
      
      const response = await dashboardAPI.getOrderStatusChart();
      
      console.log('📦 [AdminDashboard] Order Status Response:', {
        success: response.success,
        status: response.status,
        message: response.message,
        timestamp: response.timestamp,
        data: response.data
      });
      
      if (response.success && response.data) {
        console.log('✅ [AdminDashboard] Order status data loaded successfully');
        setOrderStatusData(response.data);
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      console.error('❌ [AdminDashboard] Error fetching order status chart:', error);
      setOrderStatusError(error instanceof Error ? error.message : 'Lỗi khi tải dữ liệu biểu đồ');
    } finally {
      setOrderStatusLoading(false);
    }
  }, []);

  // Fetch user registration chart data
  const fetchUserRegistrationData = useCallback(async (period: RegistrationPeriod = 'WEEKLY') => {
    try {
      setUserRegistrationLoading(true);
      setUserRegistrationError(null);
      
      console.log('🔄 [AdminDashboard] Fetching user registration chart for period:', period);
      
      const response = await dashboardAPI.getUserRegistrationChart(period);
      
      console.log('📦 [AdminDashboard] User Registration Response:', {
        success: response.success,
        status: response.status,
        message: response.message,
        timestamp: response.timestamp,
        data: response.data
      });
      
      if (response.success && response.data) {
        console.log('✅ [AdminDashboard] User registration data loaded successfully');
        setUserRegistrationData(response.data);
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      console.error('❌ [AdminDashboard] Error fetching user registration chart:', error);
      setUserRegistrationError(error instanceof Error ? error.message : 'Lỗi khi tải dữ liệu biểu đồ');
    } finally {
      setUserRegistrationLoading(false);
    }
  }, []);

  // Fetch top products data
  const fetchTopProducts = useCallback(async () => {
    try {
      setTopProductsLoading(true);
      setTopProductsError(null);
      
      console.log('🔄 [AdminDashboard] Fetching top products');
      
      const response = await dashboardAPI.getTopProducts(5);
      
      console.log('📦 [AdminDashboard] Top Products Response:', {
        success: response.success,
        status: response.status,
        message: response.message,
        timestamp: response.timestamp,
        data: response.data
      });
      
      if (response.success && response.data) {
        console.log('✅ [AdminDashboard] Top products data loaded successfully');
        setTopProducts(response.data);
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      console.error('❌ [AdminDashboard] Error fetching top products:', error);
      setTopProductsError(error instanceof Error ? error.message : 'Lỗi khi tải dữ liệu sản phẩm');
    } finally {
      setTopProductsLoading(false);
    }
  }, []);

  // ✅ UPDATE: Fetch ALL recent orders once (client-side pagination)
  const fetchRecentOrders = useCallback(async () => {
    try {
      setRecentOrdersLoading(true);
      
      console.log(`🔄 [AdminDashboard] Fetching recent orders (limit: ${TOTAL_ORDERS_TO_FETCH})`);
      
      const response = await dashboardAPI.getRecentOrders(TOTAL_ORDERS_TO_FETCH);
      
      console.log('📦 [AdminDashboard] Recent Orders Response:', {
        success: response.success,
        status: response.status,
        message: response.message,
        timestamp: response.timestamp,
        dataLength: response.data?.length
      });
      
      if (response.success && response.data) {
        console.log(`✅ [AdminDashboard] Loaded ${response.data.length} recent orders for client-side pagination`);
        setAllRecentOrders(response.data);
        setRecentOrdersPage(0); // Reset to first page
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      console.error('❌ [AdminDashboard] Error fetching recent orders:', error);
      setAllRecentOrders([]);
    } finally {
      setRecentOrdersLoading(false);
    }
  }, []);

  // ✅ UPDATE: Handle page change (no API call, just update state)
  const handleRecentOrdersPageChange = useCallback((page: number) => {
    console.log(`📄 [AdminDashboard] Changing to page ${page}`);
    setRecentOrdersPage(page);
    
    // Optional: Scroll to table top
    const tableElement = document.getElementById('recent-orders-table');
    if (tableElement) {
      tableElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }, []);

  // ✅ ADD: Fetch low stock products
  const fetchLowStockProducts = useCallback(async (threshold = 10) => {
    try {
      setLowStockProductsLoading(true);
      
      console.log(`🔄 [AdminDashboard] Fetching low stock products (threshold: ${threshold})`);
      
      const response = await dashboardAPI.getLowStockProducts(threshold);
      
      console.log('📦 [AdminDashboard] Low Stock Products Response:', {
        success: response.success,
        status: response.status,
        message: response.message,
        timestamp: response.timestamp,
        dataLength: response.data?.length
      });
      
      if (response.success && response.data) {
        console.log(`✅ [AdminDashboard] Loaded ${response.data.length} low stock products`);
        setLowStockProducts(response.data);
        setLowStockThreshold(threshold);
      } else {
        throw new Error(response.message || 'Invalid response');
      }
    } catch (error) {
      console.error('❌ [AdminDashboard] Error fetching low stock products:', error);
      setLowStockProducts([]);
    } finally {
      setLowStockProductsLoading(false);
    }
  }, []);

  // ✅ ADD: Handle threshold change
  const handleThresholdChange = useCallback((threshold: number) => {
    fetchLowStockProducts(threshold);
  }, [fetchLowStockProducts]);

  // Fetch initial revenue data
  useEffect(() => {
    fetchRevenueData();
    fetchOrderStatusData();
    fetchUserRegistrationData();
    fetchTopProducts();
    fetchRecentOrders();
    fetchLowStockProducts(); // ✅ ADD: Fetch low stock products
  }, []);

  if (statsLoading) {
    return (
      <div className="space-y-6">
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="bg-card rounded-xl border border-border p-4 animate-pulse h-24" />
          ))}
        </div>
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-96" />
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-64" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Stats Grid - 4 Cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {stats.map((stat, index) => (
          <StatsCard key={index} {...stat} />
        ))}
      </div>

      {/* Revenue Chart */}
      {!revenueData && revenueLoading ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-96" />
      ) : revenueData ? (
        <RevenueChart
          data={revenueData}
          selectedPeriod={revenuePeriod}
          loading={revenueLoading}
          onPeriodChange={fetchRevenueData}
        />
      ) : null}

      {/* Order Status Chart */}
      {orderStatusLoading ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-96" />
      ) : orderStatusError ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6">
          <div className="flex flex-col items-center justify-center h-64 space-y-4">
            <p className="text-sm text-destructive">{orderStatusError}</p>
            <Button 
              variant="outline" 
              size="sm"
              onClick={fetchOrderStatusData}
            >
              Thử lại
            </Button>
          </div>
        </div>
      ) : orderStatusData ? (
        <OrderStatusChart data={orderStatusData} />
      ) : null}

      {/* User Registration Chart */}
      {userRegistrationLoading ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-96" />
      ) : userRegistrationError ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6">
          <div className="flex flex-col items-center justify-center h-64 space-y-4">
            <p className="text-sm text-destructive">{userRegistrationError}</p>
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => fetchUserRegistrationData()}
            >
              Thử lại
            </Button>
          </div>
        </div>
      ) : userRegistrationData ? (
        <UserRegistrationChart 
          data={userRegistrationData}
          onPeriodChange={fetchUserRegistrationData}
        />
      ) : null}

      {/* Top Products Chart */}
      {topProductsLoading ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6 animate-pulse h-96" />
      ) : topProductsError ? (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6">
          <div className="flex flex-col items-center justify-center h-64 space-y-4">
            <p className="text-sm text-destructive">{topProductsError}</p>
            <Button 
              variant="outline" 
              size="sm"
              onClick={fetchTopProducts}
            >
              Thử lại
            </Button>
          </div>
        </div>
      ) : topProducts.length > 0 ? (
        <TopProductsChart data={topProducts} />
      ) : (
        <div className="bg-card rounded-xl border border-border p-4 md:p-6">
          <div className="flex flex-col items-center justify-center h-64 space-y-2">
            <p className="text-sm text-muted-foreground">Chưa có dữ liệu sản phẩm bán chạy</p>
          </div>
        </div>
      )}

      {/* ✅ UPDATE: Recent Orders Table with client-side pagination */}
      <div id="recent-orders-table">
        <RecentOrdersTable 
          data={paginatedOrders} 
          loading={recentOrdersLoading}
          currentPage={recentOrdersPage}
          hasNext={recentOrdersHasNext}
          onPageChange={handleRecentOrdersPageChange}
        />
      </div>

      {/* ✅ ADD: Low Stock Products Table */}
      <LowStockProductsTable 
        data={lowStockProducts}
        loading={lowStockProductsLoading}
        threshold={lowStockThreshold}
        onThresholdChange={handleThresholdChange}
      />
    </div>
  );
}
