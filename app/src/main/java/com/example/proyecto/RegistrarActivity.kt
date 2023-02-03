package com.example.proyecto

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegistrarActivity : AppCompatActivity() {

    private val TAG = "test"
    private var emailEditText: EditText? = null
    private var passEditText: EditText? = null
    private var passEditText2: EditText? = null
    private lateinit var  auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrar)
        title="REGISTRARSE"
        emailEditText = findViewById<EditText>(R.id.editTextTextEmailAddressAuth)
        passEditText = findViewById<EditText>(R.id.editTextTextPassword)
        passEditText2 = findViewById<EditText>(R.id.editTextTextPassword2)
        val btn_registrar = findViewById<Button>(R.id.button_registrar)
        auth= FirebaseAuth.getInstance()

        btn_registrar.setOnClickListener({
            register()
        })

    }

    fun register(){
        val email= emailEditText?.text.toString()
        val password= passEditText?.text.toString()
        val password2= passEditText2?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(password2)) {
            if(password.equals(password2)){
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        val intent= Intent(this,AuthActivity::class.java)
                        startActivity(intent)
                        Log.d(TAG, "Registro Exitoso")
                        Toast.makeText(this, "Registro Exitoso", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
                    Log.d(TAG, "registro erronea")
                }
            }else{
                Toast.makeText(this, "La Contraseña No Coincide", Toast.LENGTH_SHORT).show()
            }


        }else {
            Toast.makeText(this, "Ingrese Los Datos Correctos", Toast.LENGTH_SHORT).show()
        }
    }


}