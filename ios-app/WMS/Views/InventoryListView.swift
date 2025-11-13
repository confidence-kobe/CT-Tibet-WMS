//
//  InventoryListView.swift
//  库存列表
//

import SwiftUI

struct InventoryListView: View {
    @EnvironmentObject var authService: AuthService
    @State private var inventories: [Inventory] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedWarehouse = 1

    var filteredInventories: [Inventory] {
        if searchText.isEmpty {
            return inventories
        } else {
            return inventories.filter { inventory in
                inventory.materialName.localizedCaseInsensitiveContains(searchText) ||
                inventory.materialCode.localizedCaseInsensitiveContains(searchText)
            }
        }
    }

    var body: some View {
        NavigationView {
            VStack {
                // 仓库选择器
                Picker("仓库", selection: $selectedWarehouse) {
                    Text("主仓库").tag(1)
                    Text("备件仓库").tag(2)
                }
                .pickerStyle(.segmented)
                .padding()
                .onChange(of: selectedWarehouse) { _ in
                    Task {
                        await loadInventory()
                    }
                }

                if isLoading {
                    ProgressView("加载中...")
                } else if filteredInventories.isEmpty {
                    VStack(spacing: 20) {
                        Image(systemName: "archivebox")
                            .font(.system(size: 60))
                            .foregroundColor(.gray)

                        Text(searchText.isEmpty ? "暂无库存数据" : "未找到相关物料")
                            .foregroundColor(.gray)
                    }
                    .frame(maxHeight: .infinity)
                } else {
                    List(filteredInventories) { inventory in
                        InventoryRow(inventory: inventory)
                    }
                    .searchable(text: $searchText, prompt: "搜索物料名称或编码")
                    .refreshable {
                        await loadInventory()
                    }
                }
            }
            .navigationTitle("库存查询")
        }
        .task {
            await loadInventory()
        }
    }

    private func loadInventory() async {
        guard let token = authService.token else { return }

        isLoading = true

        do {
            let result = try await APIService.shared.getInventory(
                warehouseId: selectedWarehouse,
                token: token
            )
            await MainActor.run {
                inventories = result
                isLoading = false
            }
        } catch {
            await MainActor.run {
                isLoading = false
            }
        }
    }
}

struct InventoryRow: View {
    let inventory: Inventory

    var body: some View {
        HStack(alignment: .top) {
            Image(systemName: "cube.box.fill")
                .font(.system(size: 40))
                .foregroundColor(.blue)
                .frame(width: 50)

            VStack(alignment: .leading, spacing: 6) {
                Text(inventory.materialName)
                    .font(.headline)

                Text(inventory.materialCode)
                    .font(.caption)
                    .foregroundColor(.gray)

                HStack(spacing: 4) {
                    Image(systemName: "archivebox")
                        .font(.caption)
                        .foregroundColor(.gray)

                    Text(inventory.warehouseName)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }

            Spacer()

            VStack(alignment: .trailing) {
                Text("\(inventory.quantity)")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(stockColor(inventory.quantity))

                Text(inventory.unit)
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 8)
    }

    private func stockColor(_ quantity: Int) -> Color {
        if quantity == 0 {
            return .red
        } else if quantity < 10 {
            return .orange
        } else {
            return .green
        }
    }
}

#Preview {
    InventoryListView()
        .environmentObject(AuthService())
}
