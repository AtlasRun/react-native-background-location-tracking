import { NativeModules, NativeEventEmitter } from 'react-native';
const { BackgroundLocationTracking: Module } = NativeModules;

class BackgroundLocationTracking {
  constructor() {
    this.eventEmitter = new NativeEventEmitter(Module);
  }

  async getPoints() {
    return await Module.getPoints();
  }

  startTracking() {
    return Module.requestLocation({});
  }

  stopTracking() {
    return Module.stopLocationTracking();
  }

  on(name, f) {
    this.eventEmitter.addListener(name, f);
  }
}

const backgroundLocationTracking = new BackgroundLocationTracking();
export default backgroundLocationTracking;
