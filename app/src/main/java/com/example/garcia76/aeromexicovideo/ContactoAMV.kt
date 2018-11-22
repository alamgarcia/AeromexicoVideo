package com.example.garcia76.aeromexicovideo
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import kotlinx.android.synthetic.main.fragment_contacto_amv.view.*
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_contacto_amv.*
import org.jetbrains.anko.support.v4.indeterminateProgressDialog
import org.jetbrains.anko.support.v4.progressDialog

class ContactoAMV : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        useInsecureSSL()
        val view: View = inflater!!.inflate(R.layout.fragment_contacto_amv, container, false)
        view.initcall_btn.setOnClickListener { view ->
            "https://amv.collaboratory.avaya.com:443/avayatest/auth?displayName=${apellidos_txt.text}&userName=0000".httpGet().responseString { request, response, result ->
                //do something with response
                when (result) {
                    is Result.Failure -> {
                        val ex = result.getException()
                        Log.d("HttpR", ex.toString())
                    }
                    is Result.Success -> {
                        Log.d("intent", reservacion_txt.text.toString())

                        val data = result.get()
                        Log.d("HttpR", data)
                        var gson = Gson()
                        var Login = gson?.fromJson(data, DataToken.Response::class.java)
                        Log.d("HttpR",Login.sessionid)
                        Log.d("HttpR",Login.uuid)
                        val myIntent = Intent(activity, AMVCall::class.java)
                        myIntent.putExtra("token", data)
                        myIntent.putExtra("reserva", reservacion_txt.text.toString())
                        startActivity(myIntent)



                    }
                }
            }




        }
        return view
    }

    companion object {
        fun newInstance(): ContactoAMV = ContactoAMV()
    }
}



fun useInsecureSSL() {
    // Create a trust manager that does not validate certificate chains
    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate>? = null
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) = Unit
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) = Unit
    })

    val sc = SSLContext.getInstance("SSL")
    sc.init(null, trustAllCerts, java.security.SecureRandom())
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.socketFactory)

    // Create all-trusting host name verifier
    val allHostsValid = HostnameVerifier { _, _ -> true }

    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid)
}

