package top.zsh2401.leafbox

import android.content.Intent
import android.graphics.Bitmap
import android.net.http.SslError
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import top.zsh2401.leafbox.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var webView: WebView;
    private lateinit var swipe: SwipeRefreshLayout;
    private lateinit var progress: ProgressBar;
    private var loadingPage:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureProgressBar()
        configureWebView()
        configureSwipe()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (webView.canGoBack()) {
            webView.goBack()
        }
    }

    override fun onResume() {
        super.onResume()
        webView.settings.javaScriptEnabled = true;
    }

    override fun onStop() {
        super.onStop()
        webView.settings.javaScriptEnabled = false;
    }

    private fun configureProgressBar() {
        progress = findViewById(R.id.progress)
    }

    private fun configureSwipe() {
        swipe = findViewById<SwipeRefreshLayout>(R.id.swipe)
        swipe.setOnRefreshListener(this)
    }

    private fun configureWebView() {
        webView = findViewById<WebView>(R.id.webview)
        webView.settings.javaScriptEnabled = true;
        webView.settings.useWideViewPort = true;
        webView.webViewClient = object : WebViewClient() {

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                loadingPage = true;
                swipe.isRefreshing = true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                loadingPage = false;
                swipe.isRefreshing = false
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val content_url = request!!.url
                if (content_url.toString().startsWith(URL)) {
                    return false;
                } else {
                    val intent = Intent();
                    intent.action = "android.intent.action.VIEW";
                    intent.data = content_url;
                    startActivity(intent);
                    return true;
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                val accept = request!!.requestHeaders["Accept"]
                if (accept?.contains("text/html") == true && loadingPage) {
                    view!!.loadUrl(ERROR_PAGE_URL);
                }
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }
        }
        webView.webChromeClient = object : WebChromeClient() {

        }
        webView.addJavascriptInterface(LeafBoxWebViewInterface(this, webView), "LBWVI")
        webView.isVerticalScrollBarEnabled = true;
        webView.isHorizontalScrollBarEnabled = true;
        webView.loadUrl(URL)
    }

    override fun onRefresh() {
        val url: String = webView!!.url!!
        if (url == ERROR_PAGE_URL) {
            webView!!.loadUrl(URL)
        } else {
            webView.reload()
        }
    }
}