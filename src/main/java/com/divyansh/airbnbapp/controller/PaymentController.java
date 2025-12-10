package com.divyansh.airbnbapp.controller;

import com.divyansh.airbnbapp.dto.PaymentDTO;
import com.divyansh.airbnbapp.service.CheckoutServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController {

    private final CheckoutServiceImpl checkoutService;

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable String sessionId) {
        try {
            PaymentDTO dto = checkoutService.getPaymentDetails(sessionId);

            if (dto == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(dto);

        } catch (com.stripe.exception.InvalidRequestException e) {
            // Happens if sessionId is invalid
            return ResponseEntity.badRequest().body("Invalid session ID");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Something went wrong fetching payment details");
        }
    }
}
