package com.excelr.controller;

import com.excelr.service.RazorpayService;
import com.excelr.exception.PaymentNotFoundException;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private RazorpayService razorpayService;

    // Create order endpoint
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> data) {
        try {
            int amount = (int) data.get("amount");
            String currency = (String) data.get("currency");
            String receipt = (String) data.get("receipt");

            // Validate input parameters
            if (amount <= 0 || currency == null || currency.isEmpty() || receipt == null || receipt.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid input data. Please provide valid amount, currency, and receipt.");
            }

            // Call the service to create an order
            String order = razorpayService.createOrder(amount, currency, receipt);
            return ResponseEntity.ok(order);

        } catch (RazorpayException e) {
            // Catch Razorpay-specific errors and return a detailed message
            return ResponseEntity.badRequest().body("Failed to create order: " + e.getMessage());
        } catch (Exception e) {
            // Generic error handling for unexpected issues
            return ResponseEntity.status(500).body("An unexpected error occurred: " + e.getMessage());
        }
    }

    // Verify payment endpoint
    @PostMapping("/verify-payment")
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> data) {
        String orderId = data.get("razorpay_order_id");
        String paymentId = data.get("razorpay_payment_id");
        String signature = data.get("razorpay_signature");

        // Validate input parameters
        if (orderId == null || paymentId == null || signature == null) {
            return ResponseEntity.badRequest().body("Missing required parameters: order_id, payment_id, or signature.");
        }

        try {
            // Verify payment signature using Razorpay service
            boolean isValid = razorpayService.verifyPayment(orderId, paymentId, signature);

            if (isValid) {
                razorpayService.updatePaymentStatus(orderId, paymentId, "success"); // Updating payment status and payment ID
                return ResponseEntity.ok("Payment Verified");
            } else {
                razorpayService.updatePaymentStatus(orderId, paymentId, "failed"); // Updating payment status to failed
                return ResponseEntity.badRequest().body("Payment Verification Failed");
            }

        } catch (PaymentNotFoundException e) {
            // Catch custom exception when payment is not found
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected errors
            return ResponseEntity.status(500).body("An unexpected error occurred during payment verification: " + e.getMessage());
        }
    }
}