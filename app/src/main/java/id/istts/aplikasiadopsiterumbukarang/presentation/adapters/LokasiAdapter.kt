package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.databinding.ItemPlaceCardBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

// Adapter sekarang hanya menerima ClickListener
class LokasiAdapter(private val listener: LokasiClickListener) :
    ListAdapter<Lokasi, LokasiAdapter.LokasiViewHolder>(LokasiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LokasiViewHolder {
        return LokasiViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: LokasiViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }

    // ViewHolder sekarang menggunakan ViewBinding
    class LokasiViewHolder(private val binding: ItemPlaceCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(lokasi: Lokasi, listener: LokasiClickListener) {
            binding.lokasi = lokasi
            binding.listener = listener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): LokasiViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemPlaceCardBinding.inflate(inflater, parent, false)
                return LokasiViewHolder(binding)
            }
        }
    }
}

class LokasiDiffCallback : DiffUtil.ItemCallback<Lokasi>() {
    override fun areItemsTheSame(oldItem: Lokasi, newItem: Lokasi): Boolean {
        return oldItem.idLokasi == newItem.idLokasi
    }

    override fun areContentsTheSame(oldItem: Lokasi, newItem: Lokasi): Boolean {
        return oldItem == newItem
    }
}
