/*
 * SPDX-FileCopyrightText: 2016-2025 crDroid Android Project
 * SPDX-FileCopyrightText: 2026 Android Ice Cold Project
 * SPDX-License-Identifier: Apache-2.0
*/
package com.aicp.gear.preference

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.aicp.gear.preference.R
import com.android.settingslib.widget.SliderPreference
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.Slider
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

open class BaseSliderPreference : SliderPreference {

    companion object {
        private const val SETTINGS_NS = "http://schemas.android.com/apk/res/com.android.settings"
        private const val ANDROIDNS = "http://schemas.android.com/apk/res/android"

        private fun gcd(a0: Int, b0: Int): Int {
            var a = abs(a0)
            var b = abs(b0)
            if (a == 0) return b
            if (b == 0) return a
            while (b != 0) {
                val t = b
                b = a % b
                a = t
            }
            return a
        }

        private fun dp(v: TextView, dp: Int): Int {
            return (dp * v.resources.displayMetrics.density).roundToInt()
        }
    }

    private var mShowSign = false
    private var mUnits: String? = ""
    private var mDefaultValueText: String? = null
    private var mDefaultValueTextExists = false
    private var mDefaultValueExists = false
    private var mDefaultValue = 0

    private var mUserSummary: CharSequence? = null
    private var mInUserDrag = false

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        readLegacyAttrs(context, attrs)
        initDefaults()
        mUserSummary = super.getSummary()
        updateSummaryNow()
    }

    constructor(context: Context) : super(context, null) {
        initDefaults()
        mUserSummary = super.getSummary()
        updateSummaryNow()
    }

private fun initDefaults() {
    setShowSliderValue(true)
    setHapticFeedbackMode(HAPTIC_FEEDBACK_MODE_ON_TICKS)

    setLabelFormater(LabelFormatter { value ->
        formatValueForSummary(value.toInt())
    })
}



    private fun readLegacyAttrs(c: Context, attrs: AttributeSet?) {
        if (attrs == null) return

        val a: TypedArray = c.obtainStyledAttributes(attrs, R.styleable.SliderPreference)
        try {

            mShowSign = a.getBoolean(R.styleable.SliderPreference_showSign, false)

            val units = a.getString(R.styleable.SliderPreference_units)
            if (units != null) mUnits = units

            val continuous = a.getBoolean(
                R.styleable.SliderPreference_continuousUpdates,
                false
            )
            setUpdatesContinuously(continuous)

            mDefaultValueText = a.getString(
                R.styleable.SliderPreference_defaultValueText
            )
            mDefaultValueTextExists =
                !mDefaultValueText.isNullOrEmpty()

            var defaultValue = attrs.getAttributeValue(ANDROIDNS, "defaultValue")
            if (defaultValue == null) {
                defaultValue = attrs.getAttributeValue(SETTINGS_NS, "defaultValue")
            }

            if (!defaultValue.isNullOrEmpty()) {
                try {
                    mDefaultValue = defaultValue.toInt()
                    mDefaultValueExists = true
                } catch (_: NumberFormatException) {
                    mDefaultValueExists = false
                }
            }

            var interval = attrs.getAttributeIntValue(SETTINGS_NS, "interval", 0)
            if (interval == 0) {
                interval = attrs.getAttributeIntValue(ANDROIDNS, "interval", 0)
            }
            if (interval > 0) setSliderIncrement(interval)

            val min = min
            val max = max
            val span = max(0, max - min)

            val step = sliderIncrement
            if (step <= 0 || span == 0) {
                setSliderIncrement(1)
            } else if (span % step != 0) {
                var g = gcd(span, step)
                if (g <= 0) g = 1
                setSliderIncrement(g)
            }

        } catch (_: Throwable) {
        } finally {
            a.recycle()
        }
    }

    override fun setSummary(summary: CharSequence?) {
        mUserSummary = summary
        updateSummaryNow()
    }

    override fun setValue(sliderValue: Int) {
        super.setValue(sliderValue)
        if (!mInUserDrag) updateSummaryNow()
    }

    private fun updateSummaryNow() {
        val composed = composeSummary(mUserSummary, value)
        super.setSummary(composed)
    }

    private fun formatValueForSummary(v: Int): String {
        if (mDefaultValueExists && mDefaultValueTextExists && v == mDefaultValue) {
            return mDefaultValueText!!
        }

        var s = v.toString()
        if (mShowSign && v > 0) s = "+$s"
        if (!mUnits.isNullOrEmpty()) s = "$s $mUnits"
        return s
    }

    private fun composeSummary(userSummary: CharSequence?, v: Int): CharSequence {
        val valueText = formatValueForSummary(v)
        if (userSummary.isNullOrEmpty()) return valueText
        return "$valueText \u2022 $userSummary"
    }

    override fun setDefaultValue(defaultValue: Any?) {
        if (defaultValue is Int) {
            mDefaultValueExists = true
            mDefaultValue = defaultValue
        }
        super.setDefaultValue(defaultValue)
        updateSummaryNow()
    }

    override fun onBindViewHolder(@NonNull holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val summaryView = holder.findViewById(android.R.id.summary) as TextView?
        summaryView?.text = composeSummary(mUserSummary, value)

        val labelFrame =
            holder.findViewById(com.android.settingslib.widget.preference.slider.R.id.label_frame)

        val startText = holder.findViewById(android.R.id.text1) as TextView?
        val endText = holder.findViewById(android.R.id.text2) as TextView?

        labelFrame?.let {
            val hasStart = startText?.text?.isNotEmpty() == true
            val hasEnd = endText?.text?.isNotEmpty() == true
            val parentWantsLabels = hasStart || hasEnd

            it.visibility =
                if (parentWantsLabels || mDefaultValueExists) View.VISIBLE else View.GONE
        }

        endText?.let { attachResetIcon(it) }

        val minusFrame =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_start_frame
            ) as ViewGroup?

        val minusIcon =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_start
            ) as ImageView?

        val plusFrame =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_end_frame
            ) as ViewGroup?

        val plusIcon =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_end
            ) as ImageView?

        val slider =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.slider
            ) as Slider?

        val stepForClicks = max(1, sliderIncrement)

        minusFrame?.let { frame ->
            minusIcon?.let { icon ->
                frame.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_slider_minus)
                frame.setOnClickListener {
                    if (!isEnabled) return@setOnClickListener
                    val base = slider?.value?.roundToInt() ?: value
                    val newVal = max(min, base - stepForClicks)
                    applyUserValue(newVal, slider)
                    updatePlusMinusEnabledStates(holder)
                }
            }
        }

        plusFrame?.let { frame ->
            plusIcon?.let { icon ->
                frame.visibility = View.VISIBLE
                icon.setImageResource(R.drawable.ic_slider_plus)
                frame.setOnClickListener {
                    if (!isEnabled) return@setOnClickListener
                    val base = slider?.value?.roundToInt() ?: value
                    val newVal = kotlin.math.min(max, base + stepForClicks)
                    applyUserValue(newVal, slider)
                    updatePlusMinusEnabledStates(holder)
                }
            }
        }

        updatePlusMinusEnabledStates(holder)

        if (slider != null && summaryView != null) {

            slider.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    summaryView.text =
                        composeSummary(mUserSummary, value.toInt())
                    updatePlusMinusEnabledStates(holder)
                }
            }

            slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {

                override fun onStartTrackingTouch(slider: Slider) {
                    mInUserDrag = true
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    mInUserDrag = false
                    applyUserValue(slider.value.roundToInt(), slider)
                    updatePlusMinusEnabledStates(holder)
                }
            })
        }
    }

    override fun onDependencyChanged(
        dependency: Preference,
        disableDependent: Boolean
    ) {
        super.onDependencyChanged(dependency, disableDependent)
        notifyChanged()
    }

    private fun applyUserValue(newVal: Int, slider: Slider?) {

        if (newVal == value) return

        if (!callChangeListener(newVal)) {
            slider?.value = value.toFloat()
            return
        }

        setValue(newVal)
        updateSummaryNow()
        notifyChanged()
    }

    private fun updatePlusMinusEnabledStates(holder: PreferenceViewHolder) {

        val minusFrame =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_start_frame
            )

        val minusIcon =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_start
            ) as ImageView?

        val plusFrame =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_end_frame
            )

        val plusIcon =
            holder.findViewById(
                com.android.settingslib.widget.preference.slider.R.id.icon_end
            ) as ImageView?

        val enabled = isEnabled
        val value = value

        minusFrame?.let {
            minusIcon?.let { icon ->
                val min = min
                val state = enabled && value > min
                it.isEnabled = state
                icon.isEnabled = state
            }
        }

        plusFrame?.let {
            plusIcon?.let { icon ->
                val max = max
                val state = enabled && value < max
                it.isEnabled = state
                icon.isEnabled = state
            }
        }
    }

    private fun attachResetIcon(tv: TextView) {

        if (!mDefaultValueExists) {
            tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null)
            tv.setOnTouchListener(null)
            tv.isClickable = false
            return
        }

        val icon: Drawable? = ResourcesCompat.getDrawable(
            tv.resources,
            R.drawable.ic_slider_reset,
            tv.context.theme
        ) ?: return

        tv.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null)
        tv.compoundDrawablePadding = dp(tv, 6)
        tv.isClickable = isEnabled
        tv.isFocusable = isEnabled

        val tapSlop = dp(tv, 8)

        tv.setOnTouchListener { _, ev ->

            if (!isEnabled || ev.action != MotionEvent.ACTION_UP) return@setOnTouchListener false

            val isRtl =
                ViewCompat.getLayoutDirection(tv) == ViewCompat.LAYOUT_DIRECTION_RTL

            val end = tv.compoundDrawablesRelative[2] ?: return@setOnTouchListener false

            val iconW = end.intrinsicWidth
            val x = ev.x.toInt()

            if (!isRtl) {

                val left =
                    tv.width - ViewCompat.getPaddingEnd(tv) - iconW - tapSlop

                if (x >= left) {
                    performReset()
                    return@setOnTouchListener true
                }

            } else {

                val right =
                    ViewCompat.getPaddingStart(tv) + iconW + tapSlop

                if (x <= right) {
                    performReset()
                    return@setOnTouchListener true
                }
            }

            false
        }
    }

    private fun performReset() {
        if (mDefaultValueExists) {
            applyUserValue(mDefaultValue, null)
        }
    }
}
