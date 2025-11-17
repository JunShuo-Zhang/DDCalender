# DDCalendar

基于 Jetpack Compose 开发的 Android 日历应用，支持月/周/日视图、事件管理、提醒功能和农历显示。

## 核心功能

### 日历视图
- **月视图**：完整月历网格，支持年月快速选择
- **周视图**：当前周时间轴显示
- **日视图**：单日详细时间轴
- 滑动手势切换视图，流畅动画过渡

### 事件管理
- 创建、编辑、删除日程
- 全天事件和定时事件支持
- 时间冲突验证
- 长按日期快速创建

### 提醒系统
- 7种提醒时间：准时、提前5/15/30分钟、1小时、1天
- 提醒类型：消息通知
- 基于 WorkManager 的可靠调度

### 农历功能
- 公历转农历（1900-2100年）
- 传统节日和公历节日标记
- 24节气显示
- 基于寿星万年历算法

## 技术栈

- **架构**：MVVM + Repository 模式
- **UI**：Jetpack Compose + Material 3
- **依赖注入**：Hilt
- **数据库**：Room
- **异步**：Kotlin Coroutines + Flow
- **导航**：Navigation Compose
- **后台任务**：WorkManager

### 项目结构
```
app/src/main/java/com/calendar/ddcalendar/
├── data/          # 数据层（Room + Repository）
├── ui/            # UI 层（Compose 组件）
├── viewmodel/     # ViewModel
├── utils/         # 工具类（日期、农历）
└── di/            # Hilt 模块
```

## 系统要求

- Android 8.0 (API 26) 及以上
- 支持 Android 15 (API 35)

## 快速开始

**环境要求**：Android Studio Ladybug+ | JDK 17 | Kotlin 2.0+

1. 克隆项目并用 Android Studio 打开
2. 同步 Gradle 依赖
3. 运行应用

**运行测试**：`./gradlew test`

## 主要依赖

| 依赖 | 版本 |
|------|------|
| Compose BOM | 2024.12.01 |
| Hilt | 2.52 |
| Room | 2.6.1 |
| Navigation | 2.8.5 |
| WorkManager | 2.10.0 |

## 特色实现

- **农历算法**：基于寿星万年历，支持 1900-2100 年精确转换
- **提醒系统**：WorkManager 可靠调度，设备重启自动恢复
- **响应式 UI**：Flow + StateFlow 实现数据自动更新
- **性能优化**：批量农历转换、协程取消机制

## 许可证

本项目采用 MIT 许可证

## 致谢

- 寿星万年历算法
- Android Jetpack 团队
- Material Design 团队
