# CT-Tibet-WMS API Documentation Index

**Welcome to the CT-Tibet-WMS API Documentation**

This directory contains comprehensive, developer-friendly API documentation for the CT-Tibet-WMS (西藏电信仓库管理系统) backend system.

---

## Quick Links

| Document | Purpose | Length | Audience |
|----------|---------|--------|----------|
| [API_QUICK_START.md](./API_QUICK_START.md) | Get started in 5 minutes | 878 lines | Frontend Developers |
| [API_REFERENCE.md](./API_REFERENCE.md) | Complete endpoint reference | 3,751 lines | All Developers |
| [API_TEST_GUIDE.md](./API_TEST_GUIDE.md) | Testing strategies & examples | 1,182 lines | QA & Backend Teams |
| [API_DOCUMENTATION_SUMMARY.md](./API_DOCUMENTATION_SUMMARY.md) | Overview & statistics | 642 lines | Project Managers |

---

## Getting Started (Choose Your Path)

### 🚀 I want to start building an integration (5 minutes)

1. Read: **[API_QUICK_START.md](./API_QUICK_START.md)**
2. Follow the 5-minute guide
3. Copy code examples for your language
4. Start making API calls

**Best for**: Frontend developers, mobile developers

---

### 📚 I need complete API documentation (30 minutes)

1. Read: **[API_REFERENCE.md](./API_REFERENCE.md)**
2. Find your endpoint in the table of contents
3. Review parameters and response examples
4. Check error codes and status codes

**Best for**: Backend developers, integration specialists

---

### 🧪 I need to test the API (varies)

1. Start with: **[API_TEST_GUIDE.md](./API_TEST_GUIDE.md)**
2. Set up Postman or curl
3. Run the provided test scenarios
4. Review automated testing examples

**Best for**: QA engineers, developers testing integrations

---

### 📊 I need project overview (10 minutes)

1. Read: **[API_DOCUMENTATION_SUMMARY.md](./API_DOCUMENTATION_SUMMARY.md)**
2. Review statistics and coverage
3. Check the API matrix
4. Understand workflow examples

**Best for**: Project managers, architects, stakeholders

---

## API System Overview

### Base URL
```
http://localhost:48888/api
```

### Interactive API Documentation
```
http://localhost:48888/doc.html
```

---

## What's Documented

### 68 API Endpoints Across 12 Modules

1. **Authentication** (4 endpoints)
   - Login, logout, token refresh, user info

2. **User Management** (10 endpoints)
   - CRUD operations, profile, password management

3. **Departments** (6 endpoints)
   - Tree structure, hierarchy, management

4. **Roles** (6 endpoints)
   - Role creation, permission assignment

5. **Warehouses** (6 endpoints)
   - Warehouse operations, status management

6. **Materials** (7 endpoints)
   - Material master data, categories

7. **Inventory** (3 endpoints)
   - Stock levels, transaction logs, alerts

8. **Inbound Operations** (3 endpoints)
   - Stock receiving, auto-inventory update

9. **Outbound Operations** (5 endpoints)
   - Direct outbound, application-based outbound, confirmation

10. **Applications & Approvals** (7 endpoints)
    - Material requisition, approval workflow

11. **Statistics & Reports** (4 endpoints)
    - Dashboard, trends, analysis

12. **Message Center** (7 endpoints)
    - Notifications, message management

---

## Key Features

### Complete Workflows

Each major business flow is documented end-to-end:

✓ **Material Requisition Workflow**
- Employee submits application
- Manager reviews and approves
- System auto-creates outbound
- Employee confirms pickup
- Inventory automatically updated

✓ **Inventory Management**
- Create inbound orders
- Auto-update inventory
- Track stock levels
- Low-stock alerts

✓ **Dashboard & Reporting**
- Real-time KPIs
- Trend analysis
- Department reports

### Authentication & Security

✓ JWT-based authentication (2-hour tokens, 7-day refresh)
✓ Role-based access control (Admin, Dept Admin, Manager, Employee)
✓ Permission enforcement on all endpoints

### Developer Experience

✓ Code examples in 4 languages (Bash, JavaScript, Python, Java)
✓ Curl-ready examples for testing
✓ Postman collection setup guide
✓ Automated testing examples

---

## Document Contents

### API_QUICK_START.md (878 lines)
**Perfect for getting started immediately**

- 5-minute getting started
- Login and authentication
- Common workflows with examples
- Troubleshooting guide
- Code examples in JavaScript, Python, Java
- Debug tips

### API_REFERENCE.md (3,751 lines)
**Complete technical reference for every endpoint**

- Overview and base URL
- Authentication details
- Response format standards
- Error codes and handling
- All 68 endpoints documented:
  - Request/response examples
  - Parameter descriptions
  - Status codes
  - Permission requirements
- Common use cases
- Rate limiting and best practices

### API_TEST_GUIDE.md (1,182 lines)
**Comprehensive testing strategies**

- Postman setup and collection
- Pre-request scripts
- 5 complete test scenarios
- Curl testing examples
- Automated testing (Jest)
- Performance testing (Artillery)
- Security testing examples
- Test checklist

### API_DOCUMENTATION_SUMMARY.md (642 lines)
**High-level overview and statistics**

- Documentation overview
- API coverage summary
- Quality metrics
- Integration points
- Performance considerations
- Support resources

---

## Authentication Quick Reference

### Login
```bash
curl -X POST http://localhost:48888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "warehouse_manager",
    "password": "password123",
    "loginType": "PASSWORD"
  }'
```

### Use Token
```bash
curl -X GET http://localhost:48888/api/inventories \
  -H "Authorization: Bearer <YOUR_TOKEN>"
```

### Refresh Token
```bash
curl -X POST http://localhost:48888/api/auth/refresh-token \
  -H "Authorization: Bearer <OLD_TOKEN>"
```

---

## Common Workflows (Copy-Paste Ready)

### Workflow 1: Submit Material Application (5 minutes)
See: [API_QUICK_START.md - Workflow 1](./API_QUICK_START.md#workflow-1-create-and-manage-material-requisition)

### Workflow 2: Manage Warehouse Inventory (10 minutes)
See: [API_QUICK_START.md - Workflow 2](./API_QUICK_START.md#workflow-2-manage-warehouse-inventory)

### Workflow 3: View Dashboard & Reports (5 minutes)
See: [API_QUICK_START.md - Workflow 3](./API_QUICK_START.md#workflow-3-view-dashboard--reports)

---

## Code Examples Available

### Bash/curl Examples
- Complete authentication flows
- All CRUD operations
- Workflow examples
- Testing scripts

**Location**: Throughout all documents, especially [API_QUICK_START.md](./API_QUICK_START.md)

### JavaScript/Node.js
- Async/await examples
- Axios HTTP client
- Complete WMS client class
- Error handling

**Location**: [API_QUICK_START.md - JavaScript Example](./API_QUICK_START.md#javascriptnodejs-example)

### Python
- Requests library
- WMSClient class
- All major operations
- Error handling

**Location**: [API_QUICK_START.md - Python Example](./API_QUICK_START.md#python-example)

### Java
- OkHttp client
- Gson JSON parsing
- Complete implementation
- Exception handling

**Location**: [API_QUICK_START.md - Java Example](./API_QUICK_START.md#java-example)

---

## Testing Resources

### Postman Collection Setup
See: [API_TEST_GUIDE.md - Postman Collection](./API_TEST_GUIDE.md#postman-collection)

### Test Scenarios
1. Complete material requisition workflow
2. Inventory management and inbound
3. Dashboard and statistics
4. User management
5. Material and department management

See: [API_TEST_GUIDE.md - Test Scenarios](./API_TEST_GUIDE.md#test-scenarios)

### Automated Testing Examples
- Jest test suite (40+ tests)
- Load testing with Artillery
- Security testing scripts

See: [API_TEST_GUIDE.md - Automated Testing](./API_TEST_GUIDE.md#automated-testing)

---

## Troubleshooting Quick Reference

### Common Issues

**401 Unauthorized**
- Check token in Authorization header
- Token format: `Bearer <token>` (note the space)
- Token may be expired - refresh using refresh-token endpoint
- See: [API_QUICK_START.md - Troubleshooting](./API_QUICK_START.md#troubleshooting)

**403 Forbidden**
- User lacks required permissions
- Check role requirements in API_REFERENCE.md
- Request elevated privileges from administrator

**400 Bad Request**
- Invalid parameters
- Missing required fields
- Wrong data types
- See specific endpoint in API_REFERENCE.md

**1000 Business Error**
- Business logic violation
- Check error message for details
- Verify prerequisites (inventory levels, status, etc.)

Full troubleshooting guide: [API_QUICK_START.md - Troubleshooting](./API_QUICK_START.md#troubleshooting)

---

## API Statistics

### Endpoints by Module
- Authentication: 4
- User Management: 10
- Departments: 6
- Roles: 6
- Warehouses: 6
- Materials: 7
- Inventory: 3
- Inbound: 3
- Outbound: 5
- Applications: 7
- Statistics: 4
- Messages: 7
- **Total: 68 endpoints**

### HTTP Methods
- GET: 32 endpoints (query/read)
- POST: 20 endpoints (create/execute)
- PUT: 12 endpoints (update)
- PATCH: 2 endpoints (partial update)
- DELETE: 2 endpoints (delete)

### Documentation Coverage
- ✓ All endpoints documented
- ✓ Parameters for each endpoint
- ✓ Response examples
- ✓ Status codes
- ✓ Error handling
- ✓ Permission requirements
- ✓ Real-world use cases

---

## Development Team Checklist

### Frontend Developers
- [ ] Read API_QUICK_START.md
- [ ] Choose code example in your language
- [ ] Test authentication flow
- [ ] Implement first API integration
- [ ] Bookmark API_REFERENCE.md for reference

### Backend Integration Developers
- [ ] Read API_REFERENCE.md
- [ ] Review API_TEST_GUIDE.md for test scenarios
- [ ] Set up Postman collection
- [ ] Run provided test scenarios
- [ ] Verify integration points

### QA / Testing Teams
- [ ] Read API_TEST_GUIDE.md
- [ ] Set up test environment
- [ ] Review test scenarios
- [ ] Create test cases
- [ ] Execute manual and automated tests

### Project Managers
- [ ] Read API_DOCUMENTATION_SUMMARY.md
- [ ] Review API statistics and coverage
- [ ] Check integration points
- [ ] Understand workflow diagrams
- [ ] Plan integration timeline

---

## File Structure

```
docs/
├── README_API_DOCS.md                  <- You are here
├── API_QUICK_START.md                  (878 lines)
├── API_REFERENCE.md                    (3,751 lines)
├── API_TEST_GUIDE.md                   (1,182 lines)
└── API_DOCUMENTATION_SUMMARY.md        (642 lines)
```

---

## Version & Status

| Item | Details |
|------|---------|
| **Version** | 1.0 |
| **Status** | Production-Ready |
| **Last Updated** | 2025-11-17 |
| **Total Documentation** | 6,453 lines |
| **API Endpoints** | 68 (60+ core) |
| **Code Examples** | 30+ complete |
| **Test Scenarios** | 5 workflows |
| **Languages Supported** | 4 (Bash, JavaScript, Python, Java) |

---

## Quick Navigation

### Need This? Go Here:

**Getting started quickly**
→ [API_QUICK_START.md](./API_QUICK_START.md)

**Complete API documentation**
→ [API_REFERENCE.md](./API_REFERENCE.md)

**How to test**
→ [API_TEST_GUIDE.md](./API_TEST_GUIDE.md)

**Project overview**
→ [API_DOCUMENTATION_SUMMARY.md](./API_DOCUMENTATION_SUMMARY.md)

**System overview**
→ This file (README_API_DOCS.md)

---

## Support

### API Access
- **Base URL**: http://localhost:48888/api
- **Interactive Docs**: http://localhost:48888/doc.html
- **Backend Source**: backend/src/main/java/com/ct/wms/controller/

### Contact
For questions or clarification, contact the development team.

### Escalation
- API implementation issues: Backend team
- Integration questions: Dev lead
- System architecture: System architect

---

## Related Documentation

- **Requirements**: docs/需求分析.md
- **Database**: Database schema documentation
- **Frontend Integration**: Frontend-specific guides
- **Deployment**: Deployment procedures

---

## Tips for Success

1. **Start Simple**: Login → Get user info → List items
2. **Use curl First**: Test endpoints before writing code
3. **Check Examples**: Copy working examples before modifying
4. **Read Error Messages**: They tell you what's wrong
5. **Reference Docs**: Keep API_REFERENCE.md handy
6. **Test Locally**: Use test accounts before production
7. **Handle Tokens**: Always refresh before expiration

---

## FAQ

**Q: Where do I start?**
A: Read [API_QUICK_START.md](./API_QUICK_START.md) - it's designed for first-time users.

**Q: How long is the documentation?**
A: 6,453 lines total - but you don't need to read it all. Use the quick links above to find what you need.

**Q: Are there code examples?**
A: Yes! 30+ examples in Bash, JavaScript, Python, and Java.

**Q: Can I test without writing code?**
A: Yes! Use curl examples or Postman collection from the testing guide.

**Q: What if I get an error?**
A: Check the troubleshooting section in [API_QUICK_START.md](./API_QUICK_START.md).

**Q: Where's the interactive API documentation?**
A: Visit http://localhost:48888/doc.html in your browser.

---

## Next Step

Choose one:

1. **👨‍💻 I'm a developer**: Read [API_QUICK_START.md](./API_QUICK_START.md)
2. **🔍 I need details**: Read [API_REFERENCE.md](./API_REFERENCE.md)
3. **🧪 I'm testing**: Read [API_TEST_GUIDE.md](./API_TEST_GUIDE.md)
4. **📊 I'm managing**: Read [API_DOCUMENTATION_SUMMARY.md](./API_DOCUMENTATION_SUMMARY.md)

---

**Happy Coding! 🚀**

---

**Documentation Version**: 1.0
**Status**: Production-Ready
**Last Updated**: 2025-11-17
