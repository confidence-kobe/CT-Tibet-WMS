# CT-Tibet-WMS API Reference Guide

**Version**: 1.0
**Last Updated**: 2025-11-17
**Base URL**: `http://localhost:48888/api`

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [Response Format](#response-format)
4. [Error Handling](#error-handling)
5. [API Endpoints](#api-endpoints)
   - [Authentication](#authentication-endpoints)
   - [User Management](#user-management)
   - [Department Management](#department-management)
   - [Role Management](#role-management)
   - [Warehouse Management](#warehouse-management)
   - [Material Management](#material-management)
   - [Inventory Management](#inventory-management)
   - [Inbound Management](#inbound-management)
   - [Outbound Management](#outbound-management)
   - [Application & Approval](#application--approval-management)
   - [Statistics & Reports](#statistics--reports)
   - [Message Center](#message-center)

---

## Overview

CT-Tibet-WMS is a comprehensive warehouse management system designed for telecommunications operations in Tibet. The API provides RESTful endpoints for managing:

- User authentication and authorization
- Personnel and role management
- Department and warehouse organization
- Material/inventory master data
- Stock inbound and outbound operations
- Material requisition applications with approval workflows
- Real-time inventory tracking
- Business analytics and reporting
- In-app messaging system

### Key Features

- JWT-based token authentication
- Role-based access control (RBAC)
- Pagination support for list endpoints
- Comprehensive filtering and search capabilities
- Transaction-safe approval workflows
- Real-time inventory management
- Statistical reporting and dashboards

---

## Authentication

### JWT Token

All API endpoints (except `/login`) require authentication via JWT Bearer token.

**Token Type**: Bearer
**Header**: `Authorization: Bearer <token>`
**Expiration**: 2 hours
**Refresh Token Expiration**: 7 days

### Authentication Flow

```
1. User calls POST /api/auth/login with username and password
2. Server returns JWT token and user information
3. Client includes token in Authorization header for subsequent requests
4. Client can refresh token using POST /api/auth/refresh-token before expiration
5. User calls POST /api/auth/logout to invalidate token
```

### Available Login Types

| Login Type | Description |
|-----------|-------------|
| `PASSWORD` | Standard username/password login |
| `PHONE` | Mobile phone number login |
| `WECHAT` | WeChat Mini Program login |
| `ENTERPRISE_WECHAT` | Enterprise WeChat login |

---

## Response Format

### Success Response

All successful API responses follow this format:

```json
{
  "code": 200,
  "message": "Success message or operation description",
  "data": {
    // Response data structure varies by endpoint
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

### Error Response

```json
{
  "code": 400,
  "message": "Error description",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

### Response Codes

| Code | HTTP Status | Meaning |
|------|------------|---------|
| 200 | 200 OK | Request successful |
| 400 | 400 Bad Request | Invalid request parameters |
| 401 | 401 Unauthorized | Missing or invalid authentication |
| 403 | 403 Forbidden | Insufficient permissions |
| 404 | 404 Not Found | Resource not found |
| 500 | 500 Internal Server Error | Server error |
| 1000 | 400 Bad Request | Business logic error |

### Pagination Response

List endpoints return paginated results:

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      // Array of items
    ],
    "total": 100,
    "pages": 5,
    "pageNum": 1,
    "pageSize": 20,
    "current": 1,
    "size": 20,
    "hasNextPage": true,
    "hasPreviousPage": false
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

---

## Error Handling

### Common Error Scenarios

| Scenario | HTTP Code | Suggested Action |
|----------|-----------|------------------|
| Invalid credentials | 401 | Verify username/password and try again |
| Token expired | 401 | Call refresh-token endpoint or re-login |
| Insufficient permissions | 403 | Contact administrator for access |
| Resource not found | 404 | Verify resource ID exists |
| Validation error | 400 | Check request parameters |
| Inventory insufficient | 1000 | Check current inventory levels |
| Approval conflict | 1000 | Verify application status |
| Duplicate record | 1000 | Check for existing records |

### Error Response Example

```json
{
  "code": 401,
  "message": "Token expired, please refresh or re-login",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

---

## API Endpoints

### Authentication Endpoints

#### 1. User Login

**POST** `/auth/login`

Authenticate user and obtain JWT token.

**Permissions**: Public (no authentication required)

**Request Body**:
```json
{
  "username": "user@example.com",
  "password": "password123",
  "loginType": "PASSWORD"
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| username | String | Yes | Username or email |
| password | String | Yes | User password |
| loginType | String | No | Login method (default: PASSWORD) |

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
      "username": "user@example.com",
      "realName": "John Doe",
      "phone": "13800000001",
      "deptId": 1,
      "deptName": "Finance Department",
      "roleId": 3,
      "roleName": "Warehouse Manager",
      "roleCode": "WAREHOUSE",
      "avatar": "https://..."
    }
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Login successful
- 400: Invalid credentials or validation error
- 401: User account disabled or locked
- 500: Server error

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "warehouse_manager",
    "password": "password123",
    "loginType": "PASSWORD"
  }'
```

---

#### 2. User Logout

**POST** `/auth/logout`

Invalidate user JWT token and clear session.

**Permissions**: Authenticated users

**Request Body**: None

**Response**:
```json
{
  "code": 200,
  "message": "Logout successful",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Logout successful
- 401: Invalid or expired token

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/auth/logout \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Refresh Token

**POST** `/auth/refresh-token`

Refresh expired JWT token using old token.

**Permissions**: Authenticated users

**Headers**:
| Name | Value |
|------|-------|
| Authorization | Bearer <token> |

**Request Body**: None

**Response**:
```json
{
  "code": 200,
  "message": "Token refreshed successfully",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Token refreshed successfully
- 401: Invalid or expired token

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/auth/refresh-token \
  -H "Authorization: Bearer <old_token>"
```

---

#### 4. Get Current User Info

**GET** `/auth/user-info`

Retrieve detailed information of the currently authenticated user.

**Permissions**: Authenticated users

**Query Parameters**: None

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "warehouse_manager",
    "realName": "John Doe",
    "phone": "13800000001",
    "email": "john@example.com",
    "deptId": 1,
    "deptName": "Central Warehouse",
    "roleId": 3,
    "roleName": "Warehouse Manager",
    "roleCode": "WAREHOUSE",
    "avatar": "https://...",
    "status": 0,
    "permissions": [
      "inbound:create",
      "outbound:create",
      "apply:approve",
      "inventory:view"
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Success
- 401: Invalid or expired token

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/auth/user-info \
  -H "Authorization: Bearer <token>"
```

---

### User Management

#### 1. List Users

**GET** `/users`

List all users with pagination and filtering.

**Permissions**: ADMIN, DEPT_ADMIN

**Query Parameters**:
| Name | Type | Required | Description | Example |
|------|------|----------|-------------|---------|
| pageNum | Integer | No | Page number (default: 1) | 1 |
| pageSize | Integer | No | Items per page (default: 20) | 20 |
| deptId | Long | No | Filter by department ID | 1 |
| roleId | Long | No | Filter by role ID | 3 |
| status | Integer | No | Filter by status (0-enabled, 1-disabled) | 0 |
| keyword | String | No | Search by username or real name | john |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "username": "user1",
        "realName": "User One",
        "phone": "13800000001",
        "email": "user1@example.com",
        "deptId": 1,
        "deptName": "Warehouse A",
        "roleId": 3,
        "roleName": "Warehouse Manager",
        "status": 0,
        "createdAt": "2025-11-01T10:00:00",
        "updatedAt": "2025-11-10T15:30:00"
      }
    ],
    "total": 100,
    "pages": 5,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Success
- 400: Invalid parameters
- 401: Unauthorized
- 403: Insufficient permissions

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/users?pageNum=1&pageSize=20&deptId=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get User by ID

**GET** `/users/{id}`

Retrieve detailed information about a specific user.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | User ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "warehouse_manager",
    "realName": "John Doe",
    "phone": "13800000001",
    "email": "john@example.com",
    "deptId": 1,
    "deptName": "Central Warehouse",
    "roleId": 3,
    "roleName": "Warehouse Manager",
    "avatar": "https://...",
    "status": 0,
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-10T15:30:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Success
- 401: Unauthorized
- 403: Insufficient permissions
- 404: User not found

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/users/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Create User

**POST** `/users`

Create a new user account.

**Permissions**: ADMIN, DEPT_ADMIN

**Request Body**:
```json
{
  "username": "newuser",
  "password": "secure_password_123",
  "realName": "New User",
  "phone": "13800000002",
  "email": "newuser@example.com",
  "deptId": 1,
  "roleId": 4,
  "status": 0
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| username | String | Yes | Unique username |
| password | String | Yes | User password (min 6 chars) |
| realName | String | Yes | User's real name |
| phone | String | Yes | Phone number |
| email | String | No | Email address |
| deptId | Long | Yes | Department ID |
| roleId | Long | Yes | Role ID |
| status | Integer | No | Status (0-enabled, 1-disabled, default: 0) |

**Response**:
```json
{
  "code": 200,
  "message": "User created successfully",
  "data": 5,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: User created
- 400: Invalid parameters or duplicate username
- 401: Unauthorized
- 403: Insufficient permissions

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "secure_password_123",
    "realName": "New User",
    "phone": "13800000002",
    "email": "newuser@example.com",
    "deptId": 1,
    "roleId": 4
  }'
```

---

#### 4. Update User

**PUT** `/users/{id}`

Update user information.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | User ID |

**Request Body**:
```json
{
  "realName": "Updated Name",
  "phone": "13800000003",
  "email": "updated@example.com",
  "deptId": 2,
  "roleId": 4
}
```

**Response**:
```json
{
  "code": 200,
  "message": "User updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: User updated
- 400: Invalid parameters
- 401: Unauthorized
- 403: Insufficient permissions
- 404: User not found

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/users/1 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "Updated Name",
    "phone": "13800000003",
    "deptId": 2,
    "roleId": 4
  }'
```

---

#### 5. Delete User

**DELETE** `/users/{id}`

Soft delete a user (mark as deleted without removing from database).

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | User ID |

**Response**:
```json
{
  "code": 200,
  "message": "User deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: User deleted
- 401: Unauthorized
- 403: Insufficient permissions
- 404: User not found

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/users/5 \
  -H "Authorization: Bearer <token>"
```

---

#### 6. Update User Status

**PUT** `/users/{id}/status`

Enable or disable a user account.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | User ID |

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| status | Integer | Yes | 0-enabled, 1-disabled |

**Response**:
```json
{
  "code": 200,
  "message": "User status updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT "http://localhost:48888/api/users/1/status?status=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 7. Reset User Password

**PUT** `/users/{id}/reset-password`

Reset a user's password (admin function).

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | User ID |

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| newPassword | String | Yes | New password (min 6 chars) |

**Response**:
```json
{
  "code": 200,
  "message": "Password reset successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT "http://localhost:48888/api/users/1/reset-password?newPassword=newpass123" \
  -H "Authorization: Bearer <token>"
```

---

#### 8. Get User Profile

**GET** `/users/profile`

Get the current user's profile information.

**Permissions**: Authenticated users

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "username": "warehouse_manager",
    "realName": "John Doe",
    "phone": "13800000001",
    "email": "john@example.com",
    "avatar": "https://...",
    "deptId": 1,
    "deptName": "Central Warehouse",
    "roleId": 3,
    "roleName": "Warehouse Manager"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/users/profile \
  -H "Authorization: Bearer <token>"
```

---

#### 9. Update User Profile

**PUT** `/users/profile`

Update current user's profile information.

**Permissions**: Authenticated users

**Request Body**:
```json
{
  "realName": "John Updated",
  "phone": "13800000099",
  "email": "john.updated@example.com"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Profile updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/users/profile \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "realName": "John Updated",
    "phone": "13800000099"
  }'
```

---

#### 10. Change Password

**PUT** `/users/password`

Change current user's password.

**Permissions**: Authenticated users

**Request Body**:
```json
{
  "oldPassword": "currentpassword",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| oldPassword | String | Yes | Current password |
| newPassword | String | Yes | New password (min 6 chars) |
| confirmPassword | String | Yes | Confirm new password |

**Response**:
```json
{
  "code": 200,
  "message": "Password changed successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Password changed
- 400: Passwords don't match or old password incorrect
- 401: Unauthorized

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/users/password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "oldPassword": "currentpassword",
    "newPassword": "newpassword123",
    "confirmPassword": "newpassword123"
  }'
```

---

### Department Management

#### 1. Get Department Tree

**GET** `/depts/tree`

Retrieve department hierarchy as a tree structure.

**Permissions**: Public

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Head Office",
      "parentId": 0,
      "children": [
        {
          "id": 2,
          "name": "Finance Department",
          "parentId": 1,
          "children": []
        },
        {
          "id": 3,
          "name": "Warehouse Division",
          "parentId": 1,
          "children": [
            {
              "id": 4,
              "name": "Central Warehouse",
              "parentId": 3,
              "children": []
            }
          ]
        }
      ]
    }
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/depts/tree \
  -H "Authorization: Bearer <token>"
```

---

#### 2. List All Departments

**GET** `/depts/all`

Get all departments in flat list format.

**Permissions**: Public

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Head Office",
      "code": "HEAD_OFFICE",
      "parentId": 0,
      "description": "Organization headquarters",
      "status": 0,
      "createdAt": "2025-11-01T10:00:00"
    },
    {
      "id": 2,
      "name": "Finance Department",
      "code": "FINANCE",
      "parentId": 1,
      "description": "Financial management",
      "status": 0,
      "createdAt": "2025-11-01T10:05:00"
    }
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/depts/all \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Get Department Details

**GET** `/depts/{id}`

Get detailed information about a specific department.

**Permissions**: Public

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Department ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Central Warehouse",
    "code": "CENTRAL_WAREHOUSE",
    "parentId": 3,
    "description": "Main warehouse for central region",
    "status": 0,
    "manager": "John Doe",
    "contact": "13800000001",
    "location": "Lhasa",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-10T15:30:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/depts/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 4. Create Department

**POST** `/depts`

Create a new department.

**Permissions**: ADMIN

**Request Body**:
```json
{
  "name": "New Warehouse",
  "code": "NEW_WAREHOUSE",
  "parentId": 1,
  "description": "New warehouse location",
  "manager": "Manager Name",
  "contact": "13800000001",
  "location": "Shigatse"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Department created successfully",
  "data": 10,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/depts \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Warehouse",
    "code": "NEW_WAREHOUSE",
    "parentId": 1,
    "description": "New warehouse location"
  }'
```

---

#### 5. Update Department

**PUT** `/depts/{id}`

Update department information.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Department ID |

**Request Body**:
```json
{
  "name": "Updated Warehouse Name",
  "description": "Updated description"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Department updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/depts/10 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Warehouse Name",
    "description": "Updated description"
  }'
```

---

#### 6. Delete Department

**DELETE** `/depts/{id}`

Delete a department.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Department ID |

**Response**:
```json
{
  "code": 200,
  "message": "Department deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/depts/10 \
  -H "Authorization: Bearer <token>"
```

---

### Role Management

#### 1. List Roles

**GET** `/roles`

List all roles with pagination.

**Permissions**: ADMIN

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| keyword | String | No | Search by role name |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "System Administrator",
        "code": "ADMIN",
        "description": "Full system access",
        "status": 0,
        "userCount": 1,
        "createdAt": "2025-11-01T10:00:00"
      },
      {
        "id": 2,
        "name": "Department Administrator",
        "code": "DEPT_ADMIN",
        "description": "Department-level access",
        "status": 0,
        "userCount": 7,
        "createdAt": "2025-11-01T10:05:00"
      }
    ],
    "total": 4,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/roles?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get All Roles

**GET** `/roles/all`

Get all enabled roles without pagination.

**Permissions**: Public

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "System Administrator",
      "code": "ADMIN",
      "description": "Full system access",
      "status": 0
    },
    {
      "id": 2,
      "name": "Department Administrator",
      "code": "DEPT_ADMIN",
      "description": "Department-level access",
      "status": 0
    },
    {
      "id": 3,
      "name": "Warehouse Manager",
      "code": "WAREHOUSE",
      "description": "Warehouse operations",
      "status": 0
    },
    {
      "id": 4,
      "name": "Regular Employee",
      "code": "USER",
      "description": "Standard employee access",
      "status": 0
    }
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/roles/all \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Get Role Details

**GET** `/roles/{id}`

Get detailed information about a specific role.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Role ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 3,
    "name": "Warehouse Manager",
    "code": "WAREHOUSE",
    "description": "Manages warehouse operations",
    "status": 0,
    "permissions": [
      "inbound:create",
      "inbound:view",
      "outbound:create",
      "outbound:confirm",
      "apply:approve",
      "inventory:view"
    ],
    "userCount": 14,
    "createdAt": "2025-11-01T10:10:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/roles/3 \
  -H "Authorization: Bearer <token>"
```

---

#### 4. Create Role

**POST** `/roles`

Create a new role.

**Permissions**: ADMIN

**Request Body**:
```json
{
  "name": "Inventory Auditor",
  "code": "AUDITOR",
  "description": "Role for inventory auditing",
  "permissions": [
    "inventory:view",
    "inventory:audit"
  ]
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| name | String | Yes | Role name |
| code | String | Yes | Unique role code |
| description | String | No | Role description |
| permissions | Array | No | Array of permission codes |

**Response**:
```json
{
  "code": 200,
  "message": "Role created successfully",
  "data": 5,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/roles \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Inventory Auditor",
    "code": "AUDITOR",
    "description": "Role for inventory auditing"
  }'
```

---

#### 5. Update Role

**PUT** `/roles/{id}`

Update role information.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Role ID |

**Request Body**:
```json
{
  "name": "Senior Auditor",
  "description": "Senior inventory auditor role"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Role updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/roles/5 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Senior Auditor",
    "description": "Senior inventory auditor role"
  }'
```

---

#### 6. Delete Role

**DELETE** `/roles/{id}`

Delete a role.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Role ID |

**Response**:
```json
{
  "code": 200,
  "message": "Role deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/roles/5 \
  -H "Authorization: Bearer <token>"
```

---

### Warehouse Management

#### 1. List Warehouses

**GET** `/warehouses`

List all warehouses with optional filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| deptId | Long | No | Filter by department ID |
| status | Integer | No | Filter by status (0-enabled, 1-disabled) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Central Warehouse",
      "code": "WH001",
      "deptId": 1,
      "deptName": "Warehouse Division",
      "location": "Lhasa",
      "capacity": 10000,
      "currentStock": 5320,
      "manager": "John Doe",
      "phone": "13800000001",
      "status": 0,
      "description": "Main warehouse for central region",
      "createdAt": "2025-11-01T10:00:00"
    }
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/warehouses?deptId=1&status=0" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get Warehouse Details

**GET** `/warehouses/{id}`

Get detailed information about a specific warehouse.

**Permissions**: Public

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Warehouse ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Central Warehouse",
    "code": "WH001",
    "deptId": 1,
    "deptName": "Warehouse Division",
    "location": "Lhasa",
    "capacity": 10000,
    "currentStock": 5320,
    "manager": "John Doe",
    "phone": "13800000001",
    "email": "warehouse@example.com",
    "status": 0,
    "description": "Main warehouse for central region",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-10T15:30:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/warehouses/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Create Warehouse

**POST** `/warehouses`

Create a new warehouse.

**Permissions**: ADMIN, DEPT_ADMIN

**Request Body**:
```json
{
  "name": "Regional Warehouse",
  "code": "WH002",
  "deptId": 1,
  "location": "Shigatse",
  "capacity": 8000,
  "manager": "Jane Smith",
  "phone": "13800000002",
  "email": "regional@example.com",
  "description": "Regional warehouse"
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| name | String | Yes | Warehouse name |
| code | String | Yes | Unique warehouse code |
| deptId | Long | Yes | Department ID |
| location | String | Yes | Physical location |
| capacity | Integer | Yes | Storage capacity |
| manager | String | No | Manager name |
| phone | String | No | Contact phone |
| email | String | No | Email address |
| description | String | No | Description |

**Response**:
```json
{
  "code": 200,
  "message": "Warehouse created successfully",
  "data": 5,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/warehouses \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Regional Warehouse",
    "code": "WH002",
    "deptId": 1,
    "location": "Shigatse",
    "capacity": 8000
  }'
```

---

#### 4. Update Warehouse

**PUT** `/warehouses/{id}`

Update warehouse information.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Warehouse ID |

**Request Body**:
```json
{
  "name": "Updated Warehouse Name",
  "capacity": 9000,
  "manager": "New Manager"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Warehouse updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/warehouses/5 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Updated Warehouse Name",
    "capacity": 9000
  }'
```

---

#### 5. Delete Warehouse

**DELETE** `/warehouses/{id}`

Delete a warehouse.

**Permissions**: ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Warehouse ID |

**Response**:
```json
{
  "code": 200,
  "message": "Warehouse deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/warehouses/5 \
  -H "Authorization: Bearer <token>"
```

---

#### 6. Update Warehouse Status

**PATCH** `/warehouses/{id}/status`

Enable or disable a warehouse.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Warehouse ID |

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| status | Integer | Yes | 0-enabled, 1-disabled |

**Response**:
```json
{
  "code": 200,
  "message": "Warehouse status updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PATCH "http://localhost:48888/api/warehouses/1/status?status=1" \
  -H "Authorization: Bearer <token>"
```

---

### Material Management

#### 1. List Materials

**GET** `/materials`

List materials with pagination and filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| category | String | No | Filter by material category |
| status | Integer | No | Filter by status (0-enabled, 1-disabled) |
| keyword | String | No | Search by material name or code |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "code": "MAT001",
        "name": "Optical Fiber Cable",
        "category": "Networking",
        "unit": "meter",
        "specification": "Single-mode, 9/125",
        "unitPrice": 50.00,
        "description": "High-quality optical fiber",
        "status": 0,
        "createdAt": "2025-11-01T10:00:00"
      }
    ],
    "total": 150,
    "pages": 8,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/materials?pageNum=1&pageSize=20&category=Networking" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get Material Details

**GET** `/materials/{id}`

Get detailed information about a specific material.

**Permissions**: Public

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Material ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "code": "MAT001",
    "name": "Optical Fiber Cable",
    "category": "Networking",
    "unit": "meter",
    "specification": "Single-mode, 9/125",
    "unitPrice": 50.00,
    "minStock": 100,
    "description": "High-quality optical fiber cable for long-distance transmission",
    "status": 0,
    "supplier": "Tech Supplies Inc",
    "createdAt": "2025-11-01T10:00:00",
    "updatedAt": "2025-11-10T15:30:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/materials/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Create Material

**POST** `/materials`

Create a new material.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Request Body**:
```json
{
  "code": "MAT002",
  "name": "Router Equipment",
  "category": "Networking",
  "unit": "piece",
  "specification": "Enterprise-grade",
  "unitPrice": 1500.00,
  "minStock": 10,
  "description": "Enterprise router",
  "supplier": "Tech Supplies Inc"
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| code | String | Yes | Unique material code |
| name | String | Yes | Material name |
| category | String | Yes | Material category |
| unit | String | Yes | Unit of measurement |
| specification | String | No | Technical specification |
| unitPrice | BigDecimal | Yes | Unit price |
| minStock | Integer | No | Minimum stock level |
| description | String | No | Description |
| supplier | String | No | Supplier name |

**Response**:
```json
{
  "code": 200,
  "message": "Material created successfully",
  "data": 151,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/materials \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "MAT002",
    "name": "Router Equipment",
    "category": "Networking",
    "unit": "piece",
    "unitPrice": 1500.00
  }'
```

---

#### 4. Update Material

**PUT** `/materials/{id}`

Update material information.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Material ID |

**Request Body**:
```json
{
  "name": "Advanced Router Equipment",
  "unitPrice": 1600.00,
  "minStock": 15
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Material updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/materials/151 \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Advanced Router Equipment",
    "unitPrice": 1600.00
  }'
```

---

#### 5. Delete Material

**DELETE** `/materials/{id}`

Delete a material (soft delete if inventory exists).

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Material ID |

**Response**:
```json
{
  "code": 200,
  "message": "Material deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Material deleted
- 400: Cannot delete material with existing inventory
- 401: Unauthorized
- 403: Insufficient permissions
- 404: Material not found

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/materials/151 \
  -H "Authorization: Bearer <token>"
```

---

#### 6. Update Material Status

**PATCH** `/materials/{id}/status`

Enable or disable a material.

**Permissions**: ADMIN, DEPT_ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Material ID |

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| status | Integer | Yes | 0-enabled, 1-disabled |

**Response**:
```json
{
  "code": 200,
  "message": "Material status updated successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PATCH "http://localhost:48888/api/materials/1/status?status=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 7. Get Material Categories

**GET** `/materials/categories`

Get all distinct material categories.

**Permissions**: Public

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    "Networking",
    "Power Supply",
    "Cabling",
    "Server Equipment",
    "Safety Equipment",
    "Tools",
    "Office Supplies",
    "Other"
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/materials/categories \
  -H "Authorization: Bearer <token>"
```

---

### Inventory Management

#### 1. List Inventory

**GET** `/inventories`

List current inventory levels with pagination and filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| warehouseId | Long | No | Filter by warehouse |
| materialId | Long | No | Filter by material |
| keyword | String | No | Search by material name or code |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "materialId": 1,
        "materialCode": "MAT001",
        "materialName": "Optical Fiber Cable",
        "unit": "meter",
        "currentQty": 5000,
        "minStock": 100,
        "maxStock": 10000,
        "lastUpdateTime": "2025-11-16T14:30:00",
        "status": 0
      }
    ],
    "total": 450,
    "pages": 23,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/inventories?pageNum=1&pageSize=20&warehouseId=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. List Inventory Logs

**GET** `/inventories/logs`

Get inventory transaction history with filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| warehouseId | Long | No | Filter by warehouse |
| materialId | Long | No | Filter by material |
| changeType | Integer | No | Filter by change type (1-inbound, 2-outbound, 3-adjustment) |
| startDate | String | No | Start date (yyyy-MM-dd) |
| endDate | String | No | End date (yyyy-MM-dd) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1001,
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "materialId": 1,
        "materialCode": "MAT001",
        "materialName": "Optical Fiber Cable",
        "changeType": 1,
        "changeTypeText": "Inbound",
        "quantity": 500,
        "remark": "Received from supplier",
        "createdBy": "John Doe",
        "createdAt": "2025-11-16T14:30:00"
      }
    ],
    "total": 2345,
    "pages": 118,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Change Types**:
| Code | Meaning |
|------|---------|
| 1 | Inbound (Stock received) |
| 2 | Outbound (Stock issued) |
| 3 | Adjustment (Manual adjustment) |
| 4 | Return (Stock returned) |

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/inventories/logs?pageNum=1&warehouseId=1&changeType=1&startDate=2025-11-01&endDate=2025-11-17" \
  -H "Authorization: Bearer <token>"
```

---

#### 3. List Low Stock Alerts

**GET** `/inventories/low-stock-alerts`

Get materials with inventory below minimum stock level.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| warehouseId | Long | No | Filter by warehouse |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "id": 5,
      "warehouseId": 1,
      "warehouseName": "Central Warehouse",
      "materialId": 5,
      "materialCode": "MAT005",
      "materialName": "Server Equipment",
      "unit": "piece",
      "currentQty": 8,
      "minStock": 20,
      "maxStock": 100,
      "deficiency": 12,
      "status": 0
    },
    {
      "id": 12,
      "warehouseId": 1,
      "warehouseName": "Central Warehouse",
      "materialId": 12,
      "materialCode": "MAT012",
      "materialName": "Power Supply Unit",
      "unit": "piece",
      "currentQty": 15,
      "minStock": 30,
      "maxStock": 200,
      "deficiency": 15,
      "status": 0
    }
  ],
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/inventories/low-stock-alerts?warehouseId=1" \
  -H "Authorization: Bearer <token>"
```

---

### Inbound Management

#### 1. List Inbound Orders

**GET** `/inbounds`

List inbound orders with pagination and filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| warehouseId | Long | No | Filter by warehouse |
| inboundType | Integer | No | Filter by type (1-purchase, 2-return, 3-adjustment) |
| startDate | String | No | Start date (yyyy-MM-dd) |
| endDate | String | No | End date (yyyy-MM-dd) |
| operatorId | Long | No | Filter by operator |
| keyword | String | No | Search by inbound number |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "inboundNo": "IB202511001",
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "inboundType": 1,
        "inboundTypeText": "Purchase",
        "status": 1,
        "statusText": "Completed",
        "operatorId": 1,
        "operatorName": "John Doe",
        "totalQuantity": 2500,
        "remark": "Supplier delivery",
        "inboundTime": "2025-11-16T10:30:00",
        "createdAt": "2025-11-16T10:30:00"
      }
    ],
    "total": 456,
    "pages": 23,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Inbound Types**:
| Code | Type |
|------|------|
| 1 | Purchase (新采购) |
| 2 | Return (退货) |
| 3 | Adjustment (库存调整) |

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/inbounds?pageNum=1&warehouseId=1&startDate=2025-11-01" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get Inbound Details

**GET** `/inbounds/{id}`

Get detailed information about a specific inbound order including line items.

**Permissions**: Public

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Inbound order ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "inboundNo": "IB202511001",
    "warehouseId": 1,
    "warehouseName": "Central Warehouse",
    "inboundType": 1,
    "status": 1,
    "operatorId": 1,
    "operatorName": "John Doe",
    "totalQuantity": 2500,
    "remark": "Supplier delivery",
    "inboundTime": "2025-11-16T10:30:00",
    "createdAt": "2025-11-16T10:30:00",
    "details": [
      {
        "id": 101,
        "materialId": 1,
        "materialCode": "MAT001",
        "materialName": "Optical Fiber Cable",
        "unit": "meter",
        "quantity": 1500,
        "unitPrice": 50.00,
        "totalPrice": 75000.00,
        "remark": "High-quality fiber"
      },
      {
        "id": 102,
        "materialId": 2,
        "materialCode": "MAT002",
        "materialName": "Router Equipment",
        "unit": "piece",
        "quantity": 10,
        "unitPrice": 1500.00,
        "totalPrice": 15000.00,
        "remark": null
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/inbounds/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Create Inbound Order

**POST** `/inbounds`

Create a new inbound order and automatically update inventory.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Request Body**:
```json
{
  "warehouseId": 1,
  "inboundType": 1,
  "inboundTime": "2025-11-17T14:30:00",
  "remark": "Supplier delivery - Q4 purchase order",
  "details": [
    {
      "materialId": 1,
      "quantity": 2000,
      "unitPrice": 50.00,
      "remark": "High-quality fiber - Batch #1"
    },
    {
      "materialId": 2,
      "quantity": 15,
      "unitPrice": 1500.00,
      "remark": null
    }
  ]
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| warehouseId | Long | Yes | Destination warehouse |
| inboundType | Integer | Yes | Inbound type (1-purchase, 2-return, 3-adjustment) |
| inboundTime | DateTime | Yes | Inbound timestamp |
| remark | String | No | Order remarks |
| details | Array | Yes | Array of inbound line items |

**Detail Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| materialId | Long | Yes | Material ID |
| quantity | BigDecimal | Yes | Quantity to receive |
| unitPrice | BigDecimal | No | Unit price for this inbound |
| remark | String | No | Line item remarks |

**Response**:
```json
{
  "code": 200,
  "message": "Inbound order created successfully",
  "data": 101,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Inbound order created
- 400: Invalid parameters or inventory error
- 401: Unauthorized
- 403: Insufficient permissions
- 1000: Business logic error (e.g., warehouse not found)

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/inbounds \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "inboundType": 1,
    "inboundTime": "2025-11-17T14:30:00",
    "remark": "Supplier delivery",
    "details": [
      {
        "materialId": 1,
        "quantity": 2000,
        "unitPrice": 50.00
      }
    ]
  }'
```

---

### Outbound Management

#### 1. List Outbound Orders

**GET** `/outbounds`

List outbound orders with pagination and filtering.

**Permissions**: Public

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| warehouseId | Long | No | Filter by warehouse |
| outboundType | Integer | No | Filter by type (1-direct, 2-application) |
| status | Integer | No | Filter by status (0-pending, 1-completed, 2-cancelled) |
| startDate | String | No | Start date (yyyy-MM-dd) |
| endDate | String | No | End date (yyyy-MM-dd) |
| operatorId | Long | No | Filter by operator |
| receiverId | Long | No | Filter by receiver |
| keyword | String | No | Search by outbound number |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "outboundNo": "OB202511001",
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "outboundType": 1,
        "outboundTypeText": "Direct Outbound",
        "status": 1,
        "statusText": "Completed",
        "operatorId": 1,
        "operatorName": "Manager",
        "receiverId": 5,
        "receiverName": "Employee",
        "totalQuantity": 100,
        "outboundTime": "2025-11-16T14:30:00",
        "createdAt": "2025-11-16T14:30:00"
      }
    ],
    "total": 234,
    "pages": 12,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Outbound Types**:
| Code | Type |
|------|------|
| 1 | Direct Outbound (直接出库) |
| 2 | Application-based (申请出库) |

**Outbound Status**:
| Code | Status |
|------|--------|
| 0 | Pending Pickup (待取货) |
| 1 | Completed (已完成) |
| 2 | Cancelled (已取消) |

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/outbounds?pageNum=1&warehouseId=1&status=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get Outbound Details

**GET** `/outbounds/{id}`

Get detailed information about a specific outbound order.

**Permissions**: Public

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Outbound order ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "outboundNo": "OB202511001",
    "warehouseId": 1,
    "warehouseName": "Central Warehouse",
    "outboundType": 1,
    "status": 1,
    "operatorId": 1,
    "operatorName": "Manager",
    "receiverId": 5,
    "receiverName": "Employee",
    "totalQuantity": 100,
    "outboundTime": "2025-11-16T14:30:00",
    "createdAt": "2025-11-16T14:30:00",
    "details": [
      {
        "id": 201,
        "materialId": 1,
        "materialCode": "MAT001",
        "materialName": "Optical Fiber Cable",
        "unit": "meter",
        "quantity": 50,
        "remark": "For network expansion"
      },
      {
        "id": 202,
        "materialId": 3,
        "materialCode": "MAT003",
        "materialName": "Cabling Equipment",
        "unit": "box",
        "quantity": 50,
        "remark": null
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/outbounds/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Create Direct Outbound

**POST** `/outbounds/direct`

Create a direct outbound order (manager immediately issues inventory).

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Request Body**:
```json
{
  "warehouseId": 1,
  "receiverId": 5,
  "outboundTime": "2025-11-17T14:30:00",
  "remark": "Network expansion project",
  "details": [
    {
      "materialId": 1,
      "quantity": 50,
      "remark": "High-quality fiber for backbone"
    },
    {
      "materialId": 3,
      "quantity": 50,
      "remark": null
    }
  ]
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| warehouseId | Long | Yes | Issuing warehouse |
| receiverId | Long | Yes | Employee receiving materials |
| outboundTime | DateTime | Yes | Outbound timestamp |
| remark | String | No | Order remarks |
| details | Array | Yes | Array of outbound line items |

**Detail Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| materialId | Long | Yes | Material ID |
| quantity | BigDecimal | Yes | Quantity to issue |
| remark | String | No | Line item remarks |

**Response**:
```json
{
  "code": 200,
  "message": "Direct outbound created successfully",
  "data": 5,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Outbound order created and inventory deducted
- 400: Invalid parameters or insufficient inventory
- 401: Unauthorized
- 403: Insufficient permissions
- 1000: Business logic error

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/outbounds/direct \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "receiverId": 5,
    "outboundTime": "2025-11-17T14:30:00",
    "remark": "Network expansion",
    "details": [
      {
        "materialId": 1,
        "quantity": 50
      }
    ]
  }'
```

---

#### 4. Confirm Outbound

**POST** `/outbounds/{id}/confirm`

Confirm employee pickup and finalize outbound order (deduct inventory).

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Outbound order ID |

**Request Body**: None

**Response**:
```json
{
  "code": 200,
  "message": "Outbound confirmed successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Outbound confirmed
- 400: Invalid outbound status
- 401: Unauthorized
- 403: Insufficient permissions
- 404: Outbound order not found
- 1000: Business logic error

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/outbounds/1/confirm \
  -H "Authorization: Bearer <token>"
```

---

#### 5. Cancel Outbound

**POST** `/outbounds/{id}/cancel`

Cancel a pending outbound order.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Outbound order ID |

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| reason | String | Yes | Cancellation reason |

**Response**:
```json
{
  "code": 200,
  "message": "Outbound cancelled successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X POST "http://localhost:48888/api/outbounds/1/cancel?reason=Order%20cancelled%20per%20request" \
  -H "Authorization: Bearer <token>"
```

---

### Application & Approval Management

#### 1. List All Applications

**GET** `/applies`

List all applications with pagination and filtering.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| warehouseId | Long | No | Filter by warehouse |
| status | Integer | No | Filter by status (0-pending, 1-approved, 2-rejected, 3-completed, 4-cancelled) |
| startDate | String | No | Start date (yyyy-MM-dd) |
| endDate | String | No | End date (yyyy-MM-dd) |
| applicantId | Long | No | Filter by applicant |
| approverId | Long | No | Filter by approver |
| keyword | String | No | Search by application number |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "applyNo": "APP202511001",
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "applicantId": 5,
        "applicantName": "Employee",
        "status": 1,
        "statusText": "Approved",
        "approverId": 1,
        "approverName": "Manager",
        "applyTime": "2025-11-15T10:00:00",
        "approvalTime": "2025-11-15T11:30:00",
        "applyReason": "Materials for network project",
        "totalQuantity": 150,
        "createdAt": "2025-11-15T10:00:00"
      }
    ],
    "total": 567,
    "pages": 29,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Application Status**:
| Code | Status |
|------|--------|
| 0 | Pending Approval (待审批) |
| 1 | Approved (已批准) |
| 2 | Rejected (已拒绝) |
| 3 | Completed (已完成) |
| 4 | Cancelled (已取消) |

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/applies?pageNum=1&warehouseId=1&status=0" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. List My Applications

**GET** `/applies/my`

List current user's own applications.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| status | Integer | No | Filter by status |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 5,
        "applyNo": "APP202511005",
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "status": 0,
        "statusText": "Pending Approval",
        "applyTime": "2025-11-17T09:30:00",
        "applyReason": "Office equipment replacement",
        "totalQuantity": 30,
        "createdAt": "2025-11-17T09:30:00"
      }
    ],
    "total": 8,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/applies/my?pageNum=1&status=0" \
  -H "Authorization: Bearer <token>"
```

---

#### 3. List Pending Applications

**GET** `/applies/pending`

List applications waiting for approval (warehouse manager view).

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "applyNo": "APP202511001",
        "warehouseId": 1,
        "warehouseName": "Central Warehouse",
        "applicantId": 5,
        "applicantName": "John Smith",
        "applicantDept": "Finance",
        "status": 0,
        "applyTime": "2025-11-16T14:00:00",
        "applyReason": "Equipment maintenance",
        "totalQuantity": 45,
        "createdAt": "2025-11-16T14:00:00"
      }
    ],
    "total": 5,
    "pages": 1,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/applies/pending?pageNum=1&pageSize=20" \
  -H "Authorization: Bearer <token>"
```

---

#### 4. Get Application Details

**GET** `/applies/{id}`

Get detailed information about a specific application.

**Permissions**: Public (limited by ownership and role)

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Application ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": 1,
    "applyNo": "APP202511001",
    "warehouseId": 1,
    "warehouseName": "Central Warehouse",
    "applicantId": 5,
    "applicantName": "John Smith",
    "applicantDept": "Finance Department",
    "status": 1,
    "statusText": "Approved",
    "approverId": 1,
    "approverName": "Warehouse Manager",
    "applyTime": "2025-11-15T10:00:00",
    "approvalTime": "2025-11-15T11:30:00",
    "applyReason": "Equipment for annual maintenance",
    "approvalRemark": "Approved - sufficient stock available",
    "totalQuantity": 150,
    "details": [
      {
        "id": 101,
        "materialId": 1,
        "materialCode": "MAT001",
        "materialName": "Optical Fiber Cable",
        "unit": "meter",
        "quantity": 100,
        "remark": "For backbone network upgrade"
      },
      {
        "id": 102,
        "materialId": 3,
        "materialCode": "MAT003",
        "materialName": "Cabling Equipment",
        "unit": "box",
        "quantity": 50,
        "remark": null
      }
    ],
    "createdAt": "2025-11-15T10:00:00",
    "updatedAt": "2025-11-15T11:30:00"
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/applies/1 \
  -H "Authorization: Bearer <token>"
```

---

#### 5. Create Application

**POST** `/applies`

Submit a new material requisition application.

**Permissions**: Authenticated users

**Request Body**:
```json
{
  "warehouseId": 1,
  "applyTime": "2025-11-17T14:30:00",
  "applyReason": "Network expansion project - Q4 phase",
  "details": [
    {
      "materialId": 1,
      "quantity": 200,
      "remark": "High-quality fiber for backbone"
    },
    {
      "materialId": 3,
      "quantity": 100,
      "remark": "Cabling equipment for termination"
    }
  ]
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| warehouseId | Long | Yes | Target warehouse |
| applyTime | DateTime | Yes | Application timestamp |
| applyReason | String | No | Reason for application |
| details | Array | Yes | Array of material requests |

**Detail Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| materialId | Long | Yes | Material ID |
| quantity | BigDecimal | Yes | Requested quantity |
| remark | String | No | Item remarks/usage notes |

**Response**:
```json
{
  "code": 200,
  "message": "Application submitted successfully",
  "data": 15,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Application created successfully
- 400: Invalid parameters or validation error
- 401: Unauthorized
- 1000: Business logic error

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/applies \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseId": 1,
    "applyTime": "2025-11-17T14:30:00",
    "applyReason": "Network expansion project",
    "details": [
      {
        "materialId": 1,
        "quantity": 200
      }
    ]
  }'
```

---

#### 6. Approve Application

**POST** `/applies/approve`

Approve or reject a pending application.

**Permissions**: ADMIN, DEPT_ADMIN, WAREHOUSE

**Request Body**:
```json
{
  "applyId": 1,
  "approvalResult": 1,
  "approvalRemark": "Approved - sufficient inventory available"
}
```

**Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| applyId | Long | Yes | Application ID |
| approvalResult | Integer | Yes | 1-approve, 2-reject |
| approvalRemark | String | No | Approval comments |

**Response**:
```json
{
  "code": 200,
  "message": "Application approved successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Important Notes**:
- When approved, an outbound order is automatically created (status: pending pickup)
- The inventory is reserved but not deducted until employee picks up
- Employee has 7 days to pick up; order auto-cancels if not picked up
- Rejection sends notification to applicant

**Status Codes**:
- 200: Approval processed successfully
- 400: Invalid application status or parameters
- 401: Unauthorized
- 403: Insufficient permissions
- 404: Application not found
- 1000: Business logic error (e.g., insufficient inventory)

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/applies/approve \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "applyId": 1,
    "approvalResult": 1,
    "approvalRemark": "Approved - sufficient inventory"
  }'
```

---

#### 7. Cancel Application

**POST** `/applies/{id}/cancel`

Cancel a pending or approved application.

**Permissions**: Applicant (own application) or ADMIN

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Application ID |

**Request Body**: None

**Response**:
```json
{
  "code": 200,
  "message": "Application cancelled successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Status Codes**:
- 200: Application cancelled
- 400: Cannot cancel approved/completed applications
- 401: Unauthorized
- 403: Insufficient permissions
- 404: Application not found

**Example curl**:
```bash
curl -X POST http://localhost:48888/api/applies/5/cancel \
  -H "Authorization: Bearer <token>"
```

---

### Statistics & Reports

#### 1. Get Dashboard Statistics

**GET** `/statistics/dashboard`

Get key performance indicators for the dashboard.

**Permissions**: Authenticated users

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "todayInbound": 1250,
    "todayOutbound": 850,
    "pendingApplications": 12,
    "lowStockAlerts": 5,
    "totalInventoryValue": 2500000.00,
    "totalMaterials": 150,
    "totalWarehouses": 14,
    "totalUsers": 420,
    "outboundTrend": [
      {
        "date": "2025-11-10",
        "quantity": 2300
      },
      {
        "date": "2025-11-11",
        "quantity": 2450
      }
    ],
    "topMaterials": [
      {
        "materialName": "Optical Fiber Cable",
        "outboundQty": 5000,
        "percentage": 25.5
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/statistics/dashboard \
  -H "Authorization: Bearer <token>"
```

---

#### 2. Get Inbound Statistics

**GET** `/statistics/inbound`

Get inbound order statistics with trends and analysis.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | String | No | Start date (yyyy-MM-dd, default: 30 days ago) |
| endDate | String | No | End date (yyyy-MM-dd, default: today) |
| warehouseId | Long | No | Filter by warehouse |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalInbound": 50000,
    "orderCount": 156,
    "averageOrderSize": 320.51,
    "topMaterials": [
      {
        "materialName": "Optical Fiber Cable",
        "inboundQty": 15000,
        "percentage": 30.0
      }
    ],
    "byType": [
      {
        "type": "Purchase",
        "quantity": 45000,
        "percentage": 90.0
      },
      {
        "type": "Return",
        "quantity": 5000,
        "percentage": 10.0
      }
    ],
    "trend": [
      {
        "date": "2025-11-01",
        "quantity": 1200
      },
      {
        "date": "2025-11-02",
        "quantity": 1450
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/statistics/inbound?startDate=2025-11-01&endDate=2025-11-17&warehouseId=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Get Outbound Statistics

**GET** `/statistics/outbound`

Get outbound order statistics with trends and analysis.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| startDate | String | No | Start date (yyyy-MM-dd, default: 30 days ago) |
| endDate | String | No | End date (yyyy-MM-dd, default: today) |
| warehouseId | Long | No | Filter by warehouse |
| outboundType | Integer | No | Filter by type (1-direct, 2-application) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalOutbound": 32500,
    "orderCount": 234,
    "averageOrderSize": 138.89,
    "topMaterials": [
      {
        "materialName": "Server Equipment",
        "outboundQty": 8000,
        "percentage": 24.6
      }
    ],
    "byType": [
      {
        "type": "Direct Outbound",
        "quantity": 18500,
        "percentage": 56.9
      },
      {
        "type": "Application Outbound",
        "quantity": 14000,
        "percentage": 43.1
      }
    ],
    "trend": [
      {
        "date": "2025-11-01",
        "quantity": 950
      },
      {
        "date": "2025-11-02",
        "quantity": 1080
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/statistics/outbound?startDate=2025-11-01&endDate=2025-11-17" \
  -H "Authorization: Bearer <token>"
```

---

#### 4. Get Inventory Statistics

**GET** `/statistics/inventory`

Get inventory-level statistics and analysis.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| warehouseId | Long | No | Filter by warehouse |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "totalInventoryValue": 2500000.00,
    "totalItems": 450,
    "totalQuantity": 125000,
    "alertCount": 5,
    "utilizationRate": 78.5,
    "topItems": [
      {
        "materialName": "Optical Fiber Cable",
        "currentQty": 15000,
        "value": 750000.00,
        "turnoverRate": 3.5
      }
    ],
    "inventoryAge": [
      {
        "period": "0-30 days",
        "percentage": 45.0
      },
      {
        "period": "31-90 days",
        "percentage": 35.0
      }
    ]
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/statistics/inventory?warehouseId=1" \
  -H "Authorization: Bearer <token>"
```

---

### Message Center

#### 1. List My Messages

**GET** `/messages`

Get current user's messages with statistics.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| type | Integer | No | Filter by message type (1-apply, 2-outbound, 3-system) |
| isRead | Integer | No | Filter by read status (0-unread, 1-read) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": 1,
        "typeText": "Application",
        "title": "Application Approved",
        "content": "Your application APP202511001 has been approved",
        "isRead": 0,
        "createdAt": "2025-11-17T10:30:00"
      }
    ],
    "total": 45,
    "pages": 3,
    "pageNum": 1,
    "pageSize": 20,
    "unreadCount": 12,
    "totalUnread": 12,
    "statistics": {
      "unread": 12,
      "read": 33
    }
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/messages?pageNum=1&isRead=0" \
  -H "Authorization: Bearer <token>"
```

---

#### 2. List My Messages (Simple)

**GET** `/messages/my`

Get current user's messages without statistics.

**Permissions**: Authenticated users

**Query Parameters**:
| Name | Type | Required | Description |
|------|------|----------|-------------|
| pageNum | Integer | No | Page number (default: 1) |
| pageSize | Integer | No | Items per page (default: 20) |
| isRead | Integer | No | Filter by read status (0-unread, 1-read) |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": 1,
        "title": "Application Approved",
        "content": "Your application has been approved",
        "isRead": 0,
        "createdAt": "2025-11-17T10:30:00"
      }
    ],
    "total": 45,
    "pages": 3,
    "pageNum": 1,
    "pageSize": 20
  },
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET "http://localhost:48888/api/messages/my?pageNum=1" \
  -H "Authorization: Bearer <token>"
```

---

#### 3. Get Unread Count

**GET** `/messages/unread-count`

Get the count of unread messages for current user.

**Permissions**: Authenticated users

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": 12,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X GET http://localhost:48888/api/messages/unread-count \
  -H "Authorization: Bearer <token>"
```

---

#### 4. Mark Message as Read

**PUT** `/messages/{id}/read`

Mark a single message as read.

**Permissions**: Authenticated users

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Message ID |

**Response**:
```json
{
  "code": 200,
  "message": "Message marked as read successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/messages/1/read \
  -H "Authorization: Bearer <token>"
```

---

#### 5. Batch Mark as Read

**PUT** `/messages/read-batch`

Mark multiple messages as read.

**Permissions**: Authenticated users

**Request Body**:
```json
[1, 2, 3, 4, 5]
```

**Response**:
```json
{
  "code": 200,
  "message": "Messages marked as read successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/messages/read-batch \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '[1, 2, 3, 4, 5]'
```

---

#### 6. Mark All Messages as Read

**PUT** `/messages/read-all`

Mark all unread messages as read.

**Permissions**: Authenticated users

**Request Body**: None

**Response**:
```json
{
  "code": 200,
  "message": "All messages marked as read successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X PUT http://localhost:48888/api/messages/read-all \
  -H "Authorization: Bearer <token>"
```

---

#### 7. Delete Message

**DELETE** `/messages/{id}`

Delete (soft delete) a message.

**Permissions**: Authenticated users

**Path Parameters**:
| Name | Type | Description |
|------|------|-------------|
| id | Long | Message ID |

**Response**:
```json
{
  "code": 200,
  "message": "Message deleted successfully",
  "data": null,
  "timestamp": "2025-11-17T14:30:45.123456"
}
```

**Example curl**:
```bash
curl -X DELETE http://localhost:48888/api/messages/1 \
  -H "Authorization: Bearer <token>"
```

---

## Common Use Cases

### Use Case 1: Employee Material Requisition Workflow

1. **Employee submits application**
   ```bash
   POST /api/applies
   - Select warehouse and materials needed
   - Specify quantities and usage purpose
   ```

2. **Warehouse manager approves**
   ```bash
   POST /api/applies/approve
   - Review application and inventory availability
   - Auto-creates outbound order with pending status
   ```

3. **Employee picks up materials**
   ```bash
   POST /api/outbounds/{id}/confirm
   - Manager confirms receipt
   - Inventory is deducted
   ```

4. **Employee receives notification**
   ```bash
   GET /api/messages
   - Check message center for approval notification
   ```

---

### Use Case 2: Warehouse Stock Management

1. **Receive new shipment**
   ```bash
   POST /api/inbounds
   - Create inbound order with shipment details
   - Inventory automatically updated
   ```

2. **Monitor inventory levels**
   ```bash
   GET /api/inventories
   GET /api/inventories/low-stock-alerts
   - Check current stock and low-stock items
   ```

3. **Create direct outbound for manager use**
   ```bash
   POST /api/outbounds/direct
   - Immediately deduct inventory
   - Track outbound transactions
   ```

---

### Use Case 3: Dashboard Reporting

1. **View key metrics**
   ```bash
   GET /api/statistics/dashboard
   - Daily inbound/outbound
   - Pending approvals
   - Inventory alerts
   ```

2. **Analyze trends**
   ```bash
   GET /api/statistics/inbound
   GET /api/statistics/outbound
   GET /api/statistics/inventory
   - Date-range filtering
   - Material and warehouse breakdowns
   ```

---

## Rate Limiting & Best Practices

### Recommended Guidelines

- **Pagination**: Always use pagination for list endpoints (avoid fetching thousands of records)
- **Filtering**: Use filters to reduce result sets
- **Batch Operations**: Group related operations when possible
- **Error Handling**: Implement retry logic for transient failures
- **Caching**: Cache read-only data (materials, departments, roles) client-side when possible

### Performance Tips

- Use specific warehouse IDs or date ranges to limit result sets
- Batch multiple message marks-as-read operations
- Cache user and role information after login
- Implement client-side pagination cache

---

## Support & Documentation

For questions or issues:

1. Check the **Quick Start Guide** (`API_QUICK_START.md`)
2. Review the **Testing Guide** (`API_TEST_GUIDE.md`)
3. Contact the development team for technical support

---

**Document Version**: 1.0
**Last Updated**: 2025-11-17
**Status**: Complete and Production-Ready
