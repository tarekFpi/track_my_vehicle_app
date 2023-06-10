package com.singularity.trackmyvehicle.view.map

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.singularity.trackmyvehicle.R

class AppInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?): View? {
        return null
    }

    override fun getInfoWindow(p0: Marker?): View? {
        val data = p0?.snippet
        if (data.isNullOrEmpty()) {
            return null
        }
        val view = LayoutInflater.from(context).inflate(R.layout.layout_info_indow, null, false)

        val stringBuilder = StringBuilder()


        data?.replace(";", "\n")
                ?.split("\n")?.forEach {
                    val split = it.split(":")
                    stringBuilder.append("<b>")
                    stringBuilder.append(split.firstOrNull())
                    stringBuilder.append("</b>: ")
                    stringBuilder.append(it.substringAfter(":"))
                    if (!it.toLowerCase().contains("speed")) {
                        stringBuilder.append("<br>")
                    }
                }


        view.findViewById<TextView>(R.id.txtView)
                .text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(stringBuilder.toString(), Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(stringBuilder.toString())
        }

        return view
    }
}