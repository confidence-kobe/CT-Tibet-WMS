//
//  LoginView.swift
//  登录界面
//

import SwiftUI

struct LoginView: View {
    @EnvironmentObject var authService: AuthService
    @State private var username = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?

    var body: some View {
        VStack(spacing: 30) {
            Spacer()

            // Logo和标题
            VStack(spacing: 10) {
                Image(systemName: "shippingbox.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.blue)

                Text("西藏电信仓库管理系统")
                    .font(.title)
                    .fontWeight(.bold)

                Text("CT-Tibet-WMS")
                    .font(.subheadline)
                    .foregroundColor(.gray)
            }

            Spacer()

            // 登录表单
            VStack(spacing: 20) {
                VStack(alignment: .leading, spacing: 8) {
                    Text("用户名")
                        .font(.subheadline)
                        .foregroundColor(.gray)

                    TextField("请输入用户名", text: $username)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                        .autocapitalization(.none)
                        .textInputAutocapitalization(.never)
                }

                VStack(alignment: .leading, spacing: 8) {
                    Text("密码")
                        .font(.subheadline)
                        .foregroundColor(.gray)

                    SecureField("请输入密码", text: $password)
                        .textFieldStyle(RoundedBorderTextFieldStyle())
                }

                if let error = errorMessage {
                    Text(error)
                        .font(.footnote)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                }

                Button(action: handleLogin) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("登录")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.blue)
                .foregroundColor(.white)
                .cornerRadius(10)
                .disabled(isLoading || username.isEmpty || password.isEmpty)
            }
            .padding(.horizontal, 40)

            Spacer()

            Text("v1.0.0")
                .font(.caption)
                .foregroundColor(.gray)
                .padding(.bottom, 20)
        }
        .background(Color(.systemGroupedBackground))
    }

    private func handleLogin() {
        isLoading = true
        errorMessage = nil

        Task {
            do {
                try await authService.login(username: username, password: password)
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
    LoginView()
        .environmentObject(AuthService())
}
