# ExpoAppList

expo-app-list is an Expo module that allows you to retrieve information about installed applications on Android devices.

## Features

- Get a list of all installed applications on an Android device
- Retrieve package names, app names, and installation details

## Installation

```bash
npx expo install expo-app-list
```

## Android Permissions

This package requires the `QUERY_ALL_PACKAGES` permission to function. The permission is incluced in the code of `expo-app-list`.

⚠️ **Important Note**: The `QUERY_ALL_PACKAGES` permission is considered sensitive. If you plan to publish your app on the Google Play Store, you may need to justify the use of this permission in your Google Play Store listing.

## Usage

```javascript
import { ExpoAppList } from 'expo-app-list';

// Always call getAll() first before using other methods
const apps = await ExpoAppList.getAll();
console.log(apps);
```

The `getAll()` method returns an array of objects containing information about each installed application:

```javascript
[
  {
    packageName: "com.example.app",
    appName: "Example App",
    versionName: "1.0.0",
    versionCode: 1,
    firstInstallTime: 1234567890,
    lastUpdateTime: 1234567890
  },
  // ...
]
```

## ExpoAppList powers ReactRaptor

This package powers [ReactRaptor](https://play.google.com/store/apps/details?id=com.leonhh.reactraptor), an app for React Native developers that helps discover which Android apps are built with React Native. ReactRaptor uses ExpoAppList to scan installed applications and identify React Native-based apps.

## License

MIT

## Contributing

Contributions are welcome! Feel free to submit a Pull Request. 