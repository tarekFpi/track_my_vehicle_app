package com.singularity.trackmyvehicle.utils.cluster

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Created by Kariba Yasmin on 9/28/21.
 */
class MyClusterItem : ClusterItem {

    var mPosition: LatLng? = LatLng(0.0, 0.0)
    var mTitle: String? = ""
    var mSnippet: String? = ""
    var index : Int = 0

    constructor(mPosition: LatLng?, mTitle: String?, mSnippet: String?, index : Int){
        this.mPosition = mPosition
        this.mTitle = mTitle
        this.mSnippet = mSnippet
        this.index = index
    }

    override fun getPosition(): LatLng {
        return mPosition ?: LatLng(0.0, 0.0)
    }

    override fun getTitle(): String? {
        return mTitle
    }

    override fun getSnippet(): String? {
        return mSnippet
    }
}