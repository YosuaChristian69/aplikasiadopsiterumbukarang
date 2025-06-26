package id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.databinding.ItemWorkerMissionBinding
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting

class MissionAdapter(private val onMissionClicked: (PendingPlanting) -> Unit) :
    ListAdapter<PendingPlanting, MissionAdapter.MissionViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val binding = ItemWorkerMissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MissionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        val currentMission = getItem(position)
        holder.bind(currentMission)
        holder.itemView.setOnClickListener {
            onMissionClicked(currentMission)
        }
    }

    class MissionViewHolder(private val binding: ItemWorkerMissionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mission: PendingPlanting) {
            binding.tvMissionTitle.text = "Mission #${mission.id_htrans}"
            binding.tvBuyerName.text = mission.nama_pembeli
            binding.tvLocation.text = mission.lokasi_penanaman
            binding.tvCoralSummary.text = mission.ringkasan_coral
            binding.tvAssignmentStatus.text = mission.assignment_status.uppercase()
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<PendingPlanting>() {
            override fun areItemsTheSame(oldItem: PendingPlanting, newItem: PendingPlanting): Boolean {
                return oldItem.id_htrans == newItem.id_htrans
            }

            override fun areContentsTheSame(oldItem: PendingPlanting, newItem: PendingPlanting): Boolean {
                return oldItem == newItem
            }
        }
    }
}