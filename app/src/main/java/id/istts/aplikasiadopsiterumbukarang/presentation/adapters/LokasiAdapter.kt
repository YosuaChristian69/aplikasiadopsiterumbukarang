// File: presentation/adapters/LokasiAdapter.kt
package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

class LokasiAdapter(
    private val onEditClick: (Lokasi) -> Unit,
    private val onDeleteClick: (Lokasi) -> Unit
) : ListAdapter<Lokasi, LokasiAdapter.LokasiViewHolder>(LokasiDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LokasiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        return LokasiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LokasiViewHolder, position: Int) {
        val lokasi = getItem(position)
        holder.bind(lokasi)
    }

    inner class LokasiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvPlaceName: TextView = itemView.findViewById(R.id.tv_place_name)
        private val tvPlaceId: TextView = itemView.findViewById(R.id.tv_place_id)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvLatitude: TextView = itemView.findViewById(R.id.tv_latitude)
        private val tvLongitude: TextView = itemView.findViewById(R.id.tv_longitude)
        private val tvPreciseCoordinates: TextView = itemView.findViewById(R.id.tv_precise_coordinates)
        private val tvPopulationStatus: TextView = itemView.findViewById(R.id.tv_population_status)
        private val btnEdit: CardView = itemView.findViewById(R.id.btn_edit)
        private val btnDelete: CardView = itemView.findViewById(R.id.btn_delete)
        private val btnViewMaps: CardView = itemView.findViewById(R.id.btn_view_maps)

        fun bind(lokasi: Lokasi) {
            tvPlaceName.text = lokasi.lokasiName
            tvPlaceId.text = "ID: ${lokasi.idLokasi}"
            tvDescription.text = lokasi.description
            tvLatitude.text = String.format("%.7f", lokasi.latitude)
            tvLongitude.text = String.format("%.7f", lokasi.longitude)
            tvPopulationStatus.text = "Population: ${lokasi.initialTkPopulation}"

            // Set precise coordinates sama dengan database coordinates untuk sekarang
            tvPreciseCoordinates.text = "${String.format("%.7f", lokasi.latitude)}, ${String.format("%.7f", lokasi.longitude)}"

            // Disable maps button untuk sekarang
            btnViewMaps.alpha = 0.5f
            btnViewMaps.isEnabled = false

            btnEdit.setOnClickListener { onEditClick(lokasi) }
            btnDelete.setOnClickListener { onDeleteClick(lokasi) }
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