package com.example.clockapp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment

/**
 * Resource reference used for this TimePicker Class and Layout XML file
 * https://stackoverflow.com/questions/7847623/how-to-pick-a-second-using-timepicker-android#:~:text=The%20best%20way%20around%20this,sources%20which%20may%20contain%20viruses.
 */

/**
 * Inherits from Dialog Fragment
 * to show a dialog to select time
 */
class TimePicker : DialogFragment(){

    private lateinit var timePickerLayout: View
    private lateinit var hourPicker: NumberPicker
    private lateinit var minPicker: NumberPicker
    private lateinit var secPicker: NumberPicker

    private var timeSetText: String = "OK"
    private var cancelText: String = "Cancel"

    // Use of Unit type here for singleton objects
    // and as a placeholder for a function to carry data

    private var onTimeSetOption : (hour: Int, minute: Int, second: Int) -> Unit = {_, _, _ ->}
    private var onCancelOptions: () -> Unit = {}

    /**
     * Initial value for hour picker
     */
    private var initialHour: Int = 0
    /**
     * Initial value for minute picker
     */
    private var initialMinute: Int = 0
    /**
     * Initial value for second picker
     */
    private var initialSeconds: Int = 0

    /**
     * Max value for hour picker
     */
    private var maxValueHour: Int = 99
    /**
     * Max value for minute picker
     */
    private var maxValueMinute: Int = 59
    /**
     * Max value for second picker
     */
    private var maxValueSeconds: Int = 59

    /**
     * Min value for hour picker
     */
    private var minValueHour: Int = 0
    /**
     * Min value for minute picker
     */
    private var minValueMinute: Int = 0
    /**
     * Min value for second picker
     */
    private var minValueSeconds: Int = 0


    private var title: String? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let{
            //Use the builder class for convenient dialog
            val builder = AlertDialog.Builder(it)

            //Connect the class to xml layout
            timePickerLayout = requireActivity()
                .layoutInflater.inflate(R.layout.time_picker, null)

            setupTimePickerLayout()
            builder.setView(timePickerLayout)

            title?.let {title -> builder.setTitle(title)}
            builder.setPositiveButton(timeSetText){ _, _ ->
                onTimeSetOption(hourPicker.value, minPicker.value, secPicker.value)
            }
                .setNegativeButton(cancelText) { _, _ ->
                    onCancelOptions
                }
            //Create the AlertDialog object and return it
            builder.create()
        }   ?: throw IllegalStateException("Activity cannot be null")
    }

    /**
     * Set the title displayed in the Dialog
     */
    public fun setTitle(title: String){
        this.title = title
    }

    /**
     * Set a listener to be invoked when the Set Time button of the dialog is pressed.
     */
    public fun setOnTimeSetOption(text: String, onTimeSet: (hour: Int, minute: Int, second: Int) -> Unit){
        onTimeSetOption = onTimeSet
        timeSetText = text
    }
    /**
     * Set a listener to be invoked when the Cancel button of the dialog is pressed.
     */
    public fun setOnCancelOption(text: String, onCancelOption: () -> Unit){
        this.onCancelOptions = onCancelOption
            cancelText = text
    }

    private fun setupTimePickerLayout() {
        bindViews()

        setupMaxValues()
        setupMinValues()
        setupInitialValues()
    }

    private fun bindViews(){
        hourPicker = timePickerLayout.findViewById(R.id.hours)
        minPicker = timePickerLayout.findViewById(R.id.minutes)
        secPicker = timePickerLayout.findViewById(R.id.seconds)
    }

    private fun setupMaxValues(){
        hourPicker.maxValue = maxValueHour
        minPicker.maxValue = maxValueMinute
        secPicker.maxValue = maxValueSeconds
    }
    private fun setupMinValues(){
        hourPicker.minValue = minValueHour
        minPicker.minValue = minValueMinute
        secPicker.minValue = minValueSeconds
    }
    private fun setupInitialValues(){
        hourPicker.value = initialHour
        minPicker.value = initialMinute
        secPicker.value = initialSeconds
    }

}