 package com.example.proyecto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

 var chmuseos : CheckBox? = null
 var chparques : CheckBox? = null
 var chtemplos : CheckBox? = null
 var chestadios : CheckBox? = null
 var chiglesias : CheckBox? = null
 var chzoo : CheckBox? = null
 var chcementerios : CheckBox? = null
 var chexposiciones : CheckBox? = null
 var chdiversiones : CheckBox? = null

 var radioNorte : RadioButton? = null
 var radioCentro : RadioButton? = null
 var radioSur : RadioButton? = null
 var radioGrupo : RadioGroup? = null

 class MainActivity : AppCompatActivity() {

     private val TAG = "test"
     private var selccionUbicacion = 0
     private lateinit var auth: FirebaseAuth

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         //irlogni()
         title="MENÚ"

         auth = FirebaseAuth.getInstance()

        val btn_enviar = findViewById<Button>(R.id.btnEnviar)

         val btn_salir = findViewById<Button>(R.id.btn_salir)

        chmuseos = findViewById(R.id.cbmuseo)
        chparques = findViewById(R.id.cbparques)
        chtemplos = findViewById(R.id.cbtemplos)
        chestadios = findViewById(R.id.cbestadios)
        chiglesias = findViewById(R.id.cbiglesias)
        chzoo = findViewById(R.id.cbzoo)
        chcementerios = findViewById(R.id.cbcementerio)
        chexposiciones = findViewById(R.id.cbexposiciones)
        chdiversiones = findViewById(R.id.cbdiversiones)

        radioNorte = findViewById(R.id.radioButtonNorte)
        radioCentro = findViewById(R.id.radioButtonCentro)
        radioSur = findViewById(R.id.radioButtonSur)
        radioGrupo = findViewById(R.id.radioGrupo)

        btn_enviar.setOnClickListener({
            irMapas()
        })

         btn_salir.setOnClickListener({
             auth.signOut()
             ir_login()
         })

    }
     fun ir_login(){
         val intent= Intent(this,AuthActivity::class.java)
         startActivity(intent)
         finish()
     }
    fun optenerUbicacion(){
        if(radioNorte?.isChecked == true){
            Log.d(TAG,"norte esta seccionado")
            this.selccionUbicacion = 0
        }else if(radioCentro?.isChecked == true){
            Log.d(TAG,"centro esta seccionado")
            this.selccionUbicacion = 1
        }else if(radioSur?.isChecked == true){
            Log.d(TAG,"sur esta seccionado")
            this.selccionUbicacion = 2
        }
    }

     fun irMapas(){
         if(verificarParametros()){
             Toast.makeText(this," Ir al MAPA ",Toast.LENGTH_SHORT).show()
             optenerUbicacion()
             val intentExplicito = Intent(this, MapsActivity::class.java)
             val lista = optenerDatos()
             for (posicion in lista.indices){
                 intentExplicito.putExtra(posicion.toString(),lista.get(posicion).toString())
             }
             intentExplicito.putExtra("ubicacion",this.selccionUbicacion.toString())
             this.startActivity(intentExplicito)
         }
     }

     private fun verificarParametros():Boolean{
         val op1 = chmuseos?.isChecked
         val op2 = chparques?.isChecked
         val op3 = chtemplos?.isChecked
         val op4 = chestadios?.isChecked
         val op5 = chiglesias?.isChecked
         val op6 = chzoo?.isChecked
         val op7 = chcementerios?.isChecked
         val op8 = chexposiciones?.isChecked
         val op9 = chdiversiones?.isChecked

         val ch1 =radioNorte?.isChecked
         val ch2 =radioCentro?.isChecked
         val ch3 =radioSur?.isChecked

         if(op1 == true || op2 == true || op3 == true || op4 == true || op5 == true || op6== true || op7 == true || op8 == true || op9 == true ){
             if(ch1 == true || ch2 == true ||ch3 == true ){
                 return true
             }else{
                 Toast.makeText(this, "Seleccione El Sector", Toast.LENGTH_SHORT).show()
                 return false
             }
         }else{
             Toast.makeText(this, "Seleccione Al Menos Un Sitio", Toast.LENGTH_SHORT).show()
             return false
         }
     }

     private fun optenerDatos(): Array<Boolean?> {
         Log.d("test", "  entro a la funcion clic")
         val opcion1 = chmuseos?.isChecked
         val opcion2 = chparques?.isChecked
         val opcion3 = chtemplos?.isChecked
         val opcion4 = chestadios?.isChecked
         val opcion5 = chiglesias?.isChecked
         val opcion6 = chzoo?.isChecked
         val opcion7 = chcementerios?.isChecked
         val opcion8 = chexposiciones?.isChecked
         val opcion9 = chdiversiones?.isChecked

         return arrayOf(
             opcion1,
             opcion2,
             opcion3,
             opcion4,
             opcion5,
             opcion6,
             opcion7,
             opcion8,
             opcion9
         )
     }
}

