import { NativeModule, requireNativeModule } from "expo-modules-core";
import { AndroidAppListPackage } from "./ExpoAndroidAppList.types";

declare class ExpoAndroidAppListModule extends NativeModule {
  /**
   * Gets a list of all installed apps
   * @returns An array of installed apps
   */
  getAll(): Promise<AndroidAppListPackage[]>;
  /**
   * Gets the native libraries of an app
   * @param packageName The package name of the app
   * @returns An array of native libraries, or null if no libraries
   */
  getNativeLibraries(packageName: string): Promise<string[]>;
  /**
   * Gets the icon of an app
   * @param packageName The package name of the app
   * @param size The size of the icon to return
   * @returns The base64-encoded icon of the app, or null if no icon is found
   */
  getAppIcon(packageName: string, size?: number): Promise<string>;
  /**
   * Reads the content of specified files from an app package
   * @param packageName The package name of the app
   * @param filenames Array of filenames to search for (e.g., ["app.config.json", "app.config"])
   * @returns A map of file paths to their contents, or null if no files are found
   * @example
   * // Search for app config files
   * getFileContent("com.example.app", ["app.config.json", "app.config"])
   * // Search for specific config files
   * getFileContent("com.example.app", ["config.json", "settings.json"])
   */
  getFileContent(
    packageName: string,
    filenames: string[],
  ): Promise<Record<string, string> | null>;
  /**
   * Gets the permissions of an app
   * @param packageName The package name of the app
   * @returns An array of permissions, or null if no permissions
   */
  getPermissions(packageName: string): Promise<string[]>;
  /**
   * Gets detailed information about a specific package
   * @param packageName The package name of the app
   * @returns Detailed information about the package, or null if not found
   */
  getPackageDetails(packageName: string): Promise<AndroidAppListPackage | null>;
}

export default requireNativeModule<ExpoAndroidAppListModule>("ExpoAndroidAppList");
