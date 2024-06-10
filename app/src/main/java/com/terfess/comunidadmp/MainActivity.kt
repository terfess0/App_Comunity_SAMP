package com.terfess.comunidadmp

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.terfess.comunidadmp.clases.Utilities
import com.terfess.comunidadmp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_account, R.id.favoritesFragment
            )
        )

        navView.setupWithNavController(navController)

        val loginState = Utilities().getRegistSheredPref("LoginState", this)
        if (loginState == "true") {
            println("Usuario esta Logeado")
            navController.navigate(R.id.action_navigation_login_to_navigation_home)
        }


        // Escuchar cambios en el destino de navegación
        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {
                R.id.navigation_home, R.id.favoritesFragment, R.id.navigation_account -> {
                    navView.visibility = View.VISIBLE
                }

                R.id.navigation_login, R.id.navigation_registro -> {
                    navView.visibility = View.GONE
                }

                else -> {
                    //nada
                }
            }
        }
    }
}