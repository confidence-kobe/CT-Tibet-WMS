# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**CT-Tibet-WMS** (西藏电信仓库管理系统) is a warehouse management system for a telecommunications company in Tibet. This is a **greenfield project** currently in the planning stage with only requirements documentation available.

**Status**: No source code has been implemented yet. The repository contains only requirements documentation in `docs/需求分析.md`.

## Planned Technology Stack

- **Backend**: Spring Boot with Spring Security (RBAC)
- **ORM**: MyBatis or MyBatis-Plus (to be decided)
- **Database**: MySQL
- **PC Frontend**: Vue 3
- **Mobile Frontend**: uni-app (WeChat Mini Program)
- **Message Queue**: RabbitMQ (for async notifications)
- **Build Tool**: Maven or Gradle (to be decided)

## Project Initialization Commands

When setting up the project:

```bash
# Maven project (if chosen)
mvn clean install
mvn spring-boot:run
mvn test

# Gradle project (if chosen)
gradle build
gradle bootRun
gradle test
```

## Core Business Domain

### User Roles & Permissions

| Role | Count | Key Capabilities |
|------|-------|-----------------|
| System Administrator | 1 | Global management |
| Department Administrator | 7 | Manage department warehouses & users |
| Warehouse Manager | 14 | Direct inbound/outbound (no approval), approve employee applications |
| Regular Employee | 400+ | Submit material requisition applications (requires approval) |

### Critical Business Logic

**Two-Track Outbound System**:

1. **Direct Outbound** (Warehouse Managers only)
   - No approval required
   - Immediate inventory deduction
   - For urgent needs or manager's own usage

2. **Application-Based Outbound** (Regular Employees)
   - Submit application → Warehouse manager approval → Auto-create outbound order (status: pending pickup)
   - Employee picks up materials → Manager confirms → Inventory deducted
   - Auto-cancel if not picked up within 7 days

**Inbound**: Warehouse managers can directly add inventory without approval.

## High-Level Architecture

### Planned Layered Structure

```
Controller Layer (REST APIs)
    ↓
Service Layer (Business logic + transactions)
    ↓
DAO/Mapper Layer (Database access)
```

### Core Modules

1. **User & Role Management** - RBAC with Spring Security
2. **Material Management** - Inventory item master data
3. **Warehouse Management** - Department-specific warehouses
4. **Inbound Management** - Stock receiving
5. **Outbound Management** - Two flows (direct vs application-based)
6. **Application & Approval System** - Single-level approval by warehouse managers
7. **Inventory Management** - Real-time stock tracking
8. **Statistical Reports** - Usage analytics
9. **Notification System** - In-app messages + WeChat template messages

### Database Design (Documented)

Key tables:
- `tb_user`, `tb_role`, `tb_dept` - User/role/department management
- `tb_material`, `tb_warehouse` - Master data
- `tb_inbound`, `tb_inbound_detail` - Inbound orders
- `tb_outbound`, `tb_outbound_detail` - Outbound orders (includes `source` field: 1=direct, 2=from application)
- `tb_apply`, `tb_apply_detail` - Material requisition applications
- `tb_inventory` - Stock levels
- `tb_message` - Notification system

### Critical Technical Challenges

1. **Approval Flow + Auto-creation**: When approval is granted, automatically create outbound order in same transaction
2. **Inventory Consistency**: Approval doesn't deduct inventory; only actual pickup does (requires optimistic locking)
3. **Notification System**: In-app messages + WeChat template messages (async with message queue)
4. **Timeout Handling**: Auto-cancel orders not picked up within 7 days (scheduled task)

## Permission Control Examples

```java
// Only warehouse managers and department admins can do inbound
@PreAuthorize("hasRole('WAREHOUSE') or hasRole('DEPT_ADMIN')")
public void createInbound() { ... }

// Only warehouse managers can do direct outbound
@PreAuthorize("hasRole('WAREHOUSE') or hasRole('DEPT_ADMIN')")
public void createOutboundDirect() { ... }

// Regular employees submit applications
@PreAuthorize("hasRole('USER')")
public void createApply() { ... }

// Warehouse managers approve applications
@PreAuthorize("hasRole('WAREHOUSE') or hasRole('DEPT_ADMIN')")
public void approveApply() { ... }
```

## Development Timeline (6-Week Plan)

- **Week 1**: Framework setup - Spring Boot + Vue3 + uni-app scaffolding
- **Week 2**: Basic data management - Departments, materials, warehouses, users
- **Week 3**: Inbound & direct outbound functionality
- **Week 4**: Application & approval system (core feature)
- **Week 5**: Pickup confirmation flow & statistics
- **Week 6**: Testing & deployment

## Key API Endpoints (Planned)

### Application Management
- `POST /api/apply/create` - Submit application
- `GET /api/apply/my` - My applications
- `POST /api/apply/cancel/{id}` - Cancel application
- `GET /api/apply/pending` - Pending approvals (managers)
- `POST /api/apply/approve` - Approve/reject

### Outbound Management
- `POST /api/outbound/create` - Create direct outbound
- `GET /api/outbound/pending` - Pending pickups
- `POST /api/outbound/confirm/{id}` - Confirm pickup

## Important Business Rules

1. **Inventory Deduction**: Only deduct inventory when:
   - Warehouse manager submits direct outbound (immediate)
   - Employee picks up approved materials (manager confirms)

2. **Application Status Flow**:
   - 0: Pending approval
   - 1: Approved
   - 2: Rejected
   - 3: Completed (materials picked up)
   - 4: Canceled (timeout or user cancellation)

3. **Outbound Order Status**:
   - 0: Pending pickup (waiting for employee)
   - 1: Completed (materials picked up, inventory deducted)
   - 2: Canceled (timeout or other reasons)

4. **Approval Rules**:
   - Warehouse managers can only approve applications from their own department
   - Approval must check inventory availability
   - Approval timeout: 24 hours (send reminder notification)
   - Pickup timeout: 7 days (auto-cancel)

## Notification Strategy

- **In-app messages**: Store in `tb_message` table
- **WeChat template messages**: Use WeChat Mini Program template message API
- **Async processing**: Use RabbitMQ to decouple message sending

## Important Files

- `docs/需求分析.md` - Comprehensive requirements analysis (1089 lines, in Chinese)

## Notes for Development

- **Multi-tenancy**: Each department has isolated warehouses, implement proper data filtering
- **Concurrency**: Use optimistic locking for inventory operations to prevent overselling
- **Transaction management**: Approval + outbound creation must be in same transaction
- **Testing focus**: Permission isolation, approval workflows, inventory consistency
