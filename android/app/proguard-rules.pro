-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the classes containing JavascriptInterface methods
-keep class com.goliftngo.partner.MainActivity$RazorpayWebViewBridge { *; }
-keep class com.goliftngo.partner.MainActivity$DriverStatusBridge { *; }
