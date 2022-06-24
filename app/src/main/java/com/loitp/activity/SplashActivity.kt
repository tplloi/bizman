package com.loitp.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.annotation.IsFullScreen
import com.annotation.LogTag
import com.core.base.BaseFontActivity
import com.core.utilities.*
import com.loitp.BuildConfig
import com.loitp.R
import com.permissionx.guolindev.PermissionX
import kotlinx.android.synthetic.main.activity_splash.*

@SuppressLint("CustomSplashScreen")
@LogTag("SplashActivity")
@IsFullScreen(false)
class SplashActivity : BaseFontActivity() {
    private var isAnimDone = false
    private var isCheckReadyDone = false
    private var isShowDialogCheck = false

    override fun setLayoutResourceId(): Int {
        return R.layout.activity_splash
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LUIUtil.setDelay(
            mls = 1500,
            runnable = {
                isAnimDone = true
                goToHome()
            }
        )
        textViewVersion.text = "Version ${BuildConfig.VERSION_NAME}"
        tvPolicy.setOnClickListener {
            LSocialUtil.openBrowserPolicy(context = this)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isShowDialogCheck) {
            checkPermission()
        }
    }

    private fun checkPermission() {

        fun checkPer() {
            isShowDialogCheck = true
            val color = if (LUIUtil.isDarkTheme()) {
                Color.WHITE
            } else {
                Color.BLACK
            }
            PermissionX.init(this)
                .permissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )
                .setDialogTintColor(color, color)
                .onExplainRequestReason { scope, deniedList, _ ->
                    val message = getString(R.string.app_name) + getString(R.string.needs_per)
                    scope.showRequestReasonDialog(
                        permissions = deniedList,
                        message = message,
                        positiveText = getString(R.string.allow),
                        negativeText = getString(R.string.deny)
                    )
                }
                .onForwardToSettings { scope, deniedList ->
                    scope.showForwardToSettingsDialog(
                        permissions = deniedList,
                        message = getString(R.string.per_manually_msg),
                        positiveText = getString(R.string.ok),
                        negativeText = getString(R.string.cancel)
                    )
                }
                .request { allGranted, _, _ ->
                    if (allGranted) {
                        isCheckReadyDone = true
                        goToHome()
                    } else {
                        finish()
                        LActivityUtil.tranOut(this)
                    }
                    isShowDialogCheck = false
                }
        }

        val isCanWriteSystem = LScreenUtil.checkSystemWritePermission()
        if (isCanWriteSystem) {
            checkPer()
        } else {
            val alertDialog = LDialogUtil.showDialog2(
                context = this,
                title = "Need Permissions",
                msg = "This app needs permission to allow modifying system settings",
                button1 = getString(R.string.ok),
                button2 = getString(R.string.cancel),
                onClickButton1 = {
                    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                    intent.data = Uri.parse("package:$packageName")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    LActivityUtil.tranIn(this@SplashActivity)
                },
                onClickButton2 = {
                    onBackPressed()
                }
            )
            alertDialog.setCancelable(false)
        }
    }

    private fun goToHome() {
        if (isAnimDone && isCheckReadyDone) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            LActivityUtil.tranIn(this)
            this.finishAfterTransition()
        }
    }
}
