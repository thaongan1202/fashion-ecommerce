package vn.edu.hcmute.fashion.admin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class AdminDashboardDtos {
    private AdminDashboardDtos() {}
    public record DashboardSummary(long activeCustomers, long activeProducts, long totalOrders,
                                   long pendingOrders, long deliveredOrders, long lowStockVariants,
                                   BigDecimal deliveredRevenue, List<DailyRevenue> lastSevenDays) {}
    public record DailyRevenue(LocalDate date, BigDecimal revenue, long deliveredOrders) {}
}
