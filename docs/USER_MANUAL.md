# CT-Tibet-WMS User Manual

**西藏电信仓库管理系统用户手册**

**Version**: 1.0
**Last Updated**: 2025-11-18
**System Name**: CT-Tibet-WMS (China Telecom Tibet Warehouse Management System)

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Getting Started](#2-getting-started)
3. [User Roles and Permissions](#3-user-roles-and-permissions)
4. [Common Features](#4-common-features)
5. [Regular Employee Guide](#5-regular-employee-guide)
6. [Warehouse Manager Guide](#6-warehouse-manager-guide)
7. [Department Administrator Guide](#7-department-administrator-guide)
8. [System Administrator Guide](#8-system-administrator-guide)
9. [Tips and Best Practices](#9-tips-and-best-practices)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. System Overview

### 1.1 What is CT-Tibet-WMS?

CT-Tibet-WMS is a comprehensive warehouse management system designed specifically for China Telecom Tibet operations. The system helps manage:

- **Material Inventory**: Track telecommunications equipment like fiber optic cables, switches, routers, and network cables
- **Warehouse Operations**: Manage inbound and outbound operations across multiple departmental warehouses
- **Application Workflow**: Enable employees to request materials with manager approval
- **Real-time Tracking**: Monitor inventory levels and operation history
- **Statistical Reports**: Generate insights on material usage and warehouse efficiency

### 1.2 Key Benefits

**For Regular Employees**:
- Submit material requisition requests anytime, anywhere
- Track application status in real-time
- Receive instant notifications on approvals
- View material availability before requesting

**For Warehouse Managers**:
- Process inbound/outbound operations quickly
- Approve employee requests with one click
- Monitor inventory levels and warnings
- Generate departmental usage reports

**For Department/System Administrators**:
- Oversee all warehouse operations
- Manage users and permissions
- View comprehensive statistics and analytics
- Ensure compliance and accountability

### 1.3 System Access

**PC Web Portal**:
- URL: `http://your-company-domain.com/wms`
- Browser Requirements: Chrome 90+, Firefox 88+, Edge 90+
- Screen Resolution: 1366×768 or higher

**WeChat Mini Program**:
- Search for "西藏电信仓库管理" in WeChat
- Scan QR code provided by IT department
- Bind your employee account

---

## 2. Getting Started

### 2.1 First Time Login

**Step 1: Access the System**

1. Open your web browser
2. Navigate to the WMS login page
3. You will see the login screen with the company logo

[Screenshot: Login page showing username/password fields]

**Step 2: Enter Your Credentials**

1. **Username**: Your employee ID or assigned username
2. **Password**: Your initial password (provided by IT department)
3. Click the **Login** button

**Important**:
- Default initial password is usually `Change@123` or your employee ID
- You will be prompted to change your password on first login

**Step 3: Change Your Password** (First Login Only)

1. Enter your current password
2. Create a new password (minimum 8 characters, include uppercase, lowercase, and numbers)
3. Confirm your new password
4. Click **Submit**

[Screenshot: Change password dialog]

### 2.2 Understanding the Dashboard

After successful login, you'll see your personalized dashboard:

[Screenshot: Dashboard overview with labeled sections]

**Dashboard Components**:

1. **Top Navigation Bar**:
   - System logo and name
   - Quick search
   - Notification bell icon (shows unread count)
   - User profile menu

2. **Left Sidebar Menu**:
   - Organized by functional modules
   - Icons for easy recognition
   - Expandable sub-menus

3. **Main Content Area**:
   - Dashboard widgets (varies by role)
   - Data summaries and statistics
   - Quick action buttons
   - Recent activities

4. **Footer**:
   - System version
   - Help links
   - Contact information

### 2.3 Navigation Basics

**Using the Menu**:

1. Click menu items in the left sidebar to access different modules
2. Hover over menu items to see tooltips
3. Click the hamburger icon (☰) to collapse/expand the sidebar

**Breadcrumb Navigation**:

- Located at the top of the content area
- Shows your current location in the system
- Click any breadcrumb item to navigate back

Example: `Dashboard > Applications > My Applications`

**Quick Actions**:

Most list pages include quick action buttons:
- **+ New**: Create new record
- **Search**: Filter and find records
- **Export**: Download data to Excel
- **Refresh**: Reload current data

---

## 3. User Roles and Permissions

### 3.1 Role Overview

| Role | User Count | Primary Responsibilities | Access Level |
|------|-----------|-------------------------|--------------|
| **Regular Employee** | 400+ | Submit material requests, view inventory | Basic |
| **Warehouse Manager** | 14 | Process operations, approve requests | Department |
| **Department Admin** | 7 | Manage department users and warehouses | Department |
| **System Admin** | 1 | Full system configuration and oversight | Global |

### 3.2 Permission Matrix

| Feature | Regular Employee | Warehouse Manager | Department Admin | System Admin |
|---------|-----------------|-------------------|------------------|--------------|
| **Dashboard Access** | ✓ | ✓ | ✓ | ✓ |
| **View Materials** | ✓ | ✓ | ✓ | ✓ |
| **View Inventory** | ✓ (own dept) | ✓ (own dept) | ✓ (own dept) | ✓ (all) |
| **Submit Applications** | ✓ | ✓ | ✓ | - |
| **Approve Applications** | - | ✓ | ✓ | - |
| **Create Inbound** | - | ✓ | ✓ | - |
| **Create Direct Outbound** | - | ✓ | ✓ | - |
| **Manage Users** | - | - | ✓ (own dept) | ✓ (all) |
| **Manage Warehouses** | - | - | ✓ (own dept) | ✓ (all) |
| **Manage Departments** | - | - | - | ✓ |
| **System Statistics** | - | ✓ (own dept) | ✓ (own dept) | ✓ (all) |

---

## 4. Common Features

### 4.1 Personal Profile Management

**Accessing Your Profile**:

1. Click your username in the top-right corner
2. Select **Personal Information** from the dropdown menu

**Editable Information**:

- Real Name (姓名)
- Email Address
- Mobile Phone Number
- Department (view only, updated by admin)
- Position/Title

**Updating Your Profile**:

1. Navigate to Profile page
2. Click **Edit** button
3. Modify desired fields
4. Click **Save** to confirm

[Screenshot: Profile editing page]

### 4.2 Changing Your Password

**Security Recommendations**:
- Change password every 90 days
- Use strong passwords (8+ characters, mixed case, numbers, symbols)
- Don't reuse old passwords
- Don't share passwords with colleagues

**Steps to Change Password**:

1. Click your username → **Change Password**
2. Enter current password
3. Enter new password
4. Confirm new password
5. Click **Submit**

**Password Requirements**:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (optional but recommended)

### 4.3 Notification Center

**Accessing Notifications**:

1. Click the bell icon (🔔) in the top navigation
2. View list of unread and read notifications

**Notification Types**:

| Type | Description | Example |
|------|-------------|---------|
| **Application Update** | Status changes on your applications | "Your application #1234 has been approved" |
| **Approval Request** | New applications requiring your approval | "New application from Zhang Wei awaiting approval" |
| **Inventory Warning** | Low stock alerts for managers | "Fiber optic cable stock below minimum level" |
| **System Announcement** | Important system messages | "System maintenance scheduled for Sunday" |

**Managing Notifications**:

- Click notification to view details
- Click **Mark as Read** to dismiss
- Click **Mark All as Read** to clear all notifications
- Configure notification preferences in Settings

[Screenshot: Notification center dropdown]

### 4.4 Search and Filtering

**Global Search**:

1. Use the search box in the top navigation bar
2. Enter keywords (material name, application number, etc.)
3. Press Enter or click search icon
4. View results across all modules

**List Page Filtering**:

Most list pages include filter options:

1. **Quick Filters**: Pre-defined filter buttons (e.g., "Pending", "Approved", "This Month")
2. **Advanced Search**: Click **Advanced** to show more filter fields
3. **Date Range**: Select custom date ranges for time-based data
4. **Status Filter**: Filter by status (pending, completed, cancelled, etc.)

**Example - Filtering Applications**:

1. Navigate to **Applications > My Applications**
2. Click **Advanced Search**
3. Select status: "Pending Approval"
4. Select date range: "Last 30 days"
5. Click **Search**

[Screenshot: Advanced search panel]

### 4.5 Data Export

**Exporting Data to Excel**:

1. Navigate to the desired list page
2. Apply any filters if needed
3. Click **Export** button (usually top-right)
4. Choose export format: Excel (.xlsx) or CSV
5. Wait for download to complete

**Export Options**:

- **Current Page**: Export only visible records on current page
- **All Pages**: Export all filtered records (may take longer)
- **Selected Rows**: Export only checked records

**Tip**: Large exports (>1000 records) may take several seconds. Don't close the browser while exporting.

---

## 5. Regular Employee Guide

### 5.1 Role Overview

As a **Regular Employee**, you can:
- Browse available materials and check inventory
- Submit material requisition applications
- Track your application status
- Receive approval notifications
- Pick up approved materials from warehouse
- View your requisition history

### 5.2 Viewing Available Materials

**Browsing Material Catalog**:

1. Navigate to **Materials > Material List**
2. Browse materials by category
3. View details: name, specifications, unit, current stock

[Screenshot: Material list page]

**Material Information Displayed**:

- Material Code
- Material Name
- Category
- Specifications
- Unit (e.g., meters, pieces, boxes)
- Current Stock Quantity
- Warehouse Location

**Searching for Materials**:

1. Use search box to enter material name or code
2. Use category dropdown to filter by type
3. Click material row to view detailed information

### 5.3 Checking Inventory

**Viewing Inventory Levels**:

1. Navigate to **Inventory > Inventory Query**
2. Select your department (auto-selected)
3. View real-time stock levels for all materials

**Understanding Inventory Display**:

- **Green**: Normal stock level
- **Yellow**: Low stock (approaching minimum)
- **Red**: Critical low stock

**Tip**: Check inventory before submitting an application to ensure materials are available.

### 5.4 Submitting Material Requisition

**When to Submit an Application**:

- You need materials for work tasks or projects
- You don't have direct warehouse access
- The warehouse manager needs to approve your request

**Step-by-Step Application Process**:

**Step 1: Create New Application**

1. Navigate to **Applications > My Applications**
2. Click **+ New Application** button
3. Fill in application form

[Screenshot: New application form]

**Step 2: Fill in Application Details**

Required fields:

1. **Application Title**: Brief description (e.g., "Materials for Network Upgrade Project")
2. **Purpose**: Detailed reason for requisition
3. **Expected Pickup Date**: When you plan to collect materials
4. **Remarks**: Any additional notes (optional)

**Step 3: Add Material Items**

1. Click **+ Add Material** button
2. Search and select material from catalog
3. Enter **Quantity** needed
4. Add remarks for specific item (optional)
5. Repeat to add multiple materials

[Screenshot: Adding material items]

**Step 4: Review and Submit**

1. Review all application details
2. Check material list is correct
3. Click **Submit** button
4. Confirmation message will appear

**Important Notes**:

- You can save applications as **Draft** and submit later
- Once submitted, you cannot edit the application
- Only warehouse managers from your department can approve
- You'll receive notifications on status changes

### 5.5 Tracking Application Status

**Viewing Application Status**:

1. Navigate to **Applications > My Applications**
2. View status in the **Status** column

**Application Status Explained**:

| Status | Icon | Meaning | Next Action |
|--------|------|---------|-------------|
| **Draft** | 📝 | Saved but not submitted | Edit and submit when ready |
| **Pending Approval** | ⏳ | Waiting for manager review | Wait for approval (usually <24 hours) |
| **Approved** | ✅ | Manager approved, ready for pickup | Go to warehouse to collect materials |
| **Rejected** | ❌ | Manager rejected request | View rejection reason, submit new application if needed |
| **Completed** | ✓ | Materials picked up and issued | No further action needed |
| **Cancelled** | ⊗ | Application cancelled | No further action needed |

**Viewing Application Details**:

1. Click on any application row
2. View complete details including:
   - Application information
   - Material list
   - Approval history
   - Pickup information (if completed)
   - Timeline of status changes

[Screenshot: Application detail page]

### 5.6 Picking Up Approved Materials

**When Application is Approved**:

1. You'll receive notification via:
   - System notification (bell icon)
   - WeChat template message (if enabled)

2. The notification includes:
   - Application number
   - Approved materials list
   - Warehouse location
   - Pickup deadline (usually 7 days from approval)

**Pickup Process**:

1. Go to the warehouse within 7 days
2. Bring your employee ID
3. Tell warehouse manager your application number
4. Manager will verify and hand over materials
5. Sign pickup confirmation
6. Manager marks application as "Completed"

**Important**:

- Pickup within **7 days** or application will be **auto-cancelled**
- If you cannot pickup on time, contact warehouse manager
- Check materials for damage before leaving warehouse
- Keep your own record of issued materials

### 5.7 Cancelling Applications

**When You Can Cancel**:

- Application status is "Draft" or "Pending Approval"
- You no longer need the materials
- You made a mistake and need to resubmit

**Steps to Cancel**:

1. Navigate to **Applications > My Applications**
2. Find the application to cancel
3. Click **Cancel** button
4. Confirm cancellation in the dialog
5. Application status changes to "Cancelled"

**Note**: You cannot cancel applications that are already approved. Contact warehouse manager instead.

### 5.8 Viewing Application History

**Accessing History**:

1. Navigate to **Applications > My Applications**
2. Use date range filter to select time period
3. Use status filter to show specific statuses
4. Click **Search** to apply filters

**Exporting History**:

1. Filter applications as needed
2. Click **Export** button
3. Download Excel file with application records
4. Use for your own record keeping or reporting

---

## 6. Warehouse Manager Guide

### 6.1 Role Overview

As a **Warehouse Manager**, you can:

- Process inbound operations (receiving stock)
- Create direct outbound operations (immediate issue)
- Review and approve employee applications
- Confirm material pickups
- Monitor inventory levels and warnings
- Generate warehouse statistics and reports

**Key Responsibilities**:

- Ensure accurate inventory records
- Approve/reject employee requisitions promptly
- Verify materials during pickup
- Maintain warehouse organization
- Report low stock situations

### 6.2 Dashboard Overview

Your dashboard shows:

**Quick Statistics**:
- Pending approvals count
- Pending pickups count
- Today's inbound/outbound operations
- Current inventory value
- Low stock alerts

**Quick Actions**:
- Create Inbound
- Create Outbound
- Approve Applications
- Process Pickups

**Recent Activities**:
- Latest applications
- Recent inventory changes
- Upcoming tasks

[Screenshot: Warehouse manager dashboard]

### 6.3 Inbound Operations (Receiving Stock)

**When to Create Inbound**:

- New materials received from suppliers
- Materials returned from employees
- Stock transfers from other departments (if applicable)
- Inventory adjustments (addition)

**Step-by-Step Inbound Process**:

**Step 1: Create New Inbound Order**

1. Navigate to **Inbound > Inbound List**
2. Click **+ Create Inbound** button
3. Fill in inbound form

[Screenshot: Create inbound form]

**Step 2: Fill in Basic Information**

Required fields:

1. **Warehouse**: Select your warehouse (usually auto-selected)
2. **Inbound Type**: Select type
   - Purchase (from supplier)
   - Return (from employee)
   - Transfer (from other warehouse)
   - Other
3. **Supplier/Source**: Enter supplier name or source
4. **Inbound Date**: Select date (default: today)
5. **Remarks**: Add any notes about this inbound

**Step 3: Add Material Items**

1. Click **+ Add Material** button
2. Search and select material from catalog
3. Enter **Quantity** received
4. Enter **Unit Price** (optional, for cost tracking)
5. Add **Remarks** if needed (e.g., "Batch No. 2025001")
6. Repeat for all materials in this inbound

[Screenshot: Adding inbound material items]

**Step 4: Review and Submit**

1. Verify all information is correct
2. Check quantities match physical count
3. Click **Submit** button
4. System automatically updates inventory

**Important**:

- Inventory is updated immediately upon submission
- Double-check quantities before submitting
- Cannot edit after submission (can only view or void)
- Attach photos or documents if available (upload feature)

### 6.4 Direct Outbound Operations

**When to Use Direct Outbound**:

- Urgent material issues requiring immediate action
- Your own material usage (as warehouse manager)
- Pre-approved requisitions
- Emergency situations

**Difference from Application-Based Outbound**:

| Feature | Direct Outbound | Application-Based Outbound |
|---------|----------------|---------------------------|
| Who can create | Warehouse managers only | Created automatically after approval |
| Approval needed | No | Yes (employee submits application) |
| Inventory deduction | Immediate | After pickup confirmation |
| Use case | Manager's discretion | Employee requests |

**Step-by-Step Direct Outbound**:

**Step 1: Create New Outbound Order**

1. Navigate to **Outbound > Outbound List**
2. Click **+ Create Outbound** button
3. Fill in outbound form

[Screenshot: Create outbound form]

**Step 2: Fill in Basic Information**

Required fields:

1. **Warehouse**: Select your warehouse
2. **Outbound Type**: Select type
   - Normal Issue
   - Emergency Issue
   - Internal Use
   - Other
3. **Recipient**: Enter recipient name (employee or project)
4. **Outbound Date**: Select date (default: today)
5. **Purpose**: Describe reason for outbound
6. **Remarks**: Additional notes (optional)

**Step 3: Add Material Items**

1. Click **+ Add Material** button
2. Select material from inventory
3. **Check available quantity** before proceeding
4. Enter **Quantity** to issue
5. System warns if quantity exceeds available stock
6. Add remarks if needed
7. Repeat for all materials

**Step 4: Review and Submit**

1. Verify all information
2. Confirm sufficient inventory for all items
3. Click **Submit** button
4. **Inventory is deducted immediately**

**Important Warnings**:

- Cannot issue more than available inventory
- Inventory is deducted immediately and cannot be easily reversed
- Use this feature responsibly (all actions are logged)
- For employee requests, prefer application-based workflow

### 6.5 Reviewing Employee Applications

**Accessing Pending Approvals**:

1. Navigate to **Approval > Pending Approvals**
2. View list of applications awaiting your review
3. Applications are from employees in your department only

[Screenshot: Pending approvals list]

**Application Review Information**:

Each application shows:
- Application number
- Applicant name and department
- Submission date
- Requested materials and quantities
- Application purpose
- Urgency/priority

**Step-by-Step Approval Process**:

**Step 1: Open Application Details**

1. Click on application row to view full details
2. Review all information carefully:
   - Purpose and justification
   - Material list and quantities
   - Expected pickup date
   - Employee's application history (if available)

[Screenshot: Application detail for approval]

**Step 2: Check Inventory Availability**

1. System automatically shows current stock for each material
2. Check if sufficient quantity is available
3. If insufficient stock:
   - Reject application with reason
   - Or approve partial quantity (if acceptable)

**Step 3: Make Approval Decision**

**Option A: Approve Application**

1. Click **Approve** button
2. Verify approval (optional confirmation dialog)
3. System automatically:
   - Changes status to "Approved"
   - Creates outbound order (status: "Pending Pickup")
   - Sends notification to employee
   - **Does NOT deduct inventory yet** (deducted upon pickup confirmation)

**Option B: Reject Application**

1. Click **Reject** button
2. **Enter rejection reason** (required)
   - Common reasons: "Insufficient stock", "Not justified", "Contact me for details"
3. Confirm rejection
4. System sends notification to employee with your reason

**Step 4: Post-Approval Actions**

After approval:
- Application moves to **Approval > Approved** list
- Outbound order appears in **Outbound > Pending Pickups**
- Wait for employee to pickup materials

**Best Practices for Approval**:

- Review applications within **24 hours** (SLA)
- Check inventory before approving
- Provide clear rejection reasons
- Communicate with employee if clarification needed
- Prioritize urgent applications
- Keep records of approval patterns for auditing

### 6.6 Processing Material Pickups

**What is Pickup Confirmation**:

After you approve an application, an outbound order is created with status "Pending Pickup". When the employee comes to collect materials, you must confirm the pickup to complete the transaction and deduct inventory.

**Accessing Pending Pickups**:

1. Navigate to **Outbound > Pickup Confirmation**
2. View list of approved applications waiting for pickup
3. Each shows: application number, employee name, materials, approval date

[Screenshot: Pending pickups list]

**Step-by-Step Pickup Process**:

**Step 1: Employee Arrives at Warehouse**

1. Employee shows you their application number or employee ID
2. Find the corresponding outbound order in "Pending Pickups"
3. Click on the order to view details

**Step 2: Verify and Prepare Materials**

1. Check employee identity (employee ID)
2. Verify materials and quantities on the order
3. Physically collect materials from warehouse shelves
4. Count and verify quantities

**Step 3: Hand Over Materials**

1. Present materials to employee
2. Ask employee to verify and check for damage
3. Get employee's signature on pickup form (if using paper forms)

**Step 4: Confirm Pickup in System**

1. In the outbound order details page
2. Click **Confirm Pickup** button
3. System will:
   - Deduct inventory quantities
   - Change order status to "Completed"
   - Change application status to "Completed"
   - Send completion notification to employee
   - Record pickup timestamp

[Screenshot: Confirm pickup dialog]

**Important Notes**:

- **Inventory is only deducted at this step** (not during approval)
- Once confirmed, cannot be easily reversed (contact system admin)
- Employee has **7 days** to pickup from approval date
- After 7 days, order is auto-cancelled (inventory not deducted)

**What If Employee Doesn't Pickup**:

If employee doesn't show up within 7 days:
- System automatically cancels the outbound order
- Application status changes to "Cancelled"
- Inventory is NOT deducted (remains available)
- Employee receives cancellation notification
- Employee can submit a new application if still needed

### 6.7 Monitoring Inventory

**Inventory Query**:

1. Navigate to **Inventory > Inventory Query**
2. View real-time stock levels for all materials in your warehouse
3. Filter by category, material name, or stock status

**Understanding Inventory Display**:

| Column | Description |
|--------|-------------|
| Material Code | Unique identifier |
| Material Name | Full name and specifications |
| Category | Material type |
| Current Stock | Current available quantity |
| Unit | Measurement unit |
| Min Level | Minimum stock threshold |
| Max Level | Maximum stock threshold |
| Status | Stock level status (normal/low/critical) |

**Inventory Status Indicators**:

- **Green (Normal)**: Stock above minimum level
- **Yellow (Low Stock)**: Stock below minimum but not critical
- **Red (Critical)**: Stock at or near zero

[Screenshot: Inventory query page with color coding]

**Low Stock Warnings**:

1. Navigate to **Inventory > Low Stock Warnings**
2. View materials that have fallen below minimum levels
3. Take action:
   - Notify procurement team
   - Place purchase orders
   - Temporarily reject applications for low stock items

**Inventory Transaction Log**:

1. Navigate to **Inventory > Transaction Log**
2. View complete history of all inventory changes
3. Filter by:
   - Date range
   - Material
   - Transaction type (inbound/outbound/adjustment)
   - Operator

**Tip**: Review transaction log regularly to identify unusual patterns or errors.

### 6.8 Generating Reports

**Accessing Reports**:

Navigate to **Statistics > Reports** section

**Available Reports**:

**1. Inbound Statistics**

- Path: **Statistics > Inbound Statistics**
- Shows: Daily/monthly/yearly inbound volumes
- Filters: Date range, material category, supplier
- Metrics: Total quantity, total value, inbound count

[Screenshot: Inbound statistics chart]

**2. Outbound Statistics**

- Path: **Statistics > Outbound Statistics**
- Shows: Daily/monthly/yearly outbound volumes
- Filters: Date range, material category, recipient
- Breakdown: Direct outbound vs application-based outbound
- Metrics: Total quantity, total value, outbound count

**3. Material Usage Report**

- Path: **Statistics > Material Usage**
- Shows: Top used materials
- Analysis: Usage trends, peak periods
- Helps: Identify which materials need frequent restocking

**4. Inventory Status Report**

- Path: **Statistics > Inventory Report**
- Shows: Current stock levels for all materials
- Highlights: Low stock items, overstock items
- Export: Excel format for offline analysis

**5. Department Statistics**

- Path: **Statistics > Department Stats**
- Shows: Your department's warehouse performance
- Metrics:
  - Total applications processed
  - Average approval time
  - Inventory turnover rate
  - Material consumption by category

**Exporting Reports**:

1. Configure report parameters (date range, filters)
2. Click **Generate Report**
3. Review report on screen
4. Click **Export to Excel** to download
5. Use for presentations or record keeping

---

## 7. Department Administrator Guide

### 7.1 Role Overview

As a **Department Administrator**, you have all permissions of a Warehouse Manager, plus:

- Manage users within your department
- Assign and modify user roles
- Configure departmental warehouses
- View department-wide statistics
- Oversee all department warehouse operations

**Key Responsibilities**:

- Maintain department user accounts
- Ensure proper role assignments
- Monitor department warehouse performance
- Coordinate between multiple warehouses (if applicable)
- Report to system administrator on department needs

### 7.2 Managing Department Users

**Accessing User Management**:

1. Navigate to **System > User Management**
2. View list of all users in your department
3. You can only manage users in your department (data is filtered)

[Screenshot: User management page]

**Creating New Users**:

**Step 1: Add New User**

1. Click **+ Add User** button
2. Fill in user registration form

**Step 2: Fill in User Information**

Required fields:

1. **Username**: Login username (unique, usually employee ID)
2. **Real Name**: Employee's full name
3. **Employee ID**: Official employee number
4. **Department**: Your department (auto-selected)
5. **Role**: Select appropriate role
   - Regular Employee (普通员工)
   - Warehouse Manager (仓库管理员)
6. **Mobile Phone**: Contact number
7. **Email**: Email address
8. **Initial Password**: Set temporary password (user must change on first login)

**Step 3: Assign Warehouse** (for Warehouse Managers only)

If assigning Warehouse Manager role:
1. Select which warehouse(s) this manager oversees
2. A user can manage multiple warehouses

**Step 4: Save User**

1. Review all information
2. Click **Submit** button
3. User account is created
4. Notify the user of their username and initial password

**Editing Existing Users**:

1. Find user in the list
2. Click **Edit** button
3. Modify information as needed
4. Common edits:
   - Update contact information
   - Change role assignment
   - Update warehouse assignment
   - Enable/disable account
5. Click **Save** to confirm

**Deactivating Users**:

When employee leaves or changes department:

1. Find user in the list
2. Click **Disable** button
3. Confirm deactivation
4. User can no longer login
5. Historical data remains in system

**Note**: Cannot delete users (data integrity), only disable them.

**Resetting User Passwords**:

When user forgets password:

1. Find user in the list
2. Click **Reset Password** button
3. System generates temporary password
4. Copy and send password to user securely
5. User must change password on next login

### 7.3 Role and Permission Management

**Understanding Department Roles**:

Within your department, you can assign:

| Role | Permissions | Typical Users |
|------|-------------|---------------|
| **Regular Employee** | Submit applications, view inventory | Field technicians, engineers, support staff |
| **Warehouse Manager** | All Regular Employee permissions + approve applications, process inbound/outbound, manage inventory | Designated warehouse supervisors |

**Assigning Roles**:

1. Navigate to **System > User Management**
2. Edit user
3. Select **Role** from dropdown
4. Save changes

**Best Practices**:

- Assign Warehouse Manager role only to trained staff
- Limit number of warehouse managers (recommended: 2 per warehouse for coverage)
- Review role assignments quarterly
- Document role assignment decisions

### 7.4 Managing Department Warehouses

**Accessing Warehouse Management**:

1. Navigate to **Basic Data > Warehouse Management**
2. View warehouses assigned to your department

**Viewing Warehouse Information**:

Each warehouse shows:
- Warehouse code
- Warehouse name
- Location/address
- Department
- Warehouse manager(s)
- Status (active/inactive)

**Editing Warehouse Information**:

1. Click **Edit** on warehouse row
2. Modify information:
   - Warehouse name
   - Location details
   - Contact information
   - Capacity information
   - Assigned manager(s)
3. Save changes

**Note**: You cannot create or delete warehouses (only System Admin can). You can only edit warehouses in your department.

**Assigning Warehouse Managers**:

1. Edit warehouse
2. In **Warehouse Managers** field, select users
3. Can assign multiple managers to one warehouse
4. Can assign one manager to multiple warehouses
5. Save assignment

### 7.5 Department-Wide Reporting

**Accessing Department Statistics**:

Navigate to **Statistics > Department Dashboard**

**Available Department Metrics**:

**1. Overview Dashboard**

- Total department applications (this month)
- Total inbound/outbound operations
- Current inventory value
- Active users count
- Low stock items count

**2. User Activity Report**

- Most active users
- Application submission patterns
- Material usage by user
- Peak request times

**3. Warehouse Performance**

- Compare performance across department warehouses (if multiple)
- Average approval time
- Inventory turnover rates
- Operation efficiency metrics

**4. Material Consumption Analysis**

- Top consumed materials
- Consumption trends over time
- Seasonal patterns
- Budget vs actual consumption

**5. Compliance and Audit Reports**

- Applications exceeding normal quantities
- After-hours operations
- Rejected applications summary
- Inventory discrepancies (if any)

**Exporting Department Reports**:

1. Configure report parameters
2. Select data range and filters
3. Click **Generate Report**
4. Review on screen
5. Export to Excel or PDF
6. Use for department meetings or upper management reporting

### 7.6 Coordinating Multiple Warehouses

If your department has multiple warehouses:

**Viewing All Warehouses**:

1. Navigate to **Basic Data > Warehouse Management**
2. View all department warehouses in one view
3. Compare inventory levels across warehouses

**Inventory Overview Across Warehouses**:

1. Navigate to **Inventory > Cross-Warehouse Query**
2. View same material across different warehouses
3. Identify:
   - Which warehouse has excess stock
   - Which warehouse has shortages
   - Opportunities for internal transfers (if system supports)

**Best Practices**:

- Hold regular meetings with warehouse managers
- Coordinate on low stock items
- Balance inventory across warehouses when possible
- Standardize processes across all warehouses
- Share best practices between warehouse managers

---

## 8. System Administrator Guide

### 8.1 Role Overview

As the **System Administrator**, you have full control over the entire system:

- Manage all departments, users, and roles across the organization
- Configure global system settings
- Oversee all warehouses and operations
- View organization-wide statistics and reports
- Perform system maintenance and troubleshooting
- Manage data backups and security

**Key Responsibilities**:

- System configuration and optimization
- User account lifecycle management
- Data integrity and security
- System performance monitoring
- Support for department administrators
- Disaster recovery planning

### 8.2 Global User Management

**Accessing User Management**:

1. Navigate to **System > User Management**
2. View **all users** across **all departments** (unlike department admins)

**Advanced User Operations**:

**Creating Department Administrators**:

1. Click **+ Add User**
2. Fill in user information
3. **Role**: Select "Department Administrator"
4. **Department**: Assign to appropriate department
5. Submit to create

**Bulk User Import**:

For onboarding many users at once:

1. Click **Bulk Import** button
2. Download Excel template
3. Fill in user information in Excel
4. Upload completed file
5. System validates and imports users
6. Review import results and error report (if any)

[Screenshot: Bulk import dialog]

**User Audit Log**:

1. Navigate to **System > User Audit Log**
2. View complete history of:
   - User logins and logouts
   - Password changes
   - Permission changes
   - Failed login attempts
3. Filter by user, date, action type
4. Export for security audits

**Account Security Management**:

1. View users with:
   - Never changed password
   - Inactive accounts (no login for 90+ days)
   - Multiple failed login attempts
   - Shared passwords (password patterns)
2. Take action:
   - Force password reset
   - Disable compromised accounts
   - Send security reminders

### 8.3 Department Management

**Accessing Department Management**:

1. Navigate to **Basic Data > Department Management**
2. View organization structure

**Creating New Departments**:

1. Click **+ Add Department** button
2. Fill in department information:
   - Department Code (unique identifier)
   - Department Name
   - Parent Department (if hierarchical)
   - Department Manager
   - Contact Information
   - Description
3. Click **Submit**

**Editing Departments**:

1. Click **Edit** on department row
2. Modify information
3. Save changes

**Important**: Changing department structure affects user access and data visibility.

**Deactivating Departments**:

When department is dissolved:

1. First reassign all users to other departments
2. Archive or transfer warehouse data
3. Then disable department
4. Cannot delete departments with historical data

### 8.4 Global Warehouse Configuration

**Accessing Warehouse Management**:

1. Navigate to **Basic Data > Warehouse Management**
2. View **all warehouses** across **all departments**

**Creating New Warehouses**:

1. Click **+ Add Warehouse** button
2. Fill in warehouse information:
   - Warehouse Code (unique, e.g., "WH-NET-01")
   - Warehouse Name
   - Department (which department owns this warehouse)
   - Location/Address
   - Capacity
   - Contact Information
   - Warehouse Manager(s)
3. Click **Submit**

**Warehouse Configuration Options**:

- Set default low stock thresholds
- Configure automatic notification rules
- Set pickup timeout period (default: 7 days)
- Enable/disable features per warehouse

**Warehouse Performance Monitoring**:

1. Navigate to **Statistics > Warehouse Performance**
2. Compare all warehouses organization-wide
3. Identify:
   - High-performing warehouses (fast approvals, accurate inventory)
   - Problematic warehouses (slow operations, frequent errors)
   - Underutilized warehouses
4. Take corrective actions

### 8.5 Material Master Data Management

**Accessing Material Management**:

1. Navigate to **Basic Data > Material Management**
2. View organization-wide material catalog

**Creating Material Categories**:

1. Navigate to **Basic Data > Material Categories**
2. Click **+ Add Category**
3. Enter category information:
   - Category Code
   - Category Name
   - Parent Category (if hierarchical)
   - Description
4. Submit

**Creating New Materials**:

1. Navigate to **Basic Data > Material Management**
2. Click **+ Add Material**
3. Fill in material information:
   - **Material Code**: Unique identifier (e.g., "MAT-FC-001")
   - **Material Name**: Full descriptive name
   - **Category**: Select from categories
   - **Specifications**: Technical details
   - **Unit**: Measurement unit (meters, pieces, boxes)
   - **Brand**: Manufacturer or brand name
   - **Min Stock Level**: Minimum quantity threshold (for warnings)
   - **Max Stock Level**: Maximum quantity threshold
   - **Unit Price**: Standard unit price (optional)
   - **Description**: Additional details
   - **Image**: Upload material photo (optional)
4. Click **Submit**

[Screenshot: Add material form]

**Bulk Material Import**:

1. Click **Bulk Import**
2. Download Excel template
3. Fill in material data
4. Upload file
5. Review import results

**Editing Materials**:

1. Find material in list
2. Click **Edit**
3. Modify information
4. Save changes

**Important**: Changes to material information affect all warehouses using that material.

**Deactivating Materials**:

When material is discontinued:

1. Click **Disable** on material row
2. Material becomes unavailable for new operations
3. Historical records remain intact
4. Can reactivate if needed

### 8.6 System Settings and Configuration

**Accessing System Settings**:

1. Navigate to **System > System Settings**
2. Configure global parameters

**Key Configuration Areas**:

**1. General Settings**

- System name and logo
- Timezone settings
- Date format preferences
- Language settings (Chinese/English)
- Session timeout duration

**2. Security Settings**

- Password policy:
  - Minimum password length
  - Complexity requirements
  - Password expiration period
  - Password history (prevent reuse)
- Login attempt limits
- IP whitelisting (if needed)
- Two-factor authentication (if available)

**3. Notification Settings**

- Enable/disable notification types
- WeChat integration configuration
- Email server settings (SMTP)
- Notification templates
- Notification retention period

**4. Business Rules**

- Application approval timeout: Default 24 hours
- Pickup timeout: Default 7 days
- Auto-cancellation rules
- Inventory warning thresholds
- Maximum application quantities

**5. Integration Settings**

- WeChat Mini Program configuration
- External API settings (if any)
- Data export/import settings

**Best Practices**:

- Document all configuration changes
- Test configuration changes in non-production environment first
- Notify users before making significant changes
- Keep backup of previous settings

### 8.7 System Monitoring and Maintenance

**System Health Dashboard**:

1. Navigate to **System > System Status**
2. View real-time metrics:
   - System uptime
   - Current active users
   - Database size
   - API response times
   - Error rates
   - Resource utilization (CPU, memory, disk)

[Screenshot: System health dashboard]

**Error Log Monitoring**:

1. Navigate to **System > Error Logs**
2. Review system errors and warnings
3. Filter by:
   - Severity (critical, error, warning, info)
   - Date range
   - Module (user, inventory, application, etc.)
   - User
4. Export logs for troubleshooting

**Common Issues to Monitor**:

- High error rates (investigate root cause)
- Slow API responses (performance optimization needed)
- Frequent login failures (potential security issue)
- Database connection issues (server health check)

**Data Backup Management**:

1. Navigate to **System > Backup Management**
2. Configure automated backups:
   - Backup frequency (daily, weekly)
   - Backup retention period
   - Backup storage location
3. Perform manual backup:
   - Click **Backup Now**
   - Wait for completion
   - Verify backup file
4. Restore from backup (emergency only):
   - Select backup file
   - Confirm restoration (this will overwrite current data)
   - System restarts and restores data

**Important**: Always test backup restoration in non-production environment.

**Database Maintenance**:

1. Navigate to **System > Database Maintenance**
2. Perform maintenance tasks:
   - **Optimize Database**: Rebuild indexes, clean up fragmentation
   - **Archive Old Data**: Move historical data to archive tables
   - **Clean Temporary Data**: Remove expired sessions, cache
3. Schedule regular maintenance (recommended: weekly)

### 8.8 Organization-Wide Reporting and Analytics

**Accessing Global Reports**:

Navigate to **Statistics > Global Reports**

**Executive Dashboard**:

High-level metrics for management:

- Total users across organization
- Total departments and warehouses
- Total inventory value
- Month-over-month growth
- Top performing departments
- System adoption rate

[Screenshot: Executive dashboard]

**Cross-Department Comparisons**:

- Application volumes by department
- Average approval times
- Inventory turnover rates
- Material consumption patterns
- User activity levels

**Trend Analysis**:

- Yearly/quarterly trends
- Seasonal patterns
- Forecast future needs
- Identify growth opportunities

**Custom Report Builder**:

1. Navigate to **Statistics > Custom Reports**
2. Select data sources and metrics
3. Configure filters and groupings
4. Generate report
5. Save report template for reuse
6. Schedule automatic report generation

**Exporting for Executive Review**:

- Export to PowerPoint-ready format
- Create executive summaries
- Schedule automated email delivery
- Share dashboards with leadership

---

## 9. Tips and Best Practices

### 9.1 General Tips for All Users

**1. Keep Your Profile Updated**

- Ensure contact information is current
- Update mobile number for notifications
- Verify email address for system alerts

**2. Check Notifications Regularly**

- Review notifications at least twice daily
- Don't ignore low stock warnings (managers)
- Act on pending tasks promptly

**3. Use Search and Filters Effectively**

- Learn advanced search shortcuts
- Save common filter combinations
- Use date range filters for historical data

**4. Export Data for Offline Analysis**

- Keep personal records of applications
- Export reports for presentations
- Backup critical data periodically

**5. Follow Naming Conventions**

- Use clear, descriptive application titles
- Include project names or codes in applications
- Be consistent with material descriptions

### 9.2 For Regular Employees

**Application Submission Tips**:

1. **Check Inventory First**: Avoid rejected applications by verifying stock availability
2. **Be Specific**: Clearly explain purpose and urgency
3. **Plan Ahead**: Submit applications 1-2 days before needed (not last minute)
4. **Accurate Quantities**: Don't overestimate; return unused materials
5. **Track Status**: Check application status regularly

**Communication Tips**:

1. Contact warehouse manager if urgent
2. Provide feedback on rejected applications
3. Report damaged materials immediately
4. Suggest improvements to warehouse team

### 9.3 For Warehouse Managers

**Approval Best Practices**:

1. **Review Promptly**: Aim to approve/reject within 4 hours (SLA: 24 hours)
2. **Check Inventory**: Always verify stock before approving
3. **Clear Rejections**: Provide specific, helpful rejection reasons
4. **Communicate**: Call employee if application is unclear
5. **Audit Trail**: Document unusual approvals

**Inventory Management Tips**:

1. **Daily Checks**: Review inventory levels every morning
2. **Low Stock Alerts**: Act on warnings immediately
3. **Physical Counts**: Perform spot checks weekly, full inventory monthly
4. **Report Discrepancies**: Notify admin of any inventory mismatches
5. **First-In-First-Out**: Issue older stock first

**Operational Efficiency**:

1. **Organize Warehouse**: Clearly label shelves and bins
2. **Standard Processes**: Follow consistent workflows
3. **Batch Processing**: Group similar operations when possible
4. **Off-Peak Tasks**: Perform reports and data entry during slow periods
5. **Train Backup**: Ensure someone can cover during your absence

### 9.4 For Department Administrators

**User Management Best Practices**:

1. **Regular Audits**: Review user list quarterly
2. **Offboarding Process**: Disable accounts when employees leave
3. **Role Reviews**: Verify role assignments annually
4. **Security**: Monitor for unusual account activity
5. **Training**: Ensure all users receive proper system training

**Performance Monitoring**:

1. **Weekly Reviews**: Check department statistics weekly
2. **Benchmarking**: Compare against other departments
3. **Continuous Improvement**: Identify and address bottlenecks
4. **Team Meetings**: Discuss metrics with warehouse managers
5. **Recognition**: Acknowledge high-performing staff

### 9.5 For System Administrators

**System Health**:

1. **Daily Monitoring**: Check system status dashboard daily
2. **Proactive Maintenance**: Don't wait for issues to arise
3. **Regular Backups**: Verify backups complete successfully
4. **Update Schedule**: Plan and communicate system updates
5. **Disaster Recovery**: Test recovery procedures quarterly

**Data Integrity**:

1. **Audit Trails**: Review logs for suspicious activity
2. **Data Validation**: Implement rules to prevent bad data
3. **Reconciliation**: Perform regular inventory reconciliation
4. **Archive Strategy**: Archive old data to maintain performance
5. **Documentation**: Maintain system documentation

**User Support**:

1. **Help Desk**: Establish clear support channels
2. **Training Materials**: Keep user guides updated
3. **FAQs**: Maintain knowledge base of common issues
4. **Feedback Loop**: Collect and act on user feedback
5. **Communication**: Announce changes and updates proactively

---

## 10. Troubleshooting

### 10.1 Login Issues

**Problem: Cannot login - "Invalid username or password"**

**Solutions**:

1. Verify username (usually employee ID)
2. Check Caps Lock is OFF
3. Try resetting password:
   - Click "Forgot Password" link
   - Or contact your department administrator
4. Ensure account is not disabled (contact admin)

**Problem: "Account has been locked"**

**Cause**: Too many failed login attempts (usually 5 within 15 minutes)

**Solution**:

1. Wait 30 minutes for automatic unlock
2. Or contact system administrator to unlock immediately

**Problem: System keeps logging me out**

**Cause**: Session timeout (default: 2 hours of inactivity)

**Solutions**:

1. Save work frequently
2. Refresh page periodically to keep session active
3. Contact admin to increase session timeout if needed

### 10.2 Application Issues

**Problem: Cannot submit application - "Insufficient inventory"**

**Cause**: Requested quantity exceeds available stock

**Solutions**:

1. Check current inventory in Inventory Query
2. Reduce requested quantity
3. Contact warehouse manager about restocking
4. Split application into multiple requests over time

**Problem: Application stuck in "Pending Approval" for too long**

**Expected**: Approval within 24 hours

**Solutions**:

1. Check if warehouse manager is on leave
2. Contact warehouse manager directly (phone/WeChat)
3. If urgent, contact department administrator
4. System sends automatic reminder after 24 hours

**Problem: Cannot cancel application**

**Cause**: Application is already approved or completed

**Solution**:

1. Contact warehouse manager to cancel on their end
2. Don't pickup materials (will auto-cancel after 7 days)
3. For completed applications, cannot cancel (already processed)

### 10.3 Inventory Issues

**Problem: Inventory quantity doesn't match physical count**

**Possible Causes**:

1. Pickup not confirmed in system
2. Inbound not recorded
3. Data entry error
4. Concurrent access issue (rare)

**Solutions**:

1. Review transaction log for discrepancies
2. Perform physical recount
3. Contact system administrator to adjust inventory (with approval)
4. Document reason for adjustment

**Problem: Low stock warning not showing**

**Solutions**:

1. Verify min stock level is set for the material
2. Check notification settings are enabled
3. Refresh Inventory Warning page
4. Contact system admin if settings are correct but warnings missing

### 10.4 Performance Issues

**Problem: System is slow or unresponsive**

**Solutions**:

1. Check your internet connection
2. Clear browser cache and cookies
3. Try different browser (Chrome recommended)
4. Close unnecessary browser tabs
5. Check system status page for maintenance notices
6. Contact IT support if persists

**Problem: Page not loading or showing errors**

**Solutions**:

1. Refresh page (F5 or Ctrl+R)
2. Clear browser cache
3. Try accessing from different computer
4. Check if other pages work (isolated issue vs system-wide)
5. Report to IT support with error message screenshot

**Problem: Export/Download not working**

**Solutions**:

1. Check browser pop-up blocker settings
2. Ensure sufficient disk space
3. Try smaller data range (large exports may timeout)
4. Use different browser
5. Contact support if issue persists

### 10.5 Notification Issues

**Problem: Not receiving notifications**

**Solutions**:

1. Check notification settings in Profile
2. Verify email address and mobile number are correct
3. Check spam/junk folder for emails
4. For WeChat: Ensure Mini Program is authorized to send notifications
5. Contact admin to verify notification service is running

**Problem: Too many notifications**

**Solutions**:

1. Configure notification preferences
2. Disable less important notification types
3. Set digest mode (receive daily summary instead of real-time)

### 10.6 Access and Permission Issues

**Problem: "Access Denied" or "Insufficient Permissions"**

**Cause**: You don't have required role or permission for this action

**Solutions**:

1. Verify your assigned role with department admin
2. Confirm you're accessing data from your own department
3. Contact department admin to request additional permissions if needed
4. Ensure you're using correct account (not logged in as wrong user)

**Problem: Cannot see certain menu items**

**Cause**: Menu items are role-based; only relevant items shown

**Solution**: This is normal. Each role sees only menus they have access to.

### 10.7 Data Entry Issues

**Problem: Cannot save form - validation errors**

**Solutions**:

1. Check all required fields are filled (marked with red asterisk *)
2. Verify data formats (dates, numbers, etc.)
3. Check character limits for text fields
4. Remove special characters if not allowed
5. Review error messages for specific field issues

**Problem: Dropdown list is empty**

**Possible Causes**:

1. No data exists (e.g., no materials created yet)
2. Data filtered by department (e.g., only your department's warehouses shown)
3. Data loading error

**Solutions**:

1. Contact admin to verify master data exists
2. Refresh page
3. Try different browser
4. Report issue if persists

### 10.8 Getting Additional Help

**Contact Channels**:

1. **IT Help Desk**:
   - Phone: [Your IT Support Number]
   - Email: it-support@your-company.com
   - Hours: Monday-Friday, 9:00-17:00

2. **Department Administrator**:
   - For user account issues
   - Permission requests
   - Department-specific questions

3. **System Administrator**:
   - For system-wide issues
   - Technical problems
   - Feature requests

**When Reporting Issues**:

Provide the following information:

1. Your username and role
2. What you were trying to do
3. What actually happened
4. Error message (exact text or screenshot)
5. Steps to reproduce the issue
6. Browser and version
7. Date and time of issue

**Emergency Support**:

For critical issues affecting operations:

- Call emergency IT hotline: [Emergency Number]
- Available 24/7 for production issues

---

## Appendix A: Glossary

| Term | Chinese | Definition |
|------|---------|------------|
| **Application** | 申请单 | Material requisition request submitted by employee |
| **Approval** | 审批 | Review and authorization process for applications |
| **Inbound** | 入库 | Process of receiving materials into warehouse |
| **Outbound** | 出库 | Process of issuing materials from warehouse |
| **Inventory** | 库存 | Current stock levels of materials |
| **Material** | 物资 | Physical items managed in the warehouse |
| **Warehouse** | 仓库 | Physical or logical storage location |
| **Department** | 部门 | Organizational unit |
| **Role** | 角色 | User permission level |
| **Pickup** | 领料 | Collecting approved materials from warehouse |
| **Transaction** | 事务 | Inventory movement record |
| **Direct Outbound** | 直接出库 | Outbound created by manager without application |
| **Application-based Outbound** | 申请出库 | Outbound created from approved application |

---

## Appendix B: Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Alt + H` | Go to Home/Dashboard |
| `Alt + N` | Open Notifications |
| `Alt + S` | Focus Search Box |
| `Ctrl + S` | Save Form (when in edit mode) |
| `Esc` | Close Dialog/Modal |
| `F5` | Refresh Page |
| `Ctrl + F` | Search within page |

**Note**: Keyboard shortcuts may vary by browser and operating system.

---

## Appendix C: Quick Reference Cards

### For Regular Employees

**Common Tasks**:

1. Submit Application:
   - Applications > My Applications > + New Application
   - Fill form > Add materials > Submit

2. Check Application Status:
   - Applications > My Applications
   - View status column

3. View Inventory:
   - Inventory > Inventory Query
   - Filter by category or search material

4. Update Profile:
   - Click username > Personal Information > Edit

### For Warehouse Managers

**Common Tasks**:

1. Approve Application:
   - Approval > Pending Approvals
   - Click application > Review > Approve/Reject

2. Create Inbound:
   - Inbound > Inbound List > + Create Inbound
   - Fill form > Add materials > Submit

3. Confirm Pickup:
   - Outbound > Pickup Confirmation
   - Click order > Verify > Confirm Pickup

4. Check Low Stock:
   - Inventory > Low Stock Warnings
   - Review list and take action

### For Administrators

**Common Tasks**:

1. Add User:
   - System > User Management > + Add User
   - Fill form > Assign role > Submit

2. Assign Role:
   - System > User Management
   - Edit user > Select role > Save

3. View Reports:
   - Statistics > Department Dashboard
   - Configure filters > Generate Report

4. System Health Check:
   - System > System Status
   - Review metrics and logs

---

## Appendix D: Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-18 | Initial release of user manual |

---

## Document Information

**Document Title**: CT-Tibet-WMS User Manual
**Version**: 1.0
**Last Updated**: 2025-11-18
**Maintained By**: CT-Tibet-WMS Project Team
**Feedback**: Please send feedback and suggestions to wms-support@your-company.com

---

**End of User Manual**
