# CT-Tibet-WMS API Quick Start Guide

**Version**: 1.0
**Updated**: 2025-11-17

A practical guide to get started with CT-Tibet-WMS APIs quickly.

---

## Quick Navigation

- [Setup & Access](#setup--access)
- [5-Minute Getting Started](#5-minute-getting-started)
- [Common Workflows](#common-workflows)
- [Code Examples](#code-examples)
- [Troubleshooting](#troubleshooting)

---

## Setup & Access

### Prerequisites

- Base URL: `http://localhost:48888/api`
- Valid user credentials
- Tools: Postman, curl, or any HTTP client
- JWT token for authenticated requests

### API Documentation

- **Interactive API Docs**: http://localhost:48888/doc.html
- **Full Reference**: See `API_REFERENCE.md`
- **Testing Guide**: See `API_TEST_GUIDE.md`

---

## 5-Minute Getting Started

### Step 1: Login and Get Token

**Endpoint**: `POST /api/auth/login`

```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "warehouse_manager",
    "password": "password123",
    "loginType": "PASSWORD"
  }'
```

**Response**:
```json
{
  "code": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 7200,
    "user": {
      "id": 1,
      "username": "warehouse_manager",
      "realName": "John Doe",
      "roleCode": "WAREHOUSE"
    }
  }
}
```

**Save the token** for next requests.

---

### Step 2: Make Authenticated Requests

All subsequent requests require the token in Authorization header:

```bash
curl -X GET http://localhost:48888/api/inventories \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

---

### Step 3: Try a Simple Operation

**Get your user profile**:

```bash
curl -X GET http://localhost:48888/api/users/profile \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

You're ready to use the API!

---

## Common Workflows

### Workflow 1: Create and Manage Material Requisition

#### Step 1: Submit Application

```bash
curl -X POST http://localhost:48888/api/applies \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "applyTime": "2025-11-17T14:30:00",
    "applyReason": "Network maintenance - fiber replacement",
    "details": [
      {
        "materialId": 1,
        "quantity": 500,
        "remark": "Single-mode fiber for backbone"
      },
      {
        "materialId": 3,
        "quantity": 200,
        "remark": "Cabling equipment for termination"
      }
    ]
  }'
```

**Response**: Application ID (e.g., 15)

#### Step 2: Check My Applications

```bash
curl -X GET "http://localhost:48888/api/applies/my?pageNum=1&status=0" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Step 3: Warehouse Manager Approves

```bash
curl -X POST http://localhost:48888/api/applies/approve \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "applyId": 15,
    "approvalResult": 1,
    "approvalRemark": "Approved - materials available"
  }'
```

#### Step 4: Check Notifications

```bash
curl -X GET "http://localhost:48888/api/messages?pageNum=1" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Step 5: Employee Picks Up Materials

```bash
# Get pending outbounds
curl -X GET "http://localhost:48888/api/outbounds?status=0" \
  -H "Authorization: Bearer <MANAGER_TOKEN>"

# Confirm pickup
curl -X POST http://localhost:48888/api/outbounds/1/confirm \
  -H "Authorization: Bearer <MANAGER_TOKEN>"
```

---

### Workflow 2: Manage Warehouse Inventory

#### Step 1: Receive New Shipment

```bash
curl -X POST http://localhost:48888/api/inbounds \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "inboundType": 1,
    "inboundTime": "2025-11-17T10:00:00",
    "remark": "Quarterly supplier delivery",
    "details": [
      {
        "materialId": 1,
        "quantity": 2000,
        "unitPrice": 50.00,
        "remark": "High-quality fiber cable"
      },
      {
        "materialId": 5,
        "quantity": 50,
        "unitPrice": 1500.00,
        "remark": "Server equipment units"
      }
    ]
  }'
```

**Result**: Inventory automatically updated

#### Step 2: Check Inventory Levels

```bash
curl -X GET "http://localhost:48888/api/inventories?pageNum=1&warehouseId=1" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Step 3: Check Low Stock Alerts

```bash
curl -X GET "http://localhost:48888/api/inventories/low-stock-alerts?warehouseId=1" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Step 4: Issue Materials Directly

```bash
curl -X POST http://localhost:48888/api/outbounds/direct \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "receiverId": 5,
    "outboundTime": "2025-11-17T14:30:00",
    "remark": "Equipment for annual maintenance",
    "details": [
      {
        "materialId": 1,
        "quantity": 100,
        "remark": "For network expansion"
      }
    ]
  }'
```

**Result**: Inventory immediately deducted

#### Step 5: View Inventory Transactions

```bash
curl -X GET "http://localhost:48888/api/inventories/logs?pageNum=1&warehouseId=1&startDate=2025-11-01&endDate=2025-11-17" \
  -H "Authorization: Bearer <TOKEN>"
```

---

### Workflow 3: View Dashboard & Reports

#### Get Dashboard Overview

```bash
curl -X GET http://localhost:48888/api/statistics/dashboard \
  -H "Authorization: Bearer <TOKEN>"
```

Response includes:
- Today's inbound/outbound quantities
- Pending applications count
- Low stock alerts
- Total inventory value

#### Get Inbound Statistics

```bash
curl -X GET "http://localhost:48888/api/statistics/inbound?startDate=2025-11-01&endDate=2025-11-17&warehouseId=1" \
  -H "Authorization: Bearer <TOKEN>"
```

Response includes:
- Total quantities
- Order counts
- Top materials by category
- Daily trends

#### Get Outbound Statistics

```bash
curl -X GET "http://localhost:48888/api/statistics/outbound?startDate=2025-11-01&endDate=2025-11-17" \
  -H "Authorization: Bearer <TOKEN>"
```

Response includes:
- Total quantities
- Direct vs application outbounds
- Top materials
- Daily trends

#### Get Inventory Statistics

```bash
curl -X GET "http://localhost:48888/api/statistics/inventory?warehouseId=1" \
  -H "Authorization: Bearer <TOKEN>"
```

Response includes:
- Total inventory value
- Utilization rate
- Top items by value
- Inventory age analysis

---

### Workflow 4: User & Permission Management

#### Create New User (Admin Only)

```bash
curl -X POST http://localhost:48888/api/users \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "new_employee",
    "password": "SecurePass123!",
    "realName": "Jane Smith",
    "phone": "13800000002",
    "email": "jane.smith@example.com",
    "deptId": 1,
    "roleId": 4,
    "status": 0
  }'
```

#### List All Users

```bash
curl -X GET "http://localhost:48888/api/users?pageNum=1&pageSize=20&deptId=1" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Update User

```bash
curl -X PUT http://localhost:48888/api/users/5 \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "Jane Updated",
    "phone": "13800000099",
    "deptId": 2
  }'
```

#### Reset User Password

```bash
curl -X PUT "http://localhost:48888/api/users/5/reset-password?newPassword=NewPass123!" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

#### Disable User Account

```bash
curl -X PUT "http://localhost:48888/api/users/5/status?status=1" \
  -H "Authorization: Bearer <ADMIN_TOKEN>"
```

---

### Workflow 5: Master Data Management

#### List Materials

```bash
curl -X GET "http://localhost:48888/api/materials?pageNum=1&category=Networking&status=0" \
  -H "Authorization: Bearer <TOKEN>"
```

#### Get Material Categories

```bash
curl -X GET http://localhost:48888/api/materials/categories \
  -H "Authorization: Bearer <TOKEN>"
```

#### Create Material

```bash
curl -X POST http://localhost:48888/api/materials \
  -H "Authorization: Bearer <MANAGER_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MAT-NEW-001",
    "name": "New Equipment Model",
    "category": "Server Equipment",
    "unit": "piece",
    "specification": "Latest model",
    "unitPrice": 2000.00,
    "minStock": 5,
    "description": "Latest server equipment",
    "supplier": "TechSupply Inc"
  }'
```

#### List Warehouses

```bash
curl -X GET "http://localhost:48888/api/warehouses?deptId=1&status=0" \
  -H "Authorization: Bearer <TOKEN>"
```

#### List Departments

```bash
curl -X GET http://localhost:48888/api/depts/tree \
  -H "Authorization: Bearer <TOKEN>"
```

---

## Code Examples

### JavaScript/Node.js Example

```javascript
const axios = require('axios');

const API_BASE = 'http://localhost:48888/api';
let authToken = '';

// Step 1: Login
async function login() {
  const response = await axios.post(`${API_BASE}/auth/login`, {
    username: 'warehouse_manager',
    password: 'password123',
    loginType: 'PASSWORD'
  });
  authToken = response.data.data.token;
  console.log('Login successful, token:', authToken);
  return authToken;
}

// Step 2: Create application
async function submitApplication() {
  const response = await axios.post(
    `${API_BASE}/applies`,
    {
      warehouseId: 1,
      applyTime: new Date().toISOString(),
      applyReason: 'Network maintenance',
      details: [
        {
          materialId: 1,
          quantity: 500,
          remark: 'For backbone network'
        }
      ]
    },
    {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    }
  );
  console.log('Application submitted:', response.data.data);
  return response.data.data;
}

// Step 3: Get dashboard stats
async function getDashboard() {
  const response = await axios.get(
    `${API_BASE}/statistics/dashboard`,
    {
      headers: {
        'Authorization': `Bearer ${authToken}`
      }
    }
  );
  console.log('Dashboard stats:', response.data.data);
  return response.data.data;
}

// Main execution
async function main() {
  await login();
  await submitApplication();
  await getDashboard();
}

main().catch(error => {
  console.error('Error:', error.response?.data || error.message);
});
```

### Python Example

```python
import requests
import json
from datetime import datetime

API_BASE = 'http://localhost:48888/api'

class WMSClient:
    def __init__(self):
        self.token = None
        self.session = requests.Session()

    def login(self, username, password):
        """Login and get JWT token"""
        response = requests.post(
            f'{API_BASE}/auth/login',
            json={
                'username': username,
                'password': password,
                'loginType': 'PASSWORD'
            }
        )
        self.token = response.json()['data']['token']
        self.session.headers.update({
            'Authorization': f'Bearer {self.token}'
        })
        print(f'Login successful')
        return self.token

    def get_dashboard_stats(self):
        """Get dashboard statistics"""
        response = self.session.get(f'{API_BASE}/statistics/dashboard')
        return response.json()['data']

    def submit_application(self, warehouse_id, materials):
        """Submit material application"""
        payload = {
            'warehouseId': warehouse_id,
            'applyTime': datetime.now().isoformat(),
            'applyReason': 'Requested materials',
            'details': materials
        }
        response = self.session.post(f'{API_BASE}/applies', json=payload)
        return response.json()['data']

    def get_inventory(self, warehouse_id=None, page_num=1):
        """Get inventory listing"""
        params = {'pageNum': page_num, 'pageSize': 20}
        if warehouse_id:
            params['warehouseId'] = warehouse_id

        response = self.session.get(f'{API_BASE}/inventories', params=params)
        return response.json()['data']

    def create_inbound(self, warehouse_id, materials, inbound_type=1):
        """Create inbound order"""
        payload = {
            'warehouseId': warehouse_id,
            'inboundType': inbound_type,
            'inboundTime': datetime.now().isoformat(),
            'remark': 'Stock received',
            'details': materials
        }
        response = self.session.post(f'{API_BASE}/inbounds', json=payload)
        return response.json()['data']

    def approve_application(self, apply_id, approved=True):
        """Approve or reject application"""
        payload = {
            'applyId': apply_id,
            'approvalResult': 1 if approved else 2,
            'approvalRemark': 'Processed' if approved else 'Rejected'
        }
        response = self.session.post(f'{API_BASE}/applies/approve', json=payload)
        return response.json()

# Usage
client = WMSClient()
client.login('warehouse_manager', 'password123')

# Get dashboard stats
stats = client.get_dashboard_stats()
print(f"Today's inbound: {stats['todayInbound']}")
print(f"Today's outbound: {stats['todayOutbound']}")

# Submit application
app_id = client.submit_application(1, [
    {'materialId': 1, 'quantity': 500}
])
print(f"Application submitted: {app_id}")

# Create inbound
inbound_id = client.create_inbound(1, [
    {'materialId': 1, 'quantity': 1000, 'unitPrice': 50.0}
])
print(f"Inbound created: {inbound_id}")
```

### Java Example

```java
import okhttp3.*;
import com.google.gson.*;
import java.io.IOException;

public class WMSClient {
    private static final String API_BASE = "http://localhost:48888/api";
    private static final OkHttpClient client = new OkHttpClient();
    private static String authToken;
    private static final Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        // Login
        login("warehouse_manager", "password123");

        // Get dashboard
        getDashboard();

        // Submit application
        submitApplication();
    }

    static void login(String username, String password) throws IOException {
        JsonObject loginBody = new JsonObject();
        loginBody.addProperty("username", username);
        loginBody.addProperty("password", password);
        loginBody.addProperty("loginType", "PASSWORD");

        RequestBody body = RequestBody.create(
            loginBody.toString(),
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(API_BASE + "/auth/login")
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            JsonObject result = JsonParser.parseString(response.body().string())
                .getAsJsonObject();
            authToken = result.getAsJsonObject("data")
                .get("token").getAsString();
            System.out.println("Login successful");
        }
    }

    static void getDashboard() throws IOException {
        Request request = new Request.Builder()
            .url(API_BASE + "/statistics/dashboard")
            .addHeader("Authorization", "Bearer " + authToken)
            .build();

        try (Response response = client.newCall(request).execute()) {
            JsonObject result = JsonParser.parseString(response.body().string())
                .getAsJsonObject();
            JsonObject data = result.getAsJsonObject("data");
            System.out.println("Today's inbound: " + data.get("todayInbound"));
            System.out.println("Today's outbound: " + data.get("todayOutbound"));
        }
    }

    static void submitApplication() throws IOException {
        JsonObject detail = new JsonObject();
        detail.addProperty("materialId", 1);
        detail.addProperty("quantity", 500);

        JsonArray details = new JsonArray();
        details.add(detail);

        JsonObject appBody = new JsonObject();
        appBody.addProperty("warehouseId", 1);
        appBody.addProperty("applyTime", "2025-11-17T14:30:00");
        appBody.addProperty("applyReason", "Materials needed");
        appBody.add("details", details);

        RequestBody body = RequestBody.create(
            appBody.toString(),
            MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
            .url(API_BASE + "/applies")
            .addHeader("Authorization", "Bearer " + authToken)
            .post(body)
            .build();

        try (Response response = client.newCall(request).execute()) {
            System.out.println("Application submitted");
        }
    }
}
```

---

## Troubleshooting

### Common Issues & Solutions

#### Issue 1: "Unauthorized - 401"

**Problem**: Getting 401 error on API calls

**Solutions**:
1. Check token is included in Authorization header
2. Verify token format: `Bearer <token>` (with space)
3. Token might be expired - refresh using `/auth/refresh-token`
4. Re-login if token is completely invalid

```bash
# Refresh token
curl -X POST http://localhost:48888/api/auth/refresh-token \
  -H "Authorization: Bearer <old_token>"

# Or re-login
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"...", "password":"..."}'
```

---

#### Issue 2: "Forbidden - 403"

**Problem**: Getting 403 error (insufficient permissions)

**Solutions**:
1. Verify user role has required permissions:
   - ADMIN: Full access
   - DEPT_ADMIN: Department-level operations
   - WAREHOUSE: Warehouse operations
   - USER: Employee-level access
2. Check endpoint requirements in API Reference
3. Request elevated privileges from administrator

**Required Roles by Operation**:
- Creating users: ADMIN, DEPT_ADMIN
- Approving applications: WAREHOUSE, DEPT_ADMIN
- Creating inbounds: WAREHOUSE, DEPT_ADMIN
- Viewing reports: Any authenticated user

---

#### Issue 3: "Insufficient Inventory - 1000"

**Problem**: Outbound operation fails with inventory error

**Solutions**:
1. Check current inventory levels
2. Verify sufficient stock for the operation
3. Wait for pending inbound orders to complete
4. Request additional inventory from supplier

```bash
# Check inventory
curl -X GET "http://localhost:48888/api/inventories?materialId=1" \
  -H "Authorization: Bearer <token>"

# Check low stock alerts
curl -X GET http://localhost:48888/api/inventories/low-stock-alerts \
  -H "Authorization: Bearer <token>"
```

---

#### Issue 4: "Validation Error - 400"

**Problem**: Request parameters are invalid

**Solutions**:
1. Review error message in response for specific field
2. Check parameter types and required fields
3. Validate date formats (yyyy-MM-dd or ISO 8601)
4. Ensure numeric fields are not strings

**Example error response**:
```json
{
  "code": 400,
  "message": "Validation failed: quantity cannot be null",
  "data": null
}
```

---

#### Issue 5: "Token Expired"

**Problem**: Token is expired after 2 hours

**Solutions**:
1. Use refresh-token endpoint (valid for 7 days):
   ```bash
   curl -X POST http://localhost:48888/api/auth/refresh-token \
     -H "Authorization: Bearer <old_token>"
   ```

2. Or re-login to get fresh token:
   ```bash
   curl -X POST http://localhost:48888/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"...", "password":"..."}'
   ```

---

#### Issue 6: "Resource Not Found - 404"

**Problem**: Getting 404 error for valid-looking resource

**Solutions**:
1. Verify resource ID exists:
   ```bash
   curl -X GET http://localhost:48888/api/users/999 \
     -H "Authorization: Bearer <token>"
   ```

2. Check if resource is soft-deleted (status=1)
3. Verify correct warehouse/department context
4. Resource may have been deleted by another user

---

#### Issue 7: "CORS Error in Browser"

**Problem**: Browser blocking API requests

**Solutions**:
1. Frontend must be on same domain or use CORS-enabled proxy
2. For development, use `http://localhost:48888` as base
3. Include credentials if needed: `credentials: 'include'`

**JavaScript fetch example**:
```javascript
fetch('http://localhost:48888/api/inventories', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  credentials: 'include'
})
```

---

### Debug Tips

#### Enable Request Logging

Use curl verbose mode:
```bash
curl -v -X GET http://localhost:48888/api/inventories \
  -H "Authorization: Bearer <token>"
```

#### Check Server Logs

Server logs located in `backend/logs/`:
- `all.log` - All logs
- `error.log` - Errors only
- `sql.log` - SQL queries

#### Verify API Health

```bash
# Check server is running
curl http://localhost:48888/

# Get current user info
curl -X GET http://localhost:48888/api/auth/user-info \
  -H "Authorization: Bearer <token>"
```

---

## Next Steps

1. **Read Full API Reference**: Check `API_REFERENCE.md` for complete endpoint documentation
2. **Try API Testing**: Use `API_TEST_GUIDE.md` for testing workflows
3. **Use Interactive Docs**: Visit http://localhost:48888/doc.html
4. **Integrate with Frontend**: Start building frontend applications
5. **Contact Support**: Reach out to development team for questions

---

**Quick Start Version**: 1.0
**Last Updated**: 2025-11-17
**Status**: Production-Ready
