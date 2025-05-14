package com.example.memosapp.network

import com.example.memosapp.model.*
import retrofit2.Response
import retrofit2.http.*

interface MemosApiService {

    // 检查认证状态，并获取用户信息
    @POST("api/v1/auth/status") // Chrome扩展中是POST，但通常获取状态是GET，这里遵循扩展
    suspend fun checkAuthStatus(@Header("Authorization") token: String): Response<User> // API返回的是User对象，包含id

    // 获取Memos列表
    @GET("api/v1/memos")
    suspend fun getMemos(
        @Header("Authorization") token: String,
        @Query("creator") creatorId: String? = null, // 根据用户ID过滤，例如 "users/1"
        @Query("limit") limit: Int? = null, // 分页：每页数量
        @Query("offset") offset: Int? = null, // 分页：偏移量
        @Query("filter") filter: String? = null // 搜索和标签过滤，例如 `creator == 'users/1' && visibilities == ['PUBLIC', 'PROTECTED'] && content_search == ['keyword']`
    ): Response<MemosResponse> // MemosResponse 包含 List<Memo>

    // 创建一个新的Memo
    @POST("api/v1/memos")
    suspend fun createMemo(
        @Header("Authorization") token: String,
        @Body memoPayload: CreateMemoPayload
    ): Response<Memo> // API返回创建的Memo对象

    // 获取单个Memo的详细信息 (Memos API v1 通常通过 getMemos 带 filter 实现，或者直接用 name)
    // 如果直接用ID (uid)，路径可能是 /api/v1/memos/{uid}
    // Chrome扩展中删除和链接跳转使用的是 name (e.g., memos/123)
    // 假设可以通过UID获取
    @GET("api/v1/memos/{memoUid}")
    suspend fun getMemoByUid(
        @Header("Authorization") token: String,
        @Path("memoUid") memoUid: String // Memo的UID
    ): Response<Memo>

    // 更新一个已存在的Memo
    // Memos API v1 使用 PATCH /api/v1/memos/{memoName}
    @PATCH("api/v1/memos/{memoName}") // memoName 通常是 "memos/" + uid
    suspend fun updateMemo(
        @Header("Authorization") token: String,
        @Path("memoName") memoName: String,
        @Body updateMemoPayload: UpdateMemoPayload
    ): Response<Memo> // API返回更新后的Memo对象

    // 删除一个Memo
    // Memos API v1 使用 DELETE /api/v1/memos/{memoName}
    @DELETE("api/v1/memos/{memoName}") // memoName 通常是 "memos/" + uid
    suspend fun deleteMemo(
        @Header("Authorization") token: String,
        @Path("memoName") memoName: String
    ): Response<Unit> // 通常成功删除返回200 OK 无内容

    // 上传资源文件
    @POST("api/v1/resources")
    suspend fun uploadResource(
        @Header("Authorization") token: String,
        @Body resourcePayload: UploadResourcePayload
    ): Response<MemoResource> // API返回创建的Resource对象，包含ID (uid)

    // 获取当前用户的标签列表
    // Chrome扩展中路径是 /api/v1/memos/-/tags?filter=creator%20%3D%3D%20'users%2FUSER_ID'
    @GET("api/v1/memos/-/tags")
    suspend fun getTags(
        @Header("Authorization") token: String,
        @Query("filter") filter: String // 例如: "creator == 'users/USER_ID'"
    ): Response<TagsResponse> // TagsResponse包含Map<String, Int>
}

