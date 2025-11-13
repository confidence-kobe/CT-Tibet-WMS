//
//  HomeView.swift
//  首页
//

import SwiftUI

struct HomeView: View {
    @EnvironmentObject var authService: AuthService

    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 20) {
                    // 用户信息卡片
                    if let user = authService.currentUser {
                        VStack(alignment: .leading, spacing: 10) {
                            HStack {
                                VStack(alignment: .leading) {
                                    Text(user.realName)
                                        .font(.title2)
                                        .fontWeight(.bold)

                                    Text(user.role.displayName)
                                        .font(.subheadline)
                                        .foregroundColor(.gray)

                                    Text(user.deptName)
                                        .font(.caption)
                                        .foregroundColor(.blue)
                                }

                                Spacer()

                                Image(systemName: "person.circle.fill")
                                    .font(.system(size: 60))
                                    .foregroundColor(.blue)
                            }
                        }
                        .padding()
                        .background(Color(.systemBackground))
                        .cornerRadius(12)
                        .shadow(radius: 2)
                    }

                    // 快捷功能
                    VStack(alignment: .leading, spacing: 15) {
                        Text("快捷功能")
                            .font(.headline)

                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 15) {
                            if authService.canManageWarehouse() {
                                QuickActionCard(
                                    icon: "arrow.up.bin.fill",
                                    title: "直接出库",
                                    color: .orange
                                )

                                QuickActionCard(
                                    icon: "checkmark.circle.fill",
                                    title: "待审批",
                                    color: .blue
                                )

                                QuickActionCard(
                                    icon: "arrow.down.to.line",
                                    title: "入库管理",
                                    color: .green
                                )
                            } else {
                                QuickActionCard(
                                    icon: "doc.text.fill",
                                    title: "申领物料",
                                    color: .blue
                                )

                                QuickActionCard(
                                    icon: "clock.fill",
                                    title: "申领记录",
                                    color: .orange
                                )
                            }

                            QuickActionCard(
                                icon: "archivebox.fill",
                                title: "库存查询",
                                color: .purple
                            )

                            QuickActionCard(
                                icon: "chart.bar.fill",
                                title: "统计报表",
                                color: .red
                            )
                        }
                    }

                    // 最近动态
                    VStack(alignment: .leading, spacing: 15) {
                        Text("最近动态")
                            .font(.headline)

                        VStack(spacing: 10) {
                            ActivityRow(
                                icon: "arrow.up.bin.fill",
                                title: "出库单 OUT202501110001 已完成",
                                time: "2小时前",
                                color: .orange
                            )

                            ActivityRow(
                                icon: "checkmark.circle.fill",
                                title: "申领单 APPLY202501110023 已审批",
                                time: "5小时前",
                                color: .green
                            )

                            ActivityRow(
                                icon: "arrow.down.to.line",
                                title: "入库单 IN202501100015 已入库",
                                time: "昨天",
                                color: .blue
                            )
                        }
                        .padding()
                        .background(Color(.systemBackground))
                        .cornerRadius(12)
                        .shadow(radius: 2)
                    }
                }
                .padding()
            }
            .background(Color(.systemGroupedBackground))
            .navigationTitle("首页")
        }
    }
}

struct QuickActionCard: View {
    let icon: String
    let title: String
    let color: Color

    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 40))
                .foregroundColor(color)

            Text(title)
                .font(.subheadline)
                .fontWeight(.medium)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 25)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(radius: 2)
    }
}

struct ActivityRow: View {
    let icon: String
    let title: String
    let time: String
    let color: Color

    var body: some View {
        HStack(spacing: 15) {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(color)
                .frame(width: 40, height: 40)
                .background(color.opacity(0.1))
                .cornerRadius(8)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.subheadline)

                Text(time)
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundColor(.gray)
        }
    }
}

#Preview {
    HomeView()
        .environmentObject(AuthService())
}
