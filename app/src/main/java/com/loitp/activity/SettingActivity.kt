package com.loitp.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.browser.customtabs.CustomTabsService.KEY_URL
import androidx.core.view.isVisible
import com.loitp.R
import com.loitp.app.Cons
import com.loitpcore.annotation.IsFullScreen
import com.loitpcore.annotation.LogTag
import com.loitpcore.core.base.BaseFontActivity
import com.loitpcore.core.utilities.LSharedPrefsUtil
import com.loitpcore.core.utilities.LUIUtil
import com.loitpcore.views.setSafeOnClickListener
import kotlinx.android.synthetic.main.activity_setting.*


@LogTag("loitppSettingActivity")
@IsFullScreen(true)
class SettingActivity : BaseFontActivity() {

    companion object {
        const val RESULT_CODE = 123
        const val KEY_RESULT = "KEY_RESULT"
    }

    override fun setLayoutResourceId(): Int {
        return R.layout.activity_setting
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        setupViews()
    }

    private fun setupViews() {
        setupActionBar()

        val url = LSharedPrefsUtil.instance.getString(Cons.KEY_URL_THANOS, Cons.URL_DEV)
        if (url == Cons.URL_DEV) {
            swIsDevEnv.isChecked = true
            tvUrl.text = Cons.URL_DEV
        } else {
            swIsDevEnv.isChecked = false
            tvUrl.text = Cons.URL_PROD
        }

        swIsDevEnv.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvUrl.text = Cons.URL_DEV
            } else {
                tvUrl.text = Cons.URL_PROD
            }
        }

        btConfirm.setSafeOnClickListener {
            val isCheck = swIsDevEnv.isChecked
            logD("isCheck $isCheck")
            if (isCheck) {
                LSharedPrefsUtil.instance.putString(Cons.KEY_URL_THANOS, Cons.URL_DEV)
            } else {
                LSharedPrefsUtil.instance.putString(Cons.KEY_URL_THANOS, Cons.URL_PROD)
            }

//            val savedLink = LSharedPrefsUtil.instance.getString(Cons.KEY_URL_THANOS, Cons.URL_DEV)
//            logE(">>>>btConfirm $savedLink")

            val intent = Intent()
            intent.putExtra(KEY_RESULT, true)
            setResult(RESULT_CODE, intent)
            onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupActionBar() {
        lActionBar.apply {
            LUIUtil.setSafeOnClickListenerElastic(
                view = this.ivIconLeft,
                runnable = {
                    onBackPressed()
                }
            )
            this.ivIconRight?.isVisible = false
            this.viewShadow?.isVisible = true
            this.tvTitle?.text = "Setting"
        }
    }
}
