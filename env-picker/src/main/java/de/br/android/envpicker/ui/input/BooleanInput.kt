package de.br.android.envpicker.ui.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.SwitchCompat

class BooleanInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseInput<Boolean>(context, attrs, defStyleAttr) {
    private val input = SwitchCompat(context)

    override fun createInput(defaultValue: Boolean, suggestedLayoutParams: LayoutParams): View {
        input.isChecked = defaultValue
        input.layoutParams = suggestedLayoutParams

        return input
    }

    override val value get() = input.isChecked
}