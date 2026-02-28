# 🔑 方寸密 (FangCunMi)

> 方寸之间，守护你的每一份秘密。

一款纯本地的 Android 密码管理工具，所有数据均加密存储在你的设备上，不联网、不上传、不追踪。

## ✨ 功能特性

- 🔐 **主密码 + 指纹解锁** — 支持密码和生物识别双重认证
- 🔒 **AES-256-GCM 加密** — 基于 Android Keystore 硬件级加密存储
- 📋 **一键复制** — 快速复制用户名和密码
- 🔍 **实时搜索** — 按网站名或用户名快速查找
- 🎲 **随机密码生成** — 一键生成 16 位强密码
- 📊 **密码强度检测** — 实时显示密码安全等级
- 🎨 **Material 3 设计** — 淡粉灰色扁平化 UI，清爽简洁

## 📱 截图

*暂无*

## 🛠️ 技术栈

| 技术 | 说明 |
|---|---|
| **语言** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **架构** | MVVM (ViewModel + Flow) |
| **数据库** | Room |
| **加密** | Android Keystore + AES-256-GCM |
| **认证** | BiometricPrompt (CryptoObject) |

## 🚀 构建

1. 克隆仓库
```bash
git clone https://github.com/Echois404/FangCunMi.git
```

2. 用 Android Studio 打开项目

3. 等待 Gradle Sync 完成

4. Build → Generate APK

## 📄 开源协议

[MIT License](LICENSE)

## 👤 作者

**Echois404** — 借助 AI (Antigravity) 协助完成开发
