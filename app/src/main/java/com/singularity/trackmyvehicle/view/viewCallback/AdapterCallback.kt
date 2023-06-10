package com.singularity.trackmyvehicle.view.viewCallback

/**
 * Created by Sadman Sarar on 3/12/18.
 */

interface OnItemClickCallback<Model> {
    fun onClick(model: Model){}
    fun onClick(model: Model, position:Int){}
}