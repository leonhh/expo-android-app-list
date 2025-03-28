import { NativeModule, requireNativeModule } from "expo-modules-core";
import { AndroidAppListPackage, FileInfo } from "./ExpoAndroidAppList.types";

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
   * Reads the content and size of specified files from an app package
   * @param packageName The package name of the app
   * @param paths Array of file paths to search for
   * @returns An array of file info objects in the same order as the input paths, with null for files that were not found
   * @example
   * // Search for app config files
   * getFiles("com.example.app", ["app.config.json", "app.config"])
   * // Search for specific config files in assets directory
   * getFiles("com.example.app", ["assets/config/settings.json"])
   */
  getFiles(
    packageName: string,
    paths: string[],
  ): Promise<(FileInfo | null)[]>;
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

export default requireNativeModule("ExpoAndroidAppList") as ExpoAndroidAppListModule;
