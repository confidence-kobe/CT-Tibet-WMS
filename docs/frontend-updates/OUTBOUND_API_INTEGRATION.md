# Outbound Pages API Integration Guide

This document provides the complete code for integrating backend APIs into the 4 outbound-related pages.

## Overview of Changes

1. **Outbound List Page** - `/views/outbound/list/index.vue`
2. **Outbound Create Page** - `/views/outbound/create/index.vue`  
3. **Outbound Detail Page** - `/views/outbound/detail/index.vue`
4. **Outbound Confirm Page** - `/views/outbound/confirm/index.vue`

## Common Backend Field Mappings

| Frontend Field | Backend Field | Notes |
|---|---|---|
| code | outboundNo | 出库单号 |
| receiver | receiverName | 领取人姓名 |
| warehouseName | warehouseName | 仓库名称 |
| source | source | 1=直接出库, 2=申请出库 |
| status | status | 0=待领取, 1=已完成, 2=已取消 |
| outboundType | outboundType | 1-生产领用,2-维修领用,3-项目使用,4-其他 |

## Page 1: Outbound List

### Script Section Changes

Replace the entire `<script setup>` section with the provided code in the repository backup file.

Key API Integration Points:

1. Import APIs:
   - listOutbounds
   - confirmOutbound  
   - cancelOutbound
   - listWarehouses

2. Load warehouses on mount
3. Query with pagination (pageNum, pageSize)
4. Map response fields correctly
5. Navigate to detail page on click
6. Call confirm/cancel APIs with proper error handling

## Page 2: Outbound Create  

### New Features
- Load warehouses from API
- Load materials from API  
- Check inventory before submission
- Add required fields: receiver, contactPhone, outboundType
- Submit via createDirectOutbound API

## Page 3: Outbound Detail

### Changes
- Load detail via getOutboundById(id)
- Show confirm button if status === 0
- Call confirmOutbound API
- Remove dialog, make it full page
- Map backend fields correctly

## Page 4: Outbound Confirm

### Changes  
- Query with status=0 filter
- Load warehouses from API
- Call confirmOutbound on confirm button
- Proper field mapping

