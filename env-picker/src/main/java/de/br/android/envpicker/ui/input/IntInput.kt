package de.br.android.envpicker.ui.input

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

class IntInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseInput<Int>(context, attrs, defStyleAttr) {
    private val input = AppCompatEditText(context)

    override fun createInput(defaultValue: Int, suggestedLayoutParams: LayoutParams): View {
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(defaultValue.toString())
        input.layoutParams = suggestedLayoutParams
        input.textAlignment = TEXT_ALIGNMENT_TEXT_END
        return input
    }

    override val value get() = input.text.toString().toInt()
}