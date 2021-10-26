package de.br.android.envpicker.ui.input

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText

class StringInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseInput<String>(context, attrs, defStyleAttr) {
    private val input = AppCompatEditText(context)

    override fun createInput(defaultValue: String, suggestedLayoutParams: LayoutParams): View {
        input.setText(defaultValue)
        input.layoutParams = suggestedLayoutParams
        input.textAlignment = TEXT_ALIGNMENT_TEXT_END
        return input
    }

    override val value get() = input.text.toString()
}