# NeonStays - Backend API

A comprehensive Spring Boot REST API for an Airbnb-like hotel booking platform with dynamic pricing, inventory management, and payment processing.

## 🚀 Features

- **User Authentication & Authorization**
  - JWT-based authentication with refresh tokens
  - Role-based access control (Guest, Hotel Manager)
  - OAuth2 Google login support
  - Secure password encryption with BCrypt

- **Hotel Management**
  - CRUD operations for hotels
  - Hotel search with date and location filters
  - Dynamic pricing strategies (base, surge, holiday, occupancy, urgency)
  - Hotel activation/deactivation
  - Hotel reports and analytics

- **Room Management**
  - Room types with amenities and pricing
  - Inventory management per room
  - Real-time availability checking
  - Dynamic price calculation

- **Booking System**
  - Multi-step booking flow
  - Guest management
  - Booking status tracking
  - Booking cancellation

- **Payment Processing**
  - Stripe integration
  - Webhook handling for payment events
  - Secure payment sessions

- **Additional Features**
  - Swagger/OpenAPI documentation
  - Global exception handling
  - CORS configuration
  - Response wrapping with ApiResponse

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Stripe account (for payments)

## 🛠️ Installation

1. **Clone the repository**
   git clone <repository-url>
   cd airbnbapp
   2. **Set up PostgreSQL database**
   CREATE DATABASE airbnb;
   3. **Configure environment variables**
   
   Create a `.env` file or set the following environment variables:roperties
   DB_URL=jdbc:postgresql://localhost:5432/airbnb
   DB_USERNAME=your_db_username
   DB_PASS=your_db_password
   JWT_SECRET=your_jwt_secret_key_min_32_chars
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   STRIPE_SECRET=sk_test_your_stripe_secret_key
   STRIPE_WEBOOK_SECRET=whsec_your_webhook_secret
   4. **Build and run**
   
   mvn clean install
   mvn spring-boot:run
      The API will be available at `http://localhost:8081`

## 📚 API Documentation

Once the application is running, access Swagger UI at:
- **Swagger UI**: `http://localhost:8081/swagger-ui.html`
- **API Docs**: `http://localhost:8081/v3/api-docs`

## 🔐 API Endpoints

### Authentication (`/api/v1/auth`)
- `POST /signup` - User registration
- `POST /login` - User login (returns access token, sets refresh token cookie)
- `POST /refresh` - Refresh access token
- `POST /logout` - User logout

### Hotel Search (`/api/v1/hotels/search`)
- `POST /` - Search hotels with filters
  {
    "city": "New York",
    "startDate": "2024-01-15",
    "endDate": "2024-01-20",
    "roomsCount": 1,
    "page": 0,
    "size": 20
  }
  - `GET /{hotelId}/info` - Get hotel details with rooms
- `GET /{roomId}/price?start={date}&end={date}` - Get dynamic room price

### Bookings (`/api/v1/bookings`)
- `POST /init` - Initialize booking
- `POST /{bookingId}/addGuests` - Add guests to booking
- `POST /{bookingId}/payments` - Initiate Stripe payment
- `POST /{bookingId}/cancel` - Cancel booking
- `POST /{bookingId}/status` - Get booking status

### User Management (`/api/v1/users`)
- `GET /profile` - Get user profile (authenticated)
- `PATCH /profile` - Update user profile (authenticated)
- `GET /bookings` - Get user's bookings (authenticated)
- `PATCH /promote-to-host` - Promote user to hotel manager (authenticated)

### Admin - Hotels (`/api/v1/admin/hotels`)
- `GET /` - Get all hotels
- `POST /` - Create hotel
- `GET /{hotelId}` - Get hotel by ID
- `PUT /{hotelId}` - Update hotel
- `DELETE /{hotelId}` - Delete hotel
- `PATCH /{hotelId}/activate` - Activate hotel
- `GET /{hotelId}/bookings` - Get hotel bookings
- `GET /{hotelId}/reports?startDate={date}&endDate={date}` - Get hotel reports

### Admin - Rooms (`/api/v1/admin/hotels/{hotelId}/rooms`)
- `GET /` - Get all rooms in hotel
- `POST /` - Create room
- `GET /{roomId}` - Get room by ID
- `PUT /{roomId}` - Update room
- `DELETE /{roomId}` - Delete room

### Admin - Inventory (`/api/v1/admin/inventory`)
- `GET /rooms/{roomId}` - Get inventory by room
- `PATCH /rooms/{roomId}` - Update inventory

## 🔑 Authentication

The API uses JWT tokens for authentication:

1. **Login** - Send credentials to `/api/v1/auth/login`
   - Returns `accessToken` in response body
   - Sets `refreshToken` as HttpOnly cookie

2. **Authenticated Requests** - Include token in header:
   
