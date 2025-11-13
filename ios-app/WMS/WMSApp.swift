//
//  WMSApp.swift
//  CT-Tibet-WMS iOS App
//
//  仓库管理系统 iOS 客户端
//

import SwiftUI

@main
struct WMSApp: App {
    @StateObject private var authService = AuthService()

    var body: some Scene {
        WindowGroup {
            if authService.isAuthenticated {
                MainTabView()
                    .environmentObject(authService)
            } else {
                LoginView()
                    .environmentObject(authService)
            }
        }
    }
}
