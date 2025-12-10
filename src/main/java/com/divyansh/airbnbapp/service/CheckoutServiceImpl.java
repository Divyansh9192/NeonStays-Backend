package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.PaymentDTO;
import com.divyansh.airbnbapp.entity.Booking;
import com.divyansh.airbnbapp.entity.User;
import com.divyansh.airbnbapp.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) {
        log.info("Creating session for booking with id: {}",booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            Customer customer=  Customer.create(
                    CustomerCreateParams.builder()
                            .setName(user.getName())
                            .setEmail(user.getEmail())
                            .build()
            );
            SessionCreateParams sessionParams = SessionCreateParams.builder()

                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                    .setCustomer(customer.getId())
                    .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                    .putMetadata("bookingId", String.valueOf(booking.getId()))
                                    .putMetadata("userId", String.valueOf(user.getId()))
                                    .putMetadata("email", user.getEmail())
                                    .putMetadata("hotel", booking.getHotel().getName())
                                    .putMetadata("roomType", booking.getRoom().getType())
                                    .build()
                    )
                    .setSuccessUrl(successUrl+"?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(failureUrl)
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency("inr")
                                                    .setUnitAmount(booking.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName(booking.getHotel().getName()+" "+booking.getRoom().getType())
                                                                    .setDescription("Booking id:"+booking.getId())
                                                                    .build()
                                                    )
                                                    .build()

                                    )
                                    .build()
                    )
                    .build();

            Session session = Session.create(sessionParams);

            booking.setPaymentSessionId(session.getId());
            bookingRepository.save(booking);

            log.info("Session created successfully for booking with id: {}",booking.getId());

            return session.getUrl();

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }
    public PaymentDTO getPaymentDetails(String sessionId) throws Exception {
        // Retrieve checkout session
        Session session = Session.retrieve(sessionId);

        // Amount + currency -> format
        Long amountTotal = session.getAmountTotal(); // in smallest currency unit
        String currency = session.getCurrency();
        String formattedAmount = "N/A";
        if (amountTotal != null) {
            formattedAmount = String.format("%s %.2f",
                    currency == null ? "INR" : currency.toUpperCase(),
                    amountTotal / 100.0
            );
        }

        // Retrieve PaymentIntent (to access metadata and charges)
        String paymentIntentId = session.getPaymentIntent();
        PaymentIntent pi = null;
        if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
            pi = PaymentIntent.retrieve(paymentIntentId);
        }

        // orderId: prefer client_reference_id (Checkout session) then paymentIntent metadata bookingId then session id
        String orderId = session.getClientReferenceId();
        if ((orderId == null || orderId.isEmpty()) && pi != null && pi.getMetadata() != null) {
            orderId = pi.getMetadata().get("bookingId");
        }
        if (orderId == null || orderId.isEmpty()) orderId = session.getId();

        // email: prefer metadata.email (payment intent) then session.customer_details.email
        String email = null;
        if (pi != null && pi.getMetadata() != null) {
            email = pi.getMetadata().get("email");
        }
        if ((email == null || email.isEmpty()) && session.getCustomerDetails() != null) {
            email = session.getCustomerDetails().getEmail();
        }

        // date: from session.created (epoch seconds)
        String dateStr = "N/A";
        if (session.getCreated() != null) {
            Instant created = Instant.ofEpochSecond(session.getCreated());
            dateStr = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.of("Asia/Kolkata"))
                    .format(created);
        }

        // last4: dig into PaymentIntent -> charges -> payment_method_details.card.last4
        String last4 = "----";
        if (pi != null && pi.getLatestCharge() != null) {
            String chargeId = pi.getLatestCharge();
            Charge charge = Charge.retrieve(chargeId);

            if (charge.getPaymentMethodDetails() != null &&
                    charge.getPaymentMethodDetails().getCard() != null) {
                last4 = charge.getPaymentMethodDetails().getCard().getLast4();
            }
        }


        return new PaymentDTO(formattedAmount, orderId, email, dateStr, last4);
    }
}
