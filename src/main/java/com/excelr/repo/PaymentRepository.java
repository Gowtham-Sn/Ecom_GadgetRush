package com.excelr.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.excelr.model.Payment;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);
    Optional<Payment> findByRazorpayOrderId(String razorpayOrderId);
}