package com.InventoryManagementSystem.repository;
import com.InventoryManagementSystem.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    List<BankAccount> findAllByOrderByDefaultAccountDescIdDesc();
    List<BankAccount> findByActiveTrueAndAcceptingPaymentsTrueOrderByDefaultAccountDescIdDesc();
}
