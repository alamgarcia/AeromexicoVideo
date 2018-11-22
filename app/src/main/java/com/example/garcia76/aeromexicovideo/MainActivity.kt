package com.example.garcia76.aeromexicovideo

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.avaya.clientplatform.api.ClientPlatform
import com.avaya.clientplatform.api.ClientPlatformFactory
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                val homeamv_fg = HomeFragment.newInstance()
                openFragment(homeamv_fg)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                val mytrips_fg = mytrips.newInstance()
                openFragment(mytrips_fg)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                val reservar_fg = ReservarFragment.newInstance()
                openFragment(reservar_fg)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_contact -> {
                val contactoamv_fg = ContactoAMV.newInstance()
                openFragment(contactoamv_fg)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                val settingsamv_fg = Ajustes_amv.newInstance()
                openFragment(settingsamv_fg)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {/* ... */
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {/* ... */
                    }
                }).check()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val homeamv_fg = HomeFragment.newInstance()
        openFragment(homeamv_fg)
    }
    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
object ClientPlatformManager {

    var sClientPlatform: ClientPlatform? = null

    @Synchronized
    fun getClientPlatform(context: Context): ClientPlatform? {

        if (sClientPlatform != null) {
            return sClientPlatform
        }

        sClientPlatform = ClientPlatformFactory.getClientPlatformInterface(context)

        return sClientPlatform
    }

}