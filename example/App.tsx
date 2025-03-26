import { AndroidAppListPackage, ExpoAndroidAppList } from "expo-android-app-list";
import { Image, ImageSource } from "expo-image";
import { useState } from "react";
import { Button, SafeAreaView, ScrollView, View, Text, StyleSheet } from "react-native";

export default function App() {
  const [packages, setPackages] = useState<AndroidAppListPackage[]>([]);
  const [images, setImages] = useState<Map<string, string>>(new Map());
  const [selectedPackage, setSelectedPackage] = useState<AndroidAppListPackage | null>(null);

  const getAll = async () => {
    try {
      const result = (await ExpoAndroidAppList.getAll()).filter(
        (pkg) => !pkg.isSystemApp,
      );

      setPackages(result);
    } catch (error) {
      console.error(error);
    }
  };

  const getReactNativeApps = async () => {
    try {
      const apps = await ExpoAndroidAppList.getAll();

      const result = [];
      const icons = new Map();

      for (const pkg of apps) {
        if (pkg.isSystemApp) {
          continue;
        }

        const nativeLibraries = await ExpoAndroidAppList.getNativeLibraries(
          pkg.packageName,
        );
        const icon = await ExpoAndroidAppList.getAppIcon(pkg.packageName);

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
    const result = await ExpoAndroidAppList.getFileContent(packageName, [
      "app.config.json",
      "app.config",
    ]);

    console.log(result);
  };

  const getPermissions = async (packageName: string) => {
    const result = await ExpoAndroidAppList.getPermissions(packageName);

    console.log(result);
  };

  const getPackageDetails = async (packageName: string) => {
    try {
      const details = await ExpoAndroidAppList.getPackageDetails(packageName);
      if (details) {
        setSelectedPackage(details);
      }
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={{ padding: 16, gap: 5 }}>
        <Button onPress={getAll} title="Get all packages" />
        <Button
          onPress={getReactNativeApps}
          title="Get only react-native packages"
        />
        <Button
          onPress={() => getPackageDetails("expo.modules.androidapplist.example")}
          title="Get example details"
        />


        {selectedPackage && (
          <View style={styles.detailsContainer}>
            <Text style={styles.detailsTitle}>Package Details</Text>
            <Text style={styles.detailsText}>Name: {selectedPackage.appName}</Text>
            <Text style={styles.detailsText}>Package: {selectedPackage.packageName}</Text>
            <Text style={styles.detailsText}>Version: {selectedPackage.versionName}</Text>
            <Text style={styles.detailsText}>Size: {(selectedPackage.size / 1024 / 1024).toFixed(2)} MB</Text>
            <Text style={styles.detailsText}>First Install: {new Date(selectedPackage.firstInstallTime).toLocaleDateString()}</Text>
            <Text style={styles.detailsText}>Last Update: {new Date(selectedPackage.lastUpdateTime).toLocaleDateString()}</Text>
            <Text style={styles.detailsText}>Target SDK: {selectedPackage.targetSdkVersion}</Text>
            <Button title="Close Details" onPress={() => setSelectedPackage(null)} />
          </View>
        )}

        {packages.map((pkg, index) => (
          <View style={styles.group} key={pkg.packageName}>
            <Text>
              {pkg.appName} ({pkg.packageName})
            </Text>

            <Image
              source={{
                cacheKey: pkg.packageName,
                uri: `data:image/png;base64,${images.get(pkg.packageName)}`,
              } satisfies ImageSource}
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

            <Button
              onPress={() => getPackageDetails(pkg.packageName)}
              title="View Details"
            />
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  group: {
    padding: 10,
    borderWidth: 1,
    borderColor: '#ccc',
    borderRadius: 5,
    marginVertical: 5,
  },
  detailsContainer: {
    padding: 15,
    backgroundColor: '#f5f5f5',
    borderRadius: 10,
    marginVertical: 10,
    alignItems: 'center',
  },
  detailsTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 10,
  },
  detailsText: {
    fontSize: 16,
    marginVertical: 3,
  },
});
