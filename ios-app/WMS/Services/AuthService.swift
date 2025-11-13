//
//  AuthService.swift
//  认证服务
//

import Foundation

class AuthService: ObservableObject {
    @Published var isAuthenticated = false
    @Published var currentUser: User?
    @Published var token: String?

    private let tokenKey = "auth_token"
    private let userKey = "current_user"

    init() {
        loadAuthState()
    }

    // 加载认证状态
    private func loadAuthState() {
        if let savedToken = UserDefaults.standard.string(forKey: tokenKey),
           let userData = UserDefaults.standard.data(forKey: userKey),
           let user = try? JSONDecoder().decode(User.self, from: userData) {
            self.token = savedToken
            self.currentUser = user
            self.isAuthenticated = true
        }
    }

    // 登录
    func login(username: String, password: String) async throws {
        let response = try await APIService.shared.login(username: username, password: password)

        DispatchQueue.main.async {
            self.token = response.token
            self.currentUser = response.user
            self.isAuthenticated = true

            // 保存到本地
            UserDefaults.standard.set(response.token, forKey: self.tokenKey)
            if let userData = try? JSONEncoder().encode(response.user) {
                UserDefaults.standard.set(userData, forKey: self.userKey)
            }
        }
    }

    // 退出登录
    func logout() {
        DispatchQueue.main.async {
            self.token = nil
            self.currentUser = nil
            self.isAuthenticated = false

            UserDefaults.standard.removeObject(forKey: self.tokenKey)
            UserDefaults.standard.removeObject(forKey: self.userKey)
        }
    }

    // 检查是否有权限
    func hasRole(_ roles: [UserRole]) -> Bool {
        guard let user = currentUser else { return false }
        return roles.contains(user.role)
    }

    // 是否是仓管员或管理员
    func canManageWarehouse() -> Bool {
        return hasRole([.warehouse, .deptAdmin, .systemAdmin])
    }

    // 是否是普通员工
    func isRegularUser() -> Bool {
        return hasRole([.user])
    }
}
