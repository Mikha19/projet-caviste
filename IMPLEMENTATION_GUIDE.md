# CaveManager API Integration Guide

## ✅ Database Configuration Updated

Your VM Deployment:
- **Frontend + Backend (Web Server)**: 10.15.3.101
  - Frontend: http://10.15.3.101:3000
  - Backend API: http://10.15.3.101:8080
- **Database Server**: 10.15.3.150
- **Database User**: cavemanager_user
- **Database Password**: passCaviste@user19

Both frontend and backend run on the same web server VM and connect to the database at 10.15.3.150.

## 📝 Changes Made

### 1. Database Connection
- ✅ Updated `cavemanager-api/src/main/resources/database.properties`
- ✅ Updated `cavemanager-desktop/src/main/resources/database.properties`
- Both now point to 10.15.3.150

### 2. Backend Data Layer - Complete Implementation

#### Created DAOs:
- **ProduitDao** - Product data access
  - `findAll()` - List all products
  - `findById(id)` - Get product by ID
  - `rechercher(terme)` - Search products by name/appellation/producer
  - `findByCategorie(id)` - Filter by category
  - `findLowStock()` - Get products below alert threshold

- **CategorieDao** - Category data access
  - `findAll()` - List all categories
  - `findById(id)` - Get category by ID

- **AlerteStockDao** - Stock alerts
  - `findUnread()` - Get unread low-stock alerts
  - `findAll()` - Get all alerts
  - `markAsRead(id)` - Mark alert as read

- **ClientDao** - Already implemented (registration, login, profile)
- **CommandeDao** - Already implemented (cart, orders)

#### Updated API Routes:
- ✅ `GET /api/v1/produits` - List products (with search, filtering, pagination)
- ✅ `GET /api/v1/produits/:id` - Get product details
- ✅ `GET /api/v1/produits/alerts/stock` - Get low-stock alerts
- ✅ `GET /api/v1/commandes` - Customer's orders
- ✅ `GET /api/v1/commandes/panier` - Active cart (auto-creates if needed)
- ✅ `POST /api/v1/commandes/panier/add` - Add to cart
- ✅ `PUT /api/v1/commandes/panier/ligne/:id` - Update cart item
- ✅ `DELETE /api/v1/commandes/panier/ligne/:id` - Remove from cart
- ✅ `POST /api/v1/commandes/panier/valider` - Checkout
- ✅ `GET /api/v1/commandes/:id` - Get order details
- ✅ `GET /api/v1/profile` - Get customer profile
- ✅ `PUT /api/v1/profile` - Update profile

## 🚀 Getting Started

All services run on the **Web Server VM (10.15.3.101)**. Connect to this VM and follow these steps:

### Step 1: Verify Database Access

Test connection to 10.15.3.150 from the web server VM:
```bash
# From web server (10.15.3.101)
mysql -h 10.15.3.150 -u cavemanager_user -p

# Password: passCaviste@user19
# Then check:
USE cavemanager;
SELECT COUNT(*) FROM produits;
SELECT COUNT(*) FROM categories;
SELECT COUNT(*) FROM clients;
```

### Step 2: Start the API Server

On the **Web Server VM (10.15.3.101)**:

```bash
cd cavemanager-api
./gradlew build
./gradlew run

# Or if gradle wrapper doesn't work:
gradle build
gradle run
```

The API will start on `http://10.15.3.101:8080`

(Keep this terminal running)

### Step 3: Test API Endpoints

#### Health Check
```bash
curl http://10.15.3.101:8080/health
```

#### Get All Products
```bash
curl http://10.15.3.101:8080/api/v1/produits
```

#### Register & Login
```bash
# Register
curl -X POST http://10.15.3.101:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "nom": "Dupont",
    "prenom": "Jean"
  }'

# Login (use returned token for other requests)
curl -X POST http://10.15.3.101:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

#### Get Protected Resource (requires token)
```bash
curl -X GET http://10.15.3.101:8080/api/v1/profile \
  -H "Authorization: Bearer {your_token_here}"
```

### Step 4: Configure Frontend

On the **Web Server VM (10.15.3.101)**, create `.env.local` in `cavemanager-web/`:

```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Since both frontend and backend run on the same VM, they can communicate via localhost.

### Step 5: Start Frontend Development Server

On the **Web Server VM (10.15.3.101)** in a new terminal:

```bash
cd cavemanager-web
npm install
npm run dev
```

Frontend runs on `http://10.15.3.101:3000`

## 📊 Testing the Full Flow

1. **Open** http://10.15.3.101:3000 (or `http://localhost:3000` from the web server)
2. **Register** a new customer account
3. **Browse** products (fetched from 10.15.3.150 database)
4. **Add items** to cart
5. **Checkout** - Select pickup date and confirm
6. **View orders** - See order history and status

## 🔐 Security Notes

- JWT tokens expire after 24 hours
- Passwords are hashed with bcrypt (cost=12)
- All protected endpoints require valid token
- CORS is configured for localhost (change for production)

## 🛠️ Environment Variables

### API Backend
Set as system environment variables or in docker-compose:
```
JWT_SECRET=your-secret-key-change-in-production
```

### Frontend (.env.local) - Web Server VM 10.15.3.101
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Database (database.properties) - Web Server VM 10.15.3.101
```
db.host=10.15.3.150
db.port=3306
db.name=cavemanager
db.user=cavemanager_user
db.password=passCaviste@user19
```

### VM Architecture
```
┌──────────────────────────────────────────────────────────┐
│ Web Server VM (10.15.3.101)                              │
│ ├─ Next.js Frontend (http://10.15.3.101:3000)            │
│ │  └─ .env.local → NEXT_PUBLIC_API_URL=http://localhost │
│ ├─ Ktor API (http://10.15.3.101:8080)                    │
│ │  └─ database.properties → db.host=10.15.3.150          │
│ └─ Both services on same machine                         │
└──────────────────────────────────────────────────────────┘
                            ↓
┌──────────────────────────────────────────────────────────┐
│ Database VM (10.15.3.150)                                │
│ ├─ MySQL 8.0+                                            │
│ └─ cavemanager database (user: cavemanager_user)         │
└──────────────────────────────────────────────────────────┘
```

## 📱 Production Deployment Checklist

- [ ] Change JWT_SECRET to a secure random string
- [ ] Update CORS allowed hosts
- [ ] Use HTTPS (SSL/TLS certificates)
- [ ] Set proper database user with limited permissions
- [ ] Configure database backups
- [ ] Setup error logging/monitoring
- [ ] Implement rate limiting
- [ ] Enable database connection encryption
- [ ] Review security headers
- [ ] Setup CI/CD pipeline

## 🐛 Troubleshooting

### "Cannot connect to database"
- Verify 10.15.3.150 is reachable: `ping 10.15.3.150`
- Check MySQL is running on 10.15.3.150
- Verify credentials in database.properties
- Check network firewall rules (port 3306)

### "CORS error in frontend"
- Make sure API is running with correct CORS config
- Frontend URL must be in CORS allowlist
- Check browser console for specific CORS error

### "Token invalid/expired"
- Tokens expire after 24 hours
- Login again to get a new token
- Verify JWT_SECRET matches between API instances

### "Product list is empty"
- Run seed.sql to populate demo data: `mysql -h 10.15.3.150 -u cavemanager_user -p cavemanager < seed.sql`
- Verify database tables exist
- Check database connection is working

## 📚 API Documentation

All endpoints return JSON in this format:
```json
{
  "success": true,
  "message": "Optional message",
  "data": { ... },
  "errors": ["Optional error array"]
}
```

### Status Codes
- 200 OK - Successful GET request
- 201 Created - Successful POST/creation
- 400 Bad Request - Invalid input
- 401 Unauthorized - Missing/invalid token
- 404 Not Found - Resource not found
- 500 Internal Server Error - Server error

## 🔄 Next Steps

1. Test all API endpoints with real data
2. Verify product pricing and stock levels
3. Test cart and order workflow
4. Implement caviste dashboard features
5. Add payment processing
6. Setup email notifications
7. Configure production deployment

## 📞 Support

Check logs at:
- API: stdout/stderr from `gradle run`
- Frontend: Browser console (F12)
- Database: MySQL error log on 10.15.3.150
