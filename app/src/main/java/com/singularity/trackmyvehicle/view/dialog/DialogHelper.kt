package com.singularity.trackmyvehicle.view.dialog

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.DatePicker
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.singularity.trackmyvehicle.R


/**
 * Created by Sadman Sarar on 3/11/18.
 */
class DialogHelper {

    companion object {

        fun getLoadingDailog(context: Context, title: String, message: String): MaterialDialog.Builder? {
            val dialog = dialogBuilder(context)
                    .title(title)
                    .content(message)
                    .progress(true, 0)

            return dialog
        }

        fun getMessageDialog(context: Context, title: String, message: String): MaterialDialog.Builder? {
            return dialogBuilder(context)
                    .title(title)
                    .content(message)

        }

        fun dialogBuilder(context: Context): MaterialDialog.Builder {
            return MaterialDialog.Builder(context)
                    .titleColorRes(R.color.colorPrimary)
                    .contentColorRes(R.color.colorPrimary)
                    .backgroundColor(Color.WHITE)
                    .positiveColorRes(R.color.colorPrimary)
                    .neutralColorRes(R.color.colorPrimary)
                    .negativeColorRes(R.color.colorPrimary)
                    .widgetColorRes(R.color.colorPrimary)
                    .buttonRippleColorRes(R.color.colorPrimary)
                    .cancelable(false)
        }

        fun createDialogWithoutDateField(context: Context): DatePickerDialog {
            val dpd = DatePickerDialog(context, null, 2014, 1, 24)
            try {
                val datePickerDialogFields = dpd.javaClass.declaredFields
                for (datePickerDialogField in datePickerDialogFields) {
                    if (datePickerDialogField.name == "mDatePicker") {
                        datePickerDialogField.isAccessible = true
                        val datePicker = datePickerDialogField.get(dpd) as DatePicker
                        val datePickerFields = datePickerDialogField.type.declaredFields
                        for (datePickerField in datePickerFields) {
                            Log.i("test", datePickerField.name)
                            if ("mDaySpinner" == datePickerField.name) {
                                datePickerField.isAccessible = true
                                val dayPicker = datePickerField.get(datePicker)
                                (dayPicker as View).visibility = View.GONE
                            }
                        }
                    }
                }
            } catch (ex: Exception) {
                FirebaseCrashlytics.getInstance().recordException(ex)
            }

            return dpd
        }


        fun showInputPasswordDialog(context: Context,
                                    callback: (password: String) -> Unit,
                                    onCancel: () -> Unit): MaterialDialog.Builder? {
            return MaterialDialog.Builder(context)
                    .title("Input password")
                    .input("******", "", false, MaterialDialog.InputCallback { dialog, input ->
                        callback(input.toString())
                    })
                    .inputType(InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    .positiveText("OK")
                    .negativeText("Cancel")
                    .onNegative { dialog, which ->
                        dialog.dismiss()
                        onCancel()
                    }
        }
    }

}