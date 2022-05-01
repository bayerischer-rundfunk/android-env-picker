package de.br.android.envpicker.mocks

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
    every { context.startActivity(any()) } returns Unit
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

/**
 * Returns the name of the function that encloses the function calling [getEnclosingFunctionName].
 */
fun getEnclosingFunctionName(): String =
    Thread.currentThread().stackTrace[3].methodName