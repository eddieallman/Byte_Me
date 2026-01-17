## API Endpoints

### Auth
- `POST /api/auth/register` - Register
- `POST /api/auth/login` - Login
- `GET /api/auth/me` - Current user

### Bundles
- `GET /api/bundles` - List available
- `GET /api/bundles/{id}` - Get one
- `POST /api/bundles` - Create (seller)
- `PUT /api/bundles/{id}` - Update (seller)
- `POST /api/bundles/{id}/activate` - Activate
- `POST /api/bundles/{id}/close` - Close

### Reservations
- `POST /api/reservations` - Reserve
- `GET /api/reservations/org/{orgId}` - By org
- `GET /api/reservations/employee/{employeeId}` - By employee
- `POST /api/reservations/{id}/verify` - Verify claim code
- `POST /api/reservations/{id}/no-show` - Mark no-show
- `POST /api/reservations/{id}/cancel` - Cancel
- `POST /api/reservations/{id}/assign/{employeeId}` - Assign employee

### Analytics
- `GET /api/analytics/dashboard/{sellerId}` - Dashboard
- `GET /api/analytics/sell-through/{sellerId}` - Sell-through rates
- `GET /api/analytics/waste/{sellerId}` - Waste metrics

### Gamification
- `GET /api/gamification/streak/{employeeId}` - Get streak
- `GET /api/gamification/impact/{employeeId}` - Impact summary
- `GET /api/gamification/badges/{employeeId}` - Employee badges
- `GET /api/gamification/badges` - All badges

### Issues
- `GET /api/issues/seller/{sellerId}` - All issues
- `GET /api/issues/seller/{sellerId}/open` - Open issues
- `POST /api/issues` - Create issue
- `POST /api/issues/{id}/respond` - Respond
- `POST /api/issues/{id}/resolve` - Resolve

### Categories
- `GET /api/categories` - List all