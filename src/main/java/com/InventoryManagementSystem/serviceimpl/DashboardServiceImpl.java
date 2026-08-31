package com.InventoryManagementSystem.serviceimpl;

import com.InventoryManagementSystem.DTO.*;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.entity.InventoryProduct;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.repository.ProductRepository;
import com.InventoryManagementSystem.service.DashboardService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class DashboardServiceImpl implements DashboardService {
 private final OrderRepository orderRepository; private final ProductRepository productRepository; private final PaymentRepository paymentRepository;
 private static final int DEFAULT_LOW_STOCK_THRESHOLD=5;
 public DashboardServiceImpl(OrderRepository o, ProductRepository p, PaymentRepository pay){this.orderRepository=o;this.productRepository=p;this.paymentRepository=pay;}
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
}
