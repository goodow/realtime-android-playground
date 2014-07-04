realtime-android-playground  [![Google Play](http://developer.android.com/images/brand/en_generic_rgb_wo_45.png)](https://play.google.com/store/apps/details?id=com.goodow.realtime.android.playground) [![Build Status](https://travis-ci.org/goodow/realtime-android-playground.svg?branch=master)](https://travis-ci.org/goodow/realtime-android-playground)
==================

Goodow Realtime API Playground helps you to try out the features of the [Goodow Realtime Store API]
(https://github.com/goodow/realtime-store/).

Visit [Google groups](https://groups.google.com/forum/#!forum/goodow-realtime) for discussions and announcements.

## Build from source and run

### Pre-requisites
- [JDK 6+](https://jdk8.java.net/download.html)
- [Android SDK](http://developer.android.com/sdk/index.html)
- [Apache Maven](http://maven.apache.org/download.html)
- [Git](https://help.github.com/articles/set-up-git)

### Check out sources and run the app with Maven
```bash
git clone https://github.com/goodow/realtime-android-playground.git
cd realtime-android-playground
export ANDROID_HOME=$your-android-skd-directory/android-sdk-linux
export PATH=${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools
mvn clean package android:deploy android:run
```

### Configuration
https://github.com/goodow/realtime-android-playground/blob/master/src/main/java/com/goodow/realtime/android/playground/StoreProvider.java
