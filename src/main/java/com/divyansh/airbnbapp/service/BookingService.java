package com.divyansh.airbnbapp.service;


import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.BookingRequestDTO;
import com.divyansh.airbnbapp.dto.GuestDTO;
import com.divyansh.airbnbapp.dto.HotelReportDTO;
import com.stripe.model.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingService {

    BookingDTO initialiseBooking(BookingRequestDTO bookingRequestDTO);

    BookingDTO addGuests(Long bookingId, List<GuestDTO> guestDTOList);

    String initiatePayments(Long bookingId);

    void capturePayment(Event event);

    void cancelBooking(Long bookingId);

    String getBookingStatus(Long bookingId);

    List<BookingDTO> getAllBookingsByHotelId(Long hotelId);

    HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate);

    List<BookingDTO> getMyBookings();
}
