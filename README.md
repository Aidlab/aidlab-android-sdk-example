# Introduction

This repository is dedicated for developers who want to understand how to build publicly distributed applications featuring Aidlab sensor integration. Android-Example will guide you how to connect with Aidlab, so it will save your time and make real development faster and easier. 

You can check our [website](https://www.aidlab.com/developer) to get the answers for the most common questions related to Aidlab.

# Technical overview

This examples of Aidlab was build for Android 5.0+ (API level 21+), in Android Studio 3.3.

Connect with Aidlab to use sensors and collected data. You can ask the Aidlab for the readings listed below:

* Readings from ECG sensor.
* Readings from respiration sensor.
* Readings from object temperature sensor.
* Readings from ambient temperature sensor.
* Readings from motion sensor.
* Battery level.

# Events

**didConnectAidlab**

```
void didConnectAidlab()
```

This event is invoked right after establishing connection with Aidlab.

**didDisconnectAidlab**

```
void didDisconnectAidlab()
```

Similar event will be fired on disconnection.

**didReceiveTemperature**

```
 void didReceiveTemperature(objectTemperature: Float, ambientTemperature: Float, bodyTemperature: Float )
```

This event will be invoked to use temperature readings and to apply them in application. It shares Aidlab's data on object temperature from directional sensor and ambient temperature from built-in, hidden sensor.

**didReceiveRespiration**

```
void didReceiveRespiration(respirationSample: Float, respirationRate: Byte)
```

This way you get data from Aidlab's respiration sensor, which provides enough data to analyse and display a respiration rate graph.

**didReceiveECG**

```
void didReceiveECG(ecgSample: Float, heartRate: Int)
```

Aidlab uses similar function to receive constant ECG measurement, which will allow you to draw or analyze ECG signal.

**didReceiveBatteryStatus**

```
void didReceiveBatteryStatus(soc: Float)
```

Impotant part of Aidlab's usage is the battery status. You never want Aidlab to run low on battery, as it can lead to it's turn off. Use this event to inform your users about Aidlab's low energy.
