package com.example.proyecto

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.proyecto.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.database.ValueEventListener
import com.example.proyecto.datos.responseJson.MapData
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request as Request2

/*
* Los términos en mayúscula que no se definan en los Términos y condiciones de la prueba gratuita tendrán el significado que se les da en el Acuerdo de licencia de GCP.
* necesarior para la licencia
* */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback ,
    GoogleMap.OnMyLocationClickListener,GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnInfoWindowClickListener{
    private val TAG = "test"
    private var mdatabase = FirebaseDatabase.getInstance().getReference()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var ubucacion_persona : LatLng? = null
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var tienePermisos = false
    private var lista_lugares = arrayOf("")
    private var lugarUbicacion:Int? = null
    private var estatus = 4

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        solictitarPermisos()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        val listas = arrayOf(false, false, false, false, false, false, false, false, false)
        for (posicion in listas.indices){
            intent.getStringExtra(posicion.toString())?.let { listas.set(posicion, it.toBoolean()) }
        }
        var ubiciaion = 0
        intent.getStringExtra("ubicacion")?.let { ubiciaion = it.toInt() }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        optenerUbincacion(fusedLocationClient)
        Log.d(TAG,"ubicaion "+ubiciaion)
        this.lugarUbicacion = ubiciaion

        establecerparametros(listas)
    }

    private fun optenerUbincacion(fusedLocationClient: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location != null){
                    Log.d("posicion","location: "+location.latitude+":"+location.longitude)
                    var originLatitude: Double = location.latitude.toString().toDouble()
                    var originLongitude: Double = location.longitude.toString().toDouble()
                    obtener_oringen(LatLng(originLatitude, originLongitude))
                    Toast.makeText(this, "Ubicacion Correcta ", Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("posicion","no hay location")
                    Toast.makeText(this, "Ubicacion no encontrada", Toast.LENGTH_SHORT).show()
                    var originLatitude: Double = -0.20948825646607305
                    var originLongitude: Double = -78.4879148334935
                    obtener_oringen(LatLng(originLatitude, originLongitude))
                }
                // Got last known location. In some rare situations this can be null.
            }
    }
    fun obtener_oringen(latilong: LatLng){
        this.ubucacion_persona = latilong
    }

    fun establecerparametros(lista: Array<Boolean>){
        val puntos = arrayOf("museum", "park", "store", "stadium", "church", "zoo", "cemetery", "bar", "cafe")
        var contador = 0
        var elementos = arrayOf("", "", "", "", "", "", "", "", "")
        for (posicion in lista.indices){
            if(lista.get(posicion)){
                Log.d("datos", lista.get(posicion).toString()+" : "+puntos.get(posicion))
                elementos.set(contador, puntos.get(posicion))
                contador = contador +1
            }
        }
        this.lista_lugares = elementos
    }

    private fun getdatabasefire(googleMap: GoogleMap,valor : Int) {
        var longitud = "0"
        var latitud = "0"
        var nombre = "0"
        var riesgo = ""
        var rating = ""
        var elementos =  this.lista_lugares
        mdatabase.child(valor.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (elemento in elementos) {
                        if (!elemento.equals("")){
                            if (snapshot.child("type_place").value?.equals(elemento) == true){
                                if (snapshot.child("ubicacion_punto").value.toString()?.equals(lugarUbicacion.toString()) == true){
                                    Log.i("datos2","el nombre es: "+snapshot.child("name").value)
                                    longitud = snapshot.child("lng").value.toString()
                                    latitud = snapshot.child("lat").value.toString()
                                    nombre = snapshot.child("name").value.toString()
                                    rating = snapshot.child("rating").value.toString()
                                    riesgo = snapshot.child("nivel_riesgo").value.toString()
                                    Log.i("datos","LatLng: "+longitud+" : "+latitud)
                                    if (rating.toFloat() > estatus){
                                        agregar_marcadores(googleMap,LatLng(latitud.toFloat().toDouble(), longitud.toFloat().toDouble()),nombre,riesgo,elemento,rating)
                                    }

                                }
                            }
                        }
                    }
                }else{
                    Log.i("datos","algo fallo 1")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.i("datos","algo fallo 2")
            }
        })
    }

    fun agregar_marcadores(googleMap: GoogleMap,latilong:LatLng,nombre:String,riesgo:String,elemento:String,rating:String){

        googleMap.addMarker(
            MarkerOptions()
                .position(latilong)
                .title(nombre)
                .snippet("Riesgo: "+riesgo+"\nRating: "+rating)
                .icon(BitmapDescriptorFactory.fromResource(agregarImagen(elemento)))
                .anchor(0.0f,0.7f)
        )?.showInfoWindow()
    }
    fun agregarImagen(elemento:String): Int {
        val result = when(elemento){
            "museum" -> R.drawable.museum
            "park" -> R.drawable.park
            "store" -> R.drawable.store
            "stadium" -> R.drawable.stadium
            "church" -> R.drawable.church
            "zoo" -> R.drawable.zoo
            "cemetery" -> R.drawable.cemetery
            "bar" -> R.drawable.bar
            "cafe" -> R.drawable.cafe
            else ->  R.drawable.store
        }
        return result
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-0.176714, -78.485399)
        val zoom = 13f
        moverCamaraConZoom(sydney,zoom)
        this.ubucacion_persona?.let { moverCamaraConZoom(it,zoom) }

        for (i in 0..1860) {
            getdatabasefire(googleMap,i)
        }

        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        enableLocation()
        mMap.setInfoWindowAdapter(CustomInfoWindowGoogleMap(this))
        mMap.setOnInfoWindowClickListener(this)
    }

    fun dibujar_mapa(lugarDesctino: LatLng) {
        val originLocation = LatLng(this.ubucacion_persona!!.latitude, this.ubucacion_persona!!.longitude)
        this.ubucacion_persona = lugarDesctino
        mMap.addMarker(MarkerOptions().position(originLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 18F))
        val apiKey = "AIzaSyCqMrzL90TTuJJFaGtpwohq8z1yWL1-Fz0"
        mMap.addMarker(MarkerOptions().position(originLocation))
        val destinationLocation = lugarDesctino
        mMap.addMarker(MarkerOptions().position(destinationLocation))
        val urll = getDirectionURL(originLocation, destinationLocation, apiKey)
        GetDirection(urll).execute()
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(originLocation, 14F))

    }
    override fun onInfoWindowClick(marker: Marker) {

        var lugarDesctino = LatLng(marker.position.latitude, marker.position.longitude)
        dibujar_mapa(lugarDesctino)

        Toast.makeText(
            this, "${marker.title} window clicked ",
            Toast.LENGTH_SHORT
        ).show()
    }


    private fun moverCamaraConZoom(lating: LatLng,zoom:Float=10f){
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(lating,zoom)
        )
    }

    private fun solictitarPermisos(){
        val permisosFineLocation = ContextCompat.checkSelfPermission(
            this.applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val tienePermisos = permisosFineLocation == PackageManager.PERMISSION_GRANTED

        if(tienePermisos){
            Log.i("mapa","Tiene permisos FINE Locaction")
            this.tienePermisos = true
        }else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),1
            )
        }
    }

    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun enableLocation(){
        if(!::mMap.isInitialized) return
        if(isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = true
        }else{
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            REQUEST_CODE_LOCATION -> if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mMap.isMyLocationEnabled = true
            }else{
                Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::mMap.isInitialized) return
        if(!isLocationPermissionGranted()){
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            mMap.isMyLocationEnabled = false
            Toast.makeText(this, "Para activar la localización ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        }
    }
    private fun getDirectionURL(origin:LatLng, dest:LatLng, secret: String) : String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${dest.latitude},${dest.longitude}" +
                "&sensor=false" +
                "&mode=driving" +
                "&key=$secret"
    }
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }
        return poly
    }

    @SuppressLint("StaticFieldLeak")
    private inner class GetDirection(val url : String) : AsyncTask<Void, Void, List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {

            val client = OkHttpClient()
            val request = Request2.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body!!.string()

            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,MapData::class.java)
                val path =  ArrayList<LatLng>()
                for (i in 0 until respObj.routes[0].legs[0].steps.size){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.GREEN)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude},${p0.longitude}", Toast.LENGTH_SHORT).show()
        Log.i("mapa","Estas en ${p0.latitude},${p0.longitude}" )
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "boton pulsado", Toast.LENGTH_SHORT).show()
        Log.i("mapa","Tboton pulsado")
        return false
    }


}

