//
//  OutboundOrder.swift
//  出库单模型
//

import Foundation

enum OutboundSource: Int, Codable {
    case direct = 1            // 直接出库
    case fromApplication = 2   // 申领出库

    var displayName: String {
        switch self {
        case .direct: return "直接出库"
        case .fromApplication: return "申领出库"
        }
    }
}

enum OutboundStatus: Int, Codable {
    case pending = 0           // 待提货
    case completed = 1         // 已完成
    case canceled = 2          // 已取消

    var displayName: String {
        switch self {
        case .pending: return "待提货"
        case .completed: return "已完成"
        case .canceled: return "已取消"
        }
    }

    var color: String {
        switch self {
        case .pending: return "orange"
        case .completed: return "green"
        case .canceled: return "gray"
        }
    }
}

struct OutboundOrder: Codable, Identifiable {
    let id: Int
    let orderNo: String
    let warehouseId: Int
    let warehouseName: String
    let source: OutboundSource
    let applyId: Int?
    let operatorId: Int
    let operatorName: String
    let recipientId: Int
    let recipientName: String
    let status: OutboundStatus
    let outboundTime: String?
    let createdTime: String
    let remark: String?
    let details: [OutboundDetail]?

    enum CodingKeys: String, CodingKey {
        case id
        case orderNo = "order_no"
        case warehouseId = "warehouse_id"
        case warehouseName = "warehouse_name"
        case source
        case applyId = "apply_id"
        case operatorId = "operator_id"
        case operatorName = "operator_name"
        case recipientId = "recipient_id"
        case recipientName = "recipient_name"
        case status
        case outboundTime = "outbound_time"
        case createdTime = "created_time"
        case remark
        case details
    }
}

struct OutboundDetail: Codable, Identifiable {
    let id: Int
    let outboundId: Int
    let materialId: Int
    let materialCode: String
    let materialName: String
    let quantity: Int
    let unit: String

    enum CodingKeys: String, CodingKey {
        case id
        case outboundId = "outbound_id"
        case materialId = "material_id"
        case materialCode = "material_code"
        case materialName = "material_name"
        case quantity
        case unit
    }
}

struct CreateOutboundRequest: Codable {
    let warehouseId: Int
    let recipientId: Int
    let remark: String?
    let details: [OutboundDetailRequest]

    enum CodingKeys: String, CodingKey {
        case warehouseId = "warehouse_id"
        case recipientId = "recipient_id"
        case remark
        case details
    }
}

struct OutboundDetailRequest: Codable {
    let materialId: Int
    let quantity: Int

    enum CodingKeys: String, CodingKey {
        case materialId = "material_id"
        case quantity
    }
}
