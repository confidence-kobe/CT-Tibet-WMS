//
//  APIService.swift
//  API 服务层
//

import Foundation

enum APIError: Error {
    case invalidURL
    case networkError(Error)
    case invalidResponse
    case serverError(String)
    case decodingError(Error)

    var localizedDescription: String {
        switch self {
        case .invalidURL:
            return "无效的URL"
        case .networkError(let error):
            return "网络错误: \(error.localizedDescription)"
        case .invalidResponse:
            return "无效的响应"
        case .serverError(let message):
            return "服务器错误: \(message)"
        case .decodingError(let error):
            return "数据解析错误: \(error.localizedDescription)"
        }
    }
}

struct APIResponse<T: Codable>: Codable {
    let code: Int
    let message: String
    let data: T?
}

class APIService {
    static let shared = APIService()

    // 修改为您的后端API地址
    private let baseURL = "http://localhost:8080/api"

    private init() {}

    // 通用请求方法
    func request<T: Codable>(
        endpoint: String,
        method: String = "GET",
        body: Encodable? = nil,
        token: String? = nil
    ) async throws -> T {
        guard let url = URL(string: baseURL + endpoint) else {
            throw APIError.invalidURL
        }

        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        // 添加认证token
        if let token = token {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }

        // 添加请求体
        if let body = body {
            request.httpBody = try JSONEncoder().encode(body)
        }

        do {
            let (data, response) = try await URLSession.shared.data(for: request)

            guard let httpResponse = response as? HTTPURLResponse else {
                throw APIError.invalidResponse
            }

            // 打印调试信息
            if let jsonString = String(data: data, encoding: .utf8) {
                print("Response: \(jsonString)")
            }

            if httpResponse.statusCode == 200 {
                let apiResponse = try JSONDecoder().decode(APIResponse<T>.self, from: data)
                if apiResponse.code == 200, let data = apiResponse.data {
                    return data
                } else {
                    throw APIError.serverError(apiResponse.message)
                }
            } else {
                throw APIError.serverError("HTTP \(httpResponse.statusCode)")
            }
        } catch let error as DecodingError {
            throw APIError.decodingError(error)
        } catch let error as APIError {
            throw error
        } catch {
            throw APIError.networkError(error)
        }
    }

    // MARK: - 用户相关API

    func login(username: String, password: String) async throws -> LoginResponse {
        let request = LoginRequest(username: username, password: password)
        return try await self.request(endpoint: "/auth/login", method: "POST", body: request)
    }

    func getCurrentUser(token: String) async throws -> User {
        return try await request(endpoint: "/user/current", token: token)
    }

    // MARK: - 物料相关API

    func getMaterials(token: String) async throws -> [Material] {
        return try await request(endpoint: "/material/list", token: token)
    }

    func getInventory(warehouseId: Int, token: String) async throws -> [Inventory] {
        return try await request(endpoint: "/inventory/list?warehouseId=\(warehouseId)", token: token)
    }

    // MARK: - 出库相关API

    func createOutbound(request: CreateOutboundRequest, token: String) async throws -> OutboundOrder {
        return try await self.request(endpoint: "/outbound/create", method: "POST", body: request, token: token)
    }

    func getOutboundList(token: String) async throws -> [OutboundOrder] {
        return try await request(endpoint: "/outbound/list", token: token)
    }

    func getPendingPickups(token: String) async throws -> [OutboundOrder] {
        return try await request(endpoint: "/outbound/pending", token: token)
    }

    func confirmPickup(outboundId: Int, token: String) async throws -> Bool {
        struct EmptyResponse: Codable {}
        let _: EmptyResponse = try await request(
            endpoint: "/outbound/confirm/\(outboundId)",
            method: "POST",
            token: token
        )
        return true
    }

    // MARK: - 申领相关API

    func createApplication(request: CreateApplicationRequest, token: String) async throws -> Application {
        return try await self.request(endpoint: "/apply/create", method: "POST", body: request, token: token)
    }

    func getMyApplications(token: String) async throws -> [Application] {
        return try await request(endpoint: "/apply/my", token: token)
    }

    func cancelApplication(applyId: Int, token: String) async throws -> Bool {
        struct EmptyResponse: Codable {}
        let _: EmptyResponse = try await request(
            endpoint: "/apply/cancel/\(applyId)",
            method: "POST",
            token: token
        )
        return true
    }

    // MARK: - 审批相关API

    func getPendingApprovals(token: String) async throws -> [Application] {
        return try await request(endpoint: "/apply/pending", token: token)
    }

    func approveApplication(applyId: Int, request: ApprovalRequest, token: String) async throws -> Bool {
        struct EmptyResponse: Codable {}
        let _: EmptyResponse = try await self.request(
            endpoint: "/apply/approve/\(applyId)",
            method: "POST",
            body: request,
            token: token
        )
        return true
    }
}
