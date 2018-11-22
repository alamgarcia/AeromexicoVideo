

package com.example.garcia76.aeromexicovideo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import kotlinx.android.synthetic.main.fragment_contacto_amv.view.*

class mytrips : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_mytrips, container, false)

        return view
    }

    companion object {
        fun newInstance(): mytrips = mytrips()
    }
}