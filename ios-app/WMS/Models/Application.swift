//
//  Application.swift
//  申领单模型
//

import Foundation

enum ApplicationStatus: Int, Codable {
    case pending = 0           // 待审批
    case approved = 1          // 已通过
    case rejected = 2          // 已驳回
    case completed = 3         // 已完成(已提货)
    case canceled = 4          // 已取消

    var displayName: String {
        switch self {
        case .pending: return "待审批"
        case .approved: return "已通过"
        case .rejected: return "已驳回"
        case .completed: return "已完成"
        case .canceled: return "已取消"
        }
    }

    var color: String {
        switch self {
        case .pending: return "orange"
        case .approved: return "blue"
        case .rejected: return "red"
        case .completed: return "green"
        case .canceled: return "gray"
        }
    }
}

struct Application: Codable, Identifiable {
    let id: Int
    let applyNo: String
    let warehouseId: Int
    let warehouseName: String
    let applicantId: Int
    let applicantName: String
    let deptId: Int
    let deptName: String
    let status: ApplicationStatus
    let applyTime: String
    let approveTime: String?
    let approverId: Int?
    let approverName: String?
    let approveRemark: String?
    let remark: String?
    let details: [ApplicationDetail]?

    enum CodingKeys: String, CodingKey {
        case id
        case applyNo = "apply_no"
        case warehouseId = "warehouse_id"
        case warehouseName = "warehouse_name"
        case applicantId = "applicant_id"
        case applicantName = "applicant_name"
        case deptId = "dept_id"
        case deptName = "dept_name"
        case status
        case applyTime = "apply_time"
        case approveTime = "approve_time"
        case approverId = "approver_id"
        case approverName = "approver_name"
        case approveRemark = "approve_remark"
        case remark
        case details
    }
}

struct ApplicationDetail: Codable, Identifiable {
    let id: Int
    let applyId: Int
    let materialId: Int
    let materialCode: String
    let materialName: String
    let quantity: Int
    let unit: String

    enum CodingKeys: String, CodingKey {
        case id
        case applyId = "apply_id"
        case materialId = "material_id"
        case materialCode = "material_code"
        case materialName = "material_name"
        case quantity
        case unit
    }
}

struct CreateApplicationRequest: Codable {
    let warehouseId: Int
    let remark: String?
    let details: [ApplicationDetailRequest]

    enum CodingKeys: String, CodingKey {
        case warehouseId = "warehouse_id"
        case remark
        case details
    }
}

struct ApplicationDetailRequest: Codable {
    let materialId: Int
    let quantity: Int

    enum CodingKeys: String, CodingKey {
        case materialId = "material_id"
        case quantity
    }
}

struct ApprovalRequest: Codable {
    let approve: Bool          // true=通过, false=驳回
    let remark: String?
}
