package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.entity.Booking;

public interface CheckoutService {
    String getCheckoutSession(Booking booking, String successUrl, String failureUrl);
}
