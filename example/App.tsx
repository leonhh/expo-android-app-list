import { AppListPackage, ExpoAppList } from "expo-app-list";
import { Image } from "expo-image";
import { useState } from "react";
import { Button, SafeAreaView, ScrollView, View, Text } from "react-native";

export default function App() {
  const [packages, setPackages] = useState<AppListPackage[]>([]);
  const [images, setImages] = useState<Map<string, string>>(new Map());

  const getAll = async () => {
    try {
      const result = (await ExpoAppList.getAll()).filter(
        (pkg) => !pkg.isSystemApp,
      );

      setPackages(result);
    } catch (error) {
      console.error(error);
    }
  };

  const getReactNativeApps = async () => {
    try {
      const apps = await ExpoAppList.getAll();

      const result = [];
      const icons = new Map();

      for (const pkg of apps) {
        if (pkg.isSystemApp) {
          continue;
        }

        const nativeLibraries = await ExpoAppList.getNativeLibraries(
          pkg.packageName,
        );
        const icon = await ExpoAppList.getAppIcon(pkg.packageName);

        if (
          nativeLibraries.includes("libreactnative.so") ||
          nativeLibraries.includes("libreactnativejni.so") ||
          nativeLibraries.includes("libjsijniprofiler.so")
        ) {
          result.push(pkg);
          icons.set(pkg.packageName, icon);
        }
      }

      setPackages(result);
      setImages(icons);
    } catch (error) {
      console.error(error);
    }
  };

  const getFileContent = async (packageName: string) => {
    const result = await ExpoAppList.getFileContent(packageName, [
      "app.config.json",
      "app.config",
    ]);

    console.log(result);
  };

  const getPermissions = async (packageName: string) => {
    const result = await ExpoAppList.getPermissions(packageName);

    console.log(result);
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={{ padding: 16, gap: 5 }}>
        <Button onPress={getAll} title="Get all packages" />
        <Button
          onPress={getReactNativeApps}
          title="Get only react-native packages"
        />

        {packages.map((pkg, index) => (
          <View style={styles.group} key={pkg.packageName}>
            <Text>
              {pkg.appName} ({pkg.packageName})
            </Text>

            <Image
              source={{
                caheKey: pkg.packageName + 1,
                uri: `data:image/png;base64,${images.get(pkg.packageName)}`,
              }}
              style={{ width: 100, height: 100 }}
            />

            <Text>{JSON.stringify(pkg, null, 2)}</Text>

            <Button
              onPress={() => getFileContent(pkg.packageName)}
              title="Get file content"
            />

            <Button
              onPress={() => getPermissions(pkg.packageName)}
              title="Get permissions"
            />
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = {
  group: {
    backgroundColor: "#fff",
    borderRadius: 16,
    padding: 12,
    gap: 8,
  },
  container: {
    flex: 1,
    backgroundColor: "#eee",
  },
};
