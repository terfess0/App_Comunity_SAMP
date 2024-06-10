package com.terfess.comunidadmp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.terfess.comunidadmp.ui.home.HomeViewModel

class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle the splash screen transition.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        splashScreen.setKeepOnScreenCondition { true }

        val transicion = Intent(this, MainActivity::class.java)
        //retardo de 2 segs en iniciar app
        Handler().postDelayed({
            startActivity(transicion)
            finish()
        }, 2000)
    }
}