import { registerWebModule, NativeModule } from 'expo';

import { ExpoAppListModuleEvents } from './ExpoAppList.types';

class ExpoAppListModule extends NativeModule<ExpoAppListModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoAppListModule);
