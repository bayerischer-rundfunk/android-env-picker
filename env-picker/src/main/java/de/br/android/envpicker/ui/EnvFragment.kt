package de.br.android.envpicker.ui

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.br.android.envpicker.ConfigStore.KEY
import de.br.android.envpicker.Entry
import de.br.android.envpicker.FieldDescription
import de.br.android.envpicker.FieldType
import de.br.android.envpicker.R
import de.br.android.envpicker.ui.input.BaseInput
import de.br.android.envpicker.ui.input.BooleanInput
import de.br.android.envpicker.ui.input.IntInput
import de.br.android.envpicker.ui.input.StringInput

internal class EnvFragment<T : Entry> : Fragment(R.layout.env_fragment) {

    companion object {
        fun <T : Entry> create(configKey: String) =
            EnvFragment<T>().apply { arguments = createArgs(configKey) }

        private fun createArgs(configKey: String) = Bundle().apply { putString(KEY, configKey) }
    }

    private val recycler get() = view?.findViewById<RecyclerView>(R.id.recycler)

    private val configKey
        get() = requireArguments().getString(KEY)
            ?: throw IllegalArgumentException("EnvFragment requires a configKey as argument.")

    private val viewModel by viewModels<EnvViewModel<T>> {
        EnvViewModel.Factory<T>(
            configKey,
            requireContext().applicationContext
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.toolbar_title).text = viewModel.fragmentTitle

        val adapter = EntryAdapter(onEntryClicked, onEditEntryClicked)
        recycler?.adapter = adapter
        recycler?.layoutManager = LinearLayoutManager(requireContext())

        view.findViewById<View>(R.id.fab).setOnClickListener { showEntryDialog(null) }
        viewModel.items.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private val onEntryClicked = { entryContainer: EntryContainer<T>, _: View ->
        if (!entryContainer.active)
            showConfirmRestartDialog { _, _ ->
                viewModel.setActiveEntryAndRestart(entryContainer.entry, requireContext())
            }
    }

    private val onEditEntryClicked = { entry: EntryContainer<T>, _: View ->
        showEntryDialog(entry)
    }

    private fun showEntryDialog(entryContainer: EntryContainer<T>?) {
        val context = context ?: return
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.edit_entry_dialog, null)
        val valuesContainer = dialogView.findViewById<ViewGroup>(R.id.ll_values)
        val inputs: List<BaseInput<*>> =
            viewModel.getFieldDescriptionsAndValues(entryContainer?.entry)
                .map { (desc, value) -> createInput(value, desc, context) }
                .onEach { valuesContainer.addView(it) }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setNegativeButton(getString(R.string.ep_dialog_cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ep_dialog_ok)) { _, _ ->
                onUpdateEntry(entryContainer, inputs.map { it.value })
            }
            .setView(dialogView)

        if (entryContainer != null && !entryContainer.active) {
            dialogBuilder
                .setNeutralButton(getString(R.string.ep_dialog_remove)) { _, _ ->
                    viewModel.removeEntry(entryContainer.entry)
                }
        }
        dialogBuilder.show()
    }

    private fun createInput(
        defaultValue: Any?,
        fieldDesc: FieldDescription,
        context: Context
    ): BaseInput<*> =
        when (fieldDesc.type) {
            FieldType.String ->
                (defaultValue?.let { defaultValue as String } ?: "")
                    .let { StringInput(context).apply { init(fieldDesc.label, it) } }
            FieldType.Int ->
                (defaultValue?.toString()?.toInt() ?: 0)
                    .let { IntInput(context).apply { init(fieldDesc.label, it) } }
            FieldType.Boolean ->
                (defaultValue?.let { it as Boolean } ?: false)
                    .let { BooleanInput(context).apply { init(fieldDesc.label, it) } }
        }

    private fun onUpdateEntry(
        entryContainer: EntryContainer<T>?,
        inputsValues: List<Any>
    ) {
        if (entryContainer?.active == true)
            showConfirmRestartDialog { _, _ ->
                viewModel.updateEntryAndRestart(
                    entryContainer.entry,
                    inputsValues,
                    requireContext()
                )
            }
        else viewModel.updateEntry(
            entryContainer?.entry,
            inputsValues
        )
    }

    private fun showConfirmRestartDialog(positiveAction: DialogInterface.OnClickListener) =
        AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle(getString(R.string.ep_dialog_change_entry_title))
            .setMessage(getString(R.string.ep_dialog_change_entry_message))
            .setPositiveButton(
                getString(R.string.ep_dialog_change_entry_restart),
                positiveAction
            )
            .setNegativeButton(getString(R.string.ep_dialog_cancel)) { _, _ -> }
            .show()
}

