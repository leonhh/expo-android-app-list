import { NativeModule, requireNativeModule } from 'expo';

import { ExpoAppListModuleEvents } from './ExpoAppList.types';

declare class ExpoAppListModule extends NativeModule<ExpoAppListModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoAppListModule>('ExpoAppList');
