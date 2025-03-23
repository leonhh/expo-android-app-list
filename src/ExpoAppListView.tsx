import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoAppListViewProps } from './ExpoAppList.types';

const NativeView: React.ComponentType<ExpoAppListViewProps> =
  requireNativeView('ExpoAppList');

export default function ExpoAppListView(props: ExpoAppListViewProps) {
  return <NativeView {...props} />;
}
