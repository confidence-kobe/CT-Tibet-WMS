# CT-Tibet-WMS Quick Start Guide

**5-Minute Getting Started for Each Role**

**Version**: 1.0
**Last Updated**: 2025-11-18

---

## Table of Contents

1. [Overview](#overview)
2. [Regular Employee - Quick Start](#regular-employee---quick-start)
3. [Warehouse Manager - Quick Start](#warehouse-manager---quick-start)
4. [Department Administrator - Quick Start](#department-administrator---quick-start)
5. [System Administrator - Quick Start](#system-administrator---quick-start)
6. [Quick Reference Cards](#quick-reference-cards)

---

## Overview

This guide helps you get started with CT-Tibet-WMS in just 5 minutes. Choose your role below and follow the step-by-step instructions.

**First Time Users**: You'll need your username and initial password from IT department.

**System Access**:
- PC Web: `http://your-company-domain.com/wms`
- WeChat Mini Program: Search "西藏电信仓库管理"

---

## Regular Employee - Quick Start

### 5-Minute Setup

**Step 1: Login (1 minute)**

1. Open browser and navigate to WMS login page
2. Enter your **username** (usually employee ID)
3. Enter your **password**
4. Click **Login**
5. If first login, you'll be prompted to change password

**Step 2: Explore Dashboard (1 minute)**

After login, you'll see:

- **Left Menu**: Main navigation (Applications, Inventory, Messages, Profile)
- **Dashboard Cards**: Your application summary, pending tasks, recent activities
- **Notifications Bell**: Top-right corner shows unread messages

**Step 3: View Available Materials (1 minute)**

1. Click **Materials** in left menu
2. Browse materials your department uses
3. Use search box to find specific items
4. Note the **Current Stock** column (green = available)

**Step 4: Check Inventory (1 minute)**

1. Click **Inventory > Inventory Query**
2. View real-time stock levels
3. Check if materials you need are in stock
4. Remember this before submitting applications

**Step 5: Submit Your First Application (1 minute)**

1. Click **Applications > My Applications**
2. Click **+ New Application** button
3. Fill in basic info:
   - **Title**: "Test Application - Fiber Cable Request"
   - **Purpose**: "Testing the system"
   - **Expected Pickup Date**: Tomorrow's date
4. Click **+ Add Material**
5. Select any material, enter quantity: 1
6. Click **Submit**

**Done!** Your first application is submitted. Now wait for warehouse manager approval.

### Most Common Tasks

**Task 1: Submit Material Requisition**

```
Applications > My Applications > + New Application
→ Fill form → Add materials → Submit
```

**Time**: 2-3 minutes

**Task 2: Check Application Status**

```
Applications > My Applications
→ View Status column
```

**Time**: 10 seconds

**Task 3: View Notifications**

```
Click Bell Icon (top-right)
→ Read notifications
```

**Time**: 30 seconds

**Task 4: Pickup Approved Materials**

```
Receive approval notification
→ Go to warehouse
→ Show application number
→ Manager confirms pickup
→ Done!
```

**Time**: 5-10 minutes (physical visit)

---

## Warehouse Manager - Quick Start

### 5-Minute Setup

**Step 1: Login and Dashboard Overview (1 minute)**

1. Login with your credentials
2. Your dashboard shows:
   - **Pending Approvals**: Applications waiting for your review
   - **Pending Pickups**: Approved applications waiting for employee pickup
   - **Today's Operations**: Inbound/outbound summary
   - **Low Stock Alerts**: Materials below minimum level

**Step 2: Approve Your First Application (2 minutes)**

1. Click **Approval > Pending Approvals**
2. Click on any pending application to open details
3. Review:
   - Who is requesting
   - What materials and quantities
   - Purpose/reason
   - **Check inventory availability** (shown in detail page)
4. Click **Approve** button (or **Reject** if insufficient stock)
5. If rejecting, enter reason
6. Confirm

**Done!** Application approved. System creates outbound order automatically.

**Step 3: Process an Inbound Operation (2 minutes)**

1. Click **Inbound > Inbound List**
2. Click **+ Create Inbound**
3. Fill basic info:
   - **Warehouse**: Auto-selected (your warehouse)
   - **Inbound Type**: Select "Purchase"
   - **Supplier**: Enter supplier name
   - **Date**: Today (default)
4. Click **+ Add Material**
5. Select material, enter quantity
6. Click **Submit**

**Done!** Inventory increased automatically.

### Most Common Tasks

**Task 1: Approve Application (Fast Track)**

```
Approval > Pending Approvals
→ Click application
→ Quick review
→ Approve/Reject
```

**Time**: 1-2 minutes per application

**Task 2: Confirm Material Pickup**

```
Outbound > Pickup Confirmation
→ Employee shows up with application number
→ Click the order
→ Verify materials and employee
→ Confirm Pickup
```

**Time**: 2-3 minutes

**Task 3: Quick Inbound**

```
Inbound > Inbound List > + Create
→ Select warehouse & type
→ Add materials
→ Submit
```

**Time**: 3-5 minutes

**Task 4: Create Direct Outbound**

```
Outbound > Outbound List > + Create
→ Fill recipient and purpose
→ Add materials
→ Submit (inventory deducted immediately)
```

**Time**: 3-5 minutes

**Task 5: Check Inventory Status**

```
Inventory > Inventory Query
→ View current stock
→ Check Low Stock Warnings
```

**Time**: 1 minute

### Daily Routine Checklist

**Morning (9:00 AM)**:
- [ ] Check pending approvals (target: approve within 4 hours)
- [ ] Review low stock warnings
- [ ] Check pending pickups

**Midday (12:00 PM)**:
- [ ] Process any new applications
- [ ] Confirm scheduled pickups

**End of Day (5:00 PM)**:
- [ ] Final check on pending tasks
- [ ] Review today's operations summary
- [ ] Note any items for tomorrow

---

## Department Administrator - Quick Start

### 5-Minute Setup

**Step 1: Login and Access Admin Features (1 minute)**

1. Login with admin credentials
2. Your dashboard shows department-wide statistics:
   - Total users in your department
   - Total applications this month
   - Inventory value
   - Warehouse performance metrics

**Step 2: View Your Department Users (1 minute)**

1. Click **System > User Management**
2. View all users in your department
3. Check active vs inactive users
4. Identify who has Warehouse Manager role

**Step 3: Add a New User (3 minutes)**

1. Click **+ Add User** button
2. Fill in required information:
   - **Username**: employee ID (e.g., "EMP001")
   - **Real Name**: Full name
   - **Employee ID**: Official ID
   - **Department**: Your department (auto-selected)
   - **Role**: Select "Regular Employee" or "Warehouse Manager"
   - **Mobile**: Phone number
   - **Email**: Email address
   - **Initial Password**: Set temporary password (e.g., "Change@123")
3. If Warehouse Manager role:
   - Select which warehouse(s) to manage
4. Click **Submit**
5. Inform the user of their credentials

**Done!** New user created and can login.

### Most Common Tasks

**Task 1: Add New Employee**

```
System > User Management > + Add User
→ Fill form
→ Assign role
→ Submit
→ Share credentials with user
```

**Time**: 3-4 minutes

**Task 2: Assign/Change User Role**

```
System > User Management
→ Find user
→ Click Edit
→ Change Role dropdown
→ Save
```

**Time**: 1 minute

**Task 3: Reset User Password**

```
System > User Management
→ Find user
→ Click Reset Password
→ Copy temporary password
→ Share with user securely
```

**Time**: 1 minute

**Task 4: View Department Statistics**

```
Statistics > Department Dashboard
→ Review key metrics
→ Export report if needed
```

**Time**: 2-3 minutes

**Task 5: Manage Warehouse Assignments**

```
Basic Data > Warehouse Management
→ Edit warehouse
→ Assign/remove warehouse managers
→ Save
```

**Time**: 2 minutes

### Weekly Admin Checklist

**Monday**:
- [ ] Review last week's department statistics
- [ ] Check for any user access issues
- [ ] Review pending applications (if any stuck)

**Wednesday**:
- [ ] Mid-week user activity check
- [ ] Verify low stock items are being addressed

**Friday**:
- [ ] Review week's performance
- [ ] Prepare reports for management
- [ ] Plan for next week

**Monthly**:
- [ ] Full user audit (active/inactive)
- [ ] Role assignment review
- [ ] Warehouse performance review
- [ ] Security check (password changes, login activity)

---

## System Administrator - Quick Start

### 5-Minute Setup

**Step 1: Login and System Health Check (2 minutes)**

1. Login with admin credentials
2. Go to **System > System Status**
3. Check key indicators:
   - System uptime: Should be 99%+
   - Active users: Normal range
   - Database size: Monitor growth
   - Error rate: Should be <1%
4. Review any critical alerts

**Step 2: Review Global Statistics (1 minute)**

1. Go to **Statistics > Global Reports**
2. View organization-wide metrics:
   - Total users across all departments
   - Total applications this month
   - System-wide inventory value
   - Department performance comparison

**Step 3: Check Error Logs (1 minute)**

1. Go to **System > Error Logs**
2. Filter by severity: "Critical" and "Error"
3. Review recent errors
4. Identify any patterns
5. Take action on critical issues

**Step 4: Verify Backup Status (1 minute)**

1. Go to **System > Backup Management**
2. Check last backup date/time (should be within 24 hours)
3. Verify backup file size is reasonable
4. Check backup storage space available

### Most Common Tasks

**Task 1: Create New Department**

```
Basic Data > Department Management > + Add Department
→ Fill department info
→ Assign department manager
→ Submit
```

**Time**: 3 minutes

**Task 2: Create Department Administrator**

```
System > User Management > + Add User
→ Fill user info
→ Role: "Department Administrator"
→ Department: Select department
→ Submit
```

**Time**: 3 minutes

**Task 3: Add New Material (Global Catalog)**

```
Basic Data > Material Management > + Add Material
→ Fill material details
→ Set category, unit, specifications
→ Set min/max stock levels
→ Submit
```

**Time**: 4-5 minutes

**Task 4: Create New Warehouse**

```
Basic Data > Warehouse Management > + Add Warehouse
→ Fill warehouse info
→ Assign to department
→ Set location and capacity
→ Assign warehouse manager(s)
→ Submit
```

**Time**: 4 minutes

**Task 5: Bulk User Import**

```
System > User Management > Bulk Import
→ Download Excel template
→ Fill user data in Excel
→ Upload file
→ Review import results
→ Fix any errors and re-upload if needed
```

**Time**: 10-15 minutes (for 50+ users)

**Task 6: Configure System Settings**

```
System > System Settings
→ Review/modify settings
→ Test changes in non-production environment first
→ Save
→ Notify users of changes
```

**Time**: 5-10 minutes per setting

### Daily System Admin Routine

**Morning (8:30 AM - Before Users Arrive)**:
- [ ] Check system status dashboard
- [ ] Review overnight error logs
- [ ] Verify backup completed successfully
- [ ] Check disk space and resources
- [ ] Review any critical alerts

**Midday (12:00 PM)**:
- [ ] Monitor active user count (peak time)
- [ ] Check API response times
- [ ] Review any support tickets

**End of Day (5:30 PM)**:
- [ ] Review day's error logs
- [ ] Check pending system tasks
- [ ] Plan maintenance tasks for evening/weekend
- [ ] Prepare backup for overnight run

**Weekly Tasks**:
- [ ] Full system health review
- [ ] Database optimization
- [ ] User access audit
- [ ] Review and archive old data
- [ ] Update documentation

**Monthly Tasks**:
- [ ] Security audit
- [ ] Performance tuning
- [ ] Capacity planning
- [ ] Test disaster recovery
- [ ] Review and update user training materials

---

## Quick Reference Cards

### Application Status Flow

```
Draft → Pending Approval → Approved → Completed
           ↓                  ↓
        Rejected          Cancelled
```

**Status Meanings**:
- **Draft**: Saved but not submitted
- **Pending Approval**: Waiting for warehouse manager review
- **Approved**: Manager approved, ready for pickup
- **Rejected**: Manager denied request
- **Completed**: Materials picked up
- **Cancelled**: Application cancelled (timeout or user action)

### Inventory Transaction Types

| Type | Who | When | Inventory Impact |
|------|-----|------|------------------|
| **Inbound** | Warehouse Manager | Receiving stock | ↑ Increase |
| **Direct Outbound** | Warehouse Manager | Immediate issue | ↓ Decrease immediately |
| **Application Outbound** | Warehouse Manager | After pickup confirmation | ↓ Decrease on pickup |
| **Adjustment** | System Admin | Correction needed | ↑ or ↓ |

### Permission Quick Reference

| Action | Employee | Manager | Dept Admin | Sys Admin |
|--------|----------|---------|------------|-----------|
| Submit Application | ✓ | ✓ | ✓ | - |
| Approve Application | - | ✓ | ✓ | - |
| Create Inbound | - | ✓ | ✓ | - |
| Create Direct Outbound | - | ✓ | ✓ | - |
| Confirm Pickup | - | ✓ | ✓ | - |
| Manage Users | - | - | ✓ (dept) | ✓ (all) |
| Manage Warehouses | - | - | ✓ (dept) | ✓ (all) |
| Manage Departments | - | - | - | ✓ |
| System Settings | - | - | - | ✓ |

### Common Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Alt + H` | Home/Dashboard |
| `Alt + N` | Notifications |
| `Alt + S` | Search |
| `Ctrl + S` | Save Form |
| `Esc` | Close Dialog |
| `F5` | Refresh |

### Time SLAs (Service Level Agreements)

| Process | Target Time | Auto Action |
|---------|-------------|-------------|
| **Application Approval** | 24 hours | Reminder notification sent |
| **Material Pickup** | 7 days | Auto-cancel if not picked up |
| **Support Response** | 4 hours | During business hours |
| **System Backup** | Daily at 2 AM | Alert if failed |

### Contact Information

| Issue Type | Contact | Method |
|------------|---------|--------|
| **Login Problems** | Department Admin | Phone/Email |
| **Application Issues** | Warehouse Manager | Phone/WeChat |
| **Technical Issues** | IT Help Desk | Phone: [Number] / Email: it-support@company.com |
| **System Errors** | System Admin | Emergency: [Number] |
| **Training Questions** | Department Admin | Email/In-person |

### Color Coding System

**Inventory Status**:
- 🟢 **Green**: Normal stock (above minimum)
- 🟡 **Yellow**: Low stock (below minimum, above critical)
- 🔴 **Red**: Critical low (at or near zero)

**Application Status**:
- ⏳ **Orange**: Pending approval (action needed)
- ✅ **Green**: Approved (ready for pickup)
- ❌ **Red**: Rejected (review reason)
- ✓ **Blue**: Completed (all done)
- ⊗ **Gray**: Cancelled (no action needed)

---

## Cheat Sheet: Fast Actions

### For Regular Employees

**Need materials urgently?**

1. Check inventory first: `Inventory > Inventory Query`
2. Submit application: `Applications > + New`
3. Add materials, submit
4. Call warehouse manager if urgent
5. Wait for approval notification
6. Pickup within 7 days

**Check application status?**

`Applications > My Applications` → View status column

### For Warehouse Managers

**Daily workflow:**

**Morning:**
1. `Approval > Pending Approvals` → Process all
2. `Inventory > Low Stock Warnings` → Note issues

**Throughout Day:**
- Approve new applications as they arrive
- Process pickups when employees arrive
- Create inbound when materials arrive

**Evening:**
- Review pending pickups
- Check tomorrow's scheduled pickups

**Approve application in 30 seconds:**

1. Open application
2. Check inventory (shown in details)
3. Click Approve or Reject
4. Done!

### For Administrators

**Add user in 2 minutes:**

```
Users > + Add
→ Fill: Username, Name, Role, Department
→ Set password: Change@123
→ Submit
→ Share credentials
```

**Weekly health check:**

1. `System > System Status` → Green indicators?
2. `System > Error Logs` → Any critical errors?
3. `System > Backup` → Last backup successful?
4. `Statistics > Global Reports` → Review trends

---

## Mobile App (WeChat Mini Program) Quick Start

### Setup (1 minute)

1. Open WeChat
2. Search for "西藏电信仓库管理"
3. Authorize with same username/password as PC
4. Allow notifications

### Most Used Features on Mobile

**For Employees:**
- Submit applications on the go
- Check application status
- Receive pickup notifications
- View inventory

**For Managers:**
- Approve applications anywhere
- Confirm pickups on warehouse floor
- Check inventory while in warehouse
- Receive urgent notifications

**Navigation:**
- Bottom tab bar: Dashboard, Applications, Inventory, Messages, Me
- Faster than PC for quick checks and approvals

---

## Tips for Maximum Efficiency

### For All Users

1. **Enable Notifications**: Never miss important updates
2. **Use Search**: Faster than browsing menus
3. **Save Filters**: Create and save common filter combinations
4. **Bookmark Common Pages**: Use browser bookmarks for frequently used pages
5. **Learn Shortcuts**: Use keyboard shortcuts to speed up navigation

### For Managers

1. **Batch Processing**: Approve multiple applications in one session
2. **Set Aside Time**: Process approvals at specific times (e.g., 10 AM, 3 PM)
3. **Use Mobile App**: Approve on the go, confirm pickups on warehouse floor
4. **Check Daily**: Review pending tasks first thing in the morning
5. **Communicate**: Call employee if application is unclear (faster than back-and-forth rejections)

### For Administrators

1. **Automate**: Use bulk import for large user additions
2. **Schedule**: Perform maintenance during low-usage periods
3. **Monitor Trends**: Weekly review prevents issues from escalating
4. **Document Changes**: Keep log of configuration changes
5. **Train Users**: Invest time in training to reduce support burden

---

## Next Steps

**After Quick Start:**

1. Read full [User Manual](USER_MANUAL.md) for detailed instructions
2. Review [FAQ](FAQ.md) for common questions
3. Attend training session (contact your department admin)
4. Practice with test data before production use
5. Provide feedback to improve the system

**Get Help:**
- IT Help Desk: [Phone Number]
- Email: wms-support@company.com
- System Status: Check `System > System Status` page

---

**Document Version**: 1.0
**Last Updated**: 2025-11-18
**Feedback**: Send suggestions to wms-support@company.com

---

**End of Quick Start Guide**
