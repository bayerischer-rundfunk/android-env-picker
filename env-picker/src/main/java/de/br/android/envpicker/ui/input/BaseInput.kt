package de.br.android.envpicker.ui.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

abstract class BaseInput<T : Any> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var label = TextView(context)

    fun init(labelStr: String, defaultValue: T) {
        orientation = HORIZONTAL

        addView(label)
        label.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2f
        )
        label.text = labelStr

        val input = createInput(defaultValue,
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )
        addView(input)
    }

    abstract val value: T

    protected abstract fun createInput(defaultValue: T, suggestedLayoutParams: LayoutParams): View
}