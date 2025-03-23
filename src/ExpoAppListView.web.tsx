import * as React from 'react';

import { ExpoAppListViewProps } from './ExpoAppList.types';

export default function ExpoAppListView(props: ExpoAppListViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
