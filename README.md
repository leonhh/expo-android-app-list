# ExpoAndroidAppList

expo-android-app-list is an Expo module that allows you to retrieve information about installed applications on Android devices.

## Features

- Get a list of all installed applications on an Android device
- Retrieve package names, app names, and installation details

## Installation

```sh
npx expo install expo-android-app-list
```

## Configuration

This package requires the `QUERY_ALL_PACKAGES` permission to function. The permission is included in the code of `expo-android-app-list`.

⚠️ **Important Note**: The `QUERY_ALL_PACKAGES` permission is considered sensitive. If you plan to publish your app on the Google Play Store, you may need to justify the use of this permission in your Google Play Store listing.

## Usage

### getAll

The `getAll()` method returns an array of objects containing information about each installed application:

```typescript
import { ExpoAndroidAppList } from "expo-android-app-list";

const apps = await ExpoAndroidAppList.getAll();
```

```javascript
[
  {
    packageName: "com.example.app",
    appName: "Example App",
    versionName: "1.0.0",
    versionCode: 1,
    firstInstallTime: 1234567890,
    lastUpdateTime: 1234567890,
  },
  // ...
];
```

### getAppIcon

To display app icons, you can use the `getAppIcon()` method along with expo-image. This methods is async because we need to convert a drawable to PNG.

```typescript
import { ExpoAndroidAppList } from 'expo-android-app-list';
import { Image } from 'expo-image';

const icon = await ExpoAndroidAppList.getAppIcon("com.example.app", 100);

<Image
  source={{
    cacheKey: "com.example.app",
    uri: `data:image/png;base64,${icon}`,
  }}
  style={{ width: 100, height: 100 }}
/>
```

### getNativeLibraries

The `getNativeLibraries()` method allows you to retrieve a list of native libraries (.so files) used by an Android application:

```typescript
const libraries =
  await ExpoAndroidAppList.getNativeLibraries("com.example.app");
```

### getFileContent

The `getFileContent()` method allows you to search for and read files which might be included in the APK:

```typescript
// Search for specific configuration files
const configs = await ExpoAndroidAppList.getFileContent("com.example.app", [
  "config.json",
]);
```

### getPermissions

The `getPermissions()` method retrieves all permissions that an app can request:

```typescript
const permissions = await ExpoAndroidAppList.getPermissions("com.example.app");

const hasCameraPermission = permissions.includes("android.permission.CAMERA");
```

## ExpoAndroidAppList powers ReactRaptor

This package powers [ReactRaptor](https://play.google.com/store/apps/details?id=com.leonhh.reactraptor), an app for React Native developers that helps discover which Android apps are built with React Native. ReactRaptor uses ExpoAndroidAppList to scan installed applications and identify React Native-based apps.

## License

MIT

## Contributing

Contributions are welcome! Feel free to submit a Pull Request.
