package com.example.garcia76.aeromexicovideo

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class Ajustes_amv : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater!!.inflate(R.layout.activity_ajustes_amv, container, false)
        return view

    }

    companion object {
        fun newInstance(): Ajustes_amv = Ajustes_amv()
    }





}


