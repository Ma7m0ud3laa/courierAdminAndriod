package com.kadabra.agent.exception

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle

import com.kadabra.agent.R
import com.kadabra.agent.intro.SplashActivity
import kotlinx.android.synthetic.main.activity_crash.*

class CrashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash)
        btnRestart?.setOnClickListener {
            startActivity(Intent(this@CrashActivity, SplashActivity::class.java))
            finish()
        }
    }
}
