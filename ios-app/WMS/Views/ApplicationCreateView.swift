//
//  ApplicationCreateView.swift
//  创建申领单 (普通员工)
//

import SwiftUI

struct ApplicationCreateView: View {
    @EnvironmentObject var authService: AuthService
    @Environment(\.dismiss) var dismiss
    let onSuccess: () -> Void

    @State private var selectedWarehouseId = 1
    @State private var remark = ""
    @State private var selectedMaterials: [SelectedMaterial] = []
    @State private var isLoading = false
    @State private var errorMessage: String?

    struct SelectedMaterial: Identifiable {
        let id = UUID()
        let materialId: Int
        let materialName: String
        let materialCode: String
        let unit: String
        var quantity: Int
    }

    var body: some View {
        NavigationView {
            Form {
                Section("基本信息") {
                    Picker("申领仓库", selection: $selectedWarehouseId) {
                        Text("主仓库").tag(1)
                        Text("备件仓库").tag(2)
                    }

                    TextField("申领用途", text: $remark, axis: .vertical)
                        .lineLimit(3...6)
                }

                Section("申领物料") {
                    ForEach($selectedMaterials) { $item in
                        HStack {
                            VStack(alignment: .leading) {
                                Text(item.materialName)
                                    .font(.subheadline)

                                Text(item.materialCode)
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }

                            Spacer()

                            Stepper("\(item.quantity) \(item.unit)",
                                    value: $item.quantity,
                                    in: 1...999)
                        }
                    }
                    .onDelete(perform: deleteItems)

                    Button(action: addMockMaterial) {
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
            .navigationTitle("创建申领")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("取消") {
                        dismiss()
                    }
                }

                ToolbarItem(placement: .confirmationAction) {
                    Button("提交") {
                        submitApplication()
                    }
                    .disabled(selectedMaterials.isEmpty || isLoading)
                }
            }
        }
    }

    private func deleteItems(at offsets: IndexSet) {
        selectedMaterials.remove(atOffsets: offsets)
    }

    // 演示用:添加模拟物料
    private func addMockMaterial() {
        let mockMaterials = [
            SelectedMaterial(materialId: 1, materialName: "光纤跳线", materialCode: "MAT001", unit: "根", quantity: 1),
            SelectedMaterial(materialId: 2, materialName: "网线", materialCode: "MAT002", unit: "米", quantity: 10),
            SelectedMaterial(materialId: 3, materialName: "路由器", materialCode: "MAT003", unit: "台", quantity: 1)
        ]

        if let randomMaterial = mockMaterials.randomElement() {
            selectedMaterials.append(randomMaterial)
        }
    }

    private func submitApplication() {
        guard let token = authService.token else { return }

        isLoading = true
        errorMessage = nil

        let details = selectedMaterials.map { item in
            ApplicationDetailRequest(
                materialId: item.materialId,
                quantity: item.quantity
            )
        }

        let request = CreateApplicationRequest(
            warehouseId: selectedWarehouseId,
            remark: remark.isEmpty ? nil : remark,
            details: details
        )

        Task {
            do {
                _ = try await APIService.shared.createApplication(request: request, token: token)
                await MainActor.run {
                    isLoading = false
                    onSuccess()
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                    isLoading = false
                }
            }
        }
    }
}

#Preview {
    ApplicationCreateView(onSuccess: {})
        .environmentObject(AuthService())
}
