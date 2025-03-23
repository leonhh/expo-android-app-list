export type AndroidAppListPackage = {
  packageName: string;
  versionName?: string;
  size: number;
  appName: string;
  author: string;
  isSystemApp: boolean;
  firstInstallTime: number;
  lastUpdateTime: number;
  targetSdkVersion: number;
};
