package expo.modules.androidapplist

import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExpoAndroidAppListModule : Module() {
    private var packageUtilities: PackageUtilities? = null

    private fun ensurePackageUtilities(promise: Promise): PackageUtilities? {
        if (packageUtilities != null) return packageUtilities

        val packageManager = appContext.reactContext?.packageManager
        if (packageManager == null) {
            promise.reject("ERROR", "PackageManager is null", null)
            return null
        }

        packageUtilities = PackageUtilities(packageManager)
        return packageUtilities
    }

    override fun definition() = ModuleDefinition {
        Name("ExpoAndroidAppList")

        AsyncFunction("getAll") { promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val appList = utils.getAppList()
                        val mappedResults: List<Map<String, Any>> = appList.mapNotNull { packageName ->
                            utils.getPackageDetails(packageName)?.let { details ->
                                mapOf(
                                    "packageName" to details.packageName,
                                    "versionName" to (details.versionName ?: ""),
                                    "size" to details.size,
                                    "appName" to details.appName,
                                    "isSystemApp" to details.isSystemApp,
                                    "firstInstallTime" to details.firstInstallTime,
                                    "lastUpdateTime" to details.lastUpdateTime,
                                    "targetSdkVersion" to details.targetSdkVersion
                                )
                            }
                        }

                        promise.resolve(mappedResults as List<Any?>)
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }

        AsyncFunction("getNativeLibraries") { packageName: String, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val nativeLibraries = utils.getNativeLibraries(packageName)
                        promise.resolve(nativeLibraries as List<Any?>)
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }

        AsyncFunction("getPermissions") { packageName: String, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val permissions = utils.getPermissions(packageName)
                        promise.resolve(permissions as List<Any?>)
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }

        AsyncFunction("getAppIcon") { packageName: String, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val icon = utils.getAppIcon(packageName)
                        promise.resolve(icon as String?)
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }

        AsyncFunction("getFiles") { packageName: String, paths: List<String>, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val files = utils.getFiles(packageName, paths)
                        promise.resolve(files.map { file ->
                            if (file == null) return@map null

                            mapOf(
                                "content" to file.content,
                                "size" to file.size
                            )
                        })
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }

        AsyncFunction("getPackageDetails") { packageName: String, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val details = utils.getPackageDetails(packageName)
                        if (details != null) {
                            promise.resolve(mapOf(
                                "packageName" to details.packageName,
                                "versionName" to (details.versionName ?: ""),
                                "size" to details.size,
                                "appName" to details.appName,
                                "isSystemApp" to details.isSystemApp,
                                "firstInstallTime" to details.firstInstallTime,
                                "lastUpdateTime" to details.lastUpdateTime,
                                "targetSdkVersion" to details.targetSdkVersion
                            ))
                        } else {
                            promise.resolve(null)
                        }
                    } catch (e: Exception) {
                        promise.reject("ERROR", e.message, e)
                    }
                }
            } catch (e: Exception) {
                promise.reject("ERROR", e.message, e)
            }
        }
    }
}
