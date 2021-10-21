package de.br.android.envpicker.sample

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class SampleActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory(this) }

    private val tvCurrentEndpoint by lazy { findViewById<TextView>(R.id.tv_current_endpoint) }
    private val buttonChangeEndpoint by lazy { findViewById<TextView>(R.id.button_change_endpoint) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.sample_activity)

        viewModel.currentEndpoint.observe(this) {
            tvCurrentEndpoint.text = it
        }
        buttonChangeEndpoint.setOnClickListener {
            viewModel.onChangeEndpoint(this)
        }
    }
}