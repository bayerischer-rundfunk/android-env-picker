package de.br.envpicker.mocks

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.every
import io.mockk.mockk

fun getMockContext(): Context {
    val context = mockk<Context>()
    val mockSharedPreferences = MockSharedPreferences()
    every { context.getSharedPreferences(any(), any()) } returns mockSharedPreferences
    return context
}

val mockFragmentManager = mockk<FragmentManager>()
    .also { fragmentManager ->
        every { fragmentManager.beginTransaction() } returns mockk<FragmentTransaction>()
            .also {
                every { it.add(any<Fragment>(), any<String>()) } returns it
                every { it.commit() } returns 0
            }
    }