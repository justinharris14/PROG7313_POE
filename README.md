HOW TO RUN:
Clone the repository via guthub dekstop in a folder of your choice.
Open Android sutdio, click "open" and select the folder
Once openes, click on "Sync now" to download dependencies
if there is no "google-services.json" file, create it  in the app directory and copy the text at the end into it
Select at minimum a Pixel 5 as an emulator, preferably newer versions

CHECK: 
implementation "com.google.firebase:firebase-storage-ktx" and kotlinx-coroutines-play-services are in your build.gradle.kts, if not add this.
Link to Video Demonstration:
https://youtu.be/7BsigJf7jks

{
  "project_info": {
    "project_number": "556665764655",
    "project_id": "bybetterbudget",
    "storage_bucket": "bybetterbudget.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:556665764655:android:57f8f94591e6b4fbbeffda",
        "android_client_info": {
          "package_name": "com.example.bybetterbudget"
        }
      },
      "oauth_client": [],
      "api_key": [
        {
          "current_key": "AIzaSyCshQsova78CNetxHDma4AMvodSfs8nvNI"
        }
      ],
      "services": {
        "appinvite_service": {
          "other_platform_oauth_client": []
        }
      }
    }
  ],
  "configuration_version": "1"
}
