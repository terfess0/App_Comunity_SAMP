package com.terfess.comunidadmp.ui.login

import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.findFragment
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.terfess.comunidadmp.MainActivity
import com.terfess.comunidadmp.R
import com.terfess.comunidadmp.clases.Utilities
import java.text.SimpleDateFormat
import java.util.Date


class LoginViewModel : ViewModel() {


    //MODULO AUTENTICACION FIREBASE
    private var auth: FirebaseAuth = Firebase.auth

    fun crearCuenta(vista: View, email: String, password: String) {
        val contexto = vista.context
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val usuariosRef = db.collection("users")

        val timestamp = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = Date(timestamp)
        val fechaRegist = dateFormat.format(date)

        val default_img_profile =
            "https://firebasestorage.googleapis.com/v0/b/los-santos-sa-mp.appspot.com/o/images%2Fpngwing.com%20(1).png?alt=media&token=c5f67d4f-2b5a-49ab-bb47-3938f0a9067c"

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // El usuario fue creado exitosamente

                    // Crear el nuevo usuario en la colección "users"
                    val nuevoUsuarioId = usuariosRef.document().id
                    val nuevoUsuario = hashMapOf(
                        "userEmail" to email,
                        "userFechaRegist" to fechaRegist.toString(),
                        "userPhoto" to default_img_profile
                    )
                    usuariosRef.document(nuevoUsuarioId)
                        .set(nuevoUsuario)
                        .addOnSuccessListener {
                            // El nuevo usuario se agregó con éxito
                            val intent = Intent(contexto, MainActivity::class.java)
                            contexto.startActivity(intent)
                            Toast.makeText(
                                contexto,
                                "Usuario registrado, iniciando sesión",
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController(vista.findFragment()).navigate(R.id.action_navigation_registro_to_navigation_home)
                            Utilities().saveSharedpref(vista.context, "LoginState", "true")
                        }
                        .addOnFailureListener { e ->
                            // Error al agregar el nuevo usuario a Firestore
                            Toast.makeText(
                                contexto,
                                "Error al crear la cuenta: $e",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    // Error al crear la cuenta
                    //comportamiento boton y progress
                    val registButton = vista.findViewById<Button>(R.id.registrarse_btn)
                    val progressReg =
                        vista.findViewById<ProgressBar>(R.id.progress_regist)
                    registButton.visibility = View.VISIBLE
                    progressReg.visibility = View.GONE
                    Toast.makeText(
                        contexto,
                        "No se pudo al crear la cuenta: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }


    fun iniciarSesionContraseña(vista: View, email: String, password: String) {
        //edits de usuario y contraseña
        val correoLogin = vista.findViewById<EditText>(R.id.editUser)
        val contra = vista.findViewById<EditText>(R.id.editPassword)

        //anuncio de error en credenciales
        val anuncio = vista.findViewById<TextView>(R.id.cred_fail)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("signInWithEmail:success")
                    val user = auth.currentUser

                    //usuario logea correctamente, iniciar app
                    Toast.makeText(vista.context, "Sesión iniciada", Toast.LENGTH_SHORT).show()
                    //navegar a login
                    findNavController(vista.findFragment()).navigate(R.id.action_navigation_login_to_navigation_home)
                    Utilities().saveSharedpref(vista.context, "LoginState", "true")
                    //limpiar fragments en la pila que estan antes del destino
                    findNavController(vista.findFragment()).popBackStack(
                        R.id.navigation_login,
                        false
                    )
                } else {
                    //comportamiento boton y progress
                    val loginButton = vista.findViewById<Button>(R.id.login_btn)
                    val progressLog = vista.findViewById<ProgressBar>(R.id.progress_login)

                    loginButton.visibility = View.VISIBLE
                    progressLog.visibility = View.GONE

                    // If sign in fails, display a message to the user.
                    println("signInWithEmail:failure = ${task.exception}")
                    //callback fail
                    when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Credenciales de autenticación inválidas, como una contraseña incorrecta
                            anuncio.text = "Correo o contraseña incorrectos, intenta nuevamente."
                            anuncio.visibility = View.VISIBLE

                            //limpiar campos
                            correoLogin.setText("")
                            contra.setText("")

                        }

                        is FirebaseAuthInvalidUserException -> {
                            // El usuario no existe o fue deshabilitado
                            anuncio.text = "Este correo no existe en ninguna cuenta, regístrate."
                            anuncio.visibility = View.VISIBLE

                            //limpiar campos
                            correoLogin.setText("")
                            contra.setText("")
                        }

                        else -> {
                            // Otra excepción no especificada
                            anuncio.text = "Algo ha salido mal, intentalo nuevamente más tarde."
                            anuncio.visibility = View.VISIBLE

                            //limpiar campos
                            correoLogin.setText("")
                            contra.setText("")
                        }
                    }
                }
            }
    }
}