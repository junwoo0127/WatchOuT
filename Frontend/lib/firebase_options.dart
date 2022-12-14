import 'package:firebase_core/firebase_core.dart' show FirebaseOptions;
import 'package:flutter/foundation.dart'
    show defaultTargetPlatform, kIsWeb, TargetPlatform;

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }
    switch (defaultTargetPlatform) {
      case TargetPlatform.android:
        return android;
      case TargetPlatform.iOS:
        return ios;
      case TargetPlatform.macOS:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for macos - '
          'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.windows:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for windows - '
          'you can reconfigure this by running the FlutterFire CLI again.',
        );
      case TargetPlatform.linux:
        throw UnsupportedError(
          'DefaultFirebaseOptions have not been configured for linux - '
          'you can reconfigure this by running the FlutterFire CLI again.',
        );
      default:
        throw UnsupportedError(
          'DefaultFirebaseOptions are not supported for this platform.',
        );
    }
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyBR_wL0w6JGJDdW8iK1t3eiUIud0VtztEA',
    appId: '1:826546983331:web:2bffeed5141cd5b9c88a16',
    messagingSenderId: '826546983331',
    projectId: 'homealone-6ef54',
    authDomain: 'homealone-6ef54.firebaseapp.com',
    storageBucket: 'homealone-6ef54.appspot.com',
    measurementId: 'G-YVREHRP459',
  );

  static const FirebaseOptions android = FirebaseOptions(
    apiKey: 'AIzaSyBVao199EgfUag7vqga4ACax-bNgaPLri8',
    appId: '1:826546983331:android:44cada1a2f4fb019c88a16',
    messagingSenderId: '826546983331',
    projectId: 'homealone-6ef54',
    storageBucket: 'homealone-6ef54.appspot.com',
  );

  static const FirebaseOptions ios = FirebaseOptions(
    apiKey: 'AIzaSyDkR7VtHw0tVHZRRZI-ksoD_CrBxR0ah-E',
    appId: '1:826546983331:ios:12f4f5c9507f9c74c88a16',
    messagingSenderId: '826546983331',
    projectId: 'homealone-6ef54',
    storageBucket: 'homealone-6ef54.appspot.com',
    androidClientId:
        '826546983331-p7lm33bpprf735bif53kbslfpj6h4f7f.apps.googleusercontent.com',
    iosClientId:
        '826546983331-4a8lqls1lupockl3gr20esh06k032l4o.apps.googleusercontent.com',
    iosBundleId: 'com.ssafy.com.homealone',
  );
}
