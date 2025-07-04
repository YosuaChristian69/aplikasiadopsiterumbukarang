package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class WorkerAdapter(
    private val onEditClick: (Worker) -> Unit = {}
) : RecyclerView.Adapter<WorkerAdapter.WorkerViewHolder>() {

    private var workers: List<Worker> = emptyList()

    class WorkerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workerName: TextView = itemView.findViewById(R.id.tv_worker_name)
        val workerEmail: TextView = itemView.findViewById(R.id.tv_worker_email)
        val workerBalance: TextView = itemView.findViewById(R.id.tv_worker_balance)
        val workerJoined: TextView = itemView.findViewById(R.id.tv_worker_joined)
        val workerId: TextView = itemView.findViewById(R.id.tv_worker_id)
        val statusText: TextView = itemView.findViewById(R.id.tv_status)
        val statusDot: View = itemView.findViewById(R.id.status_dot)
        val tvWorkerInitials: TextView = itemView.findViewById(R.id.tv_worker_initials)
        val ivWorkerProfile: ImageView = itemView.findViewById(R.id.iv_worker_profile)
        val btnEditWorker: FloatingActionButton = itemView.findViewById(R.id.btn_edit_worker)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_worker_card, parent, false)
        return WorkerViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkerViewHolder, position: Int) {
        val worker = workers[position]

        // Set worker data
        holder.workerName.text = worker.full_name
        holder.workerEmail.text = worker.email
        holder.workerId.text = "ID: ${worker.id_user}"

        // Format balance
        try {
            val balance = worker.balance.toDoubleOrNull() ?: 0.0
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            holder.workerBalance.text = "Balance: ${formatter.format(balance)}"
        } catch (e: Exception) {
            holder.workerBalance.text = "Balance: ${worker.balance}"
        }

        // Format joined date
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            holder.workerJoined.text = "Joined: ${dateFormat.format(worker.joined_at)}"
        } catch (e: Exception) {
            holder.workerJoined.text = "Joined: ${worker.joined_at}"
        }

        // Set status (using user_status for display, status for logic)
        holder.statusText.text = worker.user_status

        // Load profile image or show initials
        loadWorkerProfileImage(holder, worker)

        // Set status indicator color based on user_status
        when (worker.user_status.lowercase()) {
            "active", "aktif" -> {
                holder.statusDot.setBackgroundResource(R.drawable.circle_active)
            }
            "inactive", "tidak aktif" -> {
                holder.statusDot.setBackgroundResource(R.drawable.circle_inactive)
            }
            else -> {
                holder.statusDot.setBackgroundResource(R.drawable.circle_inactive)
            }
        }

        // Set edit button click listener with animation
        holder.btnEditWorker.setOnClickListener {
            // Add subtle animation on click
            it.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction {
                    it.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()
                }
                .start()

            // Trigger edit callback
            onEditClick(worker)
        }
    }

    private fun loadWorkerProfileImage(holder: WorkerViewHolder, worker: Worker) {
        val imageUrl = buildCloudinaryUrl(worker.img_path, worker.public_id)

        if (imageUrl.isNotEmpty()) {
            // Load image with Glide
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .apply(
                    RequestOptions()
                        .circleCrop()
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .timeout(10000) // 10 second timeout
                )
                .into(holder.ivWorkerProfile)

            // Show image, hide initials
            holder.ivWorkerProfile.visibility = View.VISIBLE
            holder.tvWorkerInitials.visibility = View.GONE
        } else {
            // Show initials, hide image
            holder.ivWorkerProfile.visibility = View.GONE
            holder.tvWorkerInitials.visibility = View.VISIBLE
            holder.tvWorkerInitials.text = getInitials(worker.full_name)
        }
    }

    private fun buildCloudinaryUrl(imgPath: String?, publicId: String?): String {
        return when {
            // If img_path is already a complete URL, use it directly
            !imgPath.isNullOrEmpty() && (imgPath.startsWith("http://") || imgPath.startsWith("https://")) -> {
                imgPath
            }
            // If we have public_id, build Cloudinary URL
            !publicId.isNullOrEmpty() -> {
                "https://res.cloudinary.com/your-cloud-name/image/upload/c_fill,g_face,h_200,w_200,q_auto,f_auto/$publicId"
            }
            // If we have img_path but it's not a complete URL, assume it's a Cloudinary path
            !imgPath.isNullOrEmpty() -> {
                "https://res.cloudinary.com/your-cloud-name/image/upload/c_fill,g_face,h_200,w_200,q_auto,f_auto/$imgPath"
            }
            else -> ""
        }
    }

    override fun getItemCount(): Int = workers.size

    fun updateWorkers(newWorkers: List<Worker>) {
        val diffCallback = WorkerDiffCallback(workers, newWorkers)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        workers = newWorkers
        diffResult.dispatchUpdatesTo(this)
    }

    fun getInitials(fullName: String): String {
        return fullName.split(" ")
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
    }

    private class WorkerDiffCallback(
        private val oldList: List<Worker>,
        private val newList: List<Worker>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id_user == newList[newItemPosition].id_user
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}