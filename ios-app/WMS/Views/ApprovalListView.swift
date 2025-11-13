//
//  ApprovalListView.swift
//  审批列表 (仓管员)
//

import SwiftUI

struct ApprovalListView: View {
    @EnvironmentObject var authService: AuthService
    @State private var applications: [Application] = []
    @State private var isLoading = false

    var body: some View {
        NavigationView {
            Group {
                if isLoading {
                    ProgressView("加载中...")
                } else if applications.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "checkmark.circle")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)

                        Text("暂无待审批申领")
                            .foregroundColor(.gray)
                    }
                } else {
                    List(applications) { application in
                        NavigationLink(destination: ApprovalDetailView(
                            application: application,
                            onApproved: {
                                Task {
                                    await loadPendingApprovals()
                                }
                            }
                        )) {
                            ApprovalRow(application: application)
                        }
                    }
                    .refreshable {
                        await loadPendingApprovals()
                    }
                }
            }
            .navigationTitle("待审批")
        }
        .task {
            await loadPendingApprovals()
        }
    }

    private func loadPendingApprovals() async {
        guard let token = authService.token else { return }

        isLoading = true

        do {
            let result = try await APIService.shared.getPendingApprovals(token: token)
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

struct ApprovalRow: View {
    let application: Application

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(application.applyNo)
                    .font(.headline)

                Spacer()

                Image(systemName: "clock.fill")
                    .foregroundColor(.orange)
            }

            HStack {
                Label(application.applicantName, systemImage: "person")
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
        let components = dateString.split(separator: " ")
        return components.first.map(String.init) ?? dateString
    }
}

struct ApprovalDetailView: View {
    @EnvironmentObject var authService: AuthService
    @Environment(\.dismiss) var dismiss
    let application: Application
    let onApproved: () -> Void

    @State private var approveRemark = ""
    @State private var isProcessing = false
    @State private var showApproveConfirm = false
    @State private var showRejectConfirm = false
    @State private var errorMessage: String?

    var body: some View {
        List {
            Section("申领信息") {
                InfoRow(label: "申领单号", value: application.applyNo)
                InfoRow(label: "申领人", value: application.applicantName)
                InfoRow(label: "部门", value: application.deptName)
                InfoRow(label: "仓库", value: application.warehouseName)
                InfoRow(label: "申领时间", value: application.applyTime)
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
                Section("申领用途") {
                    Text(remark)
                        .font(.subheadline)
                }
            }

            Section("审批意见") {
                TextField("请输入审批意见(可选)", text: $approveRemark, axis: .vertical)
                    .lineLimit(3...6)
            }

            if let error = errorMessage {
                Section {
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                }
            }

            Section {
                HStack(spacing: 15) {
                    Button(action: { showRejectConfirm = true }) {
                        Text("驳回")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.red)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                    .buttonStyle(.plain)

                    Button(action: { showApproveConfirm = true }) {
                        Text("通过")
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color.green)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .navigationTitle("审批详情")
        .navigationBarTitleDisplayMode(.inline)
        .disabled(isProcessing)
        .alert("确认通过", isPresented: $showApproveConfirm) {
            Button("取消", role: .cancel) {}
            Button("确定") {
                handleApproval(approve: true)
            }
        } message: {
            Text("确认通过该申领吗?系统将自动创建出库单。")
        }
        .alert("确认驳回", isPresented: $showRejectConfirm) {
            Button("取消", role: .cancel) {}
            Button("确定", role: .destructive) {
                handleApproval(approve: false)
            }
        } message: {
            Text("确认驳回该申领吗?")
        }
    }

    private func handleApproval(approve: Bool) {
        guard let token = authService.token else { return }

        isProcessing = true
        errorMessage = nil

        let request = ApprovalRequest(
            approve: approve,
            remark: approveRemark.isEmpty ? nil : approveRemark
        )

        Task {
            do {
                _ = try await APIService.shared.approveApplication(
                    applyId: application.id,
                    request: request,
                    token: token
                )
                await MainActor.run {
                    isProcessing = false
                    onApproved()
                    dismiss()
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isProcessing = false
                }
            }
        }
    }
}

#Preview {
    ApprovalListView()
        .environmentObject(AuthService())
}
