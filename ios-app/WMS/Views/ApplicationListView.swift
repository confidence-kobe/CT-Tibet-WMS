//
//  ApplicationListView.swift
//  申领列表 (普通员工)
//

import SwiftUI

struct ApplicationListView: View {
    @EnvironmentObject var authService: AuthService
    @State private var applications: [Application] = []
    @State private var isLoading = false
    @State private var showCreateSheet = false

    var body: some View {
        NavigationView {
            Group {
                if isLoading {
                    ProgressView("加载中...")
                } else if applications.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "doc.text")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)

                        Text("暂无申领记录")
                            .foregroundColor(.gray)

                        Button("创建申领") {
                            showCreateSheet = true
                        }
                        .buttonStyle(.bordered)
                    }
                } else {
                    List(applications) { application in
                        NavigationLink(destination: ApplicationDetailView(application: application)) {
                            ApplicationRow(application: application)
                        }
                    }
                    .refreshable {
                        await loadApplications()
                    }
                }
            }
            .navigationTitle("我的申领")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button(action: { showCreateSheet = true }) {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showCreateSheet) {
                ApplicationCreateView(onSuccess: {
                    showCreateSheet = false
                    Task {
                        await loadApplications()
                    }
                })
            }
        }
        .task {
            await loadApplications()
        }
    }

    private func loadApplications() async {
        guard let token = authService.token else { return }

        isLoading = true

        do {
            let result = try await APIService.shared.getMyApplications(token: token)
            await MainActor.run {
                applications = result
                isLoading = false
            }
        } catch {
            await MainActor.run {
                isLoading = false
            }
        }
    }
}

struct ApplicationRow: View {
    let application: Application

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(application.applyNo)
                    .font(.headline)

                Spacer()

                StatusBadge(status: application.status)
            }

            HStack {
                Label(application.warehouseName, systemImage: "archivebox")
                    .font(.subheadline)
                    .foregroundColor(.gray)

                Spacer()

                Text(formatDate(application.applyTime))
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            if let remark = application.remark {
                Text(remark)
                    .font(.caption)
                    .foregroundColor(.secondary)
                    .lineLimit(2)
            }
        }
        .padding(.vertical, 4)
    }

    private func formatDate(_ dateString: String) -> String {
        // 简化版日期格式化
        let components = dateString.split(separator: " ")
        return components.first.map(String.init) ?? dateString
    }
}

struct StatusBadge: View {
    let status: ApplicationStatus

    var body: some View {
        Text(status.displayName)
            .font(.caption)
            .fontWeight(.medium)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .foregroundColor(.white)
            .cornerRadius(4)
    }

    private var backgroundColor: Color {
        switch status.color {
        case "orange": return .orange
        case "blue": return .blue
        case "red": return .red
        case "green": return .green
        case "gray": return .gray
        default: return .gray
        }
    }
}

struct ApplicationDetailView: View {
    let application: Application

    var body: some View {
        List {
            Section("基本信息") {
                InfoRow(label: "申领单号", value: application.applyNo)
                InfoRow(label: "仓库", value: application.warehouseName)
                InfoRow(label: "状态", value: application.status.displayName)
                InfoRow(label: "申领时间", value: application.applyTime)

                if let approverName = application.approverName {
                    InfoRow(label: "审批人", value: approverName)
                }

                if let approveTime = application.approveTime {
                    InfoRow(label: "审批时间", value: approveTime)
                }
            }

            if let details = application.details {
                Section("物料明细") {
                    ForEach(details) { detail in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(detail.materialName)
                                    .font(.subheadline)

                                Text(detail.materialCode)
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }

                            Spacer()

                            Text("\(detail.quantity) \(detail.unit)")
                                .font(.subheadline)
                                .fontWeight(.medium)
                        }
                    }
                }
            }

            if let remark = application.remark {
                Section("备注") {
                    Text(remark)
                        .font(.subheadline)
                }
            }

            if let approveRemark = application.approveRemark {
                Section("审批意见") {
                    Text(approveRemark)
                        .font(.subheadline)
                }
            }
        }
        .navigationTitle("申领详情")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct InfoRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .foregroundColor(.gray)
            Spacer()
            Text(value)
        }
    }
}

#Preview {
    ApplicationListView()
        .environmentObject(AuthService())
}
