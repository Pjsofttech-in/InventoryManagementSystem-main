//package com.InventoryManagementSystem.serviceimpl;
//
//import com.InventoryManagementSystem.DTO.*;
//import com.InventoryManagementSystem.Enum.PaymentStatus;
//import com.InventoryManagementSystem.entity.InventoryOrder;
//import com.InventoryManagementSystem.entity.InventoryOrderItem;
//import com.InventoryManagementSystem.entity.InventoryPayment;
//import com.InventoryManagementSystem.entity.InventoryPayment;
//import com.InventoryManagementSystem.entity.InventoryProduct;
//import com.InventoryManagementSystem.repository.OrderItemRepository;
//import com.InventoryManagementSystem.repository.OrderRepository;
//import com.InventoryManagementSystem.repository.PaymentRepository;
//import com.InventoryManagementSystem.repository.ProductRepository;
//import com.InventoryManagementSystem.service.DashboardService;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Service
//public class DashboardServiceImpl implements DashboardService {
// private final OrderRepository orderRepository; private final ProductRepository productRepository; private final PaymentRepository paymentRepository; private final OrderItemRepository orderItemRepository;
// private static final int DEFAULT_LOW_STOCK_THRESHOLD=5;
// public DashboardServiceImpl(OrderRepository o, ProductRepository p, PaymentRepository pay, OrderItemRepository oi){this.orderRepository=o;this.productRepository=p;this.paymentRepository=pay;this.orderItemRepository=oi;}
// @Override public DashboardSummaryDTO getDashboardSummary(){
//   LocalDate today=LocalDate.now();
//   BigDecimal total=n(orderRepository.findTotalRevenue());
//   BigDecimal todaySales=sumOrders(today,today), seven=sumOrders(today.minusDays(6),today), thirty=sumOrders(today.minusDays(29),today), year=sumOrders(today.minusDays(364),today);
//   BigDecimal paid=BigDecimal.ZERO;
//   for(InventoryOrder o:orderRepository.findAll()) paid=paid.add(BigDecimal.valueOf(paymentSummary(o).paid));
//   BigDecimal pending=total.subtract(paid).max(BigDecimal.ZERO);
//   return DashboardSummaryDTO.builder().todaysSales(todaySales).sevenDaysSales(seven).thirtyDaysSales(thirty).oneYearSales(year).totalRevenue(total).paidRevenue(paid).pendingRevenue(pending).totalOrdersToday(orderRepository.countTodaysOrders(today)).totalStockQuantity(productRepository.findTotalStockQuantity()).lowStockCount(productRepository.countLowStockProducts(DEFAULT_LOW_STOCK_THRESHOLD)).build();
// }
// private BigDecimal sumOrders(LocalDate s,LocalDate e){return orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(s,e).stream().map(o->BigDecimal.valueOf(v(o.getTotalAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);}
// @Override public List<TopProductDTO> getTopSellingProducts(int limit){return productRepository.findTopSellingProducts(PageRequest.of(0,limit));}
// @Override public List<TopCategoryDTO> getTopSellingCategories(int limit){return productRepository.findTopSellingCategories(PageRequest.of(0,limit));}
// @Override public List<InventoryProduct> getLowStockAlerts(int threshold){return productRepository.findLowStockProducts(threshold);}
// @Override public List<RevenuePointDTO> getDailyRevenue(int days){
//   int safe=Math.max(1,Math.min(days,365)); LocalDate end=LocalDate.now(), start=end.minusDays(safe-1);
//   Map<LocalDate,BigDecimal> map=new LinkedHashMap<>(); for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))map.put(d,BigDecimal.ZERO);
//   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end)) map.computeIfPresent(o.getOrderDate(),(k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
//   List<RevenuePointDTO> out=new ArrayList<>(); map.forEach((d,a)->out.add(new RevenuePointDTO(d.toString(),a))); return out;
// }
// @Override public List<OrderRevenueDTO> getOrderWiseRevenue(int limit){
//   return orderRepository.findTop10ByOrderByOrderDateDescIdDesc().stream().limit(Math.max(1,Math.min(limit,50))).map(o->{Summary s=paymentSummary(o);return new OrderRevenueDTO(o.getId(),"Order #"+o.getId(),BigDecimal.valueOf(v(o.getTotalAmount())),BigDecimal.valueOf(s.paid),BigDecimal.valueOf(s.due));}).toList();
// }
// @Override public List<RevenuePointDTO> getPaymentStatusRevenue(){
//   BigDecimal paid=BigDecimal.ZERO,due=BigDecimal.ZERO; for(InventoryOrder o:orderRepository.findAll()){Summary s=paymentSummary(o);paid=paid.add(BigDecimal.valueOf(s.paid));due=due.add(BigDecimal.valueOf(s.due));}
//   return List.of(new RevenuePointDTO("Paid",paid),new RevenuePointDTO("Pending",due));
// }
// private Summary paymentSummary(InventoryOrder order){double paid=paymentRepository.findByOrderIdOrderByPaymentDateDesc(order.getId()).stream().filter(p->p.getPaymentStatus()==PaymentStatus.PAID||p.getPaymentStatus()==PaymentStatus.PARTIAL).mapToDouble(p->v(p.getAmount())).sum(); double total=v(order.getTotalAmount()); return new Summary(Math.min(paid,total),Math.max(0,total-paid));}
// private static double v(Double x){return x==null?0:x;} private static BigDecimal n(BigDecimal x){return x==null?BigDecimal.ZERO:x;} private record Summary(double paid,double due){}
//
// // =========================
// // REVENUE / SELL ANALYTICS (1D, 7D, 1M, 1Y)
// // =========================
// @Override public List<AnalyticsPointDTO> getAnalytics(String range){
//   String r = range==null? "7D" : range.trim().toUpperCase();
//   return switch (r){
//     case "1D" -> hourlyAnalytics();
//     case "1M" -> dailyAnalytics(30);
//     case "1Y" -> monthlyAnalytics(12);
//     default -> dailyAnalytics(7); // "7D"
//   };
// }
//
// private List<AnalyticsPointDTO> hourlyAnalytics(){
//   LocalDate today=LocalDate.now();
//   Map<Integer,BigDecimal> revenueByHour=new LinkedHashMap<>();
//   Map<Integer,Long> unitsByHour=new LinkedHashMap<>();
//   for(int h=0;h<24;h++){revenueByHour.put(h,BigDecimal.ZERO);unitsByHour.put(h,0L);}
//
//   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(today,today)){
//     int hour = o.getOrderTime()!=null ? o.getOrderTime().getHour() : 0;
//     revenueByHour.merge(hour, BigDecimal.valueOf(v(o.getTotalAmount())), BigDecimal::add);
//   }
//   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(today,today)){
//     if(oi.getOrder()==null) continue;
//     int hour = oi.getOrder().getOrderTime()!=null ? oi.getOrder().getOrderTime().getHour() : 0;
//     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
//     unitsByHour.merge(hour, qty, Long::sum);
//   }
//
//   List<AnalyticsPointDTO> out=new ArrayList<>();
//   for(int h=0;h<24;h++) out.add(new AnalyticsPointDTO(String.format("%02d:00",h), revenueByHour.get(h), unitsByHour.get(h)));
//   return out;
// }
//
// private List<AnalyticsPointDTO> dailyAnalytics(int days){
//   int safe=Math.max(1,Math.min(days,365)); LocalDate end=LocalDate.now(), start=end.minusDays(safe-1);
//   Map<LocalDate,BigDecimal> revenueByDay=new LinkedHashMap<>();
//   Map<LocalDate,Long> unitsByDay=new LinkedHashMap<>();
//   for(LocalDate d=start; !d.isAfter(end); d=d.plusDays(1)){revenueByDay.put(d,BigDecimal.ZERO);unitsByDay.put(d,0L);}
//
//   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end))
//     revenueByDay.computeIfPresent(o.getOrderDate(), (k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
//   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(start,end)){
//     if(oi.getOrder()==null || oi.getOrder().getOrderDate()==null) continue;
//     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
//     unitsByDay.computeIfPresent(oi.getOrder().getOrderDate(), (k,x)->x+qty);
//   }
//
//   DateTimeFormatter fmt = safe<=7 ? DateTimeFormatter.ofPattern("EEE dd") : DateTimeFormatter.ofPattern("dd MMM");
//   List<AnalyticsPointDTO> out=new ArrayList<>();
//   revenueByDay.forEach((d,amt)->out.add(new AnalyticsPointDTO(d.format(fmt), amt, unitsByDay.get(d))));
//   return out;
// }
//
// private List<AnalyticsPointDTO> monthlyAnalytics(int months){
//   int safe=Math.max(1,Math.min(months,60));
//   YearMonth endYm=YearMonth.now(), startYm=endYm.minusMonths(safe-1);
//   LocalDate start=startYm.atDay(1), end=endYm.atEndOfMonth();
//
//   Map<YearMonth,BigDecimal> revenueByMonth=new LinkedHashMap<>();
//   Map<YearMonth,Long> unitsByMonth=new LinkedHashMap<>();
//   for(YearMonth ym=startYm; !ym.isAfter(endYm); ym=ym.plusMonths(1)){revenueByMonth.put(ym,BigDecimal.ZERO);unitsByMonth.put(ym,0L);}
//
//   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end)){
//     if(o.getOrderDate()==null) continue;
//     YearMonth ym=YearMonth.from(o.getOrderDate());
//     revenueByMonth.computeIfPresent(ym, (k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
//   }
//   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(start,end)){
//     if(oi.getOrder()==null || oi.getOrder().getOrderDate()==null) continue;
//     YearMonth ym=YearMonth.from(oi.getOrder().getOrderDate());
//     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
//     unitsByMonth.computeIfPresent(ym, (k,x)->x+qty);
//   }
//
//   DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
//   List<AnalyticsPointDTO> out=new ArrayList<>();
//   revenueByMonth.forEach((ym,amt)->out.add(new AnalyticsPointDTO(ym.format(fmt), amt, unitsByMonth.get(ym))));
//   return out;
// }
//}

package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.*;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.Enum.OrderStatus;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.OrderItemRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.DashboardService;
import com.InventoryManagementSystem.service.OrderInstallmentService;
import com.InventoryManagementSystem.service.VendorBillInstallmentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderInstallmentService orderInstallmentService;
    private final VendorBillInstallmentService vendorBillInstallmentService;

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    public DashboardServiceImpl(OrderRepository orderRepository,
                                ProductRepository productRepository,
                                PaymentRepository paymentRepository,
                                OrderItemRepository orderItemRepository,
                                OrderInstallmentService orderInstallmentService,
                                VendorBillInstallmentService vendorBillInstallmentService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderInstallmentService = orderInstallmentService;
        this.vendorBillInstallmentService = vendorBillInstallmentService;
    }

    /** Money customers still owe for (non-cancelled) orders: total, overdue, unpaid / partly paid counts. */
    @Override
    public PendingOrdersSummaryDTO getPendingOrdersSummary() {
        LocalDate today = LocalDate.now();
        List<InventoryOrder> active = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .collect(Collectors.toList());
        Map<Long, Summary> summaries = paymentSummaries(active);   // one payments query for all orders

        BigDecimal totalPending = BigDecimal.ZERO, overduePending = BigDecimal.ZERO, pendingToday = BigDecimal.ZERO;
        long pendingCount = 0, unpaidCount = 0, partialCount = 0, overdueCount = 0;

        for (InventoryOrder order : active) {
            Summary summary = summaries.get(order.getId());
            if (summary == null || summary.due() <= 0.009) continue;   // fully paid

            BigDecimal due = BigDecimal.valueOf(summary.due()).setScale(2, java.math.RoundingMode.HALF_UP);
            pendingCount++;
            totalPending = totalPending.add(due);
            if (summary.paid() <= 0.009) unpaidCount++; else partialCount++;

            if (order.getPaymentDueDate() != null && order.getPaymentDueDate().isBefore(today)) {
                overdueCount++;
                overduePending = overduePending.add(due);
            }
            if (today.equals(order.getOrderDate())) {
                pendingToday = pendingToday.add(due);
            }
        }

        return PendingOrdersSummaryDTO.builder()
                .totalPendingAmount(totalPending)
                .pendingOrdersCount(pendingCount)
                .unpaidOrdersCount(unpaidCount)
                .partiallyPaidOrdersCount(partialCount)
                .overduePendingAmount(overduePending)
                .overdueOrdersCount(overdueCount)
                .pendingToday(pendingToday)
                .build();
    }

    @Override
    public DashboardSummaryDTO getDashboardSummary() {
        List<InventoryOrder> orders = orderRepository.findAll();
        LocalDate today = LocalDate.now();

        BigDecimal todaysSales = BigDecimal.ZERO;
        long todaysOrdersCount = 0;

        BigDecimal sevenDaysSales = BigDecimal.ZERO;
        long sevenDaysOrdersCount = 0;

        BigDecimal thirtyDaysSales = BigDecimal.ZERO;
        long thirtyDaysOrdersCount = 0;

        BigDecimal oneYearSales = BigDecimal.ZERO;
        long oneYearOrdersCount = 0;

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrdersCount = 0;

        BigDecimal paidRevenue = BigDecimal.ZERO;

        for (InventoryOrder order : orders) {
            // Ignore cancelled orders for sales and order count
            if (order.getStatus() == OrderStatus.CANCELLED) {
                continue;
            }

            BigDecimal amount = BigDecimal.valueOf(order.getTotalAmount() != null ? order.getTotalAmount() : 0.0);
            LocalDate orderDate = order.getOrderDate();

            totalRevenue = totalRevenue.add(amount);
            totalOrdersCount++;

            // Calculate paid amount per order
            Summary summary = paymentSummary(order);
            paidRevenue = paidRevenue.add(BigDecimal.valueOf(summary.paid()));

            if (orderDate != null) {
                if (orderDate.isEqual(today)) {
                    todaysSales = todaysSales.add(amount);
                    todaysOrdersCount++;
                }

                if (!orderDate.isBefore(today.minusDays(6)) && !orderDate.isAfter(today)) {
                    sevenDaysSales = sevenDaysSales.add(amount);
                    sevenDaysOrdersCount++;
                }

                if (!orderDate.isBefore(today.minusDays(29)) && !orderDate.isAfter(today)) {
                    thirtyDaysSales = thirtyDaysSales.add(amount);
                    thirtyDaysOrdersCount++;
                }

                if (!orderDate.isBefore(today.minusYears(1)) && !orderDate.isAfter(today)) {
                    oneYearSales = oneYearSales.add(amount);
                    oneYearOrdersCount++;
                }
            }
        }

        BigDecimal pendingRevenue = totalRevenue.subtract(paidRevenue).max(BigDecimal.ZERO);

        List<InventoryProduct> products = productRepository.findAll();
        int totalStock = products.stream()
                .mapToInt(p -> p.getStock() != null ? p.getStock() : 0)
                .sum();

        long lowStockCount = products.stream()
                .filter(p -> (p.getStock() != null ? p.getStock() : 0) < DEFAULT_LOW_STOCK_THRESHOLD)
                .count();

        return DashboardSummaryDTO.builder()
                .todaysSales(todaysSales)
                .todaysOrdersCount(todaysOrdersCount)
                .sevenDaysSales(sevenDaysSales)
                .sevenDaysOrdersCount(sevenDaysOrdersCount)
                .thirtyDaysSales(thirtyDaysSales)
                .thirtyDaysOrdersCount(thirtyDaysOrdersCount)
                .oneYearSales(oneYearSales)
                .oneYearOrdersCount(oneYearOrdersCount)
                .totalRevenue(totalRevenue)
                .totalOrdersCount(totalOrdersCount)
                .paidRevenue(paidRevenue)
                .pendingRevenue(pendingRevenue)
                .totalOrdersToday(todaysOrdersCount)
                .totalStockQuantity(totalStock)
                .lowStockCount(lowStockCount)
                .pendingOrders(getPendingOrdersSummary())
                .customerInstallments(orderInstallmentService.getSummary())
                .vendorInstallments(vendorBillInstallmentService.getSummary())
                .build();
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    public List<TopCategoryDTO> getTopSellingCategories(int limit) {
        return productRepository.findTopSellingCategories(PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    public List<InventoryProduct> getLowStockAlerts(int threshold) {
        return productRepository.findAll().stream()
                .filter(p -> (p.getStock() != null ? p.getStock() : 0) < threshold)
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenuePointDTO> getDailyRevenue(int days) {
        int safe = Math.max(1, Math.min(days, 365));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(safe - 1);

        Map<LocalDate, BigDecimal> map = new LinkedHashMap<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            map.put(d, BigDecimal.ZERO);
        }

        List<InventoryOrder> orders = orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start, end);
        for (InventoryOrder o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            map.computeIfPresent(o.getOrderDate(), (k, x) -> x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
        }

        List<RevenuePointDTO> out = new ArrayList<>();
        map.forEach((d, a) -> out.add(new RevenuePointDTO(d.toString(), a)));
        return out;
    }

    @Override
    public List<OrderRevenueDTO> getOrderWiseRevenue(int limit) {
        return orderRepository.findTop10ByOrderByOrderDateDescIdDesc().stream()
                .limit(Math.max(1, Math.min(limit, 50)))
                .map(o -> {
                    Summary s = paymentSummary(o);
                    return new OrderRevenueDTO(
                            o.getId(),
                            "Order #" + o.getId(),
                            BigDecimal.valueOf(v(o.getTotalAmount())),
                            BigDecimal.valueOf(s.paid()),
                            BigDecimal.valueOf(s.due())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenuePointDTO> getPaymentStatusRevenue() {
        BigDecimal paid = BigDecimal.ZERO;
        BigDecimal due = BigDecimal.ZERO;

        List<InventoryOrder> allOrders = orderRepository.findAll();
        Map<Long, Summary> sums = paymentSummaries(allOrders);
        for (InventoryOrder o : allOrders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            Summary s = sums.getOrDefault(o.getId(), new Summary(0.0, v(o.getTotalAmount())));
            paid = paid.add(BigDecimal.valueOf(s.paid()));
            due = due.add(BigDecimal.valueOf(s.due()));
        }

        return List.of(
                new RevenuePointDTO("Paid", paid),
                new RevenuePointDTO("Pending", due)
        );
    }

    // ==========================================
    // REVENUE / SELL ANALYTICS (1D, 7D, 1M, 1Y)
    // ==========================================
    @Override
    public List<AnalyticsPointDTO> getAnalytics(String range) {
        String r = range == null ? "7D" : range.trim().toUpperCase();
        return switch (r) {
            case "1D" -> hourlyAnalytics();
            case "1M" -> dailyAnalytics(30);
            case "1Y" -> monthlyAnalytics(12);
            default -> dailyAnalytics(7); // "7D"
        };
    }

    private List<AnalyticsPointDTO> hourlyAnalytics() {
        LocalDate today = LocalDate.now();
        Map<Integer, BigDecimal> revenueByHour = new LinkedHashMap<>();
        Map<Integer, Long> unitsByHour = new LinkedHashMap<>();
        Map<Integer, BigDecimal> paidByHour = new LinkedHashMap<>();
        Map<Integer, BigDecimal> pendingByHour = new LinkedHashMap<>();
        Map<Integer, Long> ordersByHour = new LinkedHashMap<>();

        for (int h = 0; h < 24; h++) {
            revenueByHour.put(h, BigDecimal.ZERO);
            unitsByHour.put(h, 0L);
            paidByHour.put(h, BigDecimal.ZERO);
            pendingByHour.put(h, BigDecimal.ZERO);
            ordersByHour.put(h, 0L);
        }

        List<InventoryOrder> orders = orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(today, today);
        Map<Long, Summary> sums = paymentSummaries(orders); // one query for all payments
        for (InventoryOrder o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            int hour = o.getOrderTime() != null ? o.getOrderTime().getHour() : 0;
            revenueByHour.merge(hour, BigDecimal.valueOf(v(o.getTotalAmount())), BigDecimal::add);
            ordersByHour.merge(hour, 1L, Long::sum);
            Summary ps = sums.getOrDefault(o.getId(), new Summary(0.0, v(o.getTotalAmount())));
            paidByHour.merge(hour, BigDecimal.valueOf(ps.paid()), BigDecimal::add);
            pendingByHour.merge(hour, BigDecimal.valueOf(ps.due()), BigDecimal::add);
        }

        List<InventoryOrderItem> items = orderItemRepository.findByOrderDateBetween(today, today);
        for (InventoryOrderItem oi : items) {
            if (oi.getOrder() == null || oi.getOrder().getStatus() == OrderStatus.CANCELLED) continue;
            int hour = oi.getOrder().getOrderTime() != null ? oi.getOrder().getOrderTime().getHour() : 0;
            long qty = oi.getQuantity() == null ? 0L : oi.getQuantity().longValue();
            unitsByHour.merge(hour, qty, Long::sum);
        }

        List<AnalyticsPointDTO> out = new ArrayList<>();
        for (int h = 0; h < 24; h++) {
            out.add(new AnalyticsPointDTO(String.format("%02d:00", h), revenueByHour.get(h), unitsByHour.get(h),
                    revenueByHour.get(h), paidByHour.get(h), pendingByHour.get(h), ordersByHour.get(h)));
        }
        return out;
    }

    private List<AnalyticsPointDTO> dailyAnalytics(int days) {
        int safe = Math.max(1, Math.min(days, 365));
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(safe - 1);

        Map<LocalDate, BigDecimal> revenueByDay = new LinkedHashMap<>();
        Map<LocalDate, Long> unitsByDay = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> paidByDay = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> pendingByDay = new LinkedHashMap<>();
        Map<LocalDate, Long> ordersByDay = new LinkedHashMap<>();

        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            revenueByDay.put(d, BigDecimal.ZERO);
            unitsByDay.put(d, 0L);
            paidByDay.put(d, BigDecimal.ZERO);
            pendingByDay.put(d, BigDecimal.ZERO);
            ordersByDay.put(d, 0L);
        }

        List<InventoryOrder> orders = orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start, end);
        Map<Long, Summary> sums = paymentSummaries(orders); // one query for all payments
        for (InventoryOrder o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED) continue;
            revenueByDay.computeIfPresent(o.getOrderDate(), (k, x) -> x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
            ordersByDay.computeIfPresent(o.getOrderDate(), (k, x) -> x + 1);
            Summary ps = sums.getOrDefault(o.getId(), new Summary(0.0, v(o.getTotalAmount())));
            paidByDay.computeIfPresent(o.getOrderDate(), (k, x) -> x.add(BigDecimal.valueOf(ps.paid())));
            pendingByDay.computeIfPresent(o.getOrderDate(), (k, x) -> x.add(BigDecimal.valueOf(ps.due())));
        }

        List<InventoryOrderItem> items = orderItemRepository.findByOrderDateBetween(start, end);
        for (InventoryOrderItem oi : items) {
            if (oi.getOrder() == null || oi.getOrder().getOrderDate() == null || oi.getOrder().getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            long qty = oi.getQuantity() == null ? 0L : oi.getQuantity().longValue();
            unitsByDay.computeIfPresent(oi.getOrder().getOrderDate(), (k, x) -> x + qty);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd"); // 7D and 1M: show only the date
        List<AnalyticsPointDTO> out = new ArrayList<>();
        revenueByDay.forEach((d, amt) -> out.add(new AnalyticsPointDTO(d.format(fmt), amt, unitsByDay.get(d),
                amt, paidByDay.get(d), pendingByDay.get(d), ordersByDay.get(d))));
        return out;
    }

    private List<AnalyticsPointDTO> monthlyAnalytics(int months) {
        int safe = Math.max(1, Math.min(months, 60));
        YearMonth endYm = YearMonth.now();
        YearMonth startYm = endYm.minusMonths(safe - 1);
        LocalDate start = startYm.atDay(1);
        LocalDate end = endYm.atEndOfMonth();

        Map<YearMonth, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        Map<YearMonth, Long> unitsByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> paidByMonth = new LinkedHashMap<>();
        Map<YearMonth, BigDecimal> pendingByMonth = new LinkedHashMap<>();
        Map<YearMonth, Long> ordersByMonth = new LinkedHashMap<>();

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            revenueByMonth.put(ym, BigDecimal.ZERO);
            unitsByMonth.put(ym, 0L);
            paidByMonth.put(ym, BigDecimal.ZERO);
            pendingByMonth.put(ym, BigDecimal.ZERO);
            ordersByMonth.put(ym, 0L);
        }

        List<InventoryOrder> orders = orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start, end);
        Map<Long, Summary> sums = paymentSummaries(orders); // one query for all payments
        for (InventoryOrder o : orders) {
            if (o.getStatus() == OrderStatus.CANCELLED || o.getOrderDate() == null) continue;
            YearMonth ym = YearMonth.from(o.getOrderDate());
            revenueByMonth.computeIfPresent(ym, (k, x) -> x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
            ordersByMonth.computeIfPresent(ym, (k, x) -> x + 1);
            Summary ps = sums.getOrDefault(o.getId(), new Summary(0.0, v(o.getTotalAmount())));
            paidByMonth.computeIfPresent(ym, (k, x) -> x.add(BigDecimal.valueOf(ps.paid())));
            pendingByMonth.computeIfPresent(ym, (k, x) -> x.add(BigDecimal.valueOf(ps.due())));
        }

        List<InventoryOrderItem> items = orderItemRepository.findByOrderDateBetween(start, end);
        for (InventoryOrderItem oi : items) {
            if (oi.getOrder() == null || oi.getOrder().getOrderDate() == null || oi.getOrder().getStatus() == OrderStatus.CANCELLED) {
                continue;
            }
            YearMonth ym = YearMonth.from(oi.getOrder().getOrderDate());
            long qty = oi.getQuantity() == null ? 0L : oi.getQuantity().longValue();
            unitsByMonth.computeIfPresent(ym, (k, x) -> x + qty);
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM"); // 1Y: show only the month name
        List<AnalyticsPointDTO> out = new ArrayList<>();
        revenueByMonth.forEach((ym, amt) -> out.add(new AnalyticsPointDTO(ym.format(fmt), amt, unitsByMonth.get(ym),
                amt, paidByMonth.get(ym), pendingByMonth.get(ym), ordersByMonth.get(ym))));
        return out;
    }

    // Helper methods
    private Summary paymentSummary(InventoryOrder order) {
        double paid = paymentRepository.findByOrderIdOrderByPaymentDateDesc(order.getId()).stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID || p.getPaymentStatus() == PaymentStatus.PARTIAL)
                .mapToDouble(p -> v(p.getAmount()))
                .sum();
        double total = v(order.getTotalAmount());
        return new Summary(Math.min(paid, total), Math.max(0, total - paid));
    }

    /** Paid / pending for MANY orders using ONE payments query (no query per order). */
    private Map<Long, Summary> paymentSummaries(Collection<InventoryOrder> orders) {
        Map<Long, Summary> out = new HashMap<>();
        if (orders == null || orders.isEmpty()) return out;

        List<Long> ids = orders.stream().map(InventoryOrder::getId).collect(Collectors.toList());
        Map<Long, Double> paidByOrder = new HashMap<>();
        for (InventoryPayment p : paymentRepository.findByOrderIdInOrderByPaymentDateDesc(ids)) {
            if (p.getOrder() == null) continue;
            if (p.getPaymentStatus() == PaymentStatus.PAID || p.getPaymentStatus() == PaymentStatus.PARTIAL) {
                paidByOrder.merge(p.getOrder().getId(), v(p.getAmount()), Double::sum);
            }
        }
        for (InventoryOrder o : orders) {
            double total = v(o.getTotalAmount());
            double paid = paidByOrder.getOrDefault(o.getId(), 0.0);
            out.put(o.getId(), new Summary(Math.min(paid, total), Math.max(0, total - paid)));
        }
        return out;
    }

    private static double v(Double x) {
        return x == null ? 0.0 : x;
    }

    private record Summary(double paid, double due) {}
}