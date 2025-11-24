# CT-Tibet-WMS Frequently Asked Questions (FAQ)

**常见问题解答**

**Version**: 1.0
**Last Updated**: 2025-11-18

---

## Table of Contents

1. [General Questions](#1-general-questions)
2. [Login and Access](#2-login-and-access)
3. [Applications and Approvals](#3-applications-and-approvals)
4. [Inventory and Materials](#4-inventory-and-materials)
5. [Inbound and Outbound Operations](#5-inbound-and-outbound-operations)
6. [Notifications and Messages](#6-notifications-and-messages)
7. [User Management](#7-user-management)
8. [Reports and Statistics](#8-reports-and-statistics)
9. [Mobile App (WeChat Mini Program)](#9-mobile-app-wechat-mini-program)
10. [Technical Issues](#10-technical-issues)
11. [Security and Privacy](#11-security-and-privacy)
12. [Best Practices](#12-best-practices)
13. [Troubleshooting Guide](#13-troubleshooting-guide)
14. [Contact and Support](#14-contact-and-support)

---

## 1. General Questions

### Q1.1: What is CT-Tibet-WMS?

**A:** CT-Tibet-WMS (China Telecom Tibet Warehouse Management System) is a web-based system designed to manage warehouse operations for telecommunications equipment across multiple departmental warehouses. It handles material inventory, inbound/outbound operations, application workflows, and statistical reporting.

**Key Features**:
- Material requisition and approval workflow
- Real-time inventory tracking
- Multi-department warehouse management
- Role-based access control
- PC web portal and WeChat Mini Program

---

### Q1.2: Who can use the system?

**A:** The system supports four user roles:

1. **Regular Employees** (400+): Submit material requisition applications
2. **Warehouse Managers** (14): Approve requests, process inbound/outbound
3. **Department Administrators** (7): Manage department users and warehouses
4. **System Administrator** (1): Full system configuration and management

Each role has specific permissions tailored to their responsibilities.

---

### Q1.3: What browsers are supported?

**A:** Recommended browsers (latest versions):

✅ **Supported**:
- Google Chrome 90+
- Microsoft Edge 90+
- Firefox 88+
- Safari 14+ (macOS)

❌ **Not Supported**:
- Internet Explorer (any version)
- Opera Mini
- Outdated browser versions

**Tip**: For best experience, use Google Chrome and keep it updated.

---

### Q1.4: Can I use the system on mobile devices?

**A:** Yes! Two options:

1. **WeChat Mini Program** (Recommended for mobile):
   - Search "西藏电信仓库管理" in WeChat
   - Full mobile experience optimized for phones
   - Push notifications through WeChat
   - Best for employees and managers on the go

2. **Mobile Browser**:
   - Access web portal through mobile browser
   - Responsive design adapts to small screens
   - Some features may be limited on small screens

---

### Q1.5: Is the system available 24/7?

**A:** **Yes**, with planned maintenance windows:

- **Available**: 24 hours a day, 7 days a week
- **Planned Maintenance**: Typically Sundays 2:00 AM - 4:00 AM (announced in advance)
- **Uptime Target**: 99.5%

During maintenance, you'll see a maintenance notice page. Emergency maintenance is announced via email and system notifications.

---

### Q1.6: Is my data secure?

**A:** Yes. Security measures include:

- **Encrypted connections** (HTTPS/SSL)
- **Role-based access control** (RBAC)
- **Password encryption**
- **Session timeout** (2 hours inactivity)
- **Audit logs** (all actions tracked)
- **Daily backups**
- **Data isolation** (departments cannot see each other's data)

See [Security and Privacy](#11-security-and-privacy) section for more details.

---

## 2. Login and Access

### Q2.1: What are my login credentials?

**A:** Your credentials are provided by your department administrator:

- **Username**: Usually your employee ID (e.g., "EMP001")
- **Initial Password**: Temporary password (commonly "Change@123" or your employee ID)

**First Login**: You'll be required to change your password.

---

### Q2.2: I forgot my password. What should I do?

**A:** **Option 1** (Self-Service - If enabled):
1. Click "Forgot Password" on login page
2. Enter your username and registered email/phone
3. Receive password reset link
4. Follow link to create new password

**Option 2** (Contact Admin):
1. Contact your department administrator
2. They will reset your password to a temporary password
3. Login with temporary password
4. Change to your own password

**Note**: For security, administrators cannot see your actual password, only reset it.

---

### Q2.3: My account is locked. How do I unlock it?

**A:** Accounts are automatically locked after 5 failed login attempts within 15 minutes.

**Solution**:
- **Auto-unlock**: Wait 30 minutes, then try again
- **Manual unlock**: Contact your department administrator or IT support for immediate unlock

**Prevention**:
- Ensure Caps Lock is OFF
- Double-check username and password
- Use password manager to avoid typos

---

### Q2.4: Can I change my username?

**A:** **No**, usernames are permanent and tied to your employee ID.

However, you can update:
- Your display name (real name)
- Email address
- Phone number
- Profile picture (if feature is enabled)

To change these: Click your username → Personal Information → Edit

---

### Q2.5: Why does the system keep logging me out?

**A:** Common reasons:

1. **Session Timeout**: 2 hours of inactivity (security feature)
   - **Solution**: Refresh page periodically or save work frequently

2. **Multiple Login Locations**: Logging in from another device/browser logs out the previous session
   - **Solution**: Use one device at a time, or ask admin about concurrent session policy

3. **Browser Issues**: Cookies disabled or cleared
   - **Solution**: Enable cookies in browser settings

4. **System Maintenance**: Scheduled updates
   - **Solution**: Check system announcements, plan work accordingly

---

### Q2.6: Can multiple people share one account?

**A:** **No, absolutely not!** Each user must have their own account.

**Reasons**:
- **Security**: Shared accounts compromise security
- **Accountability**: Cannot track who performed which action
- **Audit Trail**: Required for compliance and investigations
- **Password Policy**: Violates company security policy

**Solution**: Request separate accounts for each person from department administrator.

---

### Q2.7: How do I change my password?

**A:**

1. Login to the system
2. Click your username in top-right corner
3. Select **Change Password**
4. Enter current password
5. Enter new password (8+ characters, mixed case, numbers)
6. Confirm new password
7. Click **Submit**

**Password Requirements**:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- Cannot reuse last 3 passwords
- Should change every 90 days

---

## 3. Applications and Approvals

### Q3.1: How do I submit a material requisition application?

**A:** **Step-by-step**:

1. Navigate to **Applications > My Applications**
2. Click **+ New Application**
3. Fill in application information:
   - Title (brief description)
   - Purpose (why you need materials)
   - Expected pickup date
4. Click **+ Add Material**
5. Search and select material
6. Enter quantity needed
7. Repeat for all materials needed
8. Click **Submit**

**Tip**: Check inventory first to ensure materials are available.

See [User Manual Section 5.4](USER_MANUAL.md#54-submitting-material-requisition) for detailed instructions.

---

### Q3.2: Can I save an application as draft and submit later?

**A:** **Yes!**

1. Fill in application form
2. Click **Save as Draft** instead of Submit
3. Application is saved with status "Draft"
4. Return later to **Applications > My Applications**
5. Click **Edit** on the draft
6. Make any changes
7. Click **Submit** when ready

**Notes**:
- Drafts do not require approval
- Drafts do not reserve inventory
- No time limit on drafts
- Can delete drafts without approval

---

### Q3.3: Can I edit or cancel an application after submission?

**A:** **Depends on status**:

| Status | Can Edit? | Can Cancel? |
|--------|-----------|-------------|
| Draft | ✅ Yes | ✅ Yes (Delete) |
| Pending Approval | ❌ No | ✅ Yes |
| Approved | ❌ No | ⚠️ Contact manager |
| Rejected | ❌ No | N/A (already rejected) |
| Completed | ❌ No | ❌ No |

**To Cancel Pending Application**:
1. Go to **Applications > My Applications**
2. Find the application
3. Click **Cancel** button
4. Confirm cancellation

**To Cancel Approved Application**:
- Contact warehouse manager directly
- If you don't pickup within 7 days, it auto-cancels

---

### Q3.4: How long does approval take?

**A:** **Target SLA**: 24 hours

**Typical Timeline**:
- **Urgent requests**: 1-4 hours
- **Normal requests**: 4-24 hours
- **Complex requests**: Up to 24 hours

**Factors affecting approval time**:
- Manager workload
- Time of day (submitted late evening = next morning approval)
- Inventory availability (may need to check stock)
- Clarity of request (unclear purpose = delayed approval)

**If delayed beyond 24 hours**:
- System sends reminder notification to manager
- You can contact manager directly (phone/WeChat)

---

### Q3.5: Why was my application rejected?

**A:** Common rejection reasons:

1. **Insufficient Inventory**: Requested quantity exceeds available stock
   - **Solution**: Reduce quantity or wait for restock, resubmit

2. **Purpose Not Justified**: Unclear why materials are needed
   - **Solution**: Provide more detailed explanation, resubmit

3. **Wrong Materials**: Selected incorrect materials for stated purpose
   - **Solution**: Review materials, select correct ones, resubmit

4. **Duplicate Request**: Similar application already approved recently
   - **Solution**: Check existing approvals, coordinate with manager

5. **Other**: Manager provides specific reason in rejection note
   - **Solution**: Read rejection reason, address issue, resubmit

**Next Steps**:
- Review rejection reason in application details
- Contact manager for clarification if needed
- Submit corrected application

---

### Q3.6: Can I request materials for someone else?

**A:** **Generally No**, unless:

1. You're submitting on behalf of your team/project
2. You clearly state in the "Purpose" field who will actually use the materials
3. You will be the one picking up the materials

**Reason**: Applications are tied to your account for accountability and audit purposes.

**Better Approach**:
- Have each person submit their own application
- Or request materials in your name for team use

---

### Q3.7: What happens if I don't pickup approved materials within 7 days?

**A:** **Auto-cancellation**:

After 7 days from approval:
1. System automatically cancels the outbound order
2. Application status changes to "Cancelled"
3. Inventory is NOT deducted (remains available for others)
4. You receive cancellation notification
5. You must submit a new application if you still need the materials

**Reason**: Prevents inventory from being "reserved" indefinitely when not actually needed.

**To Avoid**:
- Pickup materials promptly after approval
- If you cannot pickup on time, contact warehouse manager to extend deadline
- Cancel application yourself if you no longer need materials

---

### Q3.8: Can I see who approved my application?

**A:** **Yes**:

1. Open application details
2. View **Approval History** section
3. Shows:
   - Approver name
   - Approval date and time
   - Decision (approved/rejected)
   - Comments/notes from approver

**Audit Trail**: All approval actions are logged and traceable.

---

### Q3.9: How do I know when my application is approved?

**A:** You'll receive notifications through:

1. **System Notification**: Bell icon in top-right corner shows badge
2. **WeChat Message**: Template message (if WeChat Mini Program is bound)
3. **Email Notification**: (if email notifications are enabled)

**Check Status Manually**:
- Navigate to **Applications > My Applications**
- View **Status** column
- Green checkmark = Approved

---

### Q3.10: Can I apply for materials from another department's warehouse?

**A:** **No**.

- You can only apply for materials from warehouses in **your own department**
- This is by design for departmental autonomy and accountability
- Each department manages its own inventory

**If You Need Materials from Another Department**:
- Coordinate with your manager
- Your department may need to purchase or request transfer
- Not handled through regular application process

---

## 4. Inventory and Materials

### Q4.1: How do I check if materials are in stock before applying?

**A:**

1. Navigate to **Inventory > Inventory Query**
2. Browse or search for the material
3. Check **Current Stock** column
4. Color coding:
   - 🟢 **Green**: Normal stock (safe to apply)
   - 🟡 **Yellow**: Low stock (apply for small quantities only)
   - 🔴 **Red**: Very low or out of stock (likely to be rejected)

**Tip**: Always check inventory before submitting application to avoid rejections.

---

### Q4.2: Why does the inventory quantity differ from what warehouse manager told me?

**A:** **Possible reasons**:

1. **Timing**: Inventory updates in real-time; quantity changed between your conversation and check
2. **Pending Pickups**: Approved applications reserve inventory (shown separately)
3. **Data Refresh**: Browser cache; try refreshing page (F5)
4. **Different Warehouse**: You're viewing a different warehouse than discussed
5. **Counting Method**: Manager counted differently (e.g., partial boxes)

**Solution**:
- Refresh page to get latest data
- Verify you're viewing correct warehouse
- Contact manager to reconcile discrepancy

---

### Q4.3: Can I see inventory from other departments?

**A:** **No**, for security and privacy:

- **Regular Employees**: Can only see their own department's inventory
- **Warehouse Managers**: Can only see their own department's inventory
- **Department Admins**: Can only see their own department's inventory
- **System Admin**: Can see all departments' inventory

**Reason**: Departmental data isolation and confidentiality.

---

### Q4.4: What does "Available Quantity" vs "Current Stock" mean?

**A:**

| Term | Definition | Example |
|------|------------|---------|
| **Current Stock** | Total physical quantity in warehouse | 100 units |
| **Reserved/Allocated** | Quantity in approved but not picked up applications | 20 units |
| **Available Quantity** | Current Stock - Reserved = Actually available for new applications | 80 units |

**When Applying**:
- Check **Available Quantity** (not just Current Stock)
- Available quantity is what you can actually get

**Note**: Some systems show both, some show only one. Check with your admin.

---

### Q4.5: Can I request materials that show 0 in stock?

**A:** **You can submit**, but:

- Application will likely be **rejected** due to insufficient inventory
- Or manager may **partially approve** (approve when stock arrives)

**Better Approach**:
1. Check inventory first
2. If out of stock, contact warehouse manager to ask about restocking timeline
3. Submit application when stock is available
4. Or submit with note: "Please approve when stock arrives"

---

### Q4.6: How often is inventory updated?

**A:** **Real-time**:

- Inventory updates **immediately** when:
  - Inbound operation is submitted
  - Outbound operation is submitted (direct)
  - Pickup is confirmed (application-based outbound)
  - Adjustment is made by admin

- **No delay** between operation and inventory update
- Refresh page to see latest data

---

### Q4.7: Who can add new materials to the catalog?

**A:**

- **System Administrator**: Can add new materials globally
- **Department Admin**: May have permission (depends on configuration)
- **Warehouse Managers**: Cannot add materials (can only use existing catalog)
- **Regular Employees**: Cannot add materials

**If You Need a New Material**:
1. Contact your department administrator or system administrator
2. Provide material details: name, specifications, unit, category
3. Admin will add to catalog
4. Then you can apply for it

---

### Q4.8: What if I find a material with incorrect information?

**A:** **Report it**:

1. Note the material code and what's incorrect
2. Contact:
   - Your warehouse manager (for minor issues)
   - Department administrator (for significant issues)
   - System administrator (for global catalog changes)
3. Provide correct information
4. Admin will update material master data

**Examples of Issues**:
- Wrong specifications
- Incorrect unit (should be "meters" not "pieces")
- Typos in name
- Wrong category
- Outdated brand/model

---

## 5. Inbound and Outbound Operations

### Q5.1: What's the difference between "Direct Outbound" and "Application-Based Outbound"?

**A:**

| Feature | Direct Outbound | Application-Based Outbound |
|---------|----------------|---------------------------|
| **Who Creates** | Warehouse manager manually | System automatically (after approval) |
| **Approval Needed** | No | Yes (employee application) |
| **Inventory Deduction** | Immediate (upon creation) | Delayed (upon pickup confirmation) |
| **Use Case** | Urgent issues, manager's own use | Regular employee requisitions |
| **Who Can Use** | Warehouse managers only | Anyone (via application) |

**When to Use Each**:
- **Direct Outbound**: Manager needs materials urgently, no time for application process
- **Application-Based**: Normal employee requisition workflow

---

### Q5.2: As a warehouse manager, when should I use Direct Outbound vs approve an application?

**A:** **Use Direct Outbound when**:

- Emergency/urgent need with no time for application process
- You personally need materials for warehouse operations
- Pre-approved offline requisition (e.g., from upper management)
- Special circumstances with proper documentation

**Use Application-Based (Normal Process) when**:
- Regular employee requisitions
- Planned material needs
- Need audit trail and employee accountability
- Following standard approval workflow

**Important**: Direct outbound bypasses approval workflow, so use responsibly. All actions are logged.

---

### Q5.3: Can I reverse an inbound or outbound operation?

**A:** **Generally No**, once submitted:

**Inbound**: Cannot delete or reverse (inventory already increased)

**Outbound**:
- **Before pickup confirmation**: May be cancelled (contact admin)
- **After pickup confirmation**: Cannot reverse (inventory already deducted)

**If You Made a Mistake**:
1. **Immediately** contact system administrator
2. Explain the error
3. Admin may be able to:
   - Void the operation (mark as error)
   - Create adjustment entry
   - Manually correct inventory

**Prevention**: Double-check all quantities and materials before submitting!

---

### Q5.4: Why can't I create an outbound? I'm a warehouse manager.

**A:** **Check these**:

1. **Insufficient Inventory**: Cannot issue more than available stock
   - **Solution**: Check inventory, reduce quantity

2. **Wrong Warehouse Selected**: You're not assigned to this warehouse
   - **Solution**: Select your assigned warehouse

3. **Permission Issue**: Your role is not correctly configured
   - **Solution**: Contact department admin to verify role assignment

4. **System Maintenance**: Feature temporarily disabled
   - **Solution**: Check system announcements

5. **Browser Issue**: Form not loading correctly
   - **Solution**: Refresh page, try different browser

---

### Q5.5: What information do I need when creating an inbound?

**A:** **Required Information**:

1. **Warehouse**: Which warehouse is receiving (usually auto-selected for you)
2. **Inbound Type**: Purchase, Return, Transfer, or Other
3. **Source/Supplier**: Where materials are coming from
4. **Inbound Date**: When materials were received (defaults to today)
5. **Material List**:
   - Which materials
   - Quantities received
   - Optional: Unit price, batch number, remarks

**Optional but Recommended**:
- Purchase order number
- Supplier invoice number
- Quality inspection notes
- Photos of received materials
- Delivery receipt number

**Tip**: Keep physical delivery documents for reference.

---

### Q5.6: Can I create inbound for materials not in the catalog?

**A:** **No**. You must:

1. First request system admin to add new material to catalog
2. Then create inbound for that material

**Workaround** (Temporary):
- Use "Other Materials" or generic category (if available)
- Add detailed notes about actual material
- Request proper material to be added later
- Create adjustment when proper material is added

**Not Recommended**: Using wrong material code for convenience (damages data integrity).

---

### Q5.7: How do I confirm material pickup when employee arrives?

**A:** **Step-by-step**:

1. Employee arrives and provides application number or employee ID
2. Navigate to **Outbound > Pickup Confirmation**
3. Find the pending pickup (search by application number or employee name)
4. Click on the order to open details
5. **Verify**:
   - Employee identity (check ID badge)
   - Materials match the order
   - Quantities are correct
6. **Physically hand over materials**
7. Get employee to sign pickup form (if using paper forms)
8. In system: Click **Confirm Pickup** button
9. System deducts inventory and marks order complete

**Important**: Only confirm after physical handover! Cannot easily undo.

---

### Q5.8: What if employee pickup quantity is different from approved quantity?

**A:** **Scenarios**:

**Scenario 1: Employee wants less than approved**:
- Partial pickup is allowed (if system supports)
- Confirm actual quantity given
- Remaining quantity stays in inventory

**Scenario 2: Employee wants more than approved**:
- Cannot issue more than approved
- Employee must submit new application for additional quantity
- Or contact manager to adjust approval (may require re-approval)

**Best Practice**:
- Give exactly what was approved
- Any changes require manager decision and documentation

---

## 6. Notifications and Messages

### Q6.1: How do I know when I have new notifications?

**A:** Multiple indicators:

1. **Bell Icon Badge**: Red badge with number in top-right corner
2. **WeChat Message**: Push notification (if Mini Program is bound)
3. **Email**: Notification email (if email notifications enabled)
4. **Dashboard Widget**: "Unread Messages" count on dashboard

**To View Notifications**:
- Click bell icon in top navigation
- Or navigate to **Messages > Message List**

---

### Q6.2: What types of notifications will I receive?

**A:** **By Role**:

**Regular Employee**:
- Application status changes (approved, rejected)
- Pickup reminders
- System announcements
- Inventory availability (for watched materials)

**Warehouse Manager**:
- New applications requiring approval
- Approval timeout warnings (24+ hours pending)
- Low stock alerts
- Pending pickups
- Overdue pickups (approaching 7-day limit)

**Department/System Admin**:
- User account activities
- System alerts and errors
- Backup status
- Performance warnings

---

### Q6.3: Can I disable certain notifications?

**A:** **Yes**:

1. Navigate to **Profile > Notification Settings**
2. Toggle on/off specific notification types:
   - Application updates
   - Approval requests
   - Low stock alerts
   - System announcements
3. Choose delivery method:
   - In-app only
   - In-app + WeChat
   - In-app + Email
   - All channels
4. Set notification frequency:
   - Real-time
   - Daily digest
   - Weekly summary
5. Click **Save**

**Recommendation**: Keep critical notifications enabled (application updates, approval requests).

---

### Q6.4: Why am I not receiving WeChat notifications?

**A:** **Troubleshooting**:

1. **WeChat Mini Program Not Bound**:
   - Open WeChat Mini Program
   - Login with same credentials as PC
   - Authorize notifications

2. **Notifications Disabled in WeChat**:
   - WeChat Settings > Notifications > [App Name]
   - Enable notifications

3. **WeChat Integration Not Configured**:
   - Contact system admin
   - WeChat template message API may not be set up

4. **Wrong Phone Number**:
   - Verify phone number in your profile matches WeChat account
   - Update if necessary

5. **WeChat Privacy Settings**:
   - Check WeChat privacy settings
   - Ensure app can send notifications

---

### Q6.5: How long are notifications kept in the system?

**A:** **Retention Policy** (typical):

- **Unread Notifications**: Kept indefinitely until you read them
- **Read Notifications**: Kept for 90 days
- **System Announcements**: Kept for 1 year
- **Critical Alerts**: Kept for 3 years

**Archiving**: Old notifications are automatically archived and removed from your active list but remain in system logs.

**Tip**: Export important notifications for your records before they're archived.

---

### Q6.6: Can I send messages to other users through the system?

**A:** **Depends on system configuration**:

**If Messaging Feature is Enabled**:
- Navigate to **Messages > Send Message**
- Select recipient
- Type message
- Send

**If Not Enabled**:
- System only sends automated notifications
- Cannot send peer-to-peer messages
- Use WeChat, email, or phone for communication

**Current Status**: Check if "Send Message" option exists in Messages menu.

---

## 7. User Management

### Q7.1: How do I add a new user? (Department/System Admin)

**A:**

1. Navigate to **System > User Management**
2. Click **+ Add User**
3. Fill in required fields:
   - Username (unique, usually employee ID)
   - Real name
   - Employee ID
   - Department
   - Role (Regular Employee, Warehouse Manager, etc.)
   - Mobile phone
   - Email
   - Initial password
4. If Warehouse Manager role: Assign warehouse(s)
5. Click **Submit**
6. Securely share credentials with the new user

**Best Practice**: Use standardized username format (e.g., all employee IDs).

---

### Q7.2: How do I deactivate a user who left the company?

**A:** **Do NOT delete**. Instead, disable:

1. Navigate to **System > User Management**
2. Find the user
3. Click **Disable** or **Deactivate** button
4. Confirm action

**Result**:
- User cannot login
- Historical data remains intact (for audit)
- Can be reactivated if needed (e.g., rehire)

**Never Delete Users**: Deleting removes audit trail and breaks data integrity.

---

### Q7.3: Can I change a user's role?

**A:** **Yes** (if you have permission):

1. Navigate to **System > User Management**
2. Find the user
3. Click **Edit**
4. Change **Role** dropdown
5. If changing to/from Warehouse Manager: Update warehouse assignments
6. Click **Save**

**Effect**:
- User's permissions change immediately
- May need to logout and login again to see new menus
- Existing applications/operations remain valid

---

### Q7.4: What roles can a Department Admin assign?

**A:**

**Department Admin Can Assign** (within own department):
- Regular Employee
- Warehouse Manager

**Department Admin Cannot Assign**:
- Department Administrator (only System Admin can assign)
- System Administrator (only System Admin can assign)
- Roles outside their department

**To Create Department Admin**: Contact System Administrator.

---

### Q7.5: Can one person have multiple roles?

**A:** **No**, one user = one role.

**If Someone Needs Multiple Capabilities**:
- Assign the higher-privilege role
- Example: Warehouse Manager can do everything Regular Employee can do, plus more
- Example: Department Admin can do everything Warehouse Manager can do, plus more

**Role Hierarchy**:
```
System Admin (highest privileges)
  └── Department Admin
      └── Warehouse Manager
          └── Regular Employee (basic privileges)
```

---

### Q7.6: Can users belong to multiple departments?

**A:** **No**, one user = one department.

**If Employee Works in Multiple Departments**:
- **Option 1**: Assign to primary department, coordinate cross-department access manually
- **Option 2**: Create separate accounts (not recommended; complicates audit trail)
- **Option 3**: Request System Admin to configure special permissions (if critical need)

**Most Common**: Assign to primary/home department.

---

## 8. Reports and Statistics

### Q8.1: What reports are available?

**A:** **Standard Reports**:

1. **Inbound Statistics**: Volumes, trends, by supplier
2. **Outbound Statistics**: Volumes, trends, by recipient
3. **Inventory Status**: Current stock levels, valuation
4. **Material Usage**: Top materials, consumption patterns
5. **Application Summary**: Approval rates, average time, by user
6. **Low Stock Report**: Materials below minimum levels
7. **Department Performance**: Comparative metrics across departments (admins only)
8. **User Activity**: Login frequency, operation counts (admins only)

**Access**: Navigate to **Statistics** menu.

---

### Q8.2: Can I export reports to Excel?

**A:** **Yes**:

1. Navigate to desired report
2. Configure filters and date range
3. Click **Generate Report** (if needed)
4. Click **Export** or **Download** button
5. Choose format: Excel (.xlsx) or CSV
6. Download file to your computer

**Uses**:
- Further analysis in Excel
- Presentations
- Record keeping
- Budget planning

---

### Q8.3: Can I schedule automatic reports?

**A:** **If Available** (depends on system configuration):

1. Navigate to **Statistics > Scheduled Reports**
2. Click **Create Schedule**
3. Configure:
   - Report type
   - Frequency (daily, weekly, monthly)
   - Recipients (email addresses)
   - Format (Excel, PDF)
   - Delivery time
4. Save schedule

**System will automatically generate and email reports**.

**If Not Available**: Manually generate reports as needed.

---

### Q8.4: Why do report numbers differ from what I see on the screen?

**A:** **Common reasons**:

1. **Data Refresh Timing**: Reports show data at time of generation; screen shows real-time
   - **Solution**: Refresh report or screen

2. **Filter Differences**: Report and screen have different filters applied
   - **Solution**: Check filters match

3. **Date Range**: Report uses different date range than screen view
   - **Solution**: Verify date ranges match

4. **Permissions**: Report shows only data you have access to
   - **Solution**: Verify you're viewing correct department/warehouse

5. **Cache**: Browser cached old data
   - **Solution**: Clear cache, refresh page

---

### Q8.5: Can I create custom reports?

**A:** **Depends on role and system features**:

**If Custom Report Builder is Available**:
1. Navigate to **Statistics > Custom Reports**
2. Click **Create Custom Report**
3. Select data sources and fields
4. Configure filters and groupings
5. Preview report
6. Save template for reuse

**If Not Available**:
- Export raw data to Excel
- Create your own analysis and charts
- Request feature from system admin

**System Admin**: May have access to database query tools for advanced custom reports.

---

### Q8.6: How far back does historical data go?

**A:** **Typical Retention**:

- **Active Data**: All historical transactions (inbound, outbound, applications) are kept indefinitely
- **Statistical Summaries**: Aggregated monthly data kept for 5+ years
- **Audit Logs**: 3-7 years (depends on compliance requirements)
- **Notifications**: 90 days (read), 1 year (announcements)

**Archived Data**: Very old data may be moved to archive storage (slower access, but retrievable).

**Check with Admin**: Confirm your organization's data retention policy.

---

## 9. Mobile App (WeChat Mini Program)

### Q9.1: How do I access the WeChat Mini Program?

**A:**

**Method 1: Search**:
1. Open WeChat
2. Pull down to reveal search bar
3. Search for "西藏电信仓库管理"
4. Click on the Mini Program

**Method 2: Scan QR Code**:
1. Get QR code from IT department or system admin
2. Open WeChat scanner
3. Scan QR code
4. Opens Mini Program

**First Time**:
- Authorize WeChat to access your profile
- Login with same username/password as PC
- Bind your account

---

### Q9.2: Do I need to install anything?

**A:** **No installation needed**:

- Mini Programs run inside WeChat (no separate app)
- No download from App Store or Google Play
- Automatically updates when WeChat updates
- Works on both iOS and Android

**Requirement**: WeChat app version 7.0 or higher.

---

### Q9.3: Can I do everything on mobile that I can do on PC?

**A:** **Most features, with some limitations**:

✅ **Available on Mobile**:
- Submit applications
- View application status
- Approve applications (managers)
- Confirm pickups (managers)
- View inventory
- Check notifications
- View reports (simplified)
- Update profile

❌ **PC Only** (typically):
- Complex reports and data export
- User management (admins)
- System configuration (admins)
- Bulk operations
- Detailed statistical analysis

**Best Practice**:
- Use mobile for quick actions and approvals
- Use PC for detailed work and administration

---

### Q9.4: Are mobile and PC data synced?

**A:** **Yes, real-time sync**:

- Both access the same database
- Changes on mobile appear on PC immediately (and vice versa)
- Submit application on mobile → View on PC instantly
- Approve on mobile → Employee sees on PC instantly

**No separate accounts**: Same login credentials for both.

---

### Q9.5: Why is the mobile app asking for so many permissions?

**A:** **Common Permission Requests and Why**:

- **Storage**: To save downloaded files and photos
- **Camera**: To scan QR codes (for material codes or quick actions)
- **Location** (optional): For warehouse geofencing or location-based features
- **Notifications**: To send push notifications

**Privacy**:
- Permissions can be denied; some features may not work
- App does not access your WeChat contacts or messages
- Location data (if collected) is not shared

**Recommendation**: Allow notifications for best experience; others are optional.

---

### Q9.6: Can I use the mobile app offline?

**A:** **Limited offline capability**:

✅ **Offline**:
- View previously loaded data (cached)
- Browse recently viewed applications

❌ **Requires Internet**:
- Submit applications
- Real-time inventory check
- Approve applications
- Sync data

**Tip**: Most warehouse operations require connectivity. Ensure good Wi-Fi or cellular signal.

---

## 10. Technical Issues

### Q10.1: The page is not loading or very slow. What should I do?

**A:** **Troubleshooting steps**:

1. **Check Internet Connection**:
   - Test other websites
   - Try pinging server (if technical)

2. **Refresh Page**: Press F5 or Ctrl+R

3. **Clear Browser Cache**:
   - Chrome: Ctrl+Shift+Delete
   - Select "Cached images and files"
   - Clear data

4. **Try Incognito/Private Mode**:
   - Rules out browser extension conflicts

5. **Try Different Browser**:
   - Chrome recommended

6. **Check System Status**:
   - Navigate to **System > System Status** (if accessible)
   - Or check with IT department

7. **Contact IT Support**: If issue persists

---

### Q10.2: I'm getting "Error 500" or "Server Error". What does this mean?

**A:** **Error 500 = Internal Server Error**

**Meaning**: Something went wrong on the server side (not your fault).

**What to Do**:

1. **Wait 1-2 minutes**, then refresh page (may be temporary glitch)
2. **Try different page**: If other pages work, it's an isolated issue
3. **Note what you were doing**: Important for IT to diagnose
4. **Take screenshot** of error message
5. **Report to IT Support**:
   - What you were trying to do
   - Screenshot of error
   - Time of error
   - Your username (don't send password)

**Do NOT**:
- Repeatedly retry (may worsen issue)
- Try to "fix" it yourself (unless you're IT staff)

---

### Q10.3: I accidentally closed the browser. Is my work saved?

**A:** **Depends on what you were doing**:

✅ **Auto-Saved**:
- Submitted applications
- Submitted inbound/outbound operations
- Approved/rejected applications
- Any action where you clicked "Submit" or "Save"

❌ **Lost**:
- Partially filled forms (not saved as draft)
- Filters and search criteria
- Unsaved draft edits

**Prevention**:
- Click **Save as Draft** periodically when filling long forms
- Complete and submit forms in one session
- Don't rely on browser "restore session"

---

### Q10.4: I can see a page but buttons don't work. What's wrong?

**A:** **Possible causes**:

1. **JavaScript Disabled**:
   - Enable JavaScript in browser settings
   - Required for modern web apps

2. **Browser Compatibility**:
   - Update browser to latest version
   - Use supported browser (Chrome, Edge, Firefox)

3. **Extension Conflict**:
   - Try incognito mode
   - Disable ad blockers or script blockers

4. **Slow Connection**:
   - Page partially loaded
   - Wait for full load, or refresh

5. **Session Expired**:
   - Logout and login again

**Quick Test**: Open browser console (F12), check for red errors.

---

### Q10.5: The export/download file is empty or corrupted. Why?

**A:** **Possible reasons and solutions**:

1. **No Data to Export**:
   - Filters too restrictive
   - **Solution**: Broaden filters, check data exists

2. **Export Too Large**:
   - System timeout for very large exports (10,000+ rows)
   - **Solution**: Export smaller date ranges, or contact admin for database export

3. **Pop-up Blocker**:
   - Browser blocked download
   - **Solution**: Allow pop-ups for this site

4. **Browser Issue**:
   - **Solution**: Try different browser

5. **File Format Issue**:
   - **Solution**: Try CSV instead of Excel, or vice versa

6. **Incomplete Download**:
   - **Solution**: Don't close browser until download completes

---

### Q10.6: Can I use the system on a tablet?

**A:** **Yes**, with considerations:

✅ **Works On**:
- iPad (iOS)
- Android tablets
- Surface and other Windows tablets

**Experience**:
- **Mobile Browser**: Access PC web portal (responsive design adapts)
- **WeChat Mini Program**: Best on phones, but works on tablets

**Limitations**:
- Some complex screens better on larger screens
- Touch keyboard may be cumbersome for heavy data entry
- Excel exports easier to handle on PC

**Recommendation**: Tablets good for quick checks and approvals; PC better for intensive work.

---

## 11. Security and Privacy

### Q11.1: Is my password secure?

**A:** **System Security Measures**:

- Passwords are **encrypted** (hashed with salt)
- **Not visible** to anyone, including admins
- Stored securely in database
- Transmitted over **HTTPS** (encrypted connection)

**Your Responsibility**:
- Choose **strong passwords**
- **Don't share** with anyone
- **Change regularly** (every 90 days recommended)
- Don't reuse passwords from other systems

---

### Q11.2: Can admins see my password?

**A:** **No, absolutely not**.

- Passwords are **one-way encrypted** (hashed)
- Even system administrators cannot view your password
- Admins can only **reset** password to a temporary password
- You must change temporary password on next login

**If someone claims to know your password**: They don't. Change it immediately.

---

### Q11.3: Who can see my applications and activities?

**A:** **Visibility Rules**:

**Your Applications**:
- **You**: Can see all your own applications
- **Warehouse Managers** (your department): Can see applications you submitted
- **Department Admin** (your department): Can see applications you submitted
- **System Admin**: Can see all applications (system-wide)

**Other Users' Applications**:
- You **cannot** see other users' applications (unless you're a manager/admin)

**Activities and Audit Logs**:
- **System Admin**: Can view all user activities (for security and compliance)
- **Department Admin**: Can view activities within their department

**Privacy**: Data access is strictly controlled by role and department.

---

### Q11.4: Is my personal information safe?

**A:** **Data Protection**:

✅ **Security Measures**:
- Role-based access control
- Encrypted data transmission (HTTPS)
- Secure database with access controls
- Regular security audits
- Daily backups (stored securely)

❌ **What System Does NOT Do**:
- Share your data with third parties
- Use your data for marketing
- Sell your information

**Personal Data Collected**:
- Name, employee ID, department
- Work phone number and email
- Usage data (for audit and analytics)

**Your Rights**:
- View your personal data
- Request corrections
- Request account deletion (subject to audit retention requirements)

---

### Q11.5: What happens if I suspect unauthorized access to my account?

**A:** **Immediate Actions**:

1. **Change Password Immediately**:
   - Login and change password
   - If cannot login, contact admin to reset

2. **Review Recent Activity**:
   - Check recent applications
   - Check recent logins (if feature available)

3. **Report to IT Security**:
   - Contact system administrator
   - Explain what you observed
   - Admin can review audit logs

4. **Monitor for Unusual Activity**:
   - Watch for applications you didn't submit
   - Check for approval actions you didn't make

**Prevention**:
- Don't share passwords
- Logout from shared computers
- Don't write down passwords
- Be wary of phishing emails

---

### Q11.6: How long are audit logs kept?

**A:** **Retention Policy** (typical):

- **User Actions**: All logins, operations, approvals: **3-7 years**
- **System Logs**: Errors, warnings: **1 year**
- **Backup Logs**: **90 days**
- **Security Logs**: Failed logins, permission changes: **5 years**

**Purpose**:
- Compliance with regulations
- Forensic investigation if needed
- Dispute resolution

**Access**: Only System Admin can access detailed audit logs.

---

## 12. Best Practices

### Q12.1: What are best practices for submitting applications?

**A:**

1. **Check Inventory First**: Avoid rejections by ensuring stock availability
2. **Be Specific in Purpose**: Clear justification speeds up approval
3. **Accurate Quantities**: Don't overestimate; return unused materials
4. **Plan Ahead**: Submit 1-2 days before needed (not last minute)
5. **Complete Information**: Fill all fields thoroughly
6. **Attach Documentation**: Add supporting documents if needed (e.g., project approval)
7. **Track Status**: Check status regularly, don't assume approval
8. **Pickup Promptly**: Collect materials quickly after approval

---

### Q12.2: How can warehouse managers approve applications faster?

**A:**

1. **Set Aside Dedicated Time**: Review applications at specific times (e.g., 10 AM, 3 PM)
2. **Batch Processing**: Approve multiple applications in one session
3. **Check Inventory First**: Before opening application, confirm stock availability
4. **Clear Communication**: If rejecting, provide specific reason to avoid back-and-forth
5. **Use Mobile App**: Approve on the go, don't wait until back at desk
6. **Prioritize Urgent**: Check for urgent applications first
7. **Delegate**: If multiple managers, divide workload
8. **Set Expectations**: Communicate approval timeline to employees

**Target**: Approve within 4 hours (SLA: 24 hours).

---

### Q12.3: What are inventory management best practices?

**A:**

1. **Regular Physical Counts**:
   - Spot checks weekly
   - Full inventory monthly
   - Reconcile discrepancies immediately

2. **Maintain Minimum Levels**:
   - Set appropriate min/max levels
   - Act on low stock warnings promptly
   - Communicate with procurement team

3. **First-In-First-Out (FIFO)**:
   - Issue older stock first
   - Prevent material expiry or obsolescence

4. **Organize Warehouse**:
   - Clearly label shelves and bins
   - Group similar materials
   - Keep high-turnover items accessible

5. **Accurate Data Entry**:
   - Double-check quantities before submitting
   - Record inbound immediately upon receipt
   - Confirm outbound upon pickup

6. **Monitor Trends**:
   - Review usage statistics monthly
   - Identify patterns and seasonal variations
   - Adjust stock levels accordingly

---

### Q12.4: How should admins manage users effectively?

**A:**

1. **Standardize Usernames**: Use consistent format (e.g., all employee IDs)
2. **Timely Onboarding**: Create accounts before employee start date
3. **Proper Offboarding**: Disable accounts when employees leave (don't delete)
4. **Regular Audits**: Review user list quarterly
   - Remove/disable inactive accounts
   - Verify role assignments
   - Check for orphaned accounts
5. **Security Reviews**: Monitor for:
   - Users who never changed initial password
   - Accounts with no recent login (90+ days)
   - Shared accounts (not allowed)
6. **Documentation**: Maintain records of role assignments and changes
7. **Training**: Ensure all users receive proper training

---

### Q12.5: What should I do to keep my account secure?

**A:**

1. **Strong Password**:
   - 8+ characters
   - Mix of upper, lower, numbers, symbols
   - Not easily guessable (not your name or birthday)
   - Different from other system passwords

2. **Change Regularly**: Every 90 days

3. **Never Share**: Don't give password to anyone, including IT (they won't ask)

4. **Logout**: Especially on shared computers

5. **Beware of Phishing**:
   - Don't click suspicious links in emails
   - System will never ask for password via email
   - Verify sender before providing information

6. **Lock Screen**: When away from desk

7. **Report Suspicious Activity**: Immediately report unusual account behavior

---

## 13. Troubleshooting Guide

### Q13.1: Common Error Messages and Solutions

**Error: "Session Timeout - Please login again"**

**Cause**: Inactive for 2+ hours

**Solution**: Login again. Save work frequently to prevent loss.

---

**Error: "Insufficient Permissions"**

**Cause**: Trying to access feature your role doesn't allow

**Solution**:
- Verify you're logged in with correct account
- Contact admin if you believe you should have access
- Check if you're accessing your own department's data

---

**Error: "Insufficient Inventory"**

**Cause**: Requested quantity exceeds available stock

**Solution**:
- Check current inventory
- Reduce requested quantity
- Contact warehouse manager about restocking timeline

---

**Error: "Duplicate Entry"**

**Cause**: Trying to create record with duplicate unique identifier (e.g., material code)

**Solution**:
- Use different identifier
- Or edit existing record instead of creating new one

---

**Error: "Operation Failed - Please Try Again"**

**Cause**: Temporary system glitch or network issue

**Solution**:
- Wait 1-2 minutes
- Refresh page
- Try operation again
- If persists, contact IT support

---

### Q13.2: Performance Optimization Tips

**For Slow System Performance**:

1. **Browser Optimization**:
   - Clear cache and cookies weekly
   - Close unnecessary tabs (each tab uses memory)
   - Update browser to latest version
   - Use Chrome or Edge (better JavaScript performance)

2. **Network Optimization**:
   - Use wired Ethernet (faster than Wi-Fi)
   - Close bandwidth-heavy applications (streaming video, large downloads)
   - Check if others on network are using heavy bandwidth

3. **System Optimization**:
   - Work during off-peak hours if possible (early morning, late afternoon)
   - Export smaller data sets (use date ranges)
   - Avoid loading very long lists (use filters and pagination)

4. **Report Issues**:
   - If system is consistently slow, report to IT
   - May indicate server capacity issues

---

### Q13.3: What to do if data seems incorrect?

**Step 1: Verify Your View**

- Refresh page (F5)
- Check filters and date ranges
- Verify correct warehouse/department selected
- Clear browser cache and retry

**Step 2: Cross-Reference**

- Check same data on different page
- View on mobile app
- Ask colleague to check

**Step 3: Review Recent Transactions**

- Check transaction log
- Look for recent operations that might explain discrepancy
- Verify no one else modified data

**Step 4: Report Issue**

If data is definitely wrong:
- Take screenshot
- Note exact discrepancy (expected vs actual)
- Document steps to reproduce
- Report to system admin with details

**Do NOT**:
- Modify data yourself to "fix" it (may worsen issue)
- Share suspicion widely before confirming (may cause confusion)

---

## 14. Contact and Support

### Q14.1: Who do I contact for different types of issues?

**A:**

| Issue Type | Contact | Method |
|------------|---------|--------|
| **Login problems** | Department Administrator | Phone/Email |
| **Forgot password** | Department Administrator | Phone/Email |
| **Application questions** | Warehouse Manager | Phone/WeChat |
| **Inventory questions** | Warehouse Manager | Phone/WeChat |
| **Permission/access issues** | Department Administrator | Phone/Email |
| **Technical errors** | IT Help Desk | Phone: [Number]<br>Email: it-support@company.com |
| **System outage** | IT Help Desk | Emergency: [24/7 Number] |
| **New features/training** | Department Administrator | Email/In-person |
| **Data issues** | System Administrator | Email: sysadmin@company.com |

---

### Q14.2: What information should I provide when reporting an issue?

**A:** **Essential Information**:

1. **Your Details**:
   - Username (not password!)
   - Department and role
   - Contact information

2. **Issue Description**:
   - What you were trying to do
   - What actually happened
   - Error message (exact text or screenshot)

3. **Context**:
   - Date and time of issue
   - Browser and version (e.g., "Chrome 120")
   - Operating System (Windows, macOS, etc.)
   - Steps to reproduce issue

4. **Impact**:
   - How urgent is this?
   - Is it blocking your work?
   - Does it affect others?

**Attach**:
- Screenshots of error
- Relevant application/transaction numbers

**Example Good Report**:
```
Subject: Error 500 when submitting application

Details:
- Username: EMP001
- Department: Network Operations
- Issue: Getting "Error 500" when clicking Submit on new application
- Error appears immediately after clicking Submit button
- Tried 3 times with same result
- Browser: Chrome 120, Windows 11
- Time: 2025-11-18, 14:30
- Urgent: Need to submit application for tomorrow's work

Screenshot attached.
```

---

### Q14.3: What are IT support hours?

**A:** **Standard Support** (typical):

- **Hours**: Monday-Friday, 9:00 AM - 5:00 PM
- **Response Time**: Within 4 hours during business hours
- **Methods**: Phone, email, in-person (if on-site IT)

**Emergency Support**:
- **Hours**: 24/7 for critical system outages
- **Phone**: [Emergency Hotline Number]
- **For**: System completely down, data loss, security incidents

**Self-Service**:
- **Knowledge Base**: Available 24/7 at [URL]
- **System Status**: Check current status at [URL] or System menu
- **This FAQ**: Reference for common questions

---

### Q14.4: How do I request a new feature or improvement?

**A:**

1. **Document Your Request**:
   - What feature you want
   - Why it's needed (business case)
   - Who would benefit
   - Proposed solution (if you have ideas)

2. **Submit Through Proper Channel**:
   - **Small improvements**: Email department admin
   - **Major features**: Submit formal request to system admin
   - **Critical needs**: Discuss with management first

3. **Include Details**:
   - User story: "As a [role], I want to [action], so that [benefit]"
   - Examples or mockups (if applicable)
   - Impact if not implemented

4. **Follow Up**:
   - Requests are reviewed and prioritized
   - May be included in future releases
   - You'll be notified of decision

**Example Feature Request**:
```
Feature: Bulk application submission

User Story: As a warehouse manager, I want to submit applications for multiple employees at once, so that I can save time when processing team requests.

Current Process: Submit 10+ individual applications for team projects (takes 30+ minutes)

Proposed Solution: Upload Excel file with employee names and materials needed

Benefit: Save 20+ minutes per team request, reduce errors

Users Affected: All warehouse managers (14 people)
```

---

### Q14.5: Where can I find training materials?

**A:** **Available Resources**:

1. **System Documentation**:
   - [User Manual](USER_MANUAL.md): Comprehensive guide
   - [Quick Start Guide](QUICK_START_USER.md): 5-minute tutorials
   - This FAQ: Common questions

2. **Online Resources** (if available):
   - Video tutorials: [URL]
   - Knowledge base: [URL]
   - Interactive demos: [URL]

3. **In-Person Training**:
   - Contact department administrator
   - New user onboarding sessions
   - Periodic refresher training

4. **On-Demand Help**:
   - **In-app help**: Click ? icon throughout system
   - **Tooltips**: Hover over fields for explanations
   - **Help menu**: Resources and links

---

### Q14.6: How do I provide feedback about the system?

**A:** **We Welcome Feedback!**

**Methods**:

1. **Feedback Form** (if available):
   - Navigate to **Help > Send Feedback**
   - Fill out form
   - Submit

2. **Email**:
   - Send to: wms-feedback@company.com
   - Subject: Feedback - [Topic]

3. **User Surveys**:
   - Participate when invited
   - Periodic satisfaction surveys

4. **Department Admin**:
   - Share feedback during department meetings
   - Admins consolidate and submit to IT

**What to Include**:
- What's working well
- What could be improved
- Specific pain points
- Feature ideas
- Usability issues

**Your feedback helps improve the system for everyone!**

---

## Appendix: Quick Reference

### Common Workflows

**Submit Application → Approval → Pickup**

```
Employee: Submit application
    ↓
Manager: Receives notification
    ↓
Manager: Reviews and approves
    ↓
Employee: Receives approval notification
    ↓
Employee: Goes to warehouse
    ↓
Manager: Confirms pickup
    ↓
System: Deducts inventory
    ↓
Done!
```

**Timeline**: Usually 1-2 days from submission to pickup.

---

### Application Status Reference

| Status | Icon | Meaning | Your Action |
|--------|------|---------|-------------|
| Draft | 📝 | Not submitted | Edit and submit |
| Pending Approval | ⏳ | Waiting for manager | Wait (up to 24h) |
| Approved | ✅ | Ready for pickup | Go to warehouse |
| Rejected | ❌ | Manager denied | Review reason, resubmit |
| Completed | ✓ | All done | No action needed |
| Cancelled | ⊗ | Cancelled | No action needed |

---

### Inventory Status Reference

| Color | Status | Stock Level | Action |
|-------|--------|-------------|--------|
| 🟢 Green | Normal | Above minimum | Safe to request |
| 🟡 Yellow | Low | Below minimum | Request small amounts |
| 🔴 Red | Critical | Very low or zero | Likely to be rejected |

---

### Contact Quick Reference

| Need | Contact | Phone | Email |
|------|---------|-------|-------|
| **General IT Support** | Help Desk | [Number] | it-support@company.com |
| **Password Reset** | Dept Admin | [Number] | [Email] |
| **Application Questions** | Warehouse Mgr | [Number] | [Email] |
| **System Emergency** | IT Emergency | [24/7 Number] | emergency@company.com |
| **Training** | Dept Admin | [Number] | [Email] |
| **Feedback** | Feedback Team | - | wms-feedback@company.com |

---

## Document Information

**Document Title**: CT-Tibet-WMS Frequently Asked Questions (FAQ)
**Version**: 1.0
**Last Updated**: 2025-11-18
**Next Review**: 2025-12-18 (monthly updates)

**Feedback**:
- Found this FAQ helpful? Let us know!
- Missing information? Request additions!
- Email: wms-support@company.com

---

**End of FAQ Document**

---

## Related Documents

- [User Manual](USER_MANUAL.md) - Comprehensive user guide
- [Quick Start Guide](QUICK_START_USER.md) - 5-minute getting started tutorials
- [API Reference](API_REFERENCE.md) - For developers (technical)
- [Deployment Guide](DEPLOYMENT_MANUAL.md) - For system administrators (technical)

---

**Need More Help?**

If you can't find an answer here:
1. Check the [User Manual](USER_MANUAL.md)
2. Search the knowledge base (if available)
3. Contact IT support: it-support@company.com
4. Emergency: [24/7 Phone Number]
