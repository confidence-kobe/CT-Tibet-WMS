# CT-Tibet-WMS API Testing Guide

**Version**: 1.0
**Updated**: 2025-11-17

Comprehensive guide for testing CT-Tibet-WMS APIs using Postman, curl, and automated testing.

---

## Table of Contents

1. [Testing Setup](#testing-setup)
2. [Postman Collection](#postman-collection)
3. [Test Scenarios](#test-scenarios)
4. [Curl Testing](#curl-testing-examples)
5. [Automated Testing](#automated-testing)
6. [Performance Testing](#performance-testing)
7. [Security Testing](#security-testing)

---

## Testing Setup

### Prerequisites

- Base URL: `http://localhost:48888/api`
- Valid test user credentials
- Postman (or alternative HTTP client)
- curl (command line)
- Test database with sample data

### Test User Accounts

| Username | Password | Role | Purpose |
|----------|----------|------|---------|
| admin | password123 | System Admin | Full access testing |
| dept_admin | password123 | Dept Admin | Department operations |
| warehouse_mgr | password123 | Warehouse | Warehouse operations |
| employee1 | password123 | Employee | Material requisition |
| employee2 | password123 | Employee | Material requisition |

### Test Environment Configuration

Create environment variables in Postman or save as shell variables:

```bash
# Shell environment setup
export WMS_BASE_URL="http://localhost:48888/api"
export WMS_ADMIN_USER="admin"
export WMS_ADMIN_PASS="password123"
export WMS_WAREHOUSE_USER="warehouse_mgr"
export WMS_WAREHOUSE_PASS="password123"
export WMS_EMPLOYEE_USER="employee1"
export WMS_EMPLOYEE_PASS="password123"
```

---

## Postman Collection

### Import Collection

1. Open Postman
2. Click "Import" → "File"
3. Create collection with folders:
   - Authentication
   - User Management
   - Department Management
   - Material Management
   - Inventory Management
   - Inbound Operations
   - Outbound Operations
   - Application & Approval
   - Statistics
   - Messages

### Collection Variables

Add environment variables in Postman:

```json
{
  "base_url": "http://localhost:48888/api",
  "admin_token": "",
  "manager_token": "",
  "employee_token": "",
  "warehouse_id": "1",
  "material_id": "1",
  "user_id": "1",
  "application_id": ""
}
```

### Pre-request Scripts

Add authentication setup to collection pre-request:

```javascript
// Get token if not set or expired
if (!pm.environment.get("admin_token")) {
    const loginRequest = {
        url: pm.environment.get("base_url") + "/auth/login",
        method: "POST",
        header: {
            "Content-Type": "application/json"
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                username: pm.environment.get("admin_user") || "admin",
                password: pm.environment.get("admin_pass") || "password123",
                loginType: "PASSWORD"
            })
        }
    };

    pm.sendRequest(loginRequest, function (err, response) {
        if (!err) {
            var jsonData = response.json();
            pm.environment.set("admin_token", jsonData.data.token);
        }
    });
}
```

---

## Test Scenarios

### Scenario 1: Complete Material Requisition Workflow

**Objective**: Test end-to-end application workflow

#### Test Steps

**Step 1: Employee logs in**
```
POST /auth/login
- Username: employee1
- Password: password123
- Expected: 200, token returned
```

**Step 2: Employee views materials**
```
GET /materials?pageNum=1&pageSize=10
- Expected: 200, material list
```

**Step 3: Employee submits application**
```
POST /applies
- WarehouseId: 1
- Materials: MAT001 (qty: 500), MAT003 (qty: 200)
- Expected: 200, application ID returned
```

**Step 4: Employee checks their applications**
```
GET /applies/my?status=0
- Expected: 200, pending application visible
```

**Step 5: Manager logs in**
```
POST /auth/login
- Username: warehouse_mgr
- Password: password123
- Expected: 200, manager token
```

**Step 6: Manager checks pending approvals**
```
GET /applies/pending?pageNum=1
- Expected: 200, pending application visible
```

**Step 7: Manager views application details**
```
GET /applies/{application_id}
- Expected: 200, full application details with materials
```

**Step 8: Manager approves application**
```
POST /applies/approve
- ApplyId: {application_id}
- ApprovalResult: 1 (approve)
- ApprovalRemark: "Approved"
- Expected: 200, success
```

**Step 9: System auto-creates outbound**
```
GET /outbounds?status=0
- Expected: 200, new outbound visible with pending status
```

**Step 10: Manager confirms pickup**
```
POST /outbounds/{outbound_id}/confirm
- Expected: 200, inventory deducted
```

**Step 11: Check inventory updated**
```
GET /inventories?materialId=1
- Expected: Quantity reduced by 500
```

**Step 12: Employee receives notification**
```
GET /messages?isRead=0
- Expected: 200, approval notification visible
```

**Step 13: Employee marks notification read**
```
PUT /messages/{message_id}/read
- Expected: 200, message marked as read
```

---

### Scenario 2: Inventory Management & Inbound

**Objective**: Test inbound operations and inventory updates

#### Test Steps

**Step 1: Check current inventory**
```
GET /inventories?warehouseId=1
- Expected: 200, current stock levels
```

**Step 2: Manager creates inbound order**
```
POST /inbounds
- WarehouseId: 1
- InboundType: 1 (purchase)
- Materials: MAT001 (qty: 1000), MAT005 (qty: 50)
- Expected: 200, inbound ID
```

**Step 3: Verify inventory increased**
```
GET /inventories?materialId=1
- Expected: Quantity increased by 1000
```

**Step 4: Check inventory logs**
```
GET /inventories/logs?changeType=1&materialId=1
- Expected: 200, inbound log entry visible
```

**Step 5: Check low-stock alerts**
```
GET /inventories/low-stock-alerts?warehouseId=1
- Expected: 200, list of materials below minimum
```

**Step 6: Create direct outbound**
```
POST /outbounds/direct
- WarehouseId: 1
- ReceiverId: 5
- Materials: MAT001 (qty: 100)
- Expected: 200, inventory immediately deducted
```

**Step 7: Verify inventory deducted**
```
GET /inventories?materialId=1
- Expected: Quantity reduced by 100
```

---

### Scenario 3: Dashboard & Statistics

**Objective**: Test reporting and statistics endpoints

#### Test Steps

**Step 1: Get dashboard overview**
```
GET /statistics/dashboard
- Expected: 200, KPI data
- Verify: todayInbound, todayOutbound, pendingApplications present
```

**Step 2: Get inbound statistics**
```
GET /statistics/inbound?startDate=2025-11-01&endDate=2025-11-17
- Expected: 200, inbound trend data
- Verify: totalInbound, orderCount, topMaterials
```

**Step 3: Get outbound statistics**
```
GET /statistics/outbound?startDate=2025-11-01&endDate=2025-11-17
- Expected: 200, outbound trend data
- Verify: Direct vs Application breakdown
```

**Step 4: Get inventory statistics**
```
GET /statistics/inventory?warehouseId=1
- Expected: 200, inventory analysis
- Verify: totalInventoryValue, utilizationRate
```

**Step 5: Filter statistics by warehouse**
```
GET /statistics/inbound?warehouseId=1&startDate=2025-11-01
- Expected: 200, warehouse-specific data
```

---

### Scenario 4: User Management

**Objective**: Test CRUD operations on users

#### Test Steps

**Step 1: Admin logs in**
```
POST /auth/login
- Username: admin
- Password: password123
- Expected: 200, admin token
```

**Step 2: List all users**
```
GET /users?pageNum=1&pageSize=20
- Expected: 200, user list
```

**Step 3: Create new user**
```
POST /users
- Username: new_test_user
- Password: TestPass123!
- RealName: Test User
- DeptId: 1
- RoleId: 4
- Expected: 200, user ID returned
```

**Step 4: Get new user details**
```
GET /users/{new_user_id}
- Expected: 200, new user data
```

**Step 5: Update user**
```
PUT /users/{new_user_id}
- RealName: Updated Test User
- Phone: 13800000099
- Expected: 200
```

**Step 6: Disable user**
```
PUT /users/{new_user_id}/status?status=1
- Expected: 200
```

**Step 7: Verify user disabled**
```
GET /users/{new_user_id}
- Expected: 200, status=1
```

**Step 8: Reset user password**
```
PUT /users/{new_user_id}/reset-password?newPassword=NewPass123!
- Expected: 200
```

**Step 9: Login with new password (should work)**
```
POST /auth/login
- Username: new_test_user
- Password: NewPass123!
- Expected: 200, login successful
```

**Step 10: Delete user**
```
DELETE /users/{new_user_id}
- Expected: 200
```

---

### Scenario 5: Material & Department Management

**Objective**: Test master data operations

#### Test Steps

**Step 1: Get all departments (tree)**
```
GET /depts/tree
- Expected: 200, hierarchical department structure
```

**Step 2: Get all materials**
```
GET /materials?pageNum=1
- Expected: 200, material list
```

**Step 3: Get material categories**
```
GET /materials/categories
- Expected: 200, unique categories list
```

**Step 4: Create material**
```
POST /materials
- Code: TEST-MAT-001
- Name: Test Material
- Category: Testing
- Unit: piece
- UnitPrice: 100.00
- Expected: 200, material ID
```

**Step 5: Get new material**
```
GET /materials/{material_id}
- Expected: 200, material details
```

**Step 6: Update material**
```
PUT /materials/{material_id}
- Name: Updated Test Material
- UnitPrice: 150.00
- Expected: 200
```

**Step 7: Disable material**
```
PATCH /materials/{material_id}/status?status=1
- Expected: 200
```

**Step 8: Delete material**
```
DELETE /materials/{material_id}
- Expected: 200
```

---

## Curl Testing Examples

### Complete Authentication Flow

```bash
#!/bin/bash

# Configuration
BASE_URL="http://localhost:48888/api"
ADMIN_USER="admin"
ADMIN_PASS="password123"
WAREHOUSE_USER="warehouse_mgr"
WAREHOUSE_PASS="password123"

echo "=== Step 1: Admin Login ==="
ADMIN_LOGIN=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${ADMIN_USER}\",
    \"password\": \"${ADMIN_PASS}\",
    \"loginType\": \"PASSWORD\"
  }")

ADMIN_TOKEN=$(echo $ADMIN_LOGIN | jq -r '.data.token')
echo "Admin Token: ${ADMIN_TOKEN:0:20}..."

echo -e "\n=== Step 2: Get Admin User Info ==="
curl -s -X GET ${BASE_URL}/auth/user-info \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq '.data'

echo -e "\n=== Step 3: Warehouse Manager Login ==="
MGR_LOGIN=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"${WAREHOUSE_USER}\",
    \"password\": \"${WAREHOUSE_PASS}\",
    \"loginType\": \"PASSWORD\"
  }")

MGR_TOKEN=$(echo $MGR_LOGIN | jq -r '.data.token')
echo "Manager Token: ${MGR_TOKEN:0:20}..."

echo -e "\n=== Step 4: Refresh Token ==="
curl -s -X POST ${BASE_URL}/auth/refresh-token \
  -H "Authorization: Bearer ${MGR_TOKEN}" | jq '.data'

echo -e "\n=== Step 5: Logout ==="
curl -s -X POST ${BASE_URL}/auth/logout \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq '.message'
```

### Material Requisition Test

```bash
#!/bin/bash

BASE_URL="http://localhost:48888/api"
TOKEN="$1"  # Pass token as argument

echo "=== Create Material Application ==="
APP=$(curl -s -X POST ${BASE_URL}/applies \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "applyTime": "'$(date -u +%Y-%m-%dT%H:%M:%S)'",
    "applyReason": "Network maintenance",
    "details": [
      {
        "materialId": 1,
        "quantity": 500,
        "remark": "For backbone"
      },
      {
        "materialId": 3,
        "quantity": 200,
        "remark": "For termination"
      }
    ]
  }')

APP_ID=$(echo $APP | jq -r '.data')
echo "Application ID: $APP_ID"

echo -e "\n=== Get Application Details ==="
curl -s -X GET ${BASE_URL}/applies/${APP_ID} \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data'

echo -e "\n=== List My Applications ==="
curl -s -X GET ${BASE_URL}/applies/my?pageNum=1 \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.records'
```

### Inventory Check Test

```bash
#!/bin/bash

BASE_URL="http://localhost:48888/api"
TOKEN="$1"
WAREHOUSE_ID="${2:-1}"

echo "=== Check Inventory Levels ==="
curl -s -X GET "${BASE_URL}/inventories?pageNum=1&warehouseId=${WAREHOUSE_ID}" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.records[] | {materialName, currentQty, minStock}'

echo -e "\n=== Check Low Stock Alerts ==="
curl -s -X GET "${BASE_URL}/inventories/low-stock-alerts?warehouseId=${WAREHOUSE_ID}" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.[] | {materialName, currentQty, minStock, deficiency}'

echo -e "\n=== Inventory Transaction Logs ==="
curl -s -X GET "${BASE_URL}/inventories/logs?pageNum=1&warehouseId=${WAREHOUSE_ID}&startDate=2025-11-01&endDate=2025-11-17" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.records[] | {materialName, changeTypeText, quantity, createdAt}'
```

### Inbound Order Test

```bash
#!/bin/bash

BASE_URL="http://localhost:48888/api"
TOKEN="$1"

echo "=== Create Inbound Order ==="
INBOUND=$(curl -s -X POST ${BASE_URL}/inbounds \
  -H "Authorization: Bearer ${TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "inboundType": 1,
    "inboundTime": "'$(date -u +%Y-%m-%dT%H:%M:%S)'",
    "remark": "Quarterly supplier delivery",
    "details": [
      {
        "materialId": 1,
        "quantity": 2000,
        "unitPrice": 50.00,
        "remark": "High-quality fiber"
      },
      {
        "materialId": 5,
        "quantity": 50,
        "unitPrice": 1500.00,
        "remark": "Server equipment"
      }
    ]
  }')

INBOUND_ID=$(echo $INBOUND | jq -r '.data')
echo "Inbound ID: $INBOUND_ID"

echo -e "\n=== Get Inbound Details ==="
curl -s -X GET "${BASE_URL}/inbounds/${INBOUND_ID}" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data'

echo -e "\n=== List All Inbounds ==="
curl -s -X GET "${BASE_URL}/inbounds?pageNum=1&warehouseId=1" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data.records[] | {inboundNo, totalQuantity, status}'
```

### Statistics Report Test

```bash
#!/bin/bash

BASE_URL="http://localhost:48888/api"
TOKEN="$1"

echo "=== Dashboard Statistics ==="
curl -s -X GET ${BASE_URL}/statistics/dashboard \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | {
    todayInbound,
    todayOutbound,
    pendingApplications,
    lowStockAlerts,
    totalInventoryValue
  }'

echo -e "\n=== Inbound Statistics (Last 30 days) ==="
curl -s -X GET "${BASE_URL}/statistics/inbound?startDate=2025-10-18&endDate=2025-11-17" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | {
    totalInbound,
    orderCount,
    averageOrderSize,
    topMaterials
  }'

echo -e "\n=== Outbound Statistics (Last 30 days) ==="
curl -s -X GET "${BASE_URL}/statistics/outbound?startDate=2025-10-18&endDate=2025-11-17" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | {
    totalOutbound,
    orderCount,
    byType
  }'

echo -e "\n=== Inventory Statistics ==="
curl -s -X GET "${BASE_URL}/statistics/inventory?warehouseId=1" \
  -H "Authorization: Bearer ${TOKEN}" | jq '.data | {
    totalInventoryValue,
    totalItems,
    utilizationRate,
    alertCount
  }'
```

---

## Automated Testing

### Jest/Node.js Test Suite

```javascript
// tests/api.test.js
const axios = require('axios');

const API_BASE = 'http://localhost:48888/api';
let adminToken, managerToken, employeeToken;
let testApplicationId, testInboundId, testOutboundId;

describe('CT-Tibet-WMS API Tests', () => {

    describe('Authentication', () => {
        test('Admin login should return token', async () => {
            const response = await axios.post(`${API_BASE}/auth/login`, {
                username: 'admin',
                password: 'password123',
                loginType: 'PASSWORD'
            });

            expect(response.status).toBe(200);
            expect(response.data.code).toBe(200);
            expect(response.data.data.token).toBeDefined();
            adminToken = response.data.data.token;
        });

        test('Manager login should return token', async () => {
            const response = await axios.post(`${API_BASE}/auth/login`, {
                username: 'warehouse_mgr',
                password: 'password123'
            });

            expect(response.status).toBe(200);
            managerToken = response.data.data.token;
        });

        test('Employee login should return token', async () => {
            const response = await axios.post(`${API_BASE}/auth/login`, {
                username: 'employee1',
                password: 'password123'
            });

            expect(response.status).toBe(200);
            employeeToken = response.data.data.token;
        });

        test('Invalid credentials should return 401', async () => {
            try {
                await axios.post(`${API_BASE}/auth/login`, {
                    username: 'invalid',
                    password: 'invalid'
                });
            } catch (error) {
                expect(error.response.status).toBe(401);
            }
        });

        test('Refresh token should return new token', async () => {
            const response = await axios.post(
                `${API_BASE}/auth/refresh-token`,
                {},
                { headers: { Authorization: `Bearer ${adminToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data).toBeDefined();
        });

        test('Logout should succeed', async () => {
            const response = await axios.post(
                `${API_BASE}/auth/logout`,
                {},
                { headers: { Authorization: `Bearer ${adminToken}` } }
            );

            expect(response.status).toBe(200);
        });
    });

    describe('Material Requisition Workflow', () => {
        test('Employee should create application', async () => {
            const response = await axios.post(
                `${API_BASE}/applies`,
                {
                    warehouseId: 1,
                    applyTime: new Date().toISOString(),
                    applyReason: 'Test application',
                    details: [
                        { materialId: 1, quantity: 500, remark: 'Test' }
                    ]
                },
                { headers: { Authorization: `Bearer ${employeeToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.code).toBe(200);
            testApplicationId = response.data.data;
        });

        test('Employee should view own applications', async () => {
            const response = await axios.get(
                `${API_BASE}/applies/my?pageNum=1`,
                { headers: { Authorization: `Bearer ${employeeToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.records.length).toBeGreaterThan(0);
        });

        test('Manager should view pending applications', async () => {
            const response = await axios.get(
                `${API_BASE}/applies/pending?pageNum=1`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.records.length).toBeGreaterThan(0);
        });

        test('Manager should approve application', async () => {
            const response = await axios.post(
                `${API_BASE}/applies/approve`,
                {
                    applyId: testApplicationId,
                    approvalResult: 1,
                    approvalRemark: 'Test approval'
                },
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
        });

        test('Outbound order should be auto-created after approval', async () => {
            const response = await axios.get(
                `${API_BASE}/outbounds?status=0&pageNum=1`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.records.length).toBeGreaterThan(0);
            testOutboundId = response.data.data.records[0].id;
        });

        test('Manager should confirm outbound', async () => {
            const response = await axios.post(
                `${API_BASE}/outbounds/${testOutboundId}/confirm`,
                {},
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
        });
    });

    describe('Inventory Management', () => {
        test('Should list inventory', async () => {
            const response = await axios.get(
                `${API_BASE}/inventories?pageNum=1&warehouseId=1`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.records).toBeDefined();
        });

        test('Should create inbound order', async () => {
            const response = await axios.post(
                `${API_BASE}/inbounds`,
                {
                    warehouseId: 1,
                    inboundType: 1,
                    inboundTime: new Date().toISOString(),
                    remark: 'Test inbound',
                    details: [
                        { materialId: 1, quantity: 1000, unitPrice: 50.00 }
                    ]
                },
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            testInboundId = response.data.data;
        });

        test('Should get inbound details', async () => {
            const response = await axios.get(
                `${API_BASE}/inbounds/${testInboundId}`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.details).toBeDefined();
        });

        test('Should list low stock alerts', async () => {
            const response = await axios.get(
                `${API_BASE}/inventories/low-stock-alerts?warehouseId=1`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
        });

        test('Should view inventory logs', async () => {
            const response = await axios.get(
                `${API_BASE}/inventories/logs?pageNum=1&warehouseId=1`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data.records).toBeDefined();
        });
    });

    describe('Statistics', () => {
        test('Should get dashboard stats', async () => {
            const response = await axios.get(
                `${API_BASE}/statistics/dashboard`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data).toHaveProperty('todayInbound');
            expect(response.data.data).toHaveProperty('todayOutbound');
        });

        test('Should get inbound statistics', async () => {
            const response = await axios.get(
                `${API_BASE}/statistics/inbound?startDate=2025-11-01&endDate=2025-11-17`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data).toHaveProperty('totalInbound');
        });

        test('Should get outbound statistics', async () => {
            const response = await axios.get(
                `${API_BASE}/statistics/outbound?startDate=2025-11-01&endDate=2025-11-17`,
                { headers: { Authorization: `Bearer ${managerToken}` } }
            );

            expect(response.status).toBe(200);
            expect(response.data.data).toHaveProperty('totalOutbound');
        });
    });

    describe('Permissions', () => {
        test('Employee should not approve applications', async () => {
            try {
                await axios.post(
                    `${API_BASE}/applies/approve`,
                    { applyId: 999, approvalResult: 1 },
                    { headers: { Authorization: `Bearer ${employeeToken}` } }
                );
            } catch (error) {
                expect(error.response.status).toBe(403);
            }
        });

        test('Employee should not create inbound', async () => {
            try {
                await axios.post(
                    `${API_BASE}/inbounds`,
                    { warehouseId: 1, inboundType: 1, details: [] },
                    { headers: { Authorization: `Bearer ${employeeToken}` } }
                );
            } catch (error) {
                expect(error.response.status).toBe(403);
            }
        });
    });
});
```

### Run Tests

```bash
# Install dependencies
npm install jest axios

# Run tests
npm test

# Run with coverage
npm test -- --coverage

# Run specific test suite
npm test -- --testNamePattern="Authentication"
```

---

## Performance Testing

### Load Testing with Artillery

```yaml
# load-test.yml
config:
  target: "http://localhost:48888/api"
  phases:
    - duration: 60
      arrivalRate: 10
      name: "Ramp up"
    - duration: 60
      arrivalRate: 20
      name: "Sustained"
    - duration: 30
      arrivalRate: 5
      name: "Wind down"
  variables:
    token: ""

scenarios:
  - name: "API Load Test"
    flow:
      - post:
          url: "/auth/login"
          json:
            username: "warehouse_mgr"
            password: "password123"
            loginType: "PASSWORD"
          capture:
            json: "$.data.token"
            as: "token"

      - get:
          url: "/inventories?pageNum=1"
          headers:
            Authorization: "Bearer {{ token }}"

      - get:
          url: "/statistics/dashboard"
          headers:
            Authorization: "Bearer {{ token }}"

      - get:
          url: "/applies/pending?pageNum=1"
          headers:
            Authorization: "Bearer {{ token }}"

      - get:
          url: "/messages?pageNum=1"
          headers:
            Authorization: "Bearer {{ token }}"
```

```bash
# Install Artillery
npm install -g artillery

# Run load test
artillery run load-test.yml

# Generate report
artillery report results.json
```

---

## Security Testing

### Authentication Tests

```bash
#!/bin/bash

BASE_URL="http://localhost:48888/api"

echo "=== Test 1: Missing Authentication Header ==="
curl -s -w "\nStatus: %{http_code}\n" \
  -X GET ${BASE_URL}/inventories

echo -e "\n=== Test 2: Invalid Token Format ==="
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Invalid" \
  -X GET ${BASE_URL}/inventories

echo -e "\n=== Test 3: Expired Token (simulate) ==="
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer invalid.token.here" \
  -X GET ${BASE_URL}/inventories

echo -e "\n=== Test 4: Valid Token, Insufficient Permissions ==="
EMPLOYEE_TOKEN=$(curl -s -X POST ${BASE_URL}/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"employee1","password":"password123"}' | jq -r '.data.token')

curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer ${EMPLOYEE_TOKEN}" \
  -X POST ${BASE_URL}/users \
  -H "Content-Type: application/json" \
  -d '{"username":"test"}'

echo -e "\n=== Test 5: Password Strength Validation ==="
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -X POST ${BASE_URL}/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "weak",
    "realName": "Test",
    "deptId": 1,
    "roleId": 4
  }'

echo -e "\n=== Test 6: SQL Injection Attempt ==="
curl -s -w "\nStatus: %{http_code}\n" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -X GET "${BASE_URL}/materials?keyword=1' OR '1'='1"
```

---

## Test Checklist

### Pre-Testing

- [ ] Database is running and populated with test data
- [ ] Backend server is running (port 48888)
- [ ] Test user accounts are created
- [ ] Environment variables are set

### Authentication Tests

- [ ] Login returns valid JWT token
- [ ] Refresh token extends session
- [ ] Invalid credentials return 401
- [ ] Expired token returns 401
- [ ] Logout invalidates token
- [ ] User info endpoint works

### CRUD Operations Tests

- [ ] Create operations return correct IDs
- [ ] Read operations return correct data
- [ ] Update operations modify data
- [ ] Delete operations mark as deleted
- [ ] List operations support pagination

### Business Logic Tests

- [ ] Application workflow completes end-to-end
- [ ] Inventory updates correctly on inbound/outbound
- [ ] Low stock alerts trigger correctly
- [ ] Manager approval creates outbound
- [ ] Notifications are sent appropriately
- [ ] Permissions are enforced

### Data Validation Tests

- [ ] Required fields are validated
- [ ] Email format is validated
- [ ] Date formats are validated
- [ ] Numeric ranges are validated
- [ ] Duplicate records are rejected

### Performance Tests

- [ ] List endpoints handle pagination correctly
- [ ] Large result sets are paginated
- [ ] Search/filter operations are responsive
- [ ] Concurrent requests are handled
- [ ] Response times are acceptable

### Security Tests

- [ ] Authentication is required for protected endpoints
- [ ] Permissions are enforced
- [ ] SQL injection is prevented
- [ ] XSS attacks are prevented
- [ ] CSRF tokens are validated
- [ ] Sensitive data is not logged

---

## Reporting Issues

### Issue Report Format

```
Title: [Brief Description]
Severity: Critical/High/Medium/Low
Endpoint: POST /api/endpoint
Steps to Reproduce:
1. ...
2. ...
3. ...

Expected Result:
- ...

Actual Result:
- ...

Test Environment:
- OS: ...
- Browser: ...
- Test Date: ...
```

---

**Testing Guide Version**: 1.0
**Last Updated**: 2025-11-17
**Status**: Production-Ready
