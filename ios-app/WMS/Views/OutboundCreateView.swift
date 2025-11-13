//
//  OutboundCreateView.swift
//  创建直接出库单 (仓管员专用)
//

import SwiftUI

struct OutboundCreateView: View {
    @EnvironmentObject var authService: AuthService
    @State private var selectedWarehouseId = 1
    @State private var selectedRecipientId = 1
    @State private var remark = ""
    @State private var materials: [Material] = []
    @State private var selectedMaterials: [SelectedMaterial] = []
    @State private var isLoading = false
    @State private var showSuccess = false
    @State private var errorMessage: String?

    struct SelectedMaterial: Identifiable {
        let id = UUID()
        var material: Material
        var quantity: Int
    }

    var body: some View {
        NavigationView {
            Form {
                Section("基本信息") {
                    Picker("仓库", selection: $selectedWarehouseId) {
                        Text("主仓库").tag(1)
                        Text("备件仓库").tag(2)
                    }

                    Picker("领用人", selection: $selectedRecipientId) {
                        Text("张三").tag(1)
                        Text("李四").tag(2)
                        Text("王五").tag(3)
                    }

                    TextField("备注", text: $remark, axis: .vertical)
                        .lineLimit(3...6)
                }

                Section("出库物料") {
                    ForEach($selectedMaterials) { $item in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(item.material.name)
                                    .font(.subheadline)

                                Text(item.material.code)
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }

                            Spacer()

                            Stepper("\(item.quantity) \(item.material.unit)",
                                    value: $item.quantity,
                                    in: 1...999)
                        }
                    }
                    .onDelete(perform: deleteItems)

                    Button(action: { /* TODO: 选择物料 */ }) {
                        Label("添加物料", systemImage: "plus.circle.fill")
                    }
                }

                if let error = errorMessage {
                    Section {
                        Text(error)
                            .font(.footnote)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle("直接出库")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button("提交") {
                        submitOutbound()
                    }
                    .disabled(selectedMaterials.isEmpty || isLoading)
                }
            }
            .alert("出库成功", isPresented: $showSuccess) {
                Button("确定", role: .cancel) {
                    clearForm()
                }
            } message: {
                Text("出库单已创建,库存已扣减")
            }
        }
    }

    private func deleteItems(at offsets: IndexSet) {
        selectedMaterials.remove(atOffsets: offsets)
    }

    private func submitOutbound() {
        guard let token = authService.token else { return }

        isLoading = true
        errorMessage = nil

        let details = selectedMaterials.map { item in
            OutboundDetailRequest(
                materialId: item.material.id,
                quantity: item.quantity
            )
        }

        let request = CreateOutboundRequest(
            warehouseId: selectedWarehouseId,
            recipientId: selectedRecipientId,
            remark: remark.isEmpty ? nil : remark,
            details: details
        )

        Task {
            do {
                _ = try await APIService.shared.createOutbound(request: request, token: token)
                await MainActor.run {
                    showSuccess = true
                    isLoading = false
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }

    private func clearForm() {
        selectedMaterials = []
        remark = ""
    }
}

#Preview {
    OutboundCreateView()
        .environmentObject(AuthService())
}
