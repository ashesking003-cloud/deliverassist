package com.deliveryassist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final int FILE_CHOOSER_REQUEST = 101;
    private GeolocationPermissions.Callback geoCallback;
    private String geoOrigin;
    private ValueCallback<Uri[]> fileChooserCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ── Keep screen on while app is in use (driver mode) ──
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ── Hide action bar — full screen web app ──
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        // ── Status bar edge-to-edge ──
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();

        // Load app from local assets
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        // ── Core settings ──
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);        // localStorage — REQUIRED for data persistence
        settings.setDatabaseEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);  // Allow localStorage from file:// URLs
        settings.setAllowFileAccessFromFileURLs(true);       // Allow file access from file:// URLs
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);             // Disable pinch-zoom — it's a native app
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        // ── Mixed content — needed for CDN assets loaded from file:// origin ──
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // ── Cache ──
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // ── User agent — identify as Android app ──
        String ua = settings.getUserAgentString();
        settings.setUserAgentString(ua + " RoutePlannerApp/1.0");

        // ── Enable hardware acceleration for smooth map pan/zoom ──
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // ── WebViewClient — handle navigation ──
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();

                // tel: links → dial pad
                if (url.startsWith("tel:")) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                }
                // wa.me → WhatsApp
                if (url.contains("wa.me") || url.contains("whatsapp")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                // Google Maps → open in Maps app or browser
                if (url.contains("maps.google") || url.contains("google.com/maps")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                // Any other external http/https → open in browser
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                // Local file — let WebView handle
                return false;
            }
        });

        // ── WebChromeClient — handle JS alerts, geolocation, file chooser ──
        webView.setWebChromeClient(new WebChromeClient() {

            // ── JavaScript alert() / confirm() / prompt() ──
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setMessage(message)
                    .setPositiveButton("OK", (d, w) -> result.confirm())
                    .setNegativeButton("Cancel", (d, w) -> result.cancel())
                    .setOnCancelListener(d -> result.cancel())
                    .show();
                return true;
            }

            // ── Geolocation permission from HTML5 navigator.geolocation ──
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                geoOrigin = origin;
                geoCallback = callback;
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    callback.invoke(origin, true, false);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_REQUEST);
                }
            }

            // ── File chooser (for future CSV import feature) ──
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                              FileChooserParams params) {
                fileChooserCallback = callback;
                Intent intent = params.createIntent();
                startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                return true;
            }

            // ── Progress indicator ──
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                // Could add a loading bar here if needed
            }
        });
    }

    // ── Handle permission result ──
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            boolean granted = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED;
            if (geoCallback != null && geoOrigin != null) {
                geoCallback.invoke(geoOrigin, granted, false);
            }
        }
    }

    // ── Handle file chooser result ──
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (fileChooserCallback != null) {
                Uri[] results = (resultCode == RESULT_OK && data != null)
                    ? new Uri[]{data.getData()} : null;
                fileChooserCallback.onReceiveValue(results);
                fileChooserCallback = null;
            }
        }
    }

    // ── Hardware back button — navigate within WebView history ──
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            // Show exit confirmation instead of immediately closing
            new AlertDialog.Builder(this)
                .setMessage("Exit Delivery Assist?")
                .setPositiveButton("Exit", (d, w) -> finish())
                .setNegativeButton("Cancel", null)
                .show();
        }
    }

    // ── Lifecycle — pause/resume WebView with activity ──
    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        webView.resumeTimers();
    }

    @Override
    protected void onPause() {
        webView.pauseTimers();
        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
