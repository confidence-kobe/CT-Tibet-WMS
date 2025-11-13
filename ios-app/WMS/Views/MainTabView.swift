//
//  MainTabView.swift
//  主标签页
//

import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        TabView {
            HomeView()
                .tabItem {
                    Label("首页", systemImage: "house.fill")
                }

            if authService.canManageWarehouse() {
                // 仓管员:直接出库
                OutboundCreateView()
                    .tabItem {
                        Label("出库", systemImage: "arrow.up.bin.fill")
                    }

                // 仓管员:审批列表
                ApprovalListView()
                    .tabItem {
                        Label("审批", systemImage: "checkmark.circle.fill")
                    }
            } else {
                // 普通员工:申领列表
                ApplicationListView()
                    .tabItem {
                        Label("我的申领", systemImage: "doc.text.fill")
                    }
            }

            InventoryListView()
                .tabItem {
                    Label("库存", systemImage: "archivebox.fill")
                }

            ProfileView()
                .tabItem {
                    Label("我的", systemImage: "person.fill")
                }
        }
    }
}

#Preview {
    MainTabView()
        .environmentObject(AuthService())
}
