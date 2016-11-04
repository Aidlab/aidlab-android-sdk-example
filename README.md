# Introduction
This repository is dedicated for developers who want to understand how to build publicly distributed applications featuring Aidlab sensor integration. Android-Example will guide you how to connect with Aidlab health tracker, so it will save your time and make real development faster and easier. 

You can check our [website](http://www.aidlab.com/developer) to get the answers for the most common questions related to Aidlab.

# Technical overview
This examples of Aidlab was build for Android 4.3+ (Api level 18+), BLE-capable devices in Android Studio 2.2.2, using it's embedded JDK.

Connect with Aidlab to use sensors and collected data. You can ask the device for the readings listed bellow:

* Readings from ECG sensor.
* Readings from object temperature sensor.
* Readings from ambient temperature sensor.
* Readings from respiration sensor.
* Battery level.
* USB connection status.

# Events

**onConnected**

```
void onConnected()
```

This event is invoked right after establishing connection with Aidlab.

**onDisconnected**

```
void onDisconnected()
```

Similar event will be fired on disconnection of Aidlab.

**onTemperatureReceived**

```
 void onTemperatureReceived(float objectTemperature, float ambientTemperature)
```

This event will be invoked to use temperature readings and to apply them in application. It shares Aidlab's data on object temperature from directional sensor and ambient temperature from built-in, hidden sensor.

**didReceiveRespiration**

```
void onRespirationReceived(float[] respirationValues)
```

This way you get data from Aidlab's respiration sensor, which provides enough data to analyse and display a respiration rate graph.

**onECGReceived**

```
void onECGReceived(float[] ecgValues)
```

Aidlab uses similar function to receive constant ECG measurement, which will allow you to draw or analyze ECG signal.

**onBatteryStatusReceived**

```
void onBatteryStatusReceived(float soc, boolean inCharge)
```

Impotant part of Aidlab's usage is the battery status. You never want Aidlab to run low on battery, as it can lead to it's turn off. Use this event to inform your users about Aidlab's low energy.
