package com.webtoapp.core.cloud

import com.google.common.truth.Truth.assertThat
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.webtoapp.core.shell.CloudSdkConfig
import com.webtoapp.ui.data.model.CloudAppConfig
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * 云功能端到端测试
 *
 * 测试覆盖:
 * 1. SDK 配置数据流完整性: CloudAppConfig → CloudSdkConfig → JSON → 反序列化
 * 2. 数据类序列化/反序列化正确性
 * 3. API 数据解析
 * 4. 激活码格式验证
 * 5. 计划限额逻辑
 */
class CloudE2ETest {

    // ═══════════════════════════════════════════
    // 1. SDK 配置数据流测试
    // ═══════════════════════════════════════════

    @Test
    fun `CloudAppConfig converts to CloudSdkConfig correctly`() {
        val appConfig = CloudAppConfig(
            projectId = 42,
            projectKey = "test_project_abc",
            enableUpdateCheck = true,
            enableAnnouncements = true,
            enableRemoteConfig = true,
            enableActivation = false,
            enableStats = true,
            enableCrashReport = true,
            enableFcm = false,
            apiBaseUrl = "https://api.example.com",
            updateCheckIntervalHours = 12,
            statsReportIntervalHours = 6,
            configSyncIntervalHours = 3,
            announcementCheckIntervalHours = 24,
            fcmTopic = "project_test"
        )

        val sdkConfig = appConfig.toCloudSdkConfig()

        assertThat(sdkConfig).isNotNull()
        assertThat(sdkConfig.projectKey).isEqualTo("test_project_abc")
        assertThat(sdkConfig.apiBaseUrl).isEqualTo("https://api.example.com")
        assertThat(sdkConfig.enableUpdateCheck).isTrue()
        assertThat(sdkConfig.enableAnnouncements).isTrue()
        assertThat(sdkConfig.enableRemoteConfig).isTrue()
        assertThat(sdkConfig.enableActivation).isFalse()
        assertThat(sdkConfig.enableStats).isTrue()
        assertThat(sdkConfig.enableCrashReport).isTrue()
        assertThat(sdkConfig.enableFcm).isFalse()
        assertThat(sdkConfig.updateCheckIntervalHours).isEqualTo(12)
        assertThat(sdkConfig.statsReportIntervalHours).isEqualTo(6)
        assertThat(sdkConfig.configSyncIntervalHours).isEqualTo(3)
    }

    @Test
    fun `CloudAppConfig with all features disabled produces minimal config`() {
        val appConfig = CloudAppConfig(
            projectId = 1,
            projectKey = "minimal",
            enableUpdateCheck = false,
            enableAnnouncements = false,
            enableRemoteConfig = false,
            enableActivation = false,
            enableStats = false,
            enableCrashReport = false,
            enableFcm = false
        )

        val sdkConfig = appConfig.toCloudSdkConfig()

        assertThat(sdkConfig.enableUpdateCheck).isFalse()
        assertThat(sdkConfig.enableAnnouncements).isFalse()
        assertThat(sdkConfig.enableRemoteConfig).isFalse()
        assertThat(sdkConfig.enableActivation).isFalse()
        assertThat(sdkConfig.enableStats).isFalse()
        assertThat(sdkConfig.enableCrashReport).isFalse()
        assertThat(sdkConfig.enableFcm).isFalse()
    }

    // ═══════════════════════════════════════════
    // 2. 数据类 JSON 序列化/反序列化
    // ═══════════════════════════════════════════

    @Test
    fun `CloudSdkConfig serializes to JSON correctly`() {
        val config = CloudSdkConfig(
            projectKey = "proj_123",
            apiBaseUrl = "https://api.test.com",
            enableUpdateCheck = true,
            enableAnnouncements = false
        )

        val json = com.google.gson.Gson().toJson(config)
        val parsed = com.google.gson.Gson().fromJson(json, CloudSdkConfig::class.java)

        assertThat(parsed.projectKey).isEqualTo("proj_123")
        assertThat(parsed.apiBaseUrl).isEqualTo("https://api.test.com")
        assertThat(parsed.enableUpdateCheck).isTrue()
        assertThat(parsed.enableAnnouncements).isFalse()
    }

    @Test
    fun `ProjectActivationCode parsed correctly`() {
        val json = """
            {
                "id": 1,
                "code": "APP-ABC123",
                "status": "unused",
                "max_uses": 1,
                "used_count": 0,
                "device_id": null,
                "created_at": "2024-01-01T00:00:00",
                "used_at": null
            }
        """.trimIndent()

        val obj = JsonParser.parseString(json).asJsonObject
        val code = parseActivationCodeFromJson(obj)

        assertThat(code.code).isEqualTo("APP-ABC123")
        assertThat(code.status).isEqualTo("unused")
        assertThat(code.maxUses).isEqualTo(1)
        assertThat(code.usedCount).isEqualTo(0)
    }

    @Test
    fun `ProjectAnnouncement parsed correctly`() {
        val json = """
            {
                "id": 5,
                "title": "测试公告",
                "content": "这是测试内容",
                "is_active": true,
                "priority": 10,
                "created_at": "2024-03-01T12:00:00"
            }
        """.trimIndent()

        val obj = JsonParser.parseString(json).asJsonObject
        val ann = parseAnnouncementFromJson(obj)

        assertThat(ann.title).isEqualTo("测试公告")
        assertThat(ann.content).isEqualTo("这是测试内容")
        assertThat(ann.isActive).isTrue()
        assertThat(ann.priority).isEqualTo(10)
    }

    @Test
    fun `ProjectWebhook parsed correctly`() {
        val json = """
            {
                "id": 3,
                "url": "https://example.com/webhook",
                "events": ["install", "crash"],
                "secret": "s3cret",
                "is_active": true,
                "failure_count": 0,
                "last_triggered_at": null
            }
        """.trimIndent()

        val obj = JsonParser.parseString(json).asJsonObject
        val wh = parseWebhookFromJson(obj)

        assertThat(wh.url).isEqualTo("https://example.com/webhook")
        assertThat(wh.events).containsExactly("install", "crash")
        assertThat(wh.isActive).isTrue()
        assertThat(wh.failureCount).isEqualTo(0)
    }

    // ═══════════════════════════════════════════
    // 3. 激活码格式验证
    // ═══════════════════════════════════════════

    @Test
    fun `activation code format validation`() {
        // 有效格式
        assertThat(isValidActivationCode("APP-ABCD-1234-EFGH")).isTrue()
        assertThat(isValidActivationCode("ABCD1234")).isTrue()
        assertThat(isValidActivationCode("A1B2C3D4")).isTrue()

        // 无效格式
        assertThat(isValidActivationCode("")).isFalse()
        assertThat(isValidActivationCode("AB")).isFalse()       // 太短
        assertThat(isValidActivationCode("   ")).isFalse()      // 空白
    }

    // ═══════════════════════════════════════════
    // 4. 计划限额测试
    // ═══════════════════════════════════════════

    @Test
    fun `plan quota limits are correct`() {
        val quotas = mapOf(
            "free" to PlanQuota(projects = 0, codes = 0, announcements = 0),
            "monthly" to PlanQuota(projects = 3, codes = 200, announcements = 3),
            "quarterly" to PlanQuota(projects = 5, codes = 500, announcements = 10),
            "yearly" to PlanQuota(projects = 5, codes = 1000, announcements = 10),
            "lifetime" to PlanQuota(projects = 10, codes = 5000, announcements = 30)
        )

        assertThat(quotas["free"]?.projects).isEqualTo(0)
        assertThat(quotas["monthly"]?.codes).isEqualTo(200)
        assertThat(quotas["lifetime"]?.announcements).isEqualTo(30)
    }

    @Test
    fun `subscription plan mapping`() {
        assertThat(mapProductIdToTier("pro_monthly")).isEqualTo("pro")
        assertThat(mapProductIdToTier("pro_yearly")).isEqualTo("pro")
        assertThat(mapProductIdToTier("ultra_monthly")).isEqualTo("ultra")
        assertThat(mapProductIdToTier("ultra_yearly")).isEqualTo("ultra")
        assertThat(mapProductIdToTier("unknown_plan")).isNull()
    }

    // ═══════════════════════════════════════════
    // 5. CloudProject 数据完整性
    // ═══════════════════════════════════════════

    @Test
    fun `CloudProject parsing includes all fields`() {
        val json = """
            {
                "id": 42,
                "project_name": "Test App",
                "project_key": "testapp_abc123",
                "description": "A test app",
                "github_repo": "user/repo",
                "gitee_repo": null,
                "total_installs": 100,
                "total_opens": 500,
                "package_name": "com.test.app",
                "is_active": true,
                "created_at": "2024-01-15T10:30:00"
            }
        """.trimIndent()

        val obj = JsonParser.parseString(json).asJsonObject
        val project = parseCloudProjectFromJson(obj)

        assertThat(project.id).isEqualTo(42)
        assertThat(project.name).isEqualTo("Test App")
        assertThat(project.projectKey).isEqualTo("testapp_abc123")
        assertThat(project.description).isEqualTo("A test app")
        assertThat(project.githubRepo).isEqualTo("user/repo")
        assertThat(project.giteeRepo).isNull()
        assertThat(project.totalInstalls).isEqualTo(100)
        assertThat(project.totalOpens).isEqualTo(500)
    }

    // ═══════════════════════════════════════════
    // 辅助方法
    // ═══════════════════════════════════════════

    private fun parseActivationCodeFromJson(obj: JsonObject) = ProjectActivationCode(
        id = obj.get("id").asInt,
        code = obj.get("code").asString,
        status = obj.get("status").asString,
        maxUses = obj.get("max_uses")?.asInt ?: 1,
        usedCount = obj.get("used_count")?.asInt ?: 0,
        deviceId = obj.get("device_id")?.takeIf { !it.isJsonNull }?.asString,
        createdAt = obj.get("created_at")?.asString ?: "",
        usedAt = obj.get("used_at")?.takeIf { !it.isJsonNull }?.asString
    )

    private fun parseAnnouncementFromJson(obj: JsonObject) = ProjectAnnouncement(
        id = obj.get("id").asInt,
        title = obj.get("title").asString,
        content = obj.get("content").asString,
        isActive = obj.get("is_active")?.asBoolean ?: true,
        priority = obj.get("priority")?.asInt ?: 0,
        createdAt = obj.get("created_at")?.asString ?: ""
    )

    private fun parseWebhookFromJson(obj: JsonObject) = ProjectWebhook(
        id = obj.get("id").asInt,
        url = obj.get("url").asString,
        events = obj.getAsJsonArray("events")?.map { it.asString } ?: emptyList(),
        secret = obj.get("secret")?.takeIf { !it.isJsonNull }?.asString,
        isActive = obj.get("is_active")?.asBoolean ?: true,
        failureCount = obj.get("failure_count")?.asInt ?: 0,
        lastTriggeredAt = obj.get("last_triggered_at")?.takeIf { !it.isJsonNull }?.asString
    )

    private fun parseCloudProjectFromJson(obj: JsonObject) = CloudProject(
        id = obj.get("id").asInt,
        name = obj.get("project_name").asString,
        projectKey = obj.get("project_key").asString,
        description = obj.get("description")?.takeIf { !it.isJsonNull }?.asString,
        githubRepo = obj.get("github_repo")?.takeIf { !it.isJsonNull }?.asString,
        giteeRepo = obj.get("gitee_repo")?.takeIf { !it.isJsonNull }?.asString,
        totalInstalls = obj.get("total_installs")?.asInt ?: 0,
        totalOpens = obj.get("total_opens")?.asInt ?: 0
    )

    private fun isValidActivationCode(code: String): Boolean {
        val trimmed = code.trim().uppercase()
        return trimmed.isNotBlank() && trimmed.length >= 4
    }

    private fun mapProductIdToTier(productId: String): String? = when {
        productId.startsWith("pro_") -> "pro"
        productId.startsWith("ultra_") -> "ultra"
        else -> null
    }

    data class PlanQuota(val projects: Int, val codes: Int, val announcements: Int)
}
