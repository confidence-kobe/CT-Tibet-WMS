//
//  User.swift
//  用户模型
//

import Foundation

enum UserRole: String, Codable {
    case systemAdmin = "SYSTEM_ADMIN"      // 系统管理员
    case deptAdmin = "DEPT_ADMIN"          // 部门管理员
    case warehouse = "WAREHOUSE"           // 仓管员
    case user = "USER"                     // 普通员工

    var displayName: String {
        switch self {
        case .systemAdmin: return "系统管理员"
        case .deptAdmin: return "部门管理员"
        case .warehouse: return "仓管员"
        case .user: return "普通员工"
        }
    }
}

struct User: Codable, Identifiable {
    let id: Int
    let username: String
    let realName: String
    let phone: String?
    let email: String?
    let deptId: Int
    let deptName: String
    let role: UserRole
    let enabled: Bool
    let createdTime: String

    enum CodingKeys: String, CodingKey {
        case id
        case username
        case realName = "real_name"
        case phone
        case email
        case deptId = "dept_id"
        case deptName = "dept_name"
        case role
        case enabled
        case createdTime = "created_time"
    }
}

struct LoginRequest: Codable {
    let username: String
    let password: String
}

struct LoginResponse: Codable {
    let token: String
    let user: User
}
