package de.br.envpicker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class EnvActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.env_activity)

        val configKey = intent.extras?.getString(ConfigStore.KEY)
            ?: return
        val config = ConfigStore.get(configKey)
        supportFragmentManager.beginTransaction()
            .add(R.id.container, EnvFragment(config))
            .commit()
    }
}