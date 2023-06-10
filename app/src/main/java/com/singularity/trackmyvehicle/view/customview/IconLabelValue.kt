package com.singularity.trackmyvehicle.view.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.singularity.trackmyvehicle.R


class IconLabelValue : LinearLayout {

    private var mTxtLabel: TextView? = null
    private var mTxtValue: TextView? = null
    private var mImgIcon: ImageView? = null

    val value : TextView?
        get() = mTxtValue
    val label : TextView?
        get() = mTxtLabel

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
        LayoutInflater.from(context).inflate(R.layout.view_icon_label_value, this, true);

        mTxtLabel = findViewById(R.id.label)
        mTxtValue = findViewById(R.id.value)
        mImgIcon = findViewById(R.id.imgIcon)

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.IconLabelValue, defStyle, 0)

        val label = attributes.getString(R.styleable.IconLabelValue_ilv_label)
        val value = attributes.getString(R.styleable.IconLabelValue_ilv_value)
        val icon = attributes.getDrawable(R.styleable.IconLabelValue_ilv_icon)

        mTxtLabel?.text = label
        mTxtValue?.text = value
        mImgIcon?.setImageDrawable(icon)

        attributes.recycle()

    }

}
