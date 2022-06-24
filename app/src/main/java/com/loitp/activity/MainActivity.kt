package com.loitp.activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.webkit.JavascriptInterface
import com.annotation.IsFullScreen
import com.annotation.LogTag
import com.core.base.BaseApplication
import com.core.base.BaseFontActivity
import com.loitp.R
import com.loitp.viewmodels.MainViewModel
import com.views.LWebViewAdblock
import kotlinx.android.synthetic.main.activity_main.*

@LogTag("MainActivity")
@IsFullScreen(true)
class MainActivity : BaseFontActivity() {

    private var mainViewModel: MainViewModel? = null

    override fun setLayoutResourceId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViews()
        setupViewModels()
        mainViewModel?.getUserTestListByPage(page = 1, isRefresh = true)
    }

    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        showShortInformation(msg = getString(R.string.press_again_to_exit), isTopAnchor = false)
        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }

    private fun setupViews() {
        lWebView.callback = object : LWebViewAdblock.Callback {
            override fun onScroll(l: Int, t: Int, oldl: Int, oldt: Int) {
            }

            override fun onScrollTopToBottom() {
                logD("onScrollTopToBottom")
            }

            override fun onScrollBottomToTop() {
                logD("onScrollBottomToTop")
            }

            override fun onProgressChanged(progress: Int) {
                logD("onProgressChanged $progress")
            }

            override fun shouldOverrideUrlLoading(url: String) {
                logE(">shouldOverrideUrlLoading $url")
            }
        }
        onDetectClick()
        lWebView.loadUrl("https://bizman.dikauri.com/signin")
    }

    private fun setupViewModels() {
        mainViewModel = getViewModel(MainViewModel::class.java)
        mainViewModel?.let { vm ->
            vm.userActionLiveData.observe(
                owner = this,
                observer = { action ->
                    logD("userAction.observe action.isDoing ${action.isDoing}")
                    action.isDoing?.let { isDoing ->

                    }

                    action.data?.let { userTestList ->
                        val isRefresh = action.isSwipeToRefresh

                    }

                    action.errorResponse?.let { error ->
                        logE("observe error " + BaseApplication.gson.toJson(error))
                        error.message?.let {
                            showDialogError(
                                errMsg = it,
                                runnable = {
                                    // do nothing
                                }
                            )
                        }
                    }
                }
            )
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && lWebView.canGoBack()) {
            lWebView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onDetectClick() {
        lWebView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            @Throws(java.lang.Exception::class)
            fun performClick(id: String) {
                logE("isDetectButtonClickWeb order id: $id")
                showLongInformation("isDetectButtonClickWeb order id: $id");
            }
        }, "handlePrintOrder")
    }
}
