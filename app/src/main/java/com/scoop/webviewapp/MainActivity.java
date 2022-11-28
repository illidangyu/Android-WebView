package com.scoop.webviewapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
   private WebView webView;
   private long backTime;

      private CookieManager cookieManager;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      webView = findViewById(R.id.web_road_view);

      String url = "https://www.google.com";

      // [쿠키 매니저 사용해 쿠키 값 셋팅 실시]
      cookieManager = CookieManager.getInstance();
      cookieManager.setAcceptCookie(true);
      cookieManager.setAcceptThirdPartyCookies(webView, true); // [웹뷰 지정]
      cookieManager.getInstance().flush();

      webView.setWebChromeClient(new WebChromeClient(){
         public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
         }

      }); // 크롬클라이언트 사용
      webView.setWebViewClient(new WebViewClient(){
         @Override
         public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String cookies = CookieManager.getInstance().getCookie(view.getUrl());
            cookieManager.setCookie(url, cookies);
            cookieManager.getInstance().flush();
         }

         @Override
         public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            if(request.getUrl().toString().contains("tel:")){
               Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(request.getUrl().toString()));
               startActivity(intent);
               return true;
            }
            return super.shouldOverrideUrlLoading(view, request);
         }

      }); // 웹클라이언트 설정 (아래)
      webView.addJavascriptInterface(new WebAppInterface(this), "Android");
      try {
         WebSettings.class.getMethod("setLightTouchEnabled", new Class[]{Boolean.TYPE});
      } catch (NoSuchMethodException e) {

      }
      webView.getSettings().setJavaScriptEnabled(true); // 자바스크립트 허용
      webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
      webView.getSettings().setSupportMultipleWindows(false); // 여러 창 또는 탭 열리는 것 비허용
      webView.getSettings().setLoadWithOverviewMode(true); // 페이지 내에서만 이동하게끔
      webView.getSettings().setUseWideViewPort(true); // 페이지를 웹뷰 width에 맞춤
      webView.getSettings().setSupportZoom(true); // 확대 활성화
      webView.getSettings().setAllowFileAccess(true);
      webView.getSettings().setAllowContentAccess(true);
      webView.getSettings().setBuiltInZoomControls(false); // 확대 활성화
      webView.getSettings().setCacheMode(webView.getSettings().LOAD_NO_CACHE); // 캐시 사용안함 (매번 새로 로딩)

      // CookieManager
      webView.getSettings().setDomStorageEnabled(true); // 로컬스토리지 사용 허용
      webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
      webView.loadUrl(url);
   }
   public class WebAppInterface {
      Context mContext;

      /** Instantiate the interface and set the context */
      WebAppInterface(Context c) {
         mContext = c;
      }

      /** Show a toast from the web page */
      @JavascriptInterface
      public void showToast(String toast) {
         Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
      }
   }

   @Override
   protected void onDestroy() {
      super.onDestroy();
      cookieManager.getInstance().flush();
   }

   @Override
   protected void onStop() {
      super.onStop();
      cookieManager.getInstance().flush();
   }

   @Override
   protected void onResume() {
      super.onResume();
      cookieManager.getInstance().flush();
   }

   @Override
   public void onBackPressed() {
      long now = System.currentTimeMillis();
      long delay = now - backTime;
      if (webView.canGoBack()){
         webView.goBack();
      }else if (delay >= 0 && delay < 2000){
         super.onBackPressed();
         moveTaskToBack(true);
         finishAndRemoveTask();
         System.exit(0);
      }else {
         backTime = now;
         Toast.makeText(this, "한번더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
      }
   }
}