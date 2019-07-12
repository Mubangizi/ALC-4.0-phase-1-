package com.example.alc40;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class WebViewActivity extends AppCompatActivity {
    private static String TAG;
    private WebView mWebView;
    private SSLWebViewClient sslWebViewClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        mWebView = findViewById(R.id.webview);
        sslWebViewClient = new SSLWebViewClient();


        if (ContextCompat.checkSelfPermission(WebViewActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(WebViewActivity.this, new String[]{Manifest.permission.INTERNET}, 1);
        }

        //configuring the WebView
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("https://andela.com/alc/");
        mWebView.setWebViewClient(sslWebViewClient);

    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()){
            mWebView.goBack();
        }else {
            super.onBackPressed();
        }
    }

    private class SSLWebViewClient extends WebViewClient {

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            SslCertificate sslCertificateServer = error.getCertificate();
            Certificate pinnedCert = getCertificateForRawResource(R.raw.alccert, WebViewActivity.this);
            Certificate serverCert = convertSSLCertificateToCertificate(sslCertificateServer);

            if(pinnedCert.equals(serverCert)) {
                handler.proceed();
            } else {
                super.onReceivedSslError(view, handler, error);
            }
        }

        public Certificate getCertificateForRawResource(int resourceId, Context context) {
            CertificateFactory cf = null;
            Certificate ca = null;
            Resources resources = context.getResources();
            InputStream caInput = resources.openRawResource(resourceId);

            try {
                cf = CertificateFactory.getInstance("X.509");
                ca = cf.generateCertificate(caInput);
            } catch (CertificateException e) {
                Log.e(TAG, "exception", e);
            } finally {
                try {
                    caInput.close();
                } catch (IOException e) {
                    Log.e(TAG, "exception", e);
                }
            }

            return ca;
        }


        public Certificate convertSSLCertificateToCertificate(SslCertificate sslCertificate) {
            CertificateFactory cf = null;
            Certificate certificate = null;
            Bundle bundle = sslCertificate.saveState(sslCertificate);
            byte[] bytes = bundle.getByteArray("x509-certificate");

            if (bytes != null) {
                try {
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    Certificate cert = certFactory.generateCertificate(new ByteArrayInputStream(bytes));
                    certificate = cert;
                } catch (CertificateException e) {
                    Log.e(TAG, "exception", e);
                }
            }

            return certificate;
        }

    }


}
