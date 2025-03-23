package expo.modules.androidapplist.models

data class PackageDetails(
    val packageName: String,
    val versionName: String?,
    val size: Long,
    val appName: String,
    val isSystemApp: Boolean,
    val firstInstallTime: Long,
    val lastUpdateTime: Long,
    val targetSdkVersion: Int,
)