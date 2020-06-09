import { NativeModules, NativeEventEmitter, Platform } from 'react-native';
const { BackgroundLocationTracking: Module } = NativeModules;

class BackgroundLocationTracking {
  constructor() {
    if (Platform.OS === 'android'){
      this.eventEmitter = new NativeEventEmitter(Module);
    }
  }

  async getPoints() {
    return await Module.getPoints();
  }

  async checkPowerOptimizationSettings() {
   return await Module.checkPowerOptimizationSettings();
  }

  showPowerOptimizationSettings() {
     return Module.showPowerOptimizationSettings();
  }

  async checkSystemLocationAccuracySettings() {
     return await Module.checkSystemLocationAccuracySettings();
    }

    showSystemLocationAccuracySettings() {
       return Module.showSystemLocationAccuracySettings();
    }


  startTracking() {
    return Module.requestLocation({});
  }

  stopTracking() {
    return Module.stopLocationTracking();
  }

  readPersistedPoints(){
    return Module.readPersistedPoints();
  }

  resetPersistedPoints(){
    return Module.resetPersistedPoints();
  }

  on(name, f) {
    this.eventEmitter.addListener(name, f);
  }
}

const backgroundLocationTracking = new BackgroundLocationTracking();
export default backgroundLocationTracking;
