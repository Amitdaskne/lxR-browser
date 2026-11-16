package com.amit.browser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private EditText etUrl;
    private ProgressBar progressBar;
    private TextView tvTabCount;
    private ImageView ivLock;
    
    // Tab Management
    private List<String> openTabs = new ArrayList<>();
    private int currentTabIndex = 0;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        
        // Apply Saved Theme
        SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        etUrl = findViewById(R.id.etUrl);
        progressBar = findViewById(R.id.progressBar);
        tvTabCount = findViewById(R.id.tvTabCount);
        ivLock = findViewById(R.id.ivLock);

        openTabs.add("https://www.google.com");
        
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                etUrl.setText(url);
                if (url.startsWith("https://")) ivLock.setImageResource(android.R.drawable.ic_secure);
                else ivLock.setImageResource(android.R.drawable.ic_dialog_alert);
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                etUrl.setText(url);
                openTabs.set(currentTabIndex, url); // Update current tab URL
            }
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
        });

        etUrl.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                hideKeyboard();
                String input = etUrl.getText().toString().trim();
                String url = input.contains(" ") ? "https://www.google.com/search?q=" + input.replace(" ", "+") : (input.startsWith("http") ? input : "https://" + input);
                webView.loadUrl(url);
                return true;
            }
            return false;
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> { if (webView.canGoBack()) webView.goBack(); });
        findViewById(R.id.btnForward).setOnClickListener(v -> { if (webView.canGoForward()) webView.goForward(); });
        findViewById(R.id.btnRefresh).setOnClickListener(v -> webView.reload());
        
        findViewById(R.id.btnNewTab).setOnClickListener(v -> {
            openTabs.add("https://www.google.com");
            currentTabIndex = openTabs.size() - 1;
            webView.loadUrl("https://www.google.com");
            updateTabCount();
        });

        tvTabCount.setOnClickListener(v -> showTabManager());
        findViewById(R.id.btnMenu).setOnClickListener(v -> showMenu());
        
        webView.loadUrl(openTabs.get(0));
    }

    private void updateTabCount() {
        tvTabCount.setText(String.valueOf(openTabs.size()));
    }

    private void showTabManager() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_tabs, null);
        dialog.setContentView(view);

        RecyclerView rv = view.findViewById(R.id.rvTabs);
        rv.setLayoutManager(new LinearLayoutManager(this));

        TabAdapter adapter = new TabAdapter(openTabs, currentTabIndex, new TabAdapter.OnTabAction() {
            @Override
            public void onSelect(int position) {
                currentTabIndex = position;
                webView.loadUrl(openTabs.get(position));
                dialog.dismiss();
            }
            @Override
            public void onClose(int position) {
                if (openTabs.size() == 1) {
                    Toast.makeText(MainActivity.this, "Cannot close the last tab", Toast.LENGTH_SHORT).show();
                    return;
                }
                openTabs.remove(position);
                if (currentTabIndex >= openTabs.size()) currentTabIndex = openTabs.size() - 1;
                webView.loadUrl(openTabs.get(currentTabIndex));
                updateTabCount();
                dialog.dismiss();
                showTabManager(); // Refresh dialog
            }
        });
        rv.setAdapter(adapter);

        view.findViewById(R.id.btnAddNewTab).setOnClickListener(v -> {
            openTabs.add("https://www.google.com");
            currentTabIndex = openTabs.size() - 1;
            webView.loadUrl("https://www.google.com");
            updateTabCount();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showMenu() {
        SharedPreferences prefs = getSharedPreferences("theme", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        String[] options = {isDark ? "Switch to Light Mode" : "Switch to Dark Mode", "Exit"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("lxR Menu")
            .setItems(options, (dialog, which) -> {
                if (which == 0) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("dark_mode", !isDark);
                    editor.apply();
                    recreate(); // Restart activity to apply theme
                } else {
                    finish();
                }
            }).show();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
        etUrl.clearFocus();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
}
