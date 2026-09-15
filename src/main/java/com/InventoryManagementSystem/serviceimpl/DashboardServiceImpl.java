package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.*;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryOrderItem;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.OrderItemRepository;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
 private final OrderRepository orderRepository; private final ProductRepository productRepository; private final PaymentRepository paymentRepository; private final OrderItemRepository orderItemRepository;
 private static final int DEFAULT_LOW_STOCK_THRESHOLD=5;
 public DashboardServiceImpl(OrderRepository o, ProductRepository p, PaymentRepository pay, OrderItemRepository oi){this.orderRepository=o;this.productRepository=p;this.paymentRepository=pay;this.orderItemRepository=oi;}
 @Override public DashboardSummaryDTO getDashboardSummary(){
   LocalDate today=LocalDate.now();
   BigDecimal total=n(orderRepository.findTotalRevenue());
   BigDecimal todaySales=sumOrders(today,today), seven=sumOrders(today.minusDays(6),today), thirty=sumOrders(today.minusDays(29),today), year=sumOrders(today.minusDays(364),today);
   BigDecimal paid=BigDecimal.ZERO;
   for(InventoryOrder o:orderRepository.findAll()) paid=paid.add(BigDecimal.valueOf(paymentSummary(o).paid));
   BigDecimal pending=total.subtract(paid).max(BigDecimal.ZERO);
   return DashboardSummaryDTO.builder().todaysSales(todaySales).sevenDaysSales(seven).thirtyDaysSales(thirty).oneYearSales(year).totalRevenue(total).paidRevenue(paid).pendingRevenue(pending).totalOrdersToday(orderRepository.countTodaysOrders(today)).totalStockQuantity(productRepository.findTotalStockQuantity()).lowStockCount(productRepository.countLowStockProducts(DEFAULT_LOW_STOCK_THRESHOLD)).build();
 }
 private BigDecimal sumOrders(LocalDate s,LocalDate e){return orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(s,e).stream().map(o->BigDecimal.valueOf(v(o.getTotalAmount()))).reduce(BigDecimal.ZERO,BigDecimal::add);}
 @Override public List<TopProductDTO> getTopSellingProducts(int limit){return productRepository.findTopSellingProducts(PageRequest.of(0,limit));}
 @Override public List<InventoryProduct> getLowStockAlerts(int threshold){return productRepository.findLowStockProducts(threshold);}
 @Override public List<RevenuePointDTO> getDailyRevenue(int days){
   int safe=Math.max(1,Math.min(days,365)); LocalDate end=LocalDate.now(), start=end.minusDays(safe-1);
   Map<LocalDate,BigDecimal> map=new LinkedHashMap<>(); for(LocalDate d=start;!d.isAfter(end);d=d.plusDays(1))map.put(d,BigDecimal.ZERO);
   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end)) map.computeIfPresent(o.getOrderDate(),(k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
   List<RevenuePointDTO> out=new ArrayList<>(); map.forEach((d,a)->out.add(new RevenuePointDTO(d.toString(),a))); return out;
 }
 @Override public List<OrderRevenueDTO> getOrderWiseRevenue(int limit){
   return orderRepository.findTop10ByOrderByOrderDateDescIdDesc().stream().limit(Math.max(1,Math.min(limit,50))).map(o->{Summary s=paymentSummary(o);return new OrderRevenueDTO(o.getId(),"Order #"+o.getId(),BigDecimal.valueOf(v(o.getTotalAmount())),BigDecimal.valueOf(s.paid),BigDecimal.valueOf(s.due));}).toList();
 }
 @Override public List<RevenuePointDTO> getPaymentStatusRevenue(){
   BigDecimal paid=BigDecimal.ZERO,due=BigDecimal.ZERO; for(InventoryOrder o:orderRepository.findAll()){Summary s=paymentSummary(o);paid=paid.add(BigDecimal.valueOf(s.paid));due=due.add(BigDecimal.valueOf(s.due));}
   return List.of(new RevenuePointDTO("Paid",paid),new RevenuePointDTO("Pending",due));
 }
 private Summary paymentSummary(InventoryOrder order){double paid=paymentRepository.findByOrderIdOrderByPaymentDateDesc(order.getId()).stream().filter(p->p.getPaymentStatus()==PaymentStatus.PAID||p.getPaymentStatus()==PaymentStatus.PARTIAL).mapToDouble(p->v(p.getAmount())).sum(); double total=v(order.getTotalAmount()); return new Summary(Math.min(paid,total),Math.max(0,total-paid));}
 private static double v(Double x){return x==null?0:x;} private static BigDecimal n(BigDecimal x){return x==null?BigDecimal.ZERO:x;} private record Summary(double paid,double due){}

 // =========================
 // REVENUE / SELL ANALYTICS (1D, 7D, 1M, 1Y)
 // =========================
 @Override public List<AnalyticsPointDTO> getAnalytics(String range){
   String r = range==null? "7D" : range.trim().toUpperCase();
   return switch (r){
     case "1D" -> hourlyAnalytics();
     case "1M" -> dailyAnalytics(30);
     case "1Y" -> monthlyAnalytics(12);
     default -> dailyAnalytics(7); // "7D"
   };
 }

 private List<AnalyticsPointDTO> hourlyAnalytics(){
   LocalDate today=LocalDate.now();
   Map<Integer,BigDecimal> revenueByHour=new LinkedHashMap<>();
   Map<Integer,Long> unitsByHour=new LinkedHashMap<>();
   for(int h=0;h<24;h++){revenueByHour.put(h,BigDecimal.ZERO);unitsByHour.put(h,0L);}

   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(today,today)){
     int hour = o.getOrderTime()!=null ? o.getOrderTime().getHour() : 0;
     revenueByHour.merge(hour, BigDecimal.valueOf(v(o.getTotalAmount())), BigDecimal::add);
   }
   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(today,today)){
     if(oi.getOrder()==null) continue;
     int hour = oi.getOrder().getOrderTime()!=null ? oi.getOrder().getOrderTime().getHour() : 0;
     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
     unitsByHour.merge(hour, qty, Long::sum);
   }

   List<AnalyticsPointDTO> out=new ArrayList<>();
   for(int h=0;h<24;h++) out.add(new AnalyticsPointDTO(String.format("%02d:00",h), revenueByHour.get(h), unitsByHour.get(h)));
   return out;
 }

 private List<AnalyticsPointDTO> dailyAnalytics(int days){
   int safe=Math.max(1,Math.min(days,365)); LocalDate end=LocalDate.now(), start=end.minusDays(safe-1);
   Map<LocalDate,BigDecimal> revenueByDay=new LinkedHashMap<>();
   Map<LocalDate,Long> unitsByDay=new LinkedHashMap<>();
   for(LocalDate d=start; !d.isAfter(end); d=d.plusDays(1)){revenueByDay.put(d,BigDecimal.ZERO);unitsByDay.put(d,0L);}

   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end))
     revenueByDay.computeIfPresent(o.getOrderDate(), (k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(start,end)){
     if(oi.getOrder()==null || oi.getOrder().getOrderDate()==null) continue;
     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
     unitsByDay.computeIfPresent(oi.getOrder().getOrderDate(), (k,x)->x+qty);
   }

   DateTimeFormatter fmt = safe<=7 ? DateTimeFormatter.ofPattern("EEE dd") : DateTimeFormatter.ofPattern("dd MMM");
   List<AnalyticsPointDTO> out=new ArrayList<>();
   revenueByDay.forEach((d,amt)->out.add(new AnalyticsPointDTO(d.format(fmt), amt, unitsByDay.get(d))));
   return out;
 }

 private List<AnalyticsPointDTO> monthlyAnalytics(int months){
   int safe=Math.max(1,Math.min(months,60));
   YearMonth endYm=YearMonth.now(), startYm=endYm.minusMonths(safe-1);
   LocalDate start=startYm.atDay(1), end=endYm.atEndOfMonth();

   Map<YearMonth,BigDecimal> revenueByMonth=new LinkedHashMap<>();
   Map<YearMonth,Long> unitsByMonth=new LinkedHashMap<>();
   for(YearMonth ym=startYm; !ym.isAfter(endYm); ym=ym.plusMonths(1)){revenueByMonth.put(ym,BigDecimal.ZERO);unitsByMonth.put(ym,0L);}

   for(InventoryOrder o:orderRepository.findByOrderDateBetweenOrderByOrderDateAscIdAsc(start,end)){
     if(o.getOrderDate()==null) continue;
     YearMonth ym=YearMonth.from(o.getOrderDate());
     revenueByMonth.computeIfPresent(ym, (k,x)->x.add(BigDecimal.valueOf(v(o.getTotalAmount()))));
   }
   for(InventoryOrderItem oi:orderItemRepository.findByOrderDateBetween(start,end)){
     if(oi.getOrder()==null || oi.getOrder().getOrderDate()==null) continue;
     YearMonth ym=YearMonth.from(oi.getOrder().getOrderDate());
     long qty = oi.getQuantity()==null? 0L : oi.getQuantity().longValue();
     unitsByMonth.computeIfPresent(ym, (k,x)->x+qty);
   }

   DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM yyyy");
   List<AnalyticsPointDTO> out=new ArrayList<>();
   revenueByMonth.forEach((ym,amt)->out.add(new AnalyticsPointDTO(ym.format(fmt), amt, unitsByMonth.get(ym))));
   return out;
 }
}
