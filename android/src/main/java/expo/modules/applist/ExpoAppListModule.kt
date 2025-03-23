package expo.modules.applist

import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

class ExpoAppListModule : Module() {
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
        Name("ExpoAppList")

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

        AsyncFunction("getFileContent") { packageName: String, filenames: List<String>, promise: Promise ->
            try {
                val utils = ensurePackageUtilities(promise) ?: return@AsyncFunction

                CoroutineScope(Dispatchers.Default).launch {
                    try {
                        val content = utils.getFileContent(packageName, filenames)
                        promise.resolve(content)
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
