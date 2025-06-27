package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.databinding.ItemCoralCardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class CoralAdapter(private val clickListener: CoralClickListener) :
    ListAdapter<Coral, CoralAdapter.CoralViewHolder>(CoralDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoralViewHolder {
        return CoralViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: CoralViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, clickListener)
    }

    class CoralViewHolder private constructor(private val binding: ItemCoralCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var isExpanded = false

        init {
            // Kita atur listener untuk animasi di sini.
            // Listener ini hanya untuk animasi, bukan untuk logika bisnis.
            binding.cardContainer.setOnClickListener {
                toggleQuickActions()
            }
        }

        fun bind(item: Coral, listener: CoralClickListener) {
            binding.coral = item
            binding.listener = listener

            // PENTING: Reset status tampilan setiap kali ViewHolder digunakan ulang
            // untuk mencegah item yang salah muncul dalam keadaan terbuka.
            isExpanded = false
            binding.quickActionsLayout.visibility = View.GONE
            binding.quickActionsLayout.alpha = 0f

            binding.executePendingBindings()
        }

        // FUNGSI BARU UNTUK MENGATUR ANIMASI
        private fun toggleQuickActions() {
            isExpanded = !isExpanded
            val targetAlpha = if (isExpanded) 1f else 0f
            val duration = 300L

            if (isExpanded) {
                binding.quickActionsLayout.visibility = View.VISIBLE
            }

            binding.quickActionsLayout.animate()
                .alpha(targetAlpha)
                .setDuration(duration)
                .withEndAction {
                    if (!isExpanded) {
                        binding.quickActionsLayout.visibility = View.GONE
                    }
                }
                .start()
        }
        companion object {
            fun from(parent: ViewGroup): CoralViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCoralCardBinding.inflate(layoutInflater, parent, false)
                return CoralViewHolder(binding)
            }
        }
    }
}

class CoralDiffCallback : DiffUtil.ItemCallback<Coral>() {
    override fun areItemsTheSame(oldItem: Coral, newItem: Coral): Boolean {
        return oldItem.id_tk == newItem.id_tk
    }

    override fun areContentsTheSame(oldItem: Coral, newItem: Coral): Boolean {
        return oldItem == newItem
    }
}