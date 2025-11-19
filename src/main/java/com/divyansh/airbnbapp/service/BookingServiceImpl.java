package com.divyansh.airbnbapp.service;

import com.divyansh.airbnbapp.dto.BookingDTO;
import com.divyansh.airbnbapp.dto.BookingRequestDTO;
import com.divyansh.airbnbapp.dto.GuestDTO;
import com.divyansh.airbnbapp.dto.HotelReportDTO;
import com.divyansh.airbnbapp.entity.*;
import com.divyansh.airbnbapp.entity.enums.BookingStatus;
import com.divyansh.airbnbapp.exception.ResourceNotFoundException;
import com.divyansh.airbnbapp.exception.UnauthorizedException;
import com.divyansh.airbnbapp.repository.*;
import com.divyansh.airbnbapp.strategy.PricingService;
import com.stripe.exception.EventDataObjectDeserializationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import static com.divyansh.airbnbapp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{
    private final GuestRepository guestRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    @Transactional
    public BookingDTO initialiseBooking(BookingRequestDTO bookingRequestDTO) {

        log.info("Initialise booking for hotel with id: {} , room with id: {}, date - {}-{}"
        ,bookingRequestDTO.getHotelId(),bookingRequestDTO.getRoomId(),bookingRequestDTO.getCheckInDate(),bookingRequestDTO.getCheckOutDate());

        Hotel hotel = hotelRepository.findById(bookingRequestDTO.getHotelId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Hotel not exist with id: "+bookingRequestDTO.getHotelId())
                );
        Room room = roomRepository.findById(bookingRequestDTO.getRoomId())
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Room not exists with id"+bookingRequestDTO.getRoomId())
                );
        List<Inventory> inventoryList = inventoryRepository.findAndLockAvailableInventory(
                room.getId()
                ,bookingRequestDTO.getCheckInDate()
                ,bookingRequestDTO.getCheckOutDate()
                ,bookingRequestDTO.getRoomsCount()
        );
        long daysCount = ChronoUnit.DAYS.between(
                bookingRequestDTO.getCheckInDate(), bookingRequestDTO.getCheckOutDate())+1;
        if(inventoryList.size()!=daysCount){
            throw new IllegalStateException("Room is not available anymore!");
        }

        inventoryRepository.initBooking(room.getId(),bookingRequestDTO.getCheckInDate()
                ,bookingRequestDTO.getCheckOutDate(),bookingRequestDTO.getRoomsCount());

        BigDecimal priceForOneRoom = pricingService.calculateTotalPrice(inventoryList);
        BigDecimal totalPrice = priceForOneRoom.multiply(BigDecimal.valueOf(bookingRequestDTO.getRoomsCount()));

        User user = getCurrentUser();
        Booking booking = Booking.builder()
                .bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .room(room)
                .checkInDate(bookingRequestDTO.getCheckInDate())
                .checkOutDate(bookingRequestDTO.getCheckOutDate())
                .user(user)
                .roomsCount(bookingRequestDTO.getRoomsCount())
                .amount(totalPrice)
                .build();

        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    @Transactional
    public BookingDTO addGuests(Long bookingId, List<GuestDTO> guestDTOList) {
        log.info("Adding guest for booking with id {}",bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Hotel not exist with id: "+bookingId));

        User user = getCurrentUser();

        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnauthorizedException("Booking does not belong to this user with id:"+user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw new IllegalStateException("Booking is not under reserved state, Cannot add guests");
        }

        for (GuestDTO guestDTO: guestDTOList){
            Guest guest = modelMapper.map(guestDTO, Guest.class);
            guest.setUser(user);
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUEST_ADDED);
        booking = bookingRepository.save(booking);

        return modelMapper.map(booking, BookingDTO.class);
    }

    @Override
    @Transactional
    public String initiatePayments(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );
        User user = getCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnauthorizedException("Booking does not belong to this user with id:"+user.getId());
        }

        if(hasBookingExpired(booking)){
            throw new IllegalStateException("Booking has already expired");
        }

        String sessionUrl = checkoutService.getCheckoutSession(
                booking,frontendUrl+"/payment/success",frontendUrl+"/payment/failure");

        booking.setBookingStatus(BookingStatus.PAYMENT_PENDING);
        bookingRepository.save(booking);
        return sessionUrl;
    }

    @Override
    @Transactional
    public void capturePayment(Event event) {
        if("checkout.session.completed".equals(event.getType())){
            Object dataObject;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                dataObject = event.getDataObjectDeserializer().getObject().get();
            } else {
                // fallback for Stripe preview API versions
                try {
                    dataObject = event.getDataObjectDeserializer().deserializeUnsafe();
                } catch (EventDataObjectDeserializationException e) {
                    throw new RuntimeException(e);
                }
            }
            Session session = null;
            if (dataObject instanceof Session s) {
                session = s;
            }
//            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session == null) {
                log.error("Failed to deserialize checkout.session.completed event");
                log.warn("Unhandled event type: {}",event.getType());
            }else{
                String sessionId = session.getId();
                Booking booking =
                        bookingRepository.findByPaymentSessionId(sessionId).orElseThrow(
                                () -> new ResourceNotFoundException("Booking not found with session id: {}"+sessionId)
                        );
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                bookingRepository.save(booking);

                inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
                        booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
                ;
                inventoryRepository.confirmBooking(booking.getRoom().getId(),
                        booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());
                log.info("Booking confirmed for session id: {}",sessionId);
            }
        }
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );
        User user = getCurrentUser();
        if(!user.getId().equals(booking.getUser().getId())){
            throw new UnauthorizedException("Booking does not belong to this user with id:"+user.getId());
        }

        if(booking.getBookingStatus()!=BookingStatus.CONFIRMED){
            throw new IllegalStateException("Only confirmed booking can be cancelled");
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

//        inventoryRepository.findAndLockReservedInventory(booking.getRoom().getId(),
//                booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        inventoryRepository.cancelBooking(booking.getRoom().getId(),
                booking.getCheckInDate(),booking.getCheckOutDate(),booking.getRoomsCount());

        try {
            Session session = Session.retrieve(booking.getPaymentSessionId());
            RefundCreateParams refundParam = RefundCreateParams.builder()
                    .setPaymentIntent(session.getPaymentIntent())

                    .build();
            Refund.create(refundParam);
        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBookingStatus(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                ()-> new ResourceNotFoundException("Booking not found with id:"+bookingId)
        );
        User user = getCurrentUser();
        if(!user.equals(booking.getUser())){
            throw new UnauthorizedException("Booking does not belong to this user with id:"+user.getId());
        }
        return booking.getBookingStatus().name();
    }

    @Override
    public List<BookingDTO> getAllBookingsByHotelId(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not exist with id: "+hotelId));
        User user = getCurrentUser();
        log.info("Getting all bookings for the hotel with id: {}",hotelId);

        if(!user.getId().equals(hotel.getOwner().getId())) try {
            throw new AccessDeniedException("You are not the Owner of the hotel");
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }
        List<Booking> bookings =bookingRepository.findByHotel(hotel);

        return bookings.stream()
                .map((element) -> modelMapper.map(element, BookingDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public HotelReportDTO getHotelReport(Long hotelId, LocalDate startDate, LocalDate endDate) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not exist with id: "+hotelId));
        User user = getCurrentUser();
        log.info("Generating Report for hotel with Id {}",hotelId);

        if(!user.getId().equals(hotel.getOwner().getId())) try {
            throw new AccessDeniedException("You are not the Owner of the hotel");
        } catch (AccessDeniedException e) {
            throw new RuntimeException(e);
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Booking> bookings = bookingRepository.findByHotelAndCreatedAtBetween(hotel,startDateTime,endDateTime);

        Long totalConfirmedBooking = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .count();
        BigDecimal totalRevenueOfConfirmedBookings = bookings
                .stream()
                .filter(booking -> booking.getBookingStatus()==BookingStatus.CONFIRMED)
                .map(Booking::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal avgRevenue = totalConfirmedBooking == 0 ?BigDecimal.ZERO
                :totalRevenueOfConfirmedBookings
                .divide(BigDecimal.valueOf(totalConfirmedBooking), RoundingMode.HALF_UP);


        return new HotelReportDTO(totalConfirmedBooking,totalRevenueOfConfirmedBookings,avgRevenue);
    }

    @Override
    public List<BookingDTO> getMyBookings() {
        User user = getCurrentUser();

        return bookingRepository.findByUser(user)
                .stream()
                .map((element) -> modelMapper.map(element, BookingDTO.class))
                .collect(Collectors.toList());
    }

    public boolean hasBookingExpired(Booking booking){
            return booking.getCreatedAt().plusMinutes(10).isBefore(LocalDateTime.now());
    }

}
