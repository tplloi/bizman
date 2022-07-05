package com.loitp.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import androidx.core.view.isVisible
import com.loitp.R
import com.loitpcore.annotation.IsFullScreen
import com.loitpcore.annotation.LogTag
import com.loitpcore.core.base.BaseFontActivity
import com.loitpcore.core.utilities.LUIUtil
import kotlinx.android.synthetic.main.activity_setting.*

@LogTag("SettingActivity")
@IsFullScreen(true)
class SettingActivity : BaseFontActivity() {

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
