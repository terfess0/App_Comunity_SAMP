package com.terfess.comunidadmp.ui.login

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.terfess.comunidadmp.R

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logear, container, false)
        val loginButton = view.findViewById<Button>(R.id.login_btn)
        val registerTextButton = view.findViewById<TextView>(R.id.iraRegistro)

        //edits de usuario y contraseña
        val correoLogin = view.findViewById<EditText>(R.id.editUser)
        val contra = view.findViewById<EditText>(R.id.editPassword)

        val progressLog = view.findViewById<ProgressBar>(R.id.progress_login)

        //limpiar edits
        correoLogin.setText("")
        contra.setText("")

        //anuncio de error en credenciales
        val anuncio = view.findViewById<TextView>(R.id.cred_fail)


        /////////////el usuario presiona "login" ///////////////////////////////////////////////////////
        loginButton.setOnClickListener {
            println("CLick en login")

            anuncio.visibility = View.GONE

            //campos incompletos
            if (correoLogin.text.toString().isBlank() || contra.text.toString().isBlank()) {
                anuncio.text = "Completa los campos con usuario y contraseña."
                anuncio.visibility = View.VISIBLE
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correoLogin.text).matches()) {
                anuncio.text =
                    "Correo electrónico inválido. Ingresa un correo real."
                anuncio.visibility = View.VISIBLE

            } else if (contra.text.length < 8) { // Verificar si la contraseña tiene al menos 8 caracteres
                anuncio.text = "La contraseña debe tener al menos 8 caracteres."
                anuncio.visibility = View.VISIBLE

            } else { // Si el correo electrónico y la contraseña son válidos
                // Ocultar el mensaje de error
                anuncio.visibility = View.GONE
                // Verificar si ambos campos no están en blanco
                if (correoLogin.text.isNotBlank() && contra.text.isNotBlank()) {
                    // Crear la cuenta utilizando el ViewModel
                    viewModel.iniciarSesionContraseña(
                        view,
                        correoLogin.text.toString(),
                        contra.text.toString()
                    )
                    //mostrar progress en lugar de boton login
                    loginButton.visibility = View.GONE
                    progressLog.visibility = View.VISIBLE
                }
            }
        }

        //el usuario presiona "registrate aqui"
        registerTextButton.setOnClickListener {
            println("CLick en ir a registro")
            findNavController().navigate(R.id.action_navigation_login_to_navigation_registro)
        }

        return view
    }

}
