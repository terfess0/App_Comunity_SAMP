package com.terfess.comunidadmp.ui.account

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.terfess.comunidadmp.clases.Utilities

class AccountViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text
    private val db = Firebase.firestore
    private val user = Firebase.auth.currentUser

    fun getDateRegist(context: Context) {
        db.collection("users")
            .whereEqualTo("userEmail", user?.email)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val fechaRegist = documents.documents[0].getString("userFechaRegist") ?: ""
                    Utilities().saveSharedpref(context, "fechaRegistro", fechaRegist)
                } else {
                    println("Usuario no encontrado o no tiene fecha de registro.")
                }
            }
            .addOnFailureListener { exception ->
                // Manejar errores
                println("Error al obtener fecha de registro: $exception")
            }
    }


}