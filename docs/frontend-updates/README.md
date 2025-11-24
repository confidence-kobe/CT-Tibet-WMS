# Outbound Pages API Integration - Complete Guide

## Overview

This guide provides complete updated code for integrating backend APIs into 4 outbound-related pages.

## Files to Update

1. `frontend-pc/src/views/outbound/list/index.vue` - Outbound list page
2. `frontend-pc/src/views/outbound/create/index.vue` - Create outbound page  
3. `frontend-pc/src/views/outbound/detail/index.vue` - Outbound detail page
4. `frontend-pc/src/views/outbound/confirm/index.vue` - Confirm pickup page

## Key Changes Summary

### 1. Outbound List Page

**API Integrations:**
- Import: `listOutbounds`, `confirmOutbound`, `cancelOutbound`, `listWarehouses`
- Load warehouses dynamically from API
- Query with pagination (pageNum, pageSize)
- Map backend fields (outboundNo → code, receiverName → receiver)
- Navigate to detail page instead of showing dialog
- Implement confirm/cancel with proper APIs

**Field Changes:**
- `queryForm.code` → `queryForm.keyword`
- `queryForm.source` → `queryForm.outboundType`
- Added `startDate`, `endDate` for date range filtering
- Added `dateRange` ref for date picker

**Template Changes:**
- Dynamic warehouse options from API
- outboundType dropdown (1-生产领用, 2-维修领用, 3-项目使用, 4-其他出库)
- Date range picker added
- Removed detail dialog, navigate to detail page
- Cancel requires inputting reason via prompt

### 2. Outbound Create Page

**New Features:**
- Load warehouses from `listWarehouses` API
- Load materials from `listMaterials` API
- Check inventory with `listInventories` API
- Added required fields: `receiver` (领取人), `contactPhone` (联系电话)
- Added `outboundType` dropdown (1-生产领用, 2-维修领用, 3-项目使用, 4-其他出库)
- Submit via `createDirectOutbound` API

**Key Implementation:**
- When selecting warehouse + material, query current inventory
- Show available stock quantity
- Warn if stock insufficient
- Validate all fields before submission
- Map form data to OutboundDTO format

### 3. Outbound Detail Page

**API Integrations:**
- Import: `getOutboundById`, `confirmOutbound`, `cancelOutbound`
- Load detail by ID from route params
- Show confirm button only if status === 0
- Call confirm/cancel APIs

**Field Mappings:**
- Map all backend fields correctly
- Show `applyNo` (申请单号) with link if exists
- Display `receiverName`, `receiverPhone`, `outboundType`
- Show proper status labels and colors

**TODO Comments:**
- Edit and Delete buttons marked with TODO (not implemented yet)

### 4. Outbound Confirm Page

**API Integrations:**
- Import: `listOutbounds`, `confirmOutbound`, `listWarehouses`
- Query with `status=0` filter (only pending pickups)
- Load warehouses dynamically
- Call `confirmOutbound` API on confirm

**Field Mappings:**
- Map backend fields correctly
- Support keyword and warehouseId filtering
- Show proper confirmation dialog before confirming

## Backend Field Mappings Reference

| Frontend | Backend | Type | Notes |
|---|---|---|---|
| code | outboundNo | String | 出库单号 |
| receiver | receiverName | String | 领取人姓名 |
| contactPhone | receiverPhone | String | 联取人电话 |
| warehouseName | warehouseName | String | 仓库名称 |
| source | source | Integer | 1=直接出库, 2=申请出库 |
| status | status | Integer | 0=待领取, 1=已完成, 2=已取消 |
| outboundType | outboundType | Integer | 1-生产领用, 2-维修领用, 3-项目使用, 4-其他 |
| applyCode | applyNo | String | 关联申请单号 |
| createTime | createTime | String | 创建时间 |
| confirmTime | outboundTime | String | 确认时间 |

## Backend API Endpoints

### OutboundController

1. **GET /api/outbounds** - List outbounds with pagination
   - Params: pageNum, pageSize, warehouseId, outboundType, status, startDate, endDate, keyword
   - Returns: PageResult<Outbound>

2. **GET /api/outbounds/{id}** - Get outbound detail
   - Returns: Outbound (with details)

3. **POST /api/outbounds/direct** - Create direct outbound
   - Body: OutboundDTO
   - Returns: Long (outbound ID)

4. **POST /api/outbounds/{id}/confirm** - Confirm pickup
   - Returns: void

5. **POST /api/outbounds/{id}/cancel** - Cancel outbound
   - Params: reason
   - Returns: void

## OutboundDTO Structure

```typescript
{
  warehouseId: number        // 仓库ID (required)
  outboundType: number       // 出库类型 (required)
  outboundTime: string       // 出库时间 (required)
  receiverId: number         // 领用人ID  
  remark: string             // 备注
  details: [                 // 出库明细 (required)
    {
      materialId: number     // 物资ID (required)
      quantity: number       // 数量 (required)
      unitPrice: number      // 单价
      remark: string         // 备注
    }
  ]
}
```

## How to Apply Updates

### Option 1: Manual Copy-Paste
1. Navigate to each file in the `frontend-pc/src/views/outbound` directory
2. Copy the complete code from the corresponding `.txt` file in this directory
3. Replace the entire file content
4. Save the file

### Option 2: Use Python Script
```bash
cd H:/java/CT-Tibet-WMS
python docs/frontend-updates/apply_updates.py
```

## Testing Checklist

After applying updates, test the following:

### Outbound List
- [ ] Page loads without errors
- [ ] Warehouses dropdown populated from API
- [ ] Search filters work correctly
- [ ] Date range picker works
- [ ] Pagination works
- [ ] Click outbound number navigates to detail page
- [ ] Confirm button calls API and refreshes list
- [ ] Cancel button prompts for reason and calls API

### Outbound Create
- [ ] Warehouses loaded from API
- [ ] Materials loaded from API
- [ ] Selecting material shows current inventory
- [ ] Stock warning shows when insufficient
- [ ] Required fields validation works
- [ ] Submit creates outbound successfully
- [ ] Redirects to list page after success

### Outbound Detail
- [ ] Detail loaded from API by ID
- [ ] All fields displayed correctly
- [ ] Confirm button visible only for status=0
- [ ] Confirm button calls API successfully
- [ ] Cancel button works (if implemented)
- [ ] Apply link navigates correctly (if applicable)

### Outbound Confirm
- [ ] Only shows pending pickups (status=0)
- [ ] Warehouses dropdown works
- [ ] Search filters work
- [ ] Confirm dialog shows correct info
- [ ] Confirm button calls API and refreshes
- [ ] View detail link works

## Common Issues & Solutions

### Issue: API returns 401 Unauthorized
**Solution**: Ensure you're logged in and have proper role permissions (WAREHOUSE, DEPT_ADMIN, or ADMIN)

### Issue: Warehouse/Material lists are empty
**Solution**: Check if those APIs are working correctly, verify data exists in database

### Issue: Field mappings incorrect
**Solution**: Refer to the Backend Field Mappings table above, ensure frontend uses correct backend field names

### Issue: Date format errors
**Solution**: Use `YYYY-MM-DD` format for dates, `YYYY-MM-DD HH:mm:ss` for datetime

## Next Steps

After completing these 4 pages, consider:
1. Adding loading states for better UX
2. Implementing error boundaries
3. Adding success/error toast notifications
4. Implementing the Edit/Delete functions (currently TODO)
5. Adding export functionality
6. Implementing print功能 for outbound orders

## Support

If you encounter issues:
1. Check browser console for errors
2. Check network tab for API request/response
3. Verify backend is running and accessible
4. Check backend logs for errors

---

Generated: 2025-11-14
Version: 1.0
