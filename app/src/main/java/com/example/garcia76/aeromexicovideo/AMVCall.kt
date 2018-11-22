package com.example.garcia76.aeromexicovideo

import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.media.AudioManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Chronometer
import android.widget.RelativeLayout
import android.widget.Toast
import com.avaya.clientplatform.api.*
import com.avaya.clientplatform.impl.DeviceImpl
import com.avaya.clientplatform.impl.SessionImpl
import com.avaya.clientplatform.impl.UserImpl
import com.avaya.clientplatform.impl.VideoSurfaceImpl
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Method

import kotlinx.android.synthetic.main.activity_amvcall.*
import com.avaya.vivaldi.internal.e
import com.avaya.clientplatform.api.SessionState



class AMVCall : Activity(), UserListener2, SessionListener2 {


    //Asignamos las variables globales
    var mPlatform: ClientPlatform? = null
    var mUser: UserImpl? = null
    var mDevice: DeviceImpl? = null
    var mSession: SessionImpl? = null
    var mRemoteVideoSurface: VideoSurface? = null
    var mPreviewView: VideoSurface? = null
    //Override Certificados (Fix Android 5.0 del TrustManager y NullHost)

    //Creacion Aplicacion
    override fun onCreate(savedInstanceState: Bundle?) {
        val chronometer = findViewById<Chronometer>(R.id.chronometer)
        val intent = intent
        val tokendata = intent.getStringExtra("token")
        val myPreferences = "myPrefs"
        val sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("token", tokendata)
        editor.apply()
        super.onCreate(savedInstanceState)
        //Layout
        setContentView(R.layout.activity_amvcall)
        //Opcion para evitar que la pantalla s eapague durante la llamada
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //Escondemos los botones
        //Iniciamos audioManager de Android
        volumeControlStream = AudioManager.STREAM_VOICE_CALL
    }



    private fun setVisibility(button: View, visibility: Int) {
        when (visibility) {
            View.VISIBLE, View.INVISIBLE -> button.visibility = visibility
        }
    }
    //FIN
    //Como cambiamos de Actividad, al hacer focus en esta vista vamos a crear el vídeo

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        crearvideo()
    }
    //Creamos Video

    fun crearvideo() {
        try {
            when (mRemoteVideoSurface) {
                null -> {
                    chronometer.start()

                    //Mostramos el video si nuestra superficie de video es nula
                    //Asignamos el Cliente a un objeto
                    var clientPlatform = ClientPlatformManager.getClientPlatform(this)
                    //Definimos las superficies de render de video
                    var rlRemote = findViewById<View>(R.id.videoremote) as RelativeLayout
                    var rlLocal = findViewById<View>(R.id.videolocal) as RelativeLayout
                    //Asignamos el objeto dispositivo
                    var mDevice = clientPlatform!!.device as DeviceImpl?
                    //Obtenemos el tamaño de las superficioes
                    val remoteSize = Point(rlRemote.width, rlRemote.height)
                    val localSize = Point(rlLocal.width, rlLocal.height)
                    //Creamos los objetos con el metodo VideoSurfacImpl.
                    var mRemoteVideoSurface = VideoSurfaceImpl(this, remoteSize, null)
                    var mPreviewView = VideoSurfaceImpl(this, localSize, null)
                    //Agregamos las vistas a nuestros Layouts


                    (mPreviewView).layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    rlLocal.addView(mPreviewView)
                    (mRemoteVideoSurface).layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
                    rlRemote.addView(mRemoteVideoSurface)
                    //Le indicamos al SDK a que objeot debe mandar el vídeo

                    mDevice!!.remoteVideoView = mRemoteVideoSurface
                    mDevice!!.localVideoView = mPreviewView
                }
            }
            //Excepecion
        } catch (e: Exception) {
            Log.d("SDK", "Error al crear video")
        }

    }

    private fun llamada() {
        try {
            //Abrimos el Preference Manager
            var myPreferences = "myPrefs"
            var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
            //Obtenemos el numero del PreferenceManager
            var numero = sharedPreferences.getString("numero", "2681322102")
            //Obtenemos los extras de la actividad anterior para la localizacion
            val intent = intent
            val reservatoken = intent.getStringExtra("reserva")
            //Imprimimos las ultimas coordenadas obtenidas
            //Creamos un objeto con la Interface device del SDK
            var mDevice = mPlatform!!.device as DeviceImpl?
            //Creamos un objeto con la Interface ususario del SDK para crear la seison
            var mSession = mUser!!.createSession() as SessionImpl
            //Inicializamos los Listeners de la sesión
            mSession.registerListener(this)
            //Preferencias de la llamada
            mSession.enableAudio(true)
            mSession.enableVideo(true)
            mSession.muteAudio(false)
            mSession.muteVideo(false)
            //Asignamos el UserToUser to Info
            mSession.contextId = "$reservatoken"
            //Asiganmos el numero
            mSession.remoteAddress = numero
            //Iniciamos la llamada
            mSession.start()
            runOnUiThread {
                callnumber_lbl.text = numero
            }

            //Listener para cuando la llamada ha iniciado
            hangup_btn.setOnClickListener {
                mDevice!!.localVideoView = null
                mDevice!!.remoteVideoView = null
                mSession!!.unregisterListener(this)
                mUser!!.unregisterListener(this)
                mSession!!.end()
                finish()
            }
            //Funcion de Switch Video
            dropvideo_btn.setOnClickListener {
                try {
                    var camaras = mDevice!!.selectedCamera
                    when (camaras.toString()) {
                        "FRONT" -> mDevice!!.selectCamera(CameraType.BACK)
                        "BACK" -> mDevice!!.selectCamera(CameraType.FRONT)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al cambiar de camara")
                }
            }
            // Fin boton
            //Boton Mute Audio
            mute_btn.setOnClickListener {
                try {
                    var estadoaudio = mSession!!.isAudioMuted
                    when (estadoaudio) {
                        true -> mSession!!.muteAudio(false)
                        false -> mSession!!.muteAudio(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al hacer mute de audio")
                }
            }
            //Fin Mute
            //Boton DropVideo
            dropvideo_btn.setOnClickListener {
                try {
                    var estadovideo = mSession!!.isVideoEnabled
                    when (estadovideo) {
                        true -> mSession!!.enableVideo(false)
                        false -> mSession!!.enableVideo(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al hacer dropvideo")
                }
            }
            //Fin Drop
            //Boton DropVideo
            dropvideo_btn.setOnClickListener {
                try {
                    var videomute = mSession!!.isVideoMuted
                    when (videomute) {
                        true -> mSession!!.muteVideo(false)
                        false -> mSession!!.muteVideo(true)
                    }
                } catch (e: Exception) {
                    Log.d("SDK", "Fallo al mute video")
                }
            }
            //Fin Drop
            //Catch del error de la llamada
        } catch (e: Exception) {
            toast("Error en la llamada$e")
            Log.d("SDK", "error$e")
            finish()
        }
    }

    //Eliminar Token
    fun eliminartoken() {
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        var host = sharedPreferences.getString("host", "amv.collaboratory.avaya.com")
        var puerto = sharedPreferences.getString("puerto", "443")
        var sessionid = sharedPreferences.getString("sessionid", "")
        val editor = sharedPreferences.edit()
        editor.putString("token", "")
        editor.apply()
        val manager: FuelManager by lazy { FuelManager() }
        //Usamos el metodo request de FUUEL Manager, junto a la lusta de parametros
        manager.request(Method.DELETE, "https://$host:$puerto/avayatest/auth/id/$sessionid").responseString { req, res, result ->
            val (data, error) = result
            when (error) {
                null -> {
                    toast("Se ha finalizado la llamada")
                    finish()
                }
            }
        }
    }

    //Funcion de Colgar
    fun colgar() {
        Log.d("SDK", "Colgar")
        try {
            when {
            //Si tenemos un objeto en la sesion, limpiamos variables y detenemos listeners
                mSession != null -> {
                    mDevice!!.localVideoView = null
                    mDevice!!.remoteVideoView = null
                    mSession!!.unregisterListener(this)
                    mUser!!.unregisterListener(this)
                    mSession!!.end()

                }
            }
            //Matamos Procesos
            finish()
            //Catch del Try
        } catch (e: Exception) {
            Log.d("SDK", "Error al colgar$e")
            toast("Error al Colgar$e")
        }
    }

    //Snippet para llamar al toast mas rápido
    fun Activity.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
        runOnUiThread { Toast.makeText(this, message, duration).show() }
    }

    //Clases para el código
    class Login {
        data class Response(
                val sessionid: String,
                val uuid: String,
                val defaultDomain: String
        )
    }

    //Listeners

    override fun onSessionRemoteAlerting(session: Session, hasEarlyMedia: Boolean) {
        Log.d("SDK", "Timbrando")
    }
    override fun onSessionRemoteAddressChanged(p0: Session?, p1: String?, p2: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionEnded(session: Session) {
        toast("Sesion finalizada")
        colgar()
        finish()

    }

    override fun onSessionVideoMuteFailed(p0: Session?, p1: Boolean, p2: SessionException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionFailed(p0: Session?, p1: SessionError?) {

        colgar()
        finish()

    }

    override fun onSessionQueued(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDialError(p0: Session?, p1: SessionError?, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onGetMediaError(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionRedirected(session: Session) {
        Log.d("SDK", "Sesion Redirigida")
    }

    override fun onSessionServiceAvailable(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionServiceUnavailable(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onQualityChanged(session: Session, i: Int) {
        Log.d("SDK", "La Calidad ha cambiado$i")
        try {
            runOnUiThread {
               /* val progress = findViewById<View>(R.id.call_quality_bar) as ProgressBar
                if (i in 0..100) {
                    progress.progress = i
                    val mTextView = findViewById<View>(R.id.textView7) as TextView
                    mTextView.text = i.toString() + "%"
                }*/
            }

        } catch (e: Exception) {
            Log.d("SDK", "Error Calidad$e")
        }
    }

    override fun onSessionEstablished(session: Session) {
        Log.d("SDK", "Sesion Establecida")


    }

    override fun onSessionRemoteDisplayNameChanged(p0: Session?, p1: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionAudioMuteFailed(p0: Session?, p1: Boolean, p2: SessionException?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionVideoMuteStatusChanged(session: Session, muted: Boolean) {
        Log.d("SDK", "Video Mute Off")
    }

    override fun onSessionVideoRemovedRemotely(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCallError(p0: Session?, p1: SessionError?, p2: String?, p3: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCapacityReached(p0: Session?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSessionAudioMuteStatusChanged(session: Session, muted: Boolean) {
        Log.d("SDK", "Audio Mute")

    }

    override fun onConnReestablished(user: User) {
        toast("Reconectado")
    }

    override fun onServiceAvailable(user: User) {
        Log.d("SDK", "Servicio Disponible")

    }

    override fun onConnRetry(user: User) {
        toast("Reintentando conectar")
    }
    override fun onConnectionInProgress(arg0: User) {
        Log.d("SDK", "Conexion en Progreso")
    }
    override fun onConnLost(user: User) {
        toast("Se ha perdido conexion con el servidor, intente remarcar")
        colgar()
    }

    override fun onServiceUnavailable(user: User) {
        toast("Servicio No disponible")
        colgar()
        finish()
    }


    override fun onNetworkError(user: User) {
        toast("Eror en la red")
        colgar()
    }

    override fun onCriticalError(p0: User?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    override fun onDestroy() {
        super.onDestroy()
        mPlatform = null
        mUser = null
        if (mDevice != null) {
            mDevice!!.localVideoView = null
            mDevice = null
        }

        mRemoteVideoSurface = null
        mPreviewView = null
    }


    override fun onBackPressed() {
        super.onBackPressed()


    }


    override fun onResume() {

        super.onResume()
        //Abrimos el Preference Manager
        var myPreferences = "myPrefs"
        var sharedPreferences = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)
        //Obtenemos el Token
        var token = sharedPreferences.getString("token", "")
        try {
            //Creamos objetos con las interfaces
            mPlatform = ClientPlatformManager.getClientPlatform(this.applicationContext)
            //Creamos Objeto con la interface Ususario
            mUser = mPlatform!!.user as UserImpl?
            Log.d("SDK", token)
            //Asignamos el Token al Usuario para crear la sesion
            val tokenAccepted = mUser!!.setSessionAuthorizationToken(token)
            when {
            //Si el token es acpetado
                tokenAccepted -> {
                    //registramos el Listener
                    mUser!!.registerListener(this)
                    //El SDK aceptará cualquier certificado
                    mUser!!.acceptAnyCertificate(true)
                    // asignamos al objeto mPlataform la interfaz device
                    mPlatform!!.device as DeviceImpl
                    hangup_btn.setOnClickListener {
                        mDevice!!.localVideoView = null
                        mDevice!!.remoteVideoView = null
                        mSession!!.unregisterListener(this)
                        mUser!!.unregisterListener(this)
                        mSession!!.end()
                        finish()
                    }

                    Log.d("SDK", mPlatform!!.getDevice().toString())

                    when (mSession) {
                        null -> //Si no tenemos session podemos llamar
                            when {
                                mUser!!.isServiceAvailable -> {
                                    Log.d("SDK", "Llamar")
                                    llamada()
                                }
                                else -> {
                                    Log.d("SDK", "Servicio No disponible")
                                    colgar()
                                }
                            }
                        else -> Log.d("SDK", "no se puede llamar")
                    }
                }
                else -> Log.d("SDK", "Token Invalida")
            }
        } catch (e: Exception) {
            Log.d("SDK", "Error al resumir $e")
        }
    }

}
