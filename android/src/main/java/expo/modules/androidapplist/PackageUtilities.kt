package expo.modules.androidapplist

import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.os.Build
import java.io.File
import android.graphics.drawable.Drawable
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.Bitmap
import android.graphics.Canvas
import android.content.pm.ApplicationInfo
import java.util.zip.ZipFile
import android.util.Log
import kotlinx.coroutines.*
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

import expo.modules.androidapplist.models.PackageDetails

class PackageUtilities(
    private val packageManager: PackageManager,
) {
    companion object {
        private const val TAG = "PackageUtilities"
        private const val DEFAULT_ICON_SIZE = 256
    }

    private val packageInfoCache = ConcurrentHashMap<String, PackageInfo>()
    private val iconCache = ConcurrentHashMap<String, String>()

    suspend fun getAppList(): List<String> = withContext(Dispatchers.Default) {
        try {
            packageInfoCache.clear()
            val packages = getInstalledPackages()
            packages.forEach { packageInfo ->
                packageInfoCache[packageInfo.packageName] = packageInfo
            }
            Log.d(TAG, "Cache refreshed with ${packageInfoCache.size} packages")

            packageInfoCache.keys().toList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app list", e)
            emptyList()
        }
    }

    suspend fun getPackageDetails(packageName: String): PackageDetails? =
        withContext(Dispatchers.Default) {
            try {
                val packageInfo = getCachedPackageInfo(packageName) ?: return@withContext null
                val appInfo = packageInfo.applicationInfo ?: return@withContext null

                PackageDetails(
                    packageName = packageInfo.packageName,
                    versionName = packageInfo.versionName ?: "",
                    size = getApkSize(appInfo),
                    appName = getApplicationLabel(packageInfo),
                    isSystemApp = isSystemApp(appInfo),
                    firstInstallTime = packageInfo.firstInstallTime,
                    lastUpdateTime = packageInfo.lastUpdateTime,
                    targetSdkVersion = appInfo.targetSdkVersion,
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting package details for $packageName", e)
                null
            }
        }

    suspend fun getNativeLibraries(packageName: String): List<String> =
        withContext(Dispatchers.Default) {
            try {
                val packageInfo =
                    getCachedPackageInfo(packageName) ?: return@withContext emptyList()
                val appInfo = packageInfo.applicationInfo ?: return@withContext emptyList()

                findNativeLibraries(appInfo)
            } catch (e: Exception) {
                Log.e(TAG, "Error getting native libraries for $packageName", e)
                emptyList()
            }
        }

    suspend fun getAppIcon(
        packageName: String,
        maxSize: Int = DEFAULT_ICON_SIZE
    ): String? = withContext(Dispatchers.Default) {
        try {
            iconCache[packageName]?.let { return@withContext it }

            val packageInfo = getCachedPackageInfo(packageName) ?: return@withContext null
            val appInfo = packageInfo.applicationInfo ?: return@withContext null

            loadAppIcon(appInfo, maxSize)?.also { base64Icon ->
                iconCache[packageName] = base64Icon
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app icon for $packageName", e)
            null
        }
    }

    suspend fun getPermissions(packageName: String): List<String> =
        withContext(Dispatchers.Default) {
            try {
                val packageInfo =
                    getCachedPackageInfo(packageName) ?: return@withContext emptyList()
                val permissions = mutableListOf<String>()
                
                packageInfo.requestedPermissions?.let { permissions.addAll(it) }
                
                permissions
            } catch (e: Exception) {
                Log.e(TAG, "Error getting app permissions for $packageName", e)
                emptyList()
            }
        }

    suspend fun getFileContent(packageName: String, filenames: List<String>): Map<String, String>? =
        withContext(Dispatchers.IO) {
            try {
                val packageInfo = getCachedPackageInfo(packageName) ?: return@withContext null
                val appInfo = packageInfo.applicationInfo ?: return@withContext null
                val results = mutableMapOf<String, String>()

                // Try to find files in the APK
                ZipFile(appInfo.sourceDir).use { zip ->
                    zip.entries().asSequence()
                        .filter { entry -> !entry.isDirectory && filenames.any { filename -> 
                            entry.name.endsWith(filename) 
                        }}
                        .forEach { entry ->
                            try {
                                zip.getInputStream(entry).use { input ->
                                    val content = InputStreamReader(input).readText()
                                    results[entry.name] = content
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to read file ${entry.name} from APK: ${e.message}")
                            }
                        }
                }

                // Also check split APKs if they exist
                appInfo.splitSourceDirs?.forEach { splitSourceDir ->
                    try {
                        ZipFile(splitSourceDir).use { zip ->
                            zip.entries().asSequence()
                                .filter { entry -> !entry.isDirectory && filenames.any { filename -> 
                                    entry.name.endsWith(filename) 
                                }}
                                .forEach { entry ->
                                    try {
                                        zip.getInputStream(entry).use { input ->
                                            val content = InputStreamReader(input).readText()
                                            results[entry.name] = content
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to read file ${entry.name} from split APK: ${e.message}")
                                    }
                                }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to process split APK $splitSourceDir: ${e.message}")
                    }
                }

                if (results.isEmpty()) null else results
            } catch (e: Exception) {
                Log.e(TAG, "Error getting file content for $packageName", e)
                null
            }
        }

    private suspend fun getCachedPackageInfo(packageName: String): PackageInfo? =
        withContext(Dispatchers.Default) {
            packageInfoCache[packageName] ?: getPackageInfo(packageName)
        }

    private fun getPackageInfo(packageName: String): PackageInfo? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(
                        (PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS).toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting package info for $packageName", e)
            null
        }
    }

    private fun getInstalledPackages(): List<PackageInfo> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(
                    PackageManager.PackageInfoFlags.of(
                        (PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS).toLong()
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed packages", e)
            emptyList()
        }
    }

    private fun getApkSize(appInfo: ApplicationInfo): Long {
        if (appInfo.sourceDir == null) return 0L

        return try {
            File(appInfo.sourceDir).length()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get APK size for ${appInfo.packageName}", e)
            0L
        }
    }

    private fun getApplicationLabel(packageInfo: PackageInfo): String {
        return try {
            packageInfo.applicationInfo?.let { appInfo ->
                packageManager.getApplicationLabel(appInfo).toString()
            } ?: packageInfo.packageName
        } catch (e: Exception) {
            packageInfo.packageName
        }
    }

    private suspend fun loadAppIcon(
        appInfo: ApplicationInfo,
        maxSize: Int,
    ): String? = withContext(Dispatchers.IO) {
        try {
            val drawable = appInfo.loadIcon(packageManager)

            drawableToBase64(drawable, maxSize)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get app icon for ${appInfo.packageName}", e)
            null
        }
    }

    private suspend fun drawableToBase64(
        drawable: Drawable,
        maxSize: Int
    ): String = withContext(Dispatchers.IO) {
        val originalWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: maxSize
        val originalHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: maxSize

        val scale = maxSize.toFloat() / maxOf(originalWidth, originalHeight)
        val scaledWidth = (originalWidth * scale).toInt()
        val scaledHeight = (originalHeight * scale).toInt()

        val bitmap = Bitmap.createBitmap(
            scaledWidth,
            scaledHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        try {
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            ByteArrayOutputStream().use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
            }
        } finally {
            bitmap.recycle()
        }
    }

    private suspend fun findNativeLibraries(appInfo: ApplicationInfo): List<String> =
        withContext(Dispatchers.IO) {
            val nativeLibs = mutableSetOf<String>()

            val architectures = listOf(
                "arm64-v8a",
                "armeabi-v7a",
                "x86",
                "x86_64"
            )

            val nativeLibDirs = buildList {
                add(appInfo.nativeLibraryDir)
                add("${appInfo.dataDir}/lib")
                add("${appInfo.sourceDir}/lib")

                architectures.forEach { arch ->
                    add("${appInfo.sourceDir}/lib/$arch")
                }

                appInfo.splitSourceDirs?.forEach { splitSourceDir ->
                    add("$splitSourceDir/lib")
                    architectures.forEach { arch ->
                        add("$splitSourceDir/lib/$arch")
                    }
                }
            }.distinct()

            nativeLibDirs.forEach { dirPath ->
                try {
                    val dir = File(dirPath)
                    if (dir.exists() && dir.isDirectory) {
                        dir.walkTopDown()
                            .maxDepth(5)
                            .filter { it.isFile && it.extension == "so" }
                            .forEach { file ->
                                nativeLibs.add(file.name)
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to scan directory $dirPath: ${e.message}")
                }
            }

            try {
                ZipFile(appInfo.sourceDir).use { zip ->
                    zip.entries().asSequence()
                        .filter { entry ->
                            entry.name.endsWith(".so") &&
                                    architectures.any { arch -> entry.name.contains("lib/$arch/") }
                        }
                        .take(1000)
                        .forEach { entry ->
                            val libName = entry.name.split("/").last()
                            nativeLibs.add(libName)
                        }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to scan APK: ${e.message}")
            }

            appInfo.splitSourceDirs?.forEach { splitSourceDir ->
                try {
                    ZipFile(splitSourceDir).use { zip ->
                        zip.entries().asSequence()
                            .filter { entry ->
                                entry.name.endsWith(".so") &&
                                        architectures.any { arch -> entry.name.contains("lib/$arch/") }
                            }
                            .take(1000)
                            .forEach { entry ->
                                val libName = entry.name.split("/").last()
                                nativeLibs.add(libName)
                            }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to scan split APK $splitSourceDir: ${e.message}")
                }
            }

            nativeLibs.toList()
        }

    private fun isSystemApp(appInfo: ApplicationInfo?): Boolean {
        return appInfo?.let {
            (it.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        } ?: false
    }
}