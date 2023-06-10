package com.singularity.trackmyvehicle.view.customview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.singularity.trackmyvehicle.R


class VehicleSelectionToolbarView : LinearLayout {

    private var txtRightButtonTop: TextView? = null
    private var txtRightButtonBottom: TextView? = null
    private var txtCurrentVehicleId: TextView? = null
    private var containerButtonLeft: LinearLayout? = null
    private var btnChangeVehicle: ImageView? = null
    private var imgBack: ImageView? = null

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
        LayoutInflater.from(context).inflate(R.layout.view_vehicle_selection_toolbar, this, true);

        txtRightButtonTop = findViewById(R.id.txtRightButtonTop)
        txtRightButtonBottom = findViewById(R.id.txtRightButtonBottom)
        txtCurrentVehicleId = findViewById(R.id.txtCurrentVehicleId)
        containerButtonLeft = findViewById(R.id.containerButtonLeft)
        btnChangeVehicle = findViewById(R.id.btnChangeVehicle)
        imgBack = findViewById(R.id.imgBack)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.VehicleSelectionToolbarView, defStyle, 0)

        val vehicleName = attributes.getString(R.styleable.VehicleSelectionToolbarView_vst_vehicle_name)
        val rbTextTop = attributes.getString(R.styleable.VehicleSelectionToolbarView_vst_right_button_text_top)
        val rbTextBottom = attributes.getString(R.styleable.VehicleSelectionToolbarView_vst_right_button_text_bottom)
        val backDrawable = attributes.getDrawable(R.styleable.VehicleSelectionToolbarView_vst_back_drawable)
        val changeDrawable = attributes.getDrawable(R.styleable.VehicleSelectionToolbarView_vst_change_vehicle_drawable)

        txtRightButtonTop?.text = rbTextTop
        txtRightButtonBottom?.text = rbTextBottom
        txtCurrentVehicleId?.text = vehicleName
        btnChangeVehicle?.setImageDrawable(changeDrawable)
        imgBack?.setImageDrawable(backDrawable)

        attributes.recycle()

    }


    fun setRightButtonTopText(value: String) {
        txtRightButtonTop?.text = value
    }

    fun setRightButtonBottomText(value: String) {
        txtRightButtonBottom?.text = value
    }

    fun setCurrentVehicleId(value: String) {
        txtCurrentVehicleId?.text = value
    }

    fun setBtnChangeVehicleDrawable(drawable: Drawable?) {
        btnChangeVehicle?.setImageDrawable(drawable)
    }

    fun setImgBack(drawable: Drawable?) {
        imgBack?.setImageDrawable(drawable)
    }

    fun setChangeVehicleClickListener(callback: View.OnClickListener) {
        btnChangeVehicle?.setOnClickListener(callback)
    }

    fun setImgBackClickListener(callback: View.OnClickListener) {
        imgBack?.setOnClickListener(callback)
    }

    fun disableVehicleSelectWithTitle(title: String) {
        txtCurrentVehicleId?.text =title
        btnChangeVehicle?.visibility = View.GONE
    }


}
