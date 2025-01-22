package com.excelr.service;

import com.excelr.exception.PaymentNotFoundException;
import com.excelr.model.Payment;
import com.excelr.repo.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

@Service
public class RazorpayService {

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecret;

    @Autowired
    private PaymentRepository paymentRepository;

    // Create order method
    public String createOrder(int amount, String currency, String receipt) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(apiKey, apiSecret);

        // Construct the order request
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // amount in paise (cents)
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", receipt);

        // Create the order
        Order order = razorpayClient.orders.create(orderRequest);

        // Create a new payment entry and save it
        Payment payment = new Payment();
        payment.setRazorpayOrderId(order.get("id").toString());
        payment.setAmount((long) (amount * 100)); // Store amount in paise
        payment.setCurrency(currency);
        payment.setReceipt(receipt);
        payment.setStatus("created");

        // Save the payment details in the database
        paymentRepository.save(payment);

        return order.toString();
    }

    // Verify payment method
    public boolean verifyPayment(String orderId, String paymentId, String signature) {
        String generatedSignature = HmacSHA256(orderId + "|" + paymentId, apiSecret);
        return generatedSignature.equals(signature);
    }

    // Helper method to calculate HMAC SHA256 for payment verification
    private String HmacSHA256(String data, String secret) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secret.getBytes(), "HmacSHA256"));
            byte[] hmacData = mac.doFinal(data.getBytes());
            return javax.xml.bind.DatatypeConverter.printHexBinary(hmacData).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC SHA256", e);
        }
    }

    // Update payment status and payment id after verification
    public void updatePaymentStatus(String orderId, String paymentId, String status) {
        // Find payment by Razorpay Order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found with Razorpay Order ID: " + orderId));

        // Set Razorpay Payment ID and status
        payment.setRazorpayPaymentId(paymentId);  // Set Razorpay Payment ID here
        payment.setStatus(status);

        // Save updated payment status to the database
        paymentRepository.save(payment);
    }
}