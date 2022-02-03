package de.br.android.envpicker.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.br.android.envpicker.ConfigStore
import de.br.android.envpicker.Entry
import de.br.android.envpicker.EnvPicker
import de.br.android.envpicker.R

/**
 * This activity provides a UI that lets you manage the environment of the associated [EnvPicker].
 */
class EnvActivity<T : Entry> : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.env_activity)

        val configKey = intent.extras?.getString(ConfigStore.KEY)
            ?: throw IllegalArgumentException("EnvActivity requires a configKey as Intent extra.")
        supportFragmentManager.beginTransaction()
            .add(R.id.container, EnvFragment.create<T>(configKey))
            .commit()
    }
}