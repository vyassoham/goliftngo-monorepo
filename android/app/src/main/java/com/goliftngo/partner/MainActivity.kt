package com.goliftngo.partner

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.os.Message
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.GeolocationPermissions
import android.webkit.JavascriptInterface
import android.webkit.JsResult
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.goliftngo.partner.theme.DriverGoliftngoTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val TAG = "LiftnGo_Driver"
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var mainWebView: WebView? = null
    private var popupDialog: AlertDialog? = null
    @Volatile
    private var isDriverOnline: Boolean = false

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (filePathCallback != null) {
            val intentData = result.data
            val results: Array<Uri>? = if (result.resultCode == RESULT_OK && intentData != null) {
                val dataString = intentData.dataString
                if (dataString != null) {
                    arrayOf(Uri.parse(dataString))
                } else null
            } else null
            filePathCallback?.onReceiveValue(results)
            filePathCallback = null
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineLocation || coarseLocation) {
            Log.d(TAG, "Location permission granted. Reloading WebView to activate driver GPS.")
            mainWebView?.post {
                mainWebView?.reload()
            }
        }
        checkAndPromptOverlayPermission()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Enable Chrome remote inspection for debugging
        WebView.setWebContentsDebuggingEnabled(true)

        requestDriverPermissions()
        requestIgnoreBatteryOptimizations()

        setContent {
            DriverGoliftngoTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    var canGoBack by remember { mutableStateOf(false) }
                    var isLoading by remember { mutableStateOf(true) }

                    LaunchedEffect(Unit) {
                        delay(2000)
                        isLoading = false
                        checkAndPromptOverlayPermission()
                    }

                    BackHandler(enabled = canGoBack) {
                        mainWebView?.goBack()
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                                WebView(context).apply {
                                    // Fix black screen: do not force LAYER_TYPE_HARDWARE in Compose
                                    setLayerType(View.LAYER_TYPE_NONE, null)
                                    setBackgroundColor(android.graphics.Color.WHITE)

                                    CookieManager.getInstance().setAcceptCookie(true)
                                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                    settings.apply {
                                        javaScriptEnabled = true
                                        domStorageEnabled = true
                                        databaseEnabled = true

                                        // Fix popup & modal aspect ratio: mobile 1:1 viewport
                                        loadWithOverviewMode = false
                                        useWideViewPort = false
                                        builtInZoomControls = false
                                        setSupportZoom(false)
                                        textZoom = 100

                                        // Prevent white screen popups by disabling multi-window
                                        setSupportMultipleWindows(false)
                                        javaScriptCanOpenWindowsAutomatically = false

                                        // Enable media autoplay (trip alert sounds/ringtones)
                                        mediaPlaybackRequiresUserGesture = false

                                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        cacheMode = WebSettings.LOAD_DEFAULT
                                        allowFileAccess = true
                                        allowContentAccess = true
                                        setGeolocationEnabled(true)
                                        offscreenPreRaster = true

                                        // Set clean User-Agent without '; wv' and 'Version/X.X' so Razorpay and other payment gateways
                                        // identify standard mobile Chrome and enable UPI intent options
                                        userAgentString = cleanUserAgent(userAgentString)
                                    }

                                    addJavascriptInterface(RazorpayWebViewBridge(this), "RazorpayWebViewBridge")
                                    addJavascriptInterface(DriverStatusBridge(), "DriverStatusBridge")
                                    addJavascriptInterface(NotificationBridge(), "AndroidNotificationBridge")

                                    webChromeClient = object : WebChromeClient() {
                                        override fun onGeolocationPermissionsShowPrompt(
                                            origin: String?,
                                            callback: GeolocationPermissions.Callback?
                                        ) {
                                            // Always retain permission so GPS doesn't drop
                                            callback?.invoke(origin, true, true)
                                        }

                                        override fun onPermissionRequest(request: PermissionRequest?) {
                                            // Grant permissions for audio, notifications, etc.
                                            runOnUiThread {
                                                try {
                                                    request?.grant(request.resources)
                                                } catch (e: Exception) {
                                                    Log.e(TAG, "Error granting web permission", e)
                                                }
                                            }
                                        }

                                        override fun onJsAlert(
                                            view: WebView?,
                                            url: String?,
                                            message: String?,
                                            result: JsResult?
                                        ): Boolean {
                                            AlertDialog.Builder(this@MainActivity)
                                                .setTitle("LiftnGo Alert")
                                                .setMessage(message ?: "")
                                                .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                                .setOnCancelListener { result?.cancel() }
                                                .show()
                                            return true
                                        }

                                        override fun onJsConfirm(
                                            view: WebView?,
                                            url: String?,
                                            message: String?,
                                            result: JsResult?
                                        ): Boolean {
                                            AlertDialog.Builder(this@MainActivity)
                                                .setTitle("LiftnGo Alert")
                                                .setMessage(message ?: "")
                                                .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                                                .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                                                .setOnCancelListener { result?.cancel() }
                                                .show()
                                            return true
                                        }

                                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                            Log.d(
                                                TAG,
                                                "Console: ${consoleMessage?.message()} (${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()})"
                                            )
                                            return true
                                        }

                                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                            if (newProgress >= 50) {
                                                isLoading = false
                                            }
                                        }

                                        override fun onShowFileChooser(
                                            webView: WebView?,
                                            filePathCallback: ValueCallback<Array<Uri>>?,
                                            fileChooserParams: FileChooserParams?
                                        ): Boolean {
                                            this@MainActivity.filePathCallback?.onReceiveValue(null)
                                            this@MainActivity.filePathCallback = filePathCallback
                                            val intent = fileChooserParams?.createIntent()
                                            try {
                                                if (intent != null) {
                                                    filePickerLauncher.launch(intent)
                                                } else {
                                                    val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                                                        addCategory(Intent.CATEGORY_OPENABLE)
                                                        type = "*/*"
                                                    }
                                                    filePickerLauncher.launch(fallbackIntent)
                                                }
                                            } catch (e: Exception) {
                                                this@MainActivity.filePathCallback = null
                                                return false
                                            }
                                            return true
                                        }
                                    }

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                            super.onPageStarted(view, url, favicon)
                                            Log.d(TAG, "Loading URL: $url")
                                            injectNotificationOverride(view)
                                        }

                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isLoading = false
                                            CookieManager.getInstance().flush()
                                            canGoBack = view?.canGoBack() ?: false
                                            injectPopupUiUxFix(view)
                                            injectDriverStatusDetector(view)
                                            injectNotificationOverride(view)
                                        }

                                        override fun doUpdateVisitedHistory(
                                            view: WebView?,
                                            url: String?,
                                            isReload: Boolean
                                        ) {
                                            canGoBack = view?.canGoBack() ?: false
                                            super.doUpdateVisitedHistory(view, url, isReload)
                                        }

                                        override fun shouldOverrideUrlLoading(
                                            view: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            val url = request?.url?.toString() ?: return false
                                            return handleUrlLoading(view, url)
                                        }

                                        override fun onRenderProcessGone(
                                            view: WebView?,
                                            detail: RenderProcessGoneDetail?
                                        ): Boolean {
                                            Log.e(TAG, "Render process gone. Recreating activity to recover.")
                                            view?.destroy()
                                            mainWebView = null
                                            recreate()
                                            return true
                                        }
                                    }

                                    loadUrl("https://driver.goliftngo.com/")
                                    mainWebView = this
                                }
                            },
                            update = {
                                mainWebView = it
                            }
                        )

                        AnimatedVisibility(
                            visible = isLoading,
                            exit = fadeOut(),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "LiftnGo Driver Partner Logo",
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .wrapContentHeight()
                                    )
                                    Spacer(modifier = Modifier.height(36.dp))
                                    CircularProgressIndicator(
                                        color = Color(0xFF0A2540)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        stopFloatingBubble()
    }

    override fun onResume() {
        super.onResume()
        stopFloatingBubble()
        mainWebView?.onResume()
        // Removed mainWebView?.resumeTimers() to avoid interfering with global timers
    }

    override fun onPause() {
        super.onPause()
        // IMPORTANT: Do NOT call mainWebView?.onPause() here! 
        // Calling onPause() stops JavaScript execution and kills WebSockets (driver goes offline).
        CookieManager.getInstance().flush()
        if (!isChangingConfigurations) {
            startFloatingBubble()
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        startFloatingBubble()
    }

    override fun onStop() {
        super.onStop()
        CookieManager.getInstance().flush()
        if (!isChangingConfigurations && !FloatingBubbleService.isRunning) {
            try {
                startFloatingBubble()
            } catch (e: Exception) {
                Log.w(TAG, "onStop startFloatingBubble caught exception: ${e.message}")
            }
        }
    }

    override fun onDestroy() {
        try {
            popupDialog?.dismiss()
        } catch (e: Exception) {
            // Ignored
        }
        popupDialog = null
        mainWebView?.destroy()
        mainWebView = null
        CookieManager.getInstance().flush()
        super.onDestroy()
    }

    private fun startFloatingBubble() {
        if (!isDriverOnline) {
            Log.d(TAG, "Driver is offline, skipping floating bubble")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Log.d(TAG, "Cannot start FloatingBubbleService: overlay permission not granted")
                return
            }
            try {
                val intent = Intent(this, FloatingBubbleService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start FloatingBubbleService", e)
            }
        }
    }

    private fun stopFloatingBubble() {
        try {
            stopService(Intent(this, FloatingBubbleService::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop FloatingBubbleService", e)
        }
    }

    private fun requestDriverPermissions() {
        val list = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = list.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    @SuppressLint("BatteryLife")
    private fun requestIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    // Ignore if device prohibits direct intent
                }
            }
        }
    }

    private fun checkAndPromptOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                AlertDialog.Builder(this)
                    .setTitle("Enable Floating Bubble")
                    .setMessage("Allow LiftnGo to 'Display over other apps' so you can easily access ride requests and the shortcut while using navigation or other apps.")
                    .setPositiveButton("Enable") { _, _ ->
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:$packageName")
                            )
                            startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val fallbackIntent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                                startActivity(fallbackIntent)
                            } catch (ex: Exception) {
                                Toast.makeText(this@MainActivity, "Please enable 'Display over other apps' in Settings", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("Later", null)
                    .show()
            }
        }
    }

    private fun injectPopupUiUxFix(webView: WebView?) {
        // Removed CSS injection as it was interfering with React modal backdrop clicks and bottom sheets
    }

    private fun injectNotificationOverride(webView: WebView?) {
        val js = """
            (function() {
                if (window.hasInjectedNotification) return;
                
                // Override standard Notification API
                window.Notification = function(title, options) {
                    var body = options ? options.body : '';
                    if (window.AndroidNotificationBridge) {
                        window.AndroidNotificationBridge.showNotification(title, body, '');
                    }
                };
                window.Notification.requestPermission = function() {
                    return Promise.resolve('granted');
                };
                window.Notification.permission = 'granted';
                
                // Fast combined polling for Ride Alerts & Online Status (without innerText layout thrashing)
                var lastStatus = null;
                window.liftngoPollInterval = setInterval(function() {
                    try {
                        var root = document.getElementById('__next') || document.body;
                        if (!root) return;
                        var text = root.textContent || '';
                        
                        // 1. Trip Alert Detection
                        var hasTrip = false;
                        if (text.indexOf('Accept') !== -1 && (text.indexOf('Decline') !== -1 || text.indexOf('Reject') !== -1 || text.indexOf('Pass') !== -1)) {
                            hasTrip = true;
                        } else if (text.indexOf('New trip request') !== -1 || text.indexOf('New Request') !== -1) {
                            hasTrip = true;
                        }
                        
                        if (hasTrip) {
                            if (!window.isTripAlertFired) {
                                if (window.AndroidNotificationBridge) {
                                    window.AndroidNotificationBridge.showNotification('New Trip Request!', 'Tap here to accept your new ride.', '');
                                }
                                window.isTripAlertFired = true;
                            }
                        } else {
                            window.isTripAlertFired = false;
                        }
                        
                        // 2. Online/Offline Status Detection
                        var isOnline = null;
                        var rawText = (root.textContent || '').toLowerCase();
                        
                        var onlineMarkers = ['active', 'go offline', 'on trip', 'arrived', 'start trip', 'complete trip', 'you are online'];
                        var offlineMarkers = ['go online', 'you are offline', 'send verification code', 'create an account', 'log in', 'login', 'sign in', 'welcome back'];
                        
                        // Check online markers first - highest priority
                        for (var i = 0; i < onlineMarkers.length; i++) {
                            if (rawText.indexOf(onlineMarkers[i]) !== -1) {
                                isOnline = true;
                                break;
                            }
                        }
                        
                        if (isOnline === null) {
                            for (var i = 0; i < offlineMarkers.length; i++) {
                                if (rawText.indexOf(offlineMarkers[i]) !== -1) {
                                    isOnline = false;
                                    break;
                                }
                            }
                        }
                        
                        if (isOnline !== null && isOnline !== lastStatus) {
                            lastStatus = isOnline;
                            if (window.DriverStatusBridge) {
                                window.DriverStatusBridge.setDriverOnline(isOnline);
                            }
                        }
                        
                    } catch(e) {}
                }, 1500);
                
                // 3. Force Remove the Broken Notification UI
                // This checks for the Notification modal and deletes it to prevent screen freezing.
                window.liftngoUiRemoverInterval = setInterval(function() {
                    try {
                        // Find the header text element
                        var elements = document.querySelectorAll('div, h2, h3, span');
                        for (var i = 0; i < elements.length; i++) {
                            var el = elements[i];
                            if (el.textContent === 'Notifications' || el.textContent === '0 unread updates') {
                                // Find the fixed backdrop container and destroy it
                                var modal = el.closest('.fixed') || el.closest('[role="dialog"]') || el.closest('.absolute');
                                if (modal && modal.style.display !== 'none') {
                                    modal.style.display = 'none';
                                    modal.remove(); // Nuke it from DOM
                                }
                            }
                        }
                    } catch(e) {}
                }, 500);

                window.hasInjectedNotification = true;
            })();
        """.trimIndent()
        webView?.evaluateJavascript(js, null)
    }

    private fun injectDriverStatusDetector(webView: WebView?) {
        // Combined into injectNotificationOverride to save performance
    }

    private fun cleanUserAgent(rawUa: String): String {
        return rawUa
            .replace("; wv", "")
            .replace(Regex("Version/[0-9.]+\\s*"), "")
    }

    private fun handleUrlLoading(webView: WebView?, url: String): Boolean {
        Log.d(TAG, "handleUrlLoading: $url")

        if (url.startsWith("http://") || url.startsWith("https://")) {
            if (url.startsWith("https://play.google.com/store/apps/details?id=")) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(intent)
                    return true
                } catch (e: Exception) {
                    Log.e(TAG, "Cannot launch Play Store link: $url", e)
                }
            }
            return false
        }

        if (url.startsWith("upi://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error launching UPI app for uri: $url", e)
                Toast.makeText(this@MainActivity, "No UPI app found on device to complete payment", Toast.LENGTH_LONG).show()
                return true
            }
        }

        if (url.startsWith("intent://")) {
            try {
                val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (intent != null) {
                    val pm = packageManager
                    val resolveInfo = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
                    if (resolveInfo != null) {
                        startActivity(intent)
                        return true
                    }

                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (!fallbackUrl.isNullOrEmpty()) {
                        webView?.loadUrl(fallbackUrl)
                        return true
                    }

                    val packageName = intent.`package`
                    if (!packageName.isNullOrEmpty()) {
                        try {
                            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(marketIntent)
                            return true
                        } catch (e: Exception) {
                            val webMarketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            startActivity(webMarketIntent)
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling intent scheme: $url", e)
            }
            return true
        }

        if (url.startsWith("market://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                return true
            } catch (e: Exception) {
                Log.e(TAG, "Error handling market:// url: $url", e)
                return true
            }
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Cannot launch external URI: $url", e)
            return true
        }
    }

    inner class RazorpayWebViewBridge(private val webView: WebView) {
        @JavascriptInterface
        fun onLoadWebView() {
            runOnUiThread {
                try {
                    val upiPackages = listOf(
                        "com.google.android.apps.nbu.paisa.user",
                        "com.phonepe.app",
                        "net.one97.paytm",
                        "in.org.npci.upiapp",
                        "com.dreamplug.androidapp",
                        "com.whatsapp",
                        "com.upi.axispay",
                        "com.mobikwik_new",
                        "com.freecharge.android",
                        "com.msf.kbank.mobile",
                        "com.csam.icici.bank.imobile",
                        "com.sbi.upi",
                        "com.amazon.mShop.android.shopping",
                        "com.myairtelapp",
                        "com.naviapp"
                    )
                    val installedList = JSONArray()
                    for (pkg in upiPackages) {
                        try {
                            packageManager.getPackageInfo(pkg, 0)
                            val obj = JSONObject()
                            obj.put("package_name", pkg)
                            installedList.put(obj)
                        } catch (ignored: Exception) {
                        }
                    }
                    val dataObj = JSONObject()
                    dataObj.put("upi_intent_data", installedList)
                    val js = "(function(){ if (window.handleWebViewMessage) { window.handleWebViewMessage($dataObj); } })()"
                    webView.evaluateJavascript(js, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Error in RazorpayWebViewBridge onLoadWebView", e)
                }
            }
        }
    }

    inner class DriverStatusBridge {
        @JavascriptInterface
        fun setDriverOnline(online: Boolean) {
            Log.d(TAG, "DriverStatusBridge: driver online = $online")
            isDriverOnline = online
            if (!online) {
                runOnUiThread {
                    stopFloatingBubble()
                }
            }
        }
    }

    inner class NotificationBridge {
        @JavascriptInterface
        fun showNotification(title: String, body: String, icon: String) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "goliftngo_driver_channel"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Driver Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for new trips and alerts"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 500, 200, 500)
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                this@MainActivity, 
                0, 
                intent, 
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(this@MainActivity, channelId)
                .setSmallIcon(R.mipmap.ic_launcher) // Fallback to app icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(longArrayOf(0, 500, 200, 500))
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}




