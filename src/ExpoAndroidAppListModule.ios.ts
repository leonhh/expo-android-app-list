export default {
  getAll: async () => {
    return [];
  },
  getNativeLibraries: async (packageName: string) => {
    return [];
  },
  getAppIcon: async (packageName: string) => {
    return "";
  },
  getFiles: async (packageName: string, paths: string[]) => {
    return [];
  },
  getPermissions: async (packageName: string) => {
    return [];
  },
  getPackageDetails: async (packageName: string) => {
    return null;
  }
};
