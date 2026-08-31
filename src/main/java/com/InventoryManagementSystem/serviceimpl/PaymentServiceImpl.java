package com.InventoryManagementSystem.serviceimpl;
import com.InventoryManagementSystem.Enum.PaymentStatus;
import com.InventoryManagementSystem.entity.InventoryOrder;
import com.InventoryManagementSystem.entity.InventoryPayment;
import com.InventoryManagementSystem.repository.OrderRepository;
import com.InventoryManagementSystem.repository.BankAccountRepository;
import com.InventoryManagementSystem.entity.BankAccount;
import com.InventoryManagementSystem.repository.PaymentRepository;
import com.InventoryManagementSystem.service.PaymentService;
import org.springframework.stereotype.Service;
import java.time.LocalDate; import java.util.List;
@Service public class PaymentServiceImpl implements PaymentService {
 private final PaymentRepository paymentRepository; private final OrderRepository orderRepository; private final BankAccountRepository bankAccountRepository;
 public PaymentServiceImpl(PaymentRepository p,OrderRepository o,BankAccountRepository b){paymentRepository=p;orderRepository=o;bankAccountRepository=b;}
 @Override public InventoryPayment createPayment(Long orderId,InventoryPayment payment){
   InventoryOrder order=orderRepository.findById(orderId).orElseThrow(()->new RuntimeException("Order not found with ID: "+orderId));
   double amount=payment.getAmount()==null?0:payment.getAmount(); if(amount<0)throw new RuntimeException("Payment amount cannot be negative");
   double existing=getPaymentsByOrderId(orderId).stream().filter(p->p.getPaymentStatus()==PaymentStatus.PAID||p.getPaymentStatus()==PaymentStatus.PARTIAL).mapToDouble(p->p.getAmount()==null?0:p.getAmount()).sum();
   double total=order.getTotalAmount()==null?0:order.getTotalAmount(); if(amount>0 && existing+amount>total+0.009)throw new RuntimeException("Payment exceeds the pending amount");
   if(payment.getReceivingAccount()!=null && payment.getReceivingAccount().getId()!=null){ BankAccount account=bankAccountRepository.findById(payment.getReceivingAccount().getId()).orElseThrow(()->new RuntimeException("Receiving bank account not found")); if(!Boolean.TRUE.equals(account.getActive())||!Boolean.TRUE.equals(account.getAcceptingPayments())) throw new RuntimeException("Selected bank account is not accepting payments"); payment.setReceivingAccount(account); }
   payment.setOrder(order); if(payment.getPaymentDate()==null)payment.setPaymentDate(LocalDate.now());
   if(amount<=0) payment.setPaymentStatus(PaymentStatus.PENDING); else payment.setPaymentStatus(existing+amount+0.009>=total?PaymentStatus.PAID:PaymentStatus.PARTIAL);
   return paymentRepository.save(payment);
 }
 @Override public List<InventoryPayment> getAllPayments(){return paymentRepository.findAll();}
 @Override public List<InventoryPayment> getPaymentsByOrderId(Long id){return paymentRepository.findByOrderIdOrderByPaymentDateDesc(id);}
 @Override public InventoryPayment getPaymentById(Long id){return paymentRepository.findById(id).orElseThrow(()->new RuntimeException("Payment not found with ID: "+id));}
 @Override public void deletePayment(Long id){paymentRepository.deleteById(id);}
}
