//
//  Material.swift
//  物料模型
//

import Foundation

struct Material: Codable, Identifiable {
    let id: Int
    let code: String           // 物料编码
    let name: String           // 物料名称
    let category: String       // 类别
    let spec: String?          // 规格型号
    let unit: String           // 单位
    let minStock: Int          // 最低库存
    let maxStock: Int          // 最高库存
    let remark: String?        // 备注

    enum CodingKeys: String, CodingKey {
        case id
        case code
        case name
        case category
        case spec
        case unit
        case minStock = "min_stock"
        case maxStock = "max_stock"
        case remark
    }
}

struct Inventory: Codable, Identifiable {
    let id: Int
    let warehouseId: Int
    let warehouseName: String
    let materialId: Int
    let materialCode: String
    let materialName: String
    let quantity: Int          // 当前库存数量
    let unit: String

    enum CodingKeys: String, CodingKey {
        case id
        case warehouseId = "warehouse_id"
        case warehouseName = "warehouse_name"
        case materialId = "material_id"
        case materialCode = "material_code"
        case materialName = "material_name"
        case quantity
        case unit
    }
}
