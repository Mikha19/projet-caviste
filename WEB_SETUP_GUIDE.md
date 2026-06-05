# CaveManager Web - Architecture & Setup Guide

## 🎯 Project Overview

This is a comprehensive web interface for the CaveManager application, built to handle:
- **Stock Management**: Track inventory with automatic low-stock alerts
- **Customer Area**: Registration, authentication, and profile management
- **Online Ordering**: Shopping cart, order placement for in-store pickup/payment
- **Order Tracking**: Real-time status updates for customers and caviste
- **Caviste Dashboard**: Stock and order management interface

## 📁 Project Structure

```
projet-caviste/
├── cavemanager-desktop/           # Existing JavaFX desktop app
├── cavemanager-api/               # NEW: Ktor REST API backend
│   ├── src/main/kotlin/com/cavemanager/api/
│   │   ├── Application.kt          # Main Ktor app with routing
│   │   ├── data/
│   │   │   ├── DatabasePool.kt     # HikariCP connection pool
│   │   │   ├── ClientDao.kt        # Client operations
│   │   │   └── CommandeDao.kt      # Order operations
│   │   ├── models/
│   │   │   └── DTOs.kt             # Data transfer objects
│   │   └── routes/
│   │       └── Routes.kt           # API endpoints
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── src/main/resources/
│       ├── application.conf        # Ktor config
│       └── database.properties     # DB credentials
├── cavemanager-web/               # NEW: Next.js frontend
│   ├── src/
│   │   ├── app/
│   │   │   ├── (customer)/         # Customer interface routes
│   │   │   │   ├── layout.tsx      # Customer layout with nav
│   │   │   │   ├── shop/page.tsx   # Products listing
│   │   │   │   ├── login/page.tsx  # Customer login
│   │   │   │   ├── register/page.tsx
│   │   │   │   ├── cart/page.tsx   # Shopping cart
│   │   │   │   ├── orders/page.tsx # Order history
│   │   │   │   └── profile/page.tsx # Profile management
│   │   │   └── (caviste)/          # Caviste interface routes (TO DO)
│   │   ├── components/             # Reusable components
│   │   ├── lib/
│   │   │   ├── api-client.ts       # API client (axios)
│   │   │   └── store.ts            # Zustand stores
│   │   └── globals.css
│   ├── package.json
│   ├── tsconfig.json
│   ├── next.config.js
│   └── tailwind.config.js
├── schema-web.sql                 # NEW: Database extensions
└── docker-compose.yml             # NEW: Deployment setup (TO DO)
```

## 🗄️ Database Schema

### New Tables (schema-web.sql)

1. **clients** - Customer accounts
   - Secure password storage (bcrypt)
   - Profile information
   - Account status tracking

2. **commandes** - Orders
   - Status tracking (PANIER → VALIDEE → EN_PREPARATION → PRETE → RETIREE)
   - Payment tracking
   - Pickup information

3. **lignes_commande** - Order items
   - Product and quantity details
   - Price snapshot at order time

4. **historique_commandes** - Order history
   - Audit trail of status changes
   - Comment tracking

5. **alertes_stock** - Low stock alerts
   - Track stock levels
   - Alert status

6. **avis_produits** - Product reviews
   - Customer ratings (1-5 stars)
   - Review moderation

## 🚀 Quick Start

### 1. Initialize Database

```bash
# Create database and tables
mysql -u root -p < schema.sql
mysql -u root -p < schema-web.sql

# Populate demo data
mysql -u root -p < seed.sql
```

### 2. Start Ktor API Backend

```bash
cd cavemanager-api
./gradlew run

# Or build and run jar
./gradlew build
java -jar build/libs/cavemanager-api-1.0.0.jar
```

The API will be available at `http://localhost:8080`

### 3. Start Next.js Frontend

```bash
cd cavemanager-web
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000`

## 🔐 Authentication Flow

1. **Registration/Login**: 
   - User submits credentials
   - API validates and creates JWT token (24h expiry)
   - Token stored in localStorage

2. **Protected Requests**:
   - All requests include `Authorization: Bearer {token}` header
   - API validates JWT and extracts clientId
   - Invalid/expired tokens redirect to login

3. **JWT Claims**:
   ```
   {
     "iss": "cavemanager",
     "aud": "cavemanager-clients",
     "clientId": 123,
     "exp": 1234567890
   }
   ```

## 📝 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new customer
- `POST /api/v1/auth/login` - Login and get token

### Products (Protected)
- `GET /api/v1/produits` - List products (with pagination & search)
- `GET /api/v1/produits/:id` - Get product details
- `GET /api/v1/produits/alerts/stock` - Get low stock alerts

### Orders (Protected)
- `GET /api/v1/commandes` - Get customer's orders
- `GET /api/v1/commandes/panier` - Get active cart
- `POST /api/v1/commandes/panier/add` - Add item to cart
- `PUT /api/v1/commandes/panier/ligne/:id` - Update cart line quantity
- `DELETE /api/v1/commandes/panier/ligne/:id` - Remove item from cart
- `POST /api/v1/commandes/panier/valider` - Checkout/validate order
- `GET /api/v1/commandes/:id` - Get order details

### Profile (Protected)
- `GET /api/v1/profile` - Get customer profile
- `PUT /api/v1/profile` - Update profile

## 🎨 Features Implemented

### ✅ Customer Interface
- [x] Product listing with search
- [x] Shopping cart management
- [x] Login/Registration
- [x] Profile management (structure ready)
- [ ] Cart page (component ready, needs integration)
- [ ] Orders history page (needs implementation)
- [ ] Order tracking (needs implementation)
- [ ] Product reviews (needs implementation)

### ✅ Backend API
- [x] JWT authentication
- [x] Client registration and login
- [x] Password hashing with bcrypt
- [x] Database connection pooling (HikariCP)
- [x] Core DAO layer
- [x] CORS configuration
- [ ] Product listing endpoint (needs ProduitDao integration)
- [ ] Order processing (partially implemented)
- [ ] Stock alert system (needs implementation)

### ⏳ Caviste Interface (TO DO)
- [ ] Dashboard layout
- [ ] Stock management interface
- [ ] Order processing interface
- [ ] Stock alert visualization
- [ ] Sales reports

### ⏳ Additional Features
- [ ] Email notifications
- [ ] PDF order generation
- [ ] Payment integration
- [ ] Advanced search filters
- [ ] Wishlist functionality
- [ ] Admin panel

## 🔧 Development Notes

### Key Technologies
- **Backend**: Kotlin + Ktor + HikariCP + MySQL
- **Frontend**: Next.js 14 + TypeScript + Tailwind CSS + Zustand
- **Authentication**: JWT (HS256)
- **Security**: bcrypt password hashing

### Environment Variables

**Frontend** (.env.local):
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

**Backend** (set as system env or in docker-compose):
```
JWT_SECRET=your-secret-key-change-in-production
```

### Database Connection
All three components (Desktop, API, Web) use the same MySQL database. The API uses HikariCP for connection pooling (10 max connections).

## 📊 Stock Management Features

### Low Stock Alerts
- Configured per product (seuil_alerte)
- Tracked in `alertes_stock` table
- Visible to caviste in dashboard
- API endpoint: `GET /produits/alerts/stock`

### Stock Movements
- ENTREE (Stock increase)
- SORTIE (Stock decrease)
- Historisé in `mouvements_stock`
- Accessible via desktop app and web API

## 🧪 Testing

### Mock Data
Demo products and movements are in `seed.sql`:
- 30+ wines, beers, spirits
- Stock levels from 0-48 units
- Various categories

### API Testing
Use Postman or curl:
```bash
# Register
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123",
    "nom": "Dupont",
    "prenom": "Jean"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'

# Get products (with token)
curl -X GET http://localhost:8080/api/v1/produits \
  -H "Authorization: Bearer {token}"
```

## 🚢 Deployment

### Docker Setup (TO DO)
Create `docker-compose.yml` with:
- MySQL 8.0 container
- Ktor API container
- Next.js frontend container

### Production Checklist
- [ ] Change JWT_SECRET
- [ ] Setup SSL/TLS
- [ ] Configure database credentials
- [ ] Enable rate limiting
- [ ] Setup logging/monitoring
- [ ] Database backups
- [ ] Environment-specific config

## 📚 Next Steps

1. **Complete Product DAO Integration**
   - Implement `ProduitDao.findAll()` and `findById()` for API

2. **Finish Cart Pages**
   - Cart display page
   - Checkout process
   - Order confirmation

3. **Order History**
   - Customer order list
   - Order detail tracking
   - Status timeline

4. **Caviste Interface**
   - Stock management dashboard
   - Order processing
   - Alert management

5. **Admin Features**
   - Product management
   - Category management
   - User management
   - Sales analytics

## 🐛 Common Issues

### CORS Errors
- Ensure API allows your frontend URL
- Check `installCORS()` in Application.kt

### JWT Validation Fails
- Verify JWT_SECRET is the same in API
- Check token expiry (24 hours)

### Database Connection Issues
- Verify MySQL is running
- Check database.properties credentials
- Ensure database and tables exist (run schema-web.sql)

## 📖 Documentation

- [Ktor Documentation](https://ktor.io/docs)
- [Next.js Documentation](https://nextjs.org/docs)
- [Zustand Documentation](https://github.com/pmndrs/zustand)
- [Tailwind CSS](https://tailwindcss.com/docs)

## 📧 Support

For issues or questions, check:
1. API logs (stdout)
2. Browser console (Frontend)
3. MySQL error log
4. Application.conf and database.properties
