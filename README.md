# NeonStays - Backend API

A comprehensive Spring Boot REST API for an Airbnb-like hotel booking platform with dynamic pricing, inventory management, and secure payment processing.

## 📋 Table of Contents

- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [API Endpoints](#-api-endpoints)
- [Authentication](#-authentication)
- [Response Format](#-response-format)
- [Database Schema](#-database-schema)
- [Dynamic Pricing](#-dynamic-pricing-strategies)
- [Payment Integration](#-payment-integration)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)

## 🚀 Features

### Core Features

- **User Authentication & Authorization**
  - JWT-based authentication with access and refresh tokens
  - Role-based access control (Guest, Hotel Manager)
  - OAuth2 Google login support
  - Secure password encryption with BCrypt
  - HttpOnly cookie-based refresh token storage

- **Hotel Management**
  - Complete CRUD operations for hotels
  - Advanced hotel search with date and location filters
  - Hotel activation/deactivation
  - Hotel reports and analytics
  - Photo and amenities management

- **Room Management**
  - Room types with detailed amenities
  - Base pricing configuration
  - Capacity and availability settings
  - Room photo galleries

- **Inventory Management**
  - Date-based inventory tracking
  - Real-time availability checking
  - Inventory updates with surge factors
  - Closed dates management

- **Booking System**
  - Multi-step booking flow
  - Guest information management
  - Booking status tracking (PENDING, CONFIRMED, CANCELLED)
  - Booking cancellation with proper cleanup

- **Dynamic Pricing**
  - Multiple pricing strategies
  - Real-time price calculation
  - Date-based price queries

- **Payment Processing**
  - Stripe integration for secure payments
  - Payment session creation
  - Webhook handling for payment events
  - Payment status tracking

### Additional Features

- **API Documentation**
  - Swagger/OpenAPI integration
  - Interactive API explorer
  - Auto-generated documentation

- **Error Handling**
  - Global exception handling
  - Standardized error responses
  - Detailed error messages

- **Security**
  - CORS configuration
  - Secure cookie settings
  - JWT token validation
  - Role-based endpoint protection

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.5.7
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Security**: Spring Security, JWT (jjwt 0.12.6)
- **Payment**: Stripe Java SDK 24.1.0
- **Documentation**: SpringDoc OpenAPI 2.8.14
- **Mapping**: ModelMapper 3.2.5
- **OAuth**: Spring OAuth2 Client & Resource Server

## 📋 Prerequisites

Before you begin, ensure you have the following installed:

- **Java Development Kit (JDK)** 17 or higher
- **Maven** 3.6 or higher
- **PostgreSQL** 12 or higher
- **Stripe Account** (for payment processing)
- **Google OAuth Credentials** (optional, for OAuth login)

## 🔧 Installation

### 1. Clone the Repository

```bash
git clone <repository-url>
cd airbnbapp
```

### 2. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE airbnb;
```

### 3. Environment Configuration

Create a `.env` file in the root directory or set the following environment variables:

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/airbnb
DB_USERNAME=your_database_username
DB_PASS=your_database_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_minimum_32_characters_long

# Google OAuth (Optional)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# Stripe Configuration
STRIPE_SECRET=sk_test_your_stripe_secret_key
STRIPE_WEBOOK_SECRET=whsec_your_stripe_webhook_secret
```

**Important**: 
- JWT secret should be at least 32 characters long
- Use test keys for development, production keys for deployment
- Webhook secret is obtained from Stripe Dashboard → Webhooks

### 4. Build and Run

**Using Maven:**

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

**Using JAR:**

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/airbnbapp-0.0.1-SNAPSHOT.jar
```

The API will be available at: **http://localhost:8081**

## ⚙️ Configuration

### Application Properties

Key configuration options in `application.properties`:

- **Server Port**: `8081` (default)
- **Database**: Auto-configured via environment variables
- **JPA**: `ddl-auto=update` (auto-generates schema)
- **Swagger**: Enabled by default
- **CORS**: Configured in `CorsConfig.java`

### CORS Configuration

Allowed origins are configured in `WebSecurityConfig.java`:
- `http://localhost:5173` (development)
- `https://neonstays-frontend.onrender.com`
- `https://neonstays.vercel.app`

## 📚 API Documentation

### Swagger UI

Once the application is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

### API Base URL

All endpoints are prefixed with: `/api/v1`

## 🔌 API Endpoints

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/signup` | Register a new user | No |
| POST | `/login` | User login | No |
| POST | `/refresh` | Refresh access token | No (cookie) |
| POST | `/logout` | User logout | No |

**Signup Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securePassword123",
  "dateOfBirth": "1990-01-15",
  "gender": "MALE"
}
```

**Login Request:**
```json
{
  "email": "john@example.com",
  "password": "securePassword123"
}
```

**Login Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```
*Refresh token is set as HttpOnly cookie*

### Hotel Search (`/api/v1/hotels/search`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Search hotels | No |
| GET | `/{hotelId}/info` | Get hotel details with rooms | No |
| GET | `/{roomId}/price?start={date}&end={date}` | Get dynamic room price | No |

**Search Request:**
```json
{
  "city": "New York",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "roomsCount": 1,
  "page": 0,
  "size": 20
}
```

**Search Response:**
```json
{
  "content": [
    {
      "hotel": {
        "id": 1,
        "name": "Grand Hotel",
        "city": "New York",
        "photos": ["url1", "url2"],
        "amenities": ["WiFi", "Pool", "Gym"]
      },
      "price": 150.00
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

### Bookings (`/api/v1/bookings`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/init` | Initialize booking | Yes |
| POST | `/{bookingId}/addGuests` | Add guests to booking | Yes |
| POST | `/{bookingId}/payments` | Initiate Stripe payment | Yes |
| POST | `/{bookingId}/cancel` | Cancel booking | Yes |
| POST | `/{bookingId}/status` | Get booking status | Yes |

**Initialize Booking:**
```json
{
  "hotelId": 1,
  "roomId": 1,
  "checkInDate": "2024-01-15",
  "checkOutDate": "2024-01-20",
  "roomsCount": 1
}
```

**Add Guests:**
```json
[
  {
    "name": "John Doe",
    "gender": "MALE",
    "age": 30
  }
]
```

**Payment Response:**
```json
{
  "sessionUrl": "https://checkout.stripe.com/pay/cs_test_..."
}
```

### User Management (`/api/v1/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/profile` | Get user profile | Yes |
| PATCH | `/profile` | Update user profile | Yes |
| GET | `/bookings` | Get user's bookings | Yes |
| PATCH | `/promote-to-host` | Promote to hotel manager | Yes |

### Admin - Hotels (`/api/v1/admin/hotels`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/` | Get all hotels | Yes | HOTEL_MANAGER |
| POST | `/` | Create hotel | Yes | HOTEL_MANAGER |
| GET | `/{hotelId}` | Get hotel by ID | Yes | HOTEL_MANAGER |
| PUT | `/{hotelId}` | Update hotel | Yes | HOTEL_MANAGER |
| DELETE | `/{hotelId}` | Delete hotel | Yes | HOTEL_MANAGER |
| PATCH | `/{hotelId}/activate` | Activate hotel | Yes | HOTEL_MANAGER |
| GET | `/{hotelId}/bookings` | Get hotel bookings | Yes | HOTEL_MANAGER |
| GET | `/{hotelId}/reports` | Get hotel reports | Yes | HOTEL_MANAGER |

**Create Hotel:**
```json
{
  "name": "Luxury Resort",
  "city": "Miami",
  "photos": ["url1", "url2"],
  "amenities": ["WiFi", "Pool", "Spa"],
  "hotelContactInfo": {
    "phone": "+1234567890",
    "email": "contact@resort.com"
  },
  "active": true
}
```

### Admin - Rooms (`/api/v1/admin/hotels/{hotelId}/rooms`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/` | Get all rooms | Yes | HOTEL_MANAGER |
| POST | `/` | Create room | Yes | HOTEL_MANAGER |
| GET | `/{roomId}` | Get room by ID | Yes | HOTEL_MANAGER |
| PUT | `/{roomId}` | Update room | Yes | HOTEL_MANAGER |
| DELETE | `/{roomId}` | Delete room | Yes | HOTEL_MANAGER |

**Create Room:**
```json
{
  "type": "Deluxe Suite",
  "basePrice": 200.00,
  "photos": ["url1", "url2"],
  "amenities": ["WiFi", "TV", "Mini Bar"],
  "totalCount": 10,
  "capacity": 2
}
```

### Admin - Inventory (`/api/v1/admin/inventory`)

| Method | Endpoint | Description | Auth Required | Role Required |
|--------|----------|-------------|---------------|---------------|
| GET | `/rooms/{roomId}` | Get inventory by room | Yes | HOTEL_MANAGER |
| PATCH | `/rooms/{roomId}` | Update inventory | Yes | HOTEL_MANAGER |

**Update Inventory:**
```json
{
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "surgeFactor": 1.5,
  "closed": false
}
```

### Payment (`/api/v1/payment`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/session/{sessionId}` | Get payment details | No |

### Webhooks (`/api/v1/webhook`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/payment` | Stripe webhook handler | No (Stripe signature) |

## 🔑 Authentication

### JWT Token Flow

1. **Login**
   - Send POST request to `/api/v1/auth/login` with credentials
   - Response contains `accessToken` in body
   - `refreshToken` is set as HttpOnly cookie (expires in 7 days)

2. **Authenticated Requests**
   - Include access token in Authorization header:
     ```
     Authorization: Bearer <accessToken>
     ```

3. **Token Refresh**
   - When access token expires (typically 15 minutes), call `/api/v1/auth/refresh`
   - Refresh token is automatically sent via cookie
   - Returns new access token

4. **Logout**
   - Call `/api/v1/auth/logout` to clear refresh token cookie

### Role-Based Access

- **GUEST**: Can search hotels, make bookings, manage profile
- **HOTEL_MANAGER**: All guest permissions + hotel/room/inventory management

### Token Structure

Access tokens contain:
- User ID
- Email
- Roles
- Expiration time

## 📦 Response Format

All API responses follow a standardized format:

### Success Response

```json
{
  "timeStamp": "2024-01-15T10:30:00",
  "data": {
    // Response data here
  },
  "error": null
}
```

### Error Response

```json
{
  "timeStamp": "2024-01-15T10:30:00",
  "data": null,
  "error": {
    "status": "BAD_REQUEST",
    "message": "Detailed error message",
    "subErrors": []
  }
}
```

### HTTP Status Codes

- `200 OK` - Successful request
- `201 CREATED` - Resource created successfully
- `204 NO_CONTENT` - Successful request with no content
- `400 BAD_REQUEST` - Invalid request data
- `401 UNAUTHORIZED` - Authentication required
- `403 FORBIDDEN` - Insufficient permissions
- `404 NOT_FOUND` - Resource not found
- `500 INTERNAL_SERVER_ERROR` - Server error

## 🗄️ Database Schema

### Key Entities

- **User**: User accounts with authentication and role information
- **Hotel**: Hotel information, photos, amenities, contact details
- **Room**: Room types with pricing, capacity, and amenities
- **Inventory**: Date-based room availability and pricing
- **Booking**: Booking records with status and dates
- **Guest**: Guest information linked to bookings

### Entity Relationships

- User → Bookings (One-to-Many)
- Hotel → Rooms (One-to-Many)
- Room → Inventory (One-to-Many)
- Booking → Guests (One-to-Many)
- Booking → Room (Many-to-One)

### Database Auto-Configuration

The application uses `spring.jpa.hibernate.ddl-auto=update`, which:
- Automatically creates/updates database schema on startup
- Preserves existing data
- **Note**: Use `validate` or `none` in production

## 💰 Dynamic Pricing Strategies

The system implements multiple pricing strategies that can be combined:

1. **Base Pricing**: Standard room price
2. **Surge Pricing**: Price increase during high demand periods
3. **Holiday Pricing**: Special pricing for holidays
4. **Occupancy Pricing**: Price adjustment based on number of guests
5. **Urgency Pricing**: Price increase for last-minute bookings

Pricing is calculated dynamically based on:
- Base room price
- Date range
- Surge factors in inventory
- Booking urgency
- Holiday calendar

## 💳 Payment Integration

### Stripe Integration

The backend integrates with Stripe for secure payment processing:

1. **Payment Session Creation**
   - Endpoint: `POST /api/v1/bookings/{bookingId}/payments`
   - Creates Stripe Checkout session
   - Returns session URL for frontend redirect

2. **Webhook Handling**
   - Endpoint: `POST /api/v1/webhook/payment`
   - Handles Stripe payment events
   - Updates booking status automatically
   - Verifies webhook signatures

3. **Payment Status**
   - Tracked in booking entities
- Queryable via payment session ID

### Stripe Setup

1. Create Stripe account
2. Get API keys from Stripe Dashboard
3. Set up webhook endpoint in Stripe Dashboard:
   - URL: `https://your-domain.com/api/v1/webhook/payment`
   - Events: `checkout.session.completed`, `payment_intent.succeeded`
4. Copy webhook signing secret to environment variables

## 🧪 Testing

### Run Tests

```bash
mvn test
```

### Test Coverage

- Unit tests for services
- Integration tests for controllers
- Repository tests

## 🚀 Deployment

### Production Checklist

1. **Environment Variables**
   - Set all required environment variables
   - Use strong JWT secret (32+ characters)
   - Use production Stripe keys
   - Configure production database

2. **Database**
   - Change `ddl-auto` to `validate` or `none`
   - Run migrations manually if needed
   - Set up database backups

3. **CORS Configuration**
   - Update allowed origins in `WebSecurityConfig.java`
   - Remove localhost origins in production

4. **Security**
   - Enable HTTPS
   - Use secure cookies (already configured)
   - Review security headers

5. **Build and Deploy**
   ```bash
   mvn clean package
   java -jar target/airbnbapp-0.0.1-SNAPSHOT.jar
   ```

### Docker Deployment (Optional)

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/airbnbapp-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 📝 Important Notes

- **Date Format**: All dates must be in ISO format (YYYY-MM-DD)
- **JWT Secret**: Must be at least 32 characters long
- **Refresh Tokens**: Expire after 7 days, stored as HttpOnly cookies
- **Access Tokens**: Typically expire after 15 minutes (configurable)
- **Database**: Schema auto-updates on startup (change for production)
- **CORS**: Configured for specific origins (update for your frontend)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Use Lombok for boilerplate code
- Add Javadoc comments for public methods
- Write unit tests for new features

## 📄 License

This project is licensed under the MIT License.

## 👨‍💻 Author

**Divyansh**

## 📞 Support

For issues, questions, or contributions:
- Open an issue on GitHub
- Contact the maintainer

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Stripe for payment processing
- All contributors and users of this project

---

**Built with ❤️ using Spring Boot**

