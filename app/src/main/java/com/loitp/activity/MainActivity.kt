package com.loitp.activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.core.view.isVisible
import com.iposprinter.iposprinterservice.IPosPrinterCallback
import com.iposprinter.iposprinterservice.IPosPrinterService
import com.loitp.BuildConfig
import com.loitp.R
import com.loitp.model.Data
import com.loitp.print.BytesUtil
import com.loitp.print.HandlerUtils
import com.loitp.print.MemInfo.bitmapRecycle
import com.loitp.print.PrintContentsExamples.*
import com.loitp.print.ThreadPoolManager
import com.loitp.viewmodels.MainViewModel
import com.loitpcore.annotation.IsFullScreen
import com.loitpcore.annotation.LogTag
import com.loitpcore.core.base.BaseApplication
import com.loitpcore.core.base.BaseFontActivity
import com.loitpcore.core.utilities.LActivityUtil
import com.loitpcore.core.utilities.LSoundUtil
import com.loitpcore.views.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

@LogTag("loitppMainActivity")
@IsFullScreen(true)
class MainActivity : BaseFontActivity() {

    companion object {
        private const val PRINTER_NORMAL = 0
        private const val PRINTER_PAPERLESS = 1
        private const val PRINTER_THP_HIGH_TEMPERATURE = 2
        private const val PRINTER_MOTOR_HIGH_TEMPERATURE = 3
        private const val PRINTER_IS_BUSY = 4
        private const val PRINTER_ERROR_UNKNOWN = 5

        private const val PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION"
        private const val PRINTER_PAPERLESS_ACTION =
            "com.iposprinter.iposprinterservice.PAPERLESS_ACTION"
        private const val PRINTER_PAPEREXISTS_ACTION =
            "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION"
        private const val PRINTER_THP_HIGHTEMP_ACTION =
            "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION"
        private const val PRINTER_THP_NORMALTEMP_ACTION =
            "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION"
        private const val PRINTER_MOTOR_HIGHTEMP_ACTION =
            "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION"
        private const val PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION"
        private const val PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION =
            "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION"

        private const val MSG_TEST = 1
        private const val MSG_IS_NORMAL = 2
        private const val MSG_IS_BUSY = 3
        private const val MSG_PAPER_LESS = 4
        private const val MSG_PAPER_EXISTS = 5
        private const val MSG_THP_HIGH_TEMP = 6
        private const val MSG_THP_TEMP_NORMAL = 7
        private const val MSG_MOTOR_HIGH_TEMP = 8
        private const val MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9
        private const val MSG_CURRENT_TASK_PRINT_COMPLETE = 10

        private const val MULTI_THREAD_LOOP_PRINT = 1
        private const val INPUT_CONTENT_LOOP_PRINT = 2
        private const val DEMO_LOOP_PRINT = 3
        private const val PRINT_DRIVER_ERROR_TEST = 4
        private const val DEFAULT_LOOP_PRINT = 0

        private const val URL_DEV = "https://bizman.dikauri.com/signin"
        private const val URL_PROD = "https://app.dikauri.com/ "
    }

    private var mainViewModel: MainViewModel? = null

    private var loopPrintFlag = DEFAULT_LOOP_PRINT
    private var loopContent: Byte = 0x00
    private var printDriverTestCount = 0
    private var printerStatus = 0

    private var mIPosPrinterService: IPosPrinterService? = null
    private var callback: IPosPrinterCallback? = null

    private val random = Random()
    private var handler: HandlerUtils.MyHandler? = null

    /**
     * 消息处理
     */
    private val iHandlerIntent: HandlerUtils.IHandlerIntent =
        HandlerUtils.IHandlerIntent { msg ->
            when (msg.what) {
                MSG_TEST -> {}
                MSG_IS_NORMAL -> if (getPrinterStatus() == PRINTER_NORMAL) {
                    loopPrint(loopPrintFlag)
                }
                MSG_IS_BUSY -> Toast.makeText(
                    baseContext,
                    R.string.printer_is_working,
                    Toast.LENGTH_SHORT
                ).show()
                MSG_PAPER_LESS -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(
                        baseContext,
                        R.string.out_of_paper,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                MSG_PAPER_EXISTS -> Toast.makeText(
                    baseContext,
                    R.string.exists_paper,
                    Toast.LENGTH_SHORT
                ).show()
                MSG_THP_HIGH_TEMP -> Toast.makeText(
                    baseContext,
                    R.string.printer_high_temp_alarm,
                    Toast.LENGTH_SHORT
                ).show()
                MSG_MOTOR_HIGH_TEMP -> {
                    loopPrintFlag = DEFAULT_LOOP_PRINT
                    Toast.makeText(
                        baseContext,
                        R.string.motor_high_temp_alarm,
                        Toast.LENGTH_SHORT
                    ).show()
                    handler?.sendEmptyMessageDelayed(
                        MSG_MOTOR_HIGH_TEMP_INIT_PRINTER,
                        180000
                    ) //马达高温报警，等待3分钟后复位打印机
                }
                MSG_MOTOR_HIGH_TEMP_INIT_PRINTER -> printerInit()
                MSG_CURRENT_TASK_PRINT_COMPLETE -> Toast.makeText(
                    baseContext,
                    R.string.printer_current_task_print_complete,
                    Toast.LENGTH_SHORT
                ).show()
                else -> {}
            }
        }

    private val iPosPrinterStatusListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null) {
                logD("IPosPrinterStatusListener onReceive action = null")
                return
            }
            logD("IPosPrinterStatusListener action = $action")
            when (action) {
                PRINTER_NORMAL_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_IS_NORMAL, 0)
                }
                PRINTER_PAPERLESS_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_PAPER_LESS, 0)
                }
                PRINTER_BUSY_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_IS_BUSY, 0)
                }
                PRINTER_PAPEREXISTS_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_PAPER_EXISTS, 0)
                }
                PRINTER_THP_HIGHTEMP_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_THP_HIGH_TEMP, 0)
                }
                PRINTER_THP_NORMALTEMP_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_THP_TEMP_NORMAL, 0)
                }
                PRINTER_MOTOR_HIGHTEMP_ACTION //此时当前任务会继续打印，完成当前任务后，请等待2分钟以上时间，继续下一个打印任务
                -> {
                    handler?.sendEmptyMessageDelayed(MSG_MOTOR_HIGH_TEMP, 0)
                }
                PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION -> {
                    handler?.sendEmptyMessageDelayed(MSG_CURRENT_TASK_PRINT_COMPLETE, 0)
                }
                else -> {
                    handler?.sendEmptyMessageDelayed(MSG_TEST, 0)
                }
            }
        }
    }

    private val connectService: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mIPosPrinterService = IPosPrinterService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mIPosPrinterService = null
        }
    }

    override fun setLayoutResourceId(): Int {
        return R.layout.activity_main
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        handler = HandlerUtils.MyHandler(iHandlerIntent)
        callback = object : IPosPrinterCallback.Stub() {
            @Throws(RemoteException::class)
            override fun onRunResult(isSuccess: Boolean) {
                logD("result:$isSuccess\n")
            }

            @Throws(RemoteException::class)
            override fun onReturnString(value: String) {
                logD("result:$value\n")
            }
        }

        val intent = Intent()
        intent.setPackage("com.iposprinter.iposprinterservice")
        intent.action = "com.iposprinter.iposprinterservice.IPosPrintService"
        //startService(intent);
        //startService(intent);
        bindService(intent, connectService, BIND_AUTO_CREATE)

        val printerStatusFilter = IntentFilter()
        printerStatusFilter.addAction(PRINTER_NORMAL_ACTION)
        printerStatusFilter.addAction(PRINTER_PAPERLESS_ACTION)
        printerStatusFilter.addAction(PRINTER_PAPEREXISTS_ACTION)
        printerStatusFilter.addAction(PRINTER_THP_HIGHTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_THP_NORMALTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_MOTOR_HIGHTEMP_ACTION)
        printerStatusFilter.addAction(PRINTER_BUSY_ACTION)

        registerReceiver(iPosPrinterStatusListener, printerStatusFilter)

        setupViews()
        setupViewModels()
    }

    override fun onStop() {
        loopPrintFlag = DEFAULT_LOOP_PRINT
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(iPosPrinterStatusListener)
        unbindService(connectService)
        handler?.removeCallbacksAndMessages(null)
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

    private fun clearCache() {
        lWebView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null)
        lWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        lWebView.clearCache(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null)
            CookieManager.getInstance().flush()
        }
    }

    private fun setupViews() {
        lWebView.settings.javaScriptEnabled = true
        lWebView.settings.domStorageEnabled = true
//        lWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        lWebView.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        lWebView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                logD("shouldOverrideUrlLoading url ${request?.url}")
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                logD("onPageFinished url $url")
                clearCache()
            }
        }

        lWebView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
//                logD("onProgressChanged newProgress $newProgress")
                progressBar.isVisible = newProgress < 99
            }
        }

//        clearCache()

//        lWebView.callback = object : LWebViewAdblock.Callback {
//            override fun onScroll(l: Int, t: Int, oldl: Int, oldt: Int) {
//            }
//
//            override fun onScrollTopToBottom() {
//                logD("onScrollTopToBottom")
//            }
//
//            override fun onScrollBottomToTop() {
//                logD("onScrollBottomToTop")
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                logD("onPageFinished url $url")
//                clearCache()
//            }
//
//            override fun onProgressChanged(progress: Int) {
//            }
//
//            override fun shouldOverrideUrlLoading(url: String) {
//                logE(">shouldOverrideUrlLoading $url")
////                clearCache()
//            }
//        }
        onDetectClick()
        lWebView.loadUrl(URL_DEV)

//        val noCacheHeaders: MutableMap<String, String> = HashMap(2)
//        noCacheHeaders["Pragma"] = "no-cache"
//        noCacheHeaders["Cache-Control"] = "no-cache"
//        lWebView.loadUrl("https://bizman.dikauri.com/signin", noCacheHeaders)

        if (BuildConfig.DEBUG) {
            btReload.isVisible = true
        }
        btReload.setSafeOnClickListener {
            reload()
        }
        btChangeEnv.setOnClickListener() {
            handleBtChangeEnv()
        }
    }

    private var countClickBtChangeEnv = 0
    private fun handleBtChangeEnv() {

        fun changeEnv() {
            logD("changeEnv")
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
            LActivityUtil.tranIn(this)
        }

        if (countClickBtChangeEnv >= 10) {
            changeEnv()
            countClickBtChangeEnv = 0
            return
        }
        countClickBtChangeEnv++
        logD("changeEnv countClickBtChangeEnv $countClickBtChangeEnv")
    }

    private fun reload() {
        logE(">>>>>>>reload webview")
        lWebView.reload()
//        lWebView.loadUrl("javascript:window.location.reload( true )")
    }

    private fun setupViewModels() {
        mainViewModel = getViewModel(MainViewModel::class.java)
        mainViewModel?.let { vm ->
            vm.dataActionLiveData.observe(
                owner = this,
                observer = { action ->
//                    logD("dataActionLiveData action.isDoing ${action.isDoing}")
                    action.isDoing?.let { isDoing ->
//                        logD("isDoing $isDoing")
                        val data = action.data
                        if (!isDoing && data != null) {
                            print(data)
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
                logE("performClick id $id")
                mainViewModel?.getBookingDetail(id)
            }
        }, "handlePrintOrder")

        lWebView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            @Throws(java.lang.Exception::class)
            fun performClick(nameSound: String) {
                logE("performClick nameSound $nameSound")
                LSoundUtil.startMusicFromAsset(fileName = nameSound)
            }
        }, "handlerPlaySound")

//        lWebView.addJavascriptInterface(object : Any() {
//            @JavascriptInterface
//            @Throws(java.lang.Exception::class)
//            fun performClick() {
//                logE("handleRedraw")
//                reload()
//            }
//        }, "handleRedraw")
    }

    private fun print(data: Data) {
        logE("print data " + (BaseApplication.gson to data))
        if (getPrinterStatus() == PRINTER_NORMAL) {
            ThreadPoolManager.getInstance().executeTask {
                try {
                    mIPosPrinterService?.apply {
                        printSpecifiedTypeText(data.getPrintContent(), "ST", 32, callback)
                        printerPerformPrint(160, callback)
                    }
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getPrinterStatus(): Int {
        logD("***** printerStatus$printerStatus")
        try {
            mIPosPrinterService?.let {
                printerStatus = it.printerStatus
            }
        } catch (e: RemoteException) {
            e.printStackTrace()
        }
        logD("        logD(#### printerStatus$printerStatus)\n")
        return printerStatus
    }

    private fun printerInit() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.printerInit(callback)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printSelf() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    printerInit(callback)
                    printSpecifiedTypeText("   打印机自检\n", "ST", 48, callback)
                    printBlankLines(1, 20, callback)
                    printRawData(BytesUtil.BlackBlockData(300), callback)
                    printBlankLines(1, 20, callback)
                    setPrinterPrintAlignment(1, callback)
                    printQRCode("http://www.baidu.com\n", 10, 1, callback)
                    printBlankLines(1, 20, callback)
                    printSpecifiedTypeText("   打印机正常\n", "ST", 48, callback)
                    printBlankLines(1, 16, callback)
                    printSpecifiedTypeText("        欢迎使用\n", "ST", 32, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printText() {
        ThreadPoolManager.getInstance().executeTask {
            val mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.test)
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText("    智能POS机\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "    智能POS机数据终端\n",
                        "ST",
                        32,
                        callback
                    )
                    printBlankLines(1, 8, callback)
                    printSpecifiedTypeText(
                        "      欢迎使智能POS机数据终端\n",
                        "ST",
                        24,
                        callback
                    )
                    printBlankLines(1, 8, callback)
                    printSpecifiedTypeText(
                        "智能POS 数据终端 智能POS\n",
                        "ST",
                        32,
                        callback
                    )
                    printBlankLines(1, 8, callback)
                    printSpecifiedTypeText(
                        "#POS POS ipos POS POS POS POS ipos POS POS ipos#\n",
                        "ST",
                        16,
                        callback
                    )
                    printBlankLines(1, 16, callback)
                    printBitmap(1, 12, mBitmap, callback)
                    printBlankLines(1, 16, callback)
                    PrintSpecFormatText("开启打印测试\n", "ST", 32, 1, callback)
                    printSpecifiedTypeText(
                        "********************************",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText("这是一行16号字体\n", "ST", 16, callback)
                    printSpecifiedTypeText("这是一行24号字体\n", "ST", 24, callback)
                    PrintSpecFormatText("这是一行24号字体\n", "ST", 24, 2, callback)
                    printSpecifiedTypeText("这是一行32号字体\n", "ST", 32, callback)
                    PrintSpecFormatText("这是一行32号字体\n", "ST", 32, 2, callback)
                    printSpecifiedTypeText("这是一行48号字体\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "ABCDEFGHIJKLMNOPQRSTUVWXYZ01234\n",
                        "ST",
                        16,
                        callback
                    )
                    printSpecifiedTypeText(
                        "abcdefghijklmnopqrstuvwxyz56789\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "κρχκμνκλρκνκνμρτυφ\n",
                        "ST",
                        24,
                        callback
                    )
                    setPrinterPrintAlignment(0, callback)
                    printQRCode("http://www.baidu.com\n", 10, 1, callback)
                    printBlankLines(1, 16, callback)
                    printBlankLines(1, 16, callback)
                    for (i in 0..11) {
                        printRawData(BytesUtil.initLine1(384, i), callback)
                    }
                    PrintSpecFormatText("打印测试完成\n", "ST", 32, 1, callback)
                    printSpecifiedTypeText(
                        "**********END***********\n\n",
                        "ST",
                        32,
                        callback
                    )
                    bitmapRecycle(mBitmap)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printTable() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    setPrinterPrintAlignment(0, callback)
                    setPrinterPrintFontSize(24, callback)
                    var text = arrayOfNulls<String>(4)
                    var width = intArrayOf(8, 6, 6, 7)
                    var align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                    text[0] = "名称"
                    text[1] = "数量"
                    text[2] = "单价"
                    text[3] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶A布甸"
                    text[1] = "4"
                    text[2] = "12.00"
                    text[3] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "16.00"
                    text[3] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 16, callback)
                    setPrinterPrintAlignment(1, callback)
                    setPrinterPrintFontSize(24, callback)
                    text = arrayOfNulls(3)
                    width = intArrayOf(8, 6, 7)
                    align = intArrayOf(0, 2, 2)
                    text[0] = "菜品"
                    text[1] = "数量"
                    text[2] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶布甸"
                    text[1] = "4"
                    text[2] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 16, callback)
                    setPrinterPrintAlignment(2, callback)
                    setPrinterPrintFontSize(16, callback)
                    text = arrayOfNulls(4)
                    width = intArrayOf(10, 6, 6, 8)
                    align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                    text[0] = "名称"
                    text[1] = "数量"
                    text[2] = "单价"
                    text[3] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶A布甸"
                    text[1] = "4"
                    text[2] = "12.00"
                    text[3] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "16.00"
                    text[3] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 8, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printBitmap() {
        ThreadPoolManager.getInstance().executeTask {
            val mBitmap = BitmapFactory.decodeResource(resources, R.mipmap.test_p)
            try {
                mIPosPrinterService?.apply {
                    printBitmap(0, 4, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(1, 6, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(2, 8, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(2, 10, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(1, 12, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(0, 14, mBitmap, callback)
                    printBlankLines(1, 10, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printBarcode() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    setPrinterPrintAlignment(0, callback)
                    printBarCode("2017072618", 8, 2, 5, 0, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(1, callback)
                    printBarCode("2017072618", 8, 3, 6, 1, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(2, callback)
                    printBarCode("2017072618", 8, 4, 7, 2, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(2, callback)
                    printBarCode("2017072618", 8, 5, 8, 3, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(1, callback)
                    printBarCode("2017072618", 8, 3, 7, 3, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(1, callback)
                    printBarCode("2017072618", 8, 3, 6, 1, callback)
                    printBlankLines(1, 25, callback)
                    setPrinterPrintAlignment(1, callback)
                    printBarCode("2017072618", 8, 3, 4, 2, callback)
                    printBlankLines(1, 25, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printQRCode() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    setPrinterPrintAlignment(0, callback)
                    printQRCode("http://www.baidu.com\n", 2, 1, callback)
                    printBlankLines(1, 15, callback)
                    setPrinterPrintAlignment(1, callback)
                    printQRCode("http://www.baidu.com\n", 3, 0, callback)
                    printBlankLines(1, 15, callback)
                    setPrinterPrintAlignment(2, callback)
                    printQRCode("http://www.baidu.com\n", 4, 2, callback)
                    printBlankLines(1, 15, callback)
                    setPrinterPrintAlignment(0, callback)
                    printQRCode("http://www.baidu.com\n", 5, 3, callback)
                    printBlankLines(1, 15, callback)
                    setPrinterPrintAlignment(1, callback)
                    printQRCode("http://www.baidu.com\n", 6, 2, callback)
                    printBlankLines(1, 15, callback)
                    setPrinterPrintAlignment(2, callback)
                    printQRCode("http://www.baidu.com\n", 7, 1, callback)
                    printBlankLines(1, 15, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printErlmoBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText(Elemo, "ST", 32, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printBaiduBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText(Baidu, "ST", 32, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printKoubeiBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText("   #4口碑外卖\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "         " + "冯记黄焖鸡米饭\n********************************\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText("17:20 尽快送达\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "18610858337韦小宝创智天地广场7号楼(605室)\n",
                        "ST",
                        48,
                        callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText("下单: 16:35\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "********************************\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "菜品          数量   单价   " +
                                "金额\n--------------------------------\n黄焖五花肉 (大) (不辣)\n" +
                                "               1      25      25\n黄焖五花肉 (小) (不辣)\n               1      " +
                                "25      25黄焖五花肉 (小) (微辣)\n               1      25      25\n--------------------------------\n配送费" +
                                "  " +
                                "               " +
                                "        2\n--------------------------------\n", "ST", 24, callback
                    )
                    printSpecifiedTypeText(
                        "            实付金额: 27\n\n",
                        "ST",
                        32,
                        callback
                    )
                    printSpecifiedTypeText("    口碑外卖\n\n\n", "ST", 48, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun printMeiTuanBill() {
        ThreadPoolManager.getInstance().executeTask {
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText("  #1  美团测试\n\n", "ST", 48, callback)
                    printSpecifiedTypeText(
                        "      粤香港式烧腊(第1联)\n\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "------------------------\n\n*********预订单*********\n",
                        "ST",
                        32,
                        callback
                    )
                    printSpecifiedTypeText(
                        "  期望送达时间:[18:00]\n\n",
                        "ST",
                        32,
                        callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n下单时间: " + "01-01 12:00",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText("备注: 别太辣\n", "ST", 32, callback)
                    printSpecifiedTypeText(
                        "菜品          数量   小计" + "金额\n--------------------------------\n\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "红烧肉          X1    12\n红烧肉1         X1   " + " 12\n红烧肉2         X1    12\n\n",
                        "ST",
                        32,
                        callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        ("配送费                         5\n餐盒费        " +
                                " " +
                                " " +
                                "               1\n[超时赔付] - 详见订单\n可口可乐: x1"), "ST", 24, callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "合计                18元\n\n",
                        "ST",
                        32,
                        callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText(
                        "张* 18312345678\n地址信息\n",
                        "ST",
                        48,
                        callback
                    )
                    printSpecifiedTypeText(
                        "--------------------------------\n",
                        "ST",
                        24,
                        callback
                    )
                    printSpecifiedTypeText("  #1  美团测试\n\n\n", "ST", 48, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun bigDataPrintTest(numK: Int, data: Byte) {
        ThreadPoolManager.getInstance().executeTask {
            val num4K = 1024 * 4
            val length = if (numK > 127) num4K * 127 else num4K * numK
            val dataBytes = ByteArray(length)
            for (i in 0 until length) {
                dataBytes[i] = data
            }
            try {
                mIPosPrinterService?.apply {
                    printRawData(dataBytes, callback)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun fullTest() {
        ThreadPoolManager.getInstance().executeTask {
            var bmp: Bitmap?
            try {
                mIPosPrinterService?.apply {
                    printRawData(BytesUtil.initBlackBlock(384), callback)
                    printBlankLines(1, 10, callback)
                    printRawData(BytesUtil.initBlackBlock(48, 384), callback)
                    printBlankLines(1, 10, callback)
                    printRawData(BytesUtil.initGrayBlock(48, 384), callback)
                    printBlankLines(1, 10, callback)
                    setPrinterPrintAlignment(0, callback)
                    setPrinterPrintFontSize(24, callback)
                    var text = arrayOfNulls<String>(4)
                    var width = intArrayOf(10, 6, 6, 8)
                    var align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                    text[0] = "名称"
                    text[1] = "数量"
                    text[2] = "单价"
                    text[3] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶A布甸"
                    text[1] = "4"
                    text[2] = "12.00"
                    text[3] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "16.00"
                    text[3] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 16, callback)
                    setPrinterPrintAlignment(1, callback)
                    setPrinterPrintFontSize(24, callback)
                    text = arrayOfNulls(3)
                    width = intArrayOf(10, 6, 8)
                    align = intArrayOf(0, 2, 2)
                    text[0] = "菜品"
                    text[1] = "数量"
                    text[2] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶布甸"
                    text[1] = "4"
                    text[2] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 16, callback)
                    setPrinterPrintAlignment(2, callback)
                    setPrinterPrintFontSize(16, callback)
                    text = arrayOfNulls(4)
                    width = intArrayOf(10, 6, 6, 8)
                    align = intArrayOf(0, 2, 2, 2) // 左齐,右齐,右齐,右齐
                    text[0] = "名称"
                    text[1] = "数量"
                    text[2] = "单价"
                    text[3] = "金额"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "草莓酸奶A布甸"
                    text[1] = "4"
                    text[2] = "12.00"
                    text[3] = "48.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包B"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果布甸香橙软桃蛋糕" // 文字超长,换行
                    text[1] = "100"
                    text[2] = "16.00"
                    text[3] = "1600.00"
                    printColumnsText(text, width, align, 1, callback)
                    text[0] = "酸奶水果夹心面包"
                    text[1] = "10"
                    text[2] = "4.00"
                    text[3] = "40.00"
                    printColumnsText(text, width, align, 0, callback)
                    printBlankLines(1, 10, callback)
                    bmp = BitmapFactory.decodeResource(resources, R.mipmap.test_p)
                    printBitmap(0, 12, bmp, callback)
                    printBitmap(1, 6, bmp, callback)
                    printBitmap(2, 16, bmp, callback)
                    printBlankLines(1, 10, callback)
                    printSpecifiedTypeText(
                        ("智能POS\n" +
                                "智能POS智能POS\n" +
                                "智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS智能POS智能POS智能\n" +
                                "智能POS智能POS智能POS智能POS智能POS智能POS智能\n" +
                                "智能POS智能POS智能POS智能POS智能POS智能POS智能\n" +
                                "智能POS智能POS智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS\n" +
                                "智能POS智能POS\n" +
                                "智能POS\n"), "ST", 16, callback
                    )
                    printBlankLines(1, 10, callback)
                    printSpecifiedTypeText(
                        ("智能POS\n" +
                                "智能POS智能POS\n" +
                                "智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS智能POS智能\n" +
                                "智能POS智能POS智能POS智能POS\n" +
                                "智能POS智能POS智能POS\n" +
                                "智能POS智能POS\n" +
                                "智能POS\n"), "ST", 24, callback
                    )
                    printBlankLines(1, 10, callback)
                    printSpecifiedTypeText(
                        ("手\n" +
                                "手手\n" +
                                "手手手\n" +
                                "手手手手\n" +
                                "手手手手手\n" +
                                "手手手手手手\n" +
                                "手手手手手手手\n" +
                                "手手手手手手手手\n" +
                                "手手手手手手手手手\n" +
                                "手手手手手手手手手手\n" +
                                "手手手手手手手手手手手\n" +
                                "手手手手手手手手手手手手" +
                                "手手手手手手手手手手手\n" +
                                "手手手手手手手手手手\n" +
                                "手手手手手手手手手\n" +
                                "手手手手手手手手\n" +
                                "手手手手手手手\n" +
                                "手手手手手手\n" +
                                "手手手手手\n" +
                                "手手手手\n" +
                                "手手手\n" +
                                "手手\n" +
                                "手\n"), "ST", 32, callback
                    )
                    printBlankLines(1, 10, callback)
                    printSpecifiedTypeText(
                        ("手\n" +
                                "手手\n" +
                                "手手手\n" +
                                "手手手手\n" +
                                "手手手手手\n" +
                                "手手手手手手\n" +
                                "手手手手手手手\n" +
                                "手手手手手手手手" +
                                "手手手手手手手\n" +
                                "手手手手手手\n" +
                                "手手手手手\n" +
                                "手手手手\n" +
                                "手手手\n" +
                                "手手\n" +
                                "手\n"), "ST", 48, callback
                    )
                    printBlankLines(1, 10, callback)
                    var k = 8
                    for (i in 0..47) {
                        bmp = BytesUtil.getLineBitmapFromData(12, k)
                        k += 8
                        if (null != bmp) {
                            printBitmap(1, 11, bmp, callback)
                        }
                    }
                    printBlankLines(1, 10, callback)
                    /*加快bitmap回收，减少内存占用*/bitmapRecycle(bmp)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun continuPrint() {
        ThreadPoolManager.getInstance().executeTask {
            val bmp = BitmapFactory.decodeResource(resources, R.mipmap.test)
            try {
                mIPosPrinterService?.apply {
                    printSpecifiedTypeText(customCHR, "ST", 16, callback)
                    printSpecifiedTypeText(Text, "ST", 16, callback)
                    printSpecifiedTypeText(customCHR, "ST", 24, callback)
                    printSpecifiedTypeText(Text, "ST", 24, callback)
                    printSpecifiedTypeText(customCHR, "ST", 32, callback)
                    printSpecifiedTypeText(Text, "ST", 32, callback)
                    printSpecifiedTypeText(customCHR, "ST", 48, callback)
                    printSpecifiedTypeText(customCHZ1, "ST", 48, callback)
                    printBlankLines(1, 10, callback)
                    printBitmap(0, 4, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(0, 5, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(0, 6, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(0, 7, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(0, 8, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(1, 9, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(1, 10, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(1, 11, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(1, 12, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(1, 13, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(2, 12, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(3, 11, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(4, 10, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(5, 9, bmp, callback)
                    printBlankLines(1, 20, callback)
                    printBitmap(6, 8, bmp, callback)
                    printBlankLines(1, 20, callback)
                    /*加快bitmap回收，减少内存占用*/bitmapRecycle(bmp)
                    printerPerformPrint(160, callback)
                }
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }

    private fun loopPrint(flag: Int) {
        when (flag) {
            MULTI_THREAD_LOOP_PRINT -> multiThreadLoopPrint()
            DEMO_LOOP_PRINT -> demoLoopPrint()
            INPUT_CONTENT_LOOP_PRINT -> bigDataPrintTest(127, loopContent)
            PRINT_DRIVER_ERROR_TEST -> printDriverTest()
            else -> {}
        }
    }

    private fun multiThreadLoopPrint() {
        logD("multiThreadLoopPrint")
        when (random.nextInt(12)) {
            0 -> printText()
            1 -> printBarcode()
            2 -> fullTest()
            3 -> printQRCode()
            4 -> printBitmap()
            5 -> printTable()
            6 -> printBaiduBill()
            7 -> printKoubeiBill()
            8 -> printMeiTuanBill()
            9 -> printErlmoBill()
            10 -> printSelf()
            11 -> continuPrint()
            else -> {}
        }
    }

    private fun demoLoopPrint() {
        logD("demoLoopPrint")
        when (random.nextInt(7)) {
            0 -> printKoubeiBill()
            1 -> printBarcode()
            2 -> printBaiduBill()
            3 -> printBitmap()
            4 -> printErlmoBill()
            5 -> printQRCode()
            6 -> printMeiTuanBill()
            else -> {}
        }
    }

    private fun printDriverTest() {
        if (printDriverTestCount >= 8) {
            loopPrintFlag = DEFAULT_LOOP_PRINT
            printDriverTestCount = 0
        } else {
            printDriverTestCount++
            bigDataPrintTest(printDriverTestCount * 16, 0x11.toByte())
        }
    }
}
