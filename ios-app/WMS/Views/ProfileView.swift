//
//  ProfileView.swift
//  个人中心
//

import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var authService: AuthService
    @State private var showLogoutAlert = false

    var body: some View {
        NavigationView {
            List {
                // 用户信息
                if let user = authService.currentUser {
                    Section {
                        HStack(spacing: 15) {
                            Image(systemName: "person.circle.fill")
                                .font(.system(size: 60))
                                .foregroundColor(.blue)

                            VStack(alignment: .leading, spacing: 6) {
                                Text(user.realName)
                                    .font(.title3)
                                    .fontWeight(.semibold)

                                Text(user.role.displayName)
                                    .font(.subheadline)
                                    .foregroundColor(.gray)

                                Text(user.deptName)
                                    .font(.caption)
                                    .foregroundColor(.blue)
                            }

                            Spacer()
                        }
                        .padding(.vertical, 10)
                    }

                    Section("账号信息") {
                        InfoRow(label: "用户名", value: user.username)

                        if let phone = user.phone {
                            InfoRow(label: "手机号", value: phone)
                        }

                        if let email = user.email {
                            InfoRow(label: "邮箱", value: email)
                        }

                        InfoRow(label: "状态", value: user.enabled ? "正常" : "已禁用")
                    }
                }

                Section("功能设置") {
                    NavigationLink(destination: Text("消息通知")) {
                        Label("消息通知", systemImage: "bell.fill")
                    }

                    NavigationLink(destination: Text("修改密码")) {
                        Label("修改密码", systemImage: "lock.fill")
                    }

                    NavigationLink(destination: Text("关于我们")) {
                        Label("关于我们", systemImage: "info.circle.fill")
                    }
                }

                Section {
                    Button(action: { showLogoutAlert = true }) {
                        HStack {
                            Spacer()
                            Text("退出登录")
                                .foregroundColor(.red)
                            Spacer()
                        }
                    }
                }
            }
            .navigationTitle("我的")
            .alert("确认退出", isPresented: $showLogoutAlert) {
                Button("取消", role: .cancel) {}
                Button("退出", role: .destructive) {
                    authService.logout()
                }
            } message: {
                Text("确定要退出登录吗?")
            }
        }
    }
}

#Preview {
    ProfileView()
        .environmentObject(AuthService())
}
