package com.terfess.comunidadmp.ui.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.terfess.comunidadmp.R

class RegisterFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_registro, container, false)

        val emailRegist = view.findViewById<EditText>(R.id.editUserR)
        val passwrdRegist = view.findViewById<EditText>(R.id.editPasswordR)
        val anuncioFail = view.findViewById<TextView>(R.id.anuncioRegistroFail)

        //comportamiento boton y progress
        val botonRegistro = view.findViewById<Button>(R.id.registrarse_btn)
        val progressReg = view.findViewById<ProgressBar>(R.id.progress_regist)


        //------------------------------------
        botonRegistro.setOnClickListener {
            //si campos estan llenos
            if (emailRegist.text.isNotBlank() && passwrdRegist.text.isNotBlank() ){
                // Verificar si el correo electrónico es válido
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailRegist.text).matches()) {
                    anuncioFail.text =
                        "Correo electrónico inválido. Utiliza un correo real."
                    anuncioFail.visibility = View.VISIBLE

                } else if (passwrdRegist.text.length < 8) { // Verificar si la contraseña tiene al menos 8 caracteres
                    anuncioFail.text = "La contraseña debe tener al menos 8 caracteres."
                    anuncioFail.visibility = View.VISIBLE

                } else { // Si el correo electrónico y la contraseña son válidos
                    // Ocultar el mensaje de error
                    anuncioFail.visibility = View.GONE
                    // Verificar si ambos campos no están en blanco
                    if (emailRegist.text.isNotBlank() && passwrdRegist.text.isNotBlank()) {
                        // Crear la cuenta utilizando el ViewModel
                        viewModel.crearCuenta(
                            view,
                            emailRegist.text.toString(),
                            passwrdRegist.text.toString()
                        )
                        botonRegistro.visibility = View.GONE
                        progressReg.visibility = View.VISIBLE
                    }
                }
            }else{
                anuncioFail.text = "Debe llenar todos los campos."
                anuncioFail.visibility = View.VISIBLE
            }


        }

        val botonIrLogin = view.findViewById<TextView>(R.id.iraLogin)
        botonIrLogin.setOnClickListener {
            println("Click en volver a login")
            findNavController().navigate(R.id.navigation_login)
        }


        return view
    }

}