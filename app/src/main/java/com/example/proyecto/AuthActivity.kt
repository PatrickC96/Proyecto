package com.example.proyecto

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth


class AuthActivity : ComponentActivity() {

    private val TAG = "test"
    private var emailEditText: EditText? = null
    private var passEditText: EditText? = null
    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        title="INICIAR SESIÓN"

        emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddressAuth)
        passEditText = findViewById<EditText>(R.id.editTextTextPassword)
        auth = FirebaseAuth.getInstance()

        val btn_acceder = findViewById<Button>(R.id.button_acceder)
        val btn_registrar = findViewById<Button>(R.id.button_registrar)

        btn_acceder.setOnClickListener({
            login()
        })
        btn_registrar.setOnClickListener({
            goToRegister()
        })


    }

    fun login(){
        val email=emailEditText?.text.toString()
        val password=passEditText?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val intent= Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    Log.d(TAG, "acceso exitoso")
                    finish()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(applicationContext,exception.localizedMessage, Toast.LENGTH_LONG).show()
                Log.d(TAG, "acceso erroneo")
            }
        }else {
            Toast.makeText(this, "Ingrese Los Datos Correctos", Toast.LENGTH_SHORT).show()
        }
    }

    fun goToRegister(){
        val intent= Intent(this,RegistrarActivity::class.java)
        startActivity(intent)
    }

}