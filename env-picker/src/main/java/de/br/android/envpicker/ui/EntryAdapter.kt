package de.br.android.envpicker.ui

import android.content.Context
import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import de.br.android.envpicker.Entry
import de.br.android.envpicker.R

internal class EntryAdapter<T : Entry>(
    private val primaryClickListener: (EntryContainer<T>, View) -> Unit,
    private val secondaryClickListener: (EntryContainer<T>, View) -> Unit
) :
    ListAdapter<EntryContainer<T>, EntryAdapter<T>.EntryViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder =
        EntryViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        )

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    class EntryDiffCallback<T : Entry> : DiffUtil.ItemCallback<EntryContainer<T>>() {
        override fun areItemsTheSame(oldItem: EntryContainer<T>, newItem: EntryContainer<T>) =
            oldItem.entry.name == newItem.entry.name

        override fun areContentsTheSame(oldItem: EntryContainer<T>, newItem: EntryContainer<T>) =
            oldItem == newItem
    }

    inner class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        @ColorInt
        private fun getThemeColor(@AttrRes attrId: Int, context: Context): Int {
            val attrs = intArrayOf(attrId)
            val ta: TypedArray = context.theme.obtainStyledAttributes(attrs)
            return ta.getColor(0, 0)
        }

        fun bindTo(entryContainer: EntryContainer<T>) {
            val color =
                if (entryContainer.active)
                    getThemeColor(R.attr.colorSecondary, itemView.context)
                else ContextCompat.getColor(itemView.context, android.R.color.black)

            itemView.findViewById<TextView>(R.id.title).apply {
                text = entryContainer.entry.name
                setTextColor(color)
            }
            itemView.findViewById<TextView>(R.id.summary).apply {
                text = entryContainer.entry.summary
                setTextColor(color)
            }
            itemView.findViewById<AppCompatImageView>(R.id.btn_edit).apply {
                setColorFilter(color)
                setOnClickListener { secondaryClickListener(entryContainer, itemView) }
            }
            itemView.setOnClickListener { primaryClickListener(entryContainer, itemView) }
        }
    }
}
