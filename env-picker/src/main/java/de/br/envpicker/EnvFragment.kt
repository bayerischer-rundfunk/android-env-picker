package de.br.envpicker

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.br.envpicker.ConfigStore.KEY

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
        val nameEditText = dialogView.findViewById<EditText>(R.id.et_name)
            .apply { setText(entryContainer?.entry?.name) }
        val valuesContainer = dialogView.findViewById<ViewGroup>(R.id.ll_values)
        val editTexts = viewModel.config.entryDescription.fieldNames
            .mapIndexed { i, fieldName ->
                val editText = AppCompatEditText(context)
                editText.hint = fieldName
                entryContainer?.entry?.fields?.getOrNull(i)
                    ?.let { editText.setText(it) }
                editText
            }
            .onEach { valuesContainer.addView(it) }

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setNegativeButton(getString(R.string.ep_dialog_cancel)) { _, _ -> }
            .setPositiveButton(getString(R.string.ep_dialog_ok)) { _, _ ->
                onUpdateEntry(entryContainer, nameEditText, editTexts)
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

    private fun onUpdateEntry(
        entryContainer: EntryContainer<T>?,
        nameEditText: EditText,
        editTexts: List<EditText>
    ) {
        if (entryContainer?.active == true) {
            showConfirmRestartDialog { _, _ ->
                viewModel.updateEntryAndRestart(
                    entryContainer.entry,
                    nameEditText.text.toString(),
                    editTexts.map { it.text.toString() },
                    requireContext()
                )
            }
        } else {
            viewModel.updateEntry(
                entryContainer?.entry,
                nameEditText.text.toString(),
                editTexts.map { it.text.toString() }
            )
        }
    }

    private fun showConfirmRestartDialog(
        positiveAction: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setTitle(getString(R.string.ep_dialog_change_entry_title))
            .setMessage(getString(R.string.ep_dialog_change_entry_message))
            .setPositiveButton(getString(R.string.ep_dialog_change_entry_restart), positiveAction)
            .setNegativeButton(getString(R.string.ep_dialog_cancel)) { _, _ -> }
            .show()
    }
}

