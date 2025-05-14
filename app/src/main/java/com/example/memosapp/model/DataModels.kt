package com.example.memosapp.model

// 代表一个Memo对象 (根据API响应调整)
data class Memo(
    val id: String, // 或 Int, 根据API的uid/name (Chrome扩展中使用name, e.g. memos/123, API响应中是uid)
    val creatorId: String,
    val createdTs: Long,
    val updatedTs: Long,
    val content: String,
    val visibility: String, // PUBLIC, PROTECTED, PRIVATE
    val pinned: Boolean,
    val resourceList: List<MemoResource>? = null, // API可能返回null或空列表
    val relationList: List<Any>? = null // 根据API调整, API可能返回null或空列表
)

data class MemoResource(
    val id: String, // 或 Int
    val name: String, // Chrome扩展中使用name, API响应中是uid
    val publicId: String? = null,
    val filename: String,
    val type: String, // e.g., "image/jpeg"
    val size: Long,
    val createdTs: Long,
    val updatedTs: Long,
    val externalLink: String? = null,
    // 根据API响应添加获取实际资源URL的逻辑
    // 这个字段需要在使用时基于apiUrl, name, publicId/filename动态构建
    // 例如: fun getDisplayUrl(apiUrl: String): String {
    //    return if (!externalLink.isNullOrEmpty()) externalLink 
    //           else "${apiUrl}file/${name}/${publicId ?: filename}"
    // }
)

data class User(
    val id: String // 或 Int, API响应中是id
    // 其他用户信息，例如 name, email, etc. 根据API /api/v1/auth/status 响应添加
)

// API配置，用于本地存储
data class ApiConfig(
    val apiUrl: String,
    val apiToken: String,
    val userId: String?
)

// 用于创建Memo的Payload
data class CreateMemoPayload(
    val content: String,
    val visibility: String? = "PUBLIC", // 默认为PUBLIC，与Chrome扩展行为一致
    val resourceIdList: List<String>? = null, // 资源ID列表 (注意：Chrome扩展中是resourceIdList，但Memos API v1 实际使用的是 name)
                                             // 经过确认，Memos API v1 创建memo时，如果有关联资源，是直接在content中引用，或者通过 resourceIdList 传递资源 *ID* (不是name)
                                             // Chrome 扩展中 resourceIdList 存储的是 {name, uid, type} 对象列表，提交时可能需要转换
    val relationList: List<Map<String, Any>>? = null
)

// 用于上传资源的Payload
data class UploadResourcePayload(
    val content: String, // Base64 encoded string
    val visibility: String? = "PUBLIC", // 资源可见性，通常跟随memo
    val filename: String, // 文件名
    val type: String // MIME type
)

// API响应的通用包装 (如果API总是返回一个固定结构)
// 例如，Memos API的列表通常在 `data` 或直接在根对象下
data class MemosResponse(val memos: List<Memo>)

// 标签API响应
data class TagsResponse(val tagAmounts: Map<String, Int>)

// 用于PATCH更新Memo的Payload
data class UpdateMemoPayload(
    val content: String? = null,
    val visibility: String? = null,
    val resourceIdList: List<String>? = null,
    val relationList: List<Map<String, Any>>? = null,
    val pinned: Boolean? = null
)

