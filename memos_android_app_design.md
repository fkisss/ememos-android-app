 # Memos 安卓应用设计文档

## 1. 引言

本文档旨在详细描述基于现有Memos Chrome扩展功能，并新增记事本内容查看功能的一款安卓应用的设计方案。该应用将主要运行在三星S23等安卓设备上，为用户提供便捷的Memos服务访问和管理体验。

## 2. 需求分析

### 2.1. 核心功能（源自Chrome扩展）

*   **API配置**: 用户可以设置Memos服务的API URL和API Token，用于连接到其Memos实例。
*   **创建Memo**: 用户可以输入文本内容创建新的Memo。
*   **设置可见性**: 创建Memo时，用户可以指定其可见性（如 PUBLIC, PRIVATE, PROTECTED）。
*   **添加标签**: 用户可以为Memo添加标签。
*   **上传资源**: 支持上传图片等资源到Memo。
*   **搜索Memo**: 用户可以根据关键词搜索其Memos。
*   **随机Memo**: 提供一个功能以随机显示一条Memo。
*   **保存草稿**: 未发送的Memo内容应能自动保存，并在下次打开时恢复。

### 2.2. 新增功能

*   **查看记事本内容**: 用户能够浏览其所有Memos的列表，并查看单个Memo的详细内容。这是本次安卓应用的核心增强功能。
*   **编辑Memo** (推荐): 查看详细内容时，应允许用户编辑已存在的Memo。
*   **删除Memo** (推荐): 查看详细内容或在列表中，应允许用户删除Memo。

### 2.3. 用户体验目标

*   **移动端优化**: 界面和交互应针对三星S23等手机屏幕和触摸操作进行优化。
*   **响应快速**: 应用操作应流畅，网络请求有明确的加载提示。
*   **数据同步**: 与Memos后端API实时同步数据。
*   **易用性**: 界面简洁直观，易于上手。

## 3. 应用架构

推荐采用**MVVM (Model-View-ViewModel)** 架构模式，结合Jetpack组件（如LiveData, ViewModel, Room, Navigation, Coroutines, Retrofit, Jetpack Compose）进行开发。

*   **Model**: 数据层，负责处理业务逻辑、数据获取（网络API、本地数据库）。
    *   **Repository**: 协调来自不同数据源（网络、本地缓存）的数据。
    *   **Network API Client**: 使用Retrofit与Memos API进行通信。
    *   **Local Database**: 使用Room持久化存储API配置、用户偏好设置以及可选的Memos缓存。
*   **View**: UI层，负责展示数据和接收用户交互。主要由Activities/Fragments和Jetpack Compose可组合函数构成。
*   **ViewModel**: 连接View和Model，持有并准备UI所需的数据，处理用户交互逻辑，独立于UI生命周期。

## 4. 主要功能模块与界面设计

### 4.1. 启动与认证

*   **启动页 (Splash Screen)**: 应用启动时显示。
*   **API配置页 (Settings/Login Screen)**:
    *   如果未配置API或配置无效，则引导用户进入此页面。
    *   输入框：API URL, API Token。
    *   按钮：“保存/连接”。
    *   验证API有效性（如调用 `/api/v1/auth/status`）。
    *   配置信息使用SharedPreferences或Room存储。

### 4.2. 主界面 (Main Screen / Home Screen)

此界面可以作为核心交互入口，可能包含底部导航栏切换不同功能区。

*   **底部导航栏 (可选)**:
    *   **新建 (Create)**: 快速创建Memo的界面。
    *   **列表 (Memos List)**: 查看所有Memos的界面。
    *   **搜索 (Search)**: 搜索Memos的界面。
    *   **设置 (Settings)**: 进入API配置和其他设置。

### 4.3. 创建/编辑Memo界面 (Create/Edit Memo Screen)

*   **Activity/Composable Screen**
*   **UI元素**:
    *   多行文本输入框：用于输入Memo内容。
    *   可见性选择器 (Dropdown/Radio Group): PUBLIC, PRIVATE, PROTECTED。
    *   标签输入/选择器。
    *   附件按钮：点击后调用系统文件选择器或相机上传图片/文件。
    *   已选附件预览区。
    *   提交按钮。
*   **逻辑**:
    *   调用ViewModel将Memo数据（内容、可见性、标签、资源）发送到Memos API。
    *   编辑模式下，加载现有Memo数据进行修改。

### 4.4. Memos列表界面 (Memos List Screen - 新增核心)

*   **Activity/Composable Screen**
*   **UI元素**:
    *   列表 (RecyclerView / LazyColumn): 显示Memo条目。
    *   每个条目显示：Memo内容摘要、创建时间、标签、可见性图标。
    *   下拉刷新功能。
    *   顶部搜索栏 (可选，或独立搜索页)。
    *   筛选/排序按钮 (可选)。
    *   悬浮操作按钮 (FAB): 用于快速跳转到创建Memo界面。
*   **逻辑**:
    *   ViewModel从Repository获取Memos列表（优先从缓存加载，然后从API更新）。
    *   支持分页加载以提高性能。
    *   点击列表项跳转到Memo详情页。

### 4.5. Memo详情界面 (Memo Detail Screen)

*   **Activity/Composable Screen**
*   **UI元素**:
    *   完整Memo内容展示区 (支持Markdown渲染)。
    *   关联的图片/资源展示区。
    *   创建时间、更新时间、可见性、标签等信息。
    *   操作按钮：编辑、删除。
*   **逻辑**:
    *   从API或缓存加载指定Memo的完整数据。
    *   提供编辑和删除功能接口。

### 4.6. 搜索界面 (Search Screen)

*   **Activity/Composable Screen**
*   **UI元素**:
    *   搜索输入框。
    *   搜索结果列表 (类似Memos列表界面)。
*   **逻辑**:
    *   根据用户输入的关键词，调用Memos API的搜索接口 (`content_search`)。
    *   实时或延迟显示搜索结果。

### 4.7. 设置界面 (Settings Screen)

*   **Activity/Composable Screen**
*   **UI元素**:
    *   API URL输入框。
    *   API Token输入框。
    *   保存按钮。
    *   其他应用偏好设置 (如默认可见性、主题等)。
*   **逻辑**:
    *   读取和保存API配置到SharedPreferences或Room。

## 5. 数据模型 (Data Models - Kotlin Data Classes)

```kotlin
// 代表一个Memo对象 (根据API响应调整)
data class Memo(
    val id: String, // 或 Int, 根据API的uid/name
    val creatorId: String,
    val createdTs: Long,
    val updatedTs: Long,
    val content: String,
    val visibility: String, // PUBLIC, PROTECTED, PRIVATE
    val pinned: Boolean,
    val resourceList: List<MemoResource>,
    val relationList: List<Any> // 根据API调整
)

data class MemoResource(
    val id: String, // 或 Int
    val name: String,
    val publicId: String?,
    val filename: String,
    val type: String, // e.g., "image/jpeg"
    val size: Long,
    val createdTs: Long,
    val updatedTs: Long,
    val externalLink: String?,
    // 根据API响应添加获取实际资源URL的逻辑
    val displayUrl: String // 构造出的可直接显示的URL
)

data class User(
    val id: String // 或 Int
    // 其他用户信息
)

// API配置
data class ApiConfig(
    val apiUrl: String,
    val apiToken: String,
    val userId: String?
)
```

## 6. API交互

使用Retrofit库定义Memos API接口。

```kotlin
interface MemosApiService {
    @POST("api/v1/auth/status")
    suspend fun checkAuthStatus(@Header("Authorization") token: String): Response<User>

    @GET("api/v1/memos")
    suspend fun getMemos(
        @Header("Authorization") token: String,
        @Query("creator") creatorId: String? = null, // 根据需要调整
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("filter") filter: String? = null // 用于搜索和标签过滤
    ): Response<MemosResponse> // MemosResponse包含memo列表

    @POST("api/v1/memos")
    suspend fun createMemo(
        @Header("Authorization") token: String,
        @Body memoPayload: CreateMemoPayload
    ): Response<Memo>

    @GET("api/v1/memos/{memoId}") // 假设有此接口，或通过getMemos过滤获取
    suspend fun getMemoById(
        @Header("Authorization") token: String,
        @Path("memoId") memoId: String
    ): Response<Memo>

    @DELETE("api/v1/memos/{memoName}") // Chrome扩展中使用的是name, e.g., memos/123
    suspend fun deleteMemo(
        @Header("Authorization") token: String,
        @Path("memoName") memoName: String // 通常是 "memos/" + uid
    ): Response<Unit>

    // PATCH api/v1/memos/{memoName} for updates

    @POST("api/v1/resources")
    suspend fun uploadResource(
        @Header("Authorization") token: String,
        @Body resourcePayload: UploadResourcePayload
    ): Response<MemoResource> // 或包含resourceId的响应

    @GET("api/v1/memos/-/tags")
    suspend fun getTags(
        @Header("Authorization") token: String,
        @Query("filter") filter: String? // e.g., creator == 'users/USER_ID'
    ): Response<TagsResponse>
}

// Payloads and Responses
data class CreateMemoPayload(
    val content: String,
    val visibility: String? = null, // e.g., "PUBLIC"
    val resourceIdList: List<String>? = null, // 资源ID列表
    val relationList: List<Map<String, Any>>? = null
)

data class UploadResourcePayload(
    val content: String, // Base64 encoded string
    val visibility: String? = null,
    val filename: String,
    val type: String
)

data class MemosResponse(val memos: List<Memo>)
data class TagsResponse(val tagAmounts: Map<String, Int>)
```

## 7. 本地数据存储

*   **SharedPreferences**: 存储API URL, API Token, 用户ID等简单配置。
*   **Room Database (可选但推荐)**:
    *   缓存Memos列表数据，实现离线查看和更快的加载速度。
    *   存储用户偏好设置。
    *   表结构应对应`Memo`和`MemoResource`等数据模型。

## 8. 权限

*   `android.permission.INTERNET`: 用于网络API请求。
*   `android.permission.READ_EXTERNAL_STORAGE` / `android.permission.READ_MEDIA_IMAGES` / `android.permission.READ_MEDIA_VIDEO`: 用于从设备存储中选择图片/文件上传。
*   `android.permission.CAMERA`: 如果支持直接拍照上传。

## 9. 开发环境与技术栈

*   **语言**: Kotlin
*   **UI**: Jetpack Compose (推荐) 或 XML + ViewBinding
*   **架构**: MVVM
*   **异步处理**: Kotlin Coroutines
*   **依赖注入**: Hilt 或 Koin (推荐)
*   **网络**: Retrofit + OkHttp
*   **JSON解析**: Gson 或 Moshi
*   **数据库**: Room
*   **导航**: Jetpack Navigation Component
*   **IDE**: Android Studio

## 10. 后续步骤

1.  搭建安卓项目基础结构。
2.  实现API配置和认证流程。
3.  实现创建Memo功能。
4.  实现Memos列表和详情查看功能。
5.  实现搜索、标签、随机Memo等辅助功能。
6.  完善UI/UX，进行测试和调试。
7.  打包生成APK。

本文档提供了初步的设计思路，具体实现细节可能在开发过程中进行调整和优化。

