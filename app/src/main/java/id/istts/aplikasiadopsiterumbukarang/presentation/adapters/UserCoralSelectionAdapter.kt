package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
// Import the correct MaterialCardView
import com.google.android.material.card.MaterialCardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
// Import Lottie
import com.airbnb.lottie.LottieAnimationView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class UserCoralSelectionAdapter(
    private var corals: List<Coral>,
    private val onItemClick: (Coral) -> Unit
) : RecyclerView.Adapter<UserCoralSelectionAdapter.ViewHolder>() {

    private var selectedItems = setOf<Coral>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // THE FIX: Change the type here from CardView to MaterialCardView
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardViewItem)
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoral)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCoralSpecies)

        // Add Lottie animation view - pastikan ID ini sesuai dengan XML Anda
        private val lottieAnimation: LottieAnimationView = itemView.findViewById(R.id.lottieSelection)
        fun bind(coral: Coral, isSelected: Boolean) {
            nameTextView.text = coral.tk_name
            val imageUrl = coral.img_path

            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .into(imageView)
            } else {
                imageView.setImageResource(R.drawable.ic_coral_seed)
            }

            // Handle selection visual feedback
            cardView.strokeWidth = if (isSelected) 8 else 0 // 8dp stroke width when selected
            cardView.strokeColor = if (isSelected) Color.parseColor("#4EF8BF") else Color.TRANSPARENT

            // Handle Lottie animation based on selection state
            if (isSelected) {
                // Show and play animation when selected
                lottieAnimation.visibility = View.VISIBLE
                lottieAnimation.playAnimation()
            } else {
                // Hide animation when not selected
                lottieAnimation.visibility = View.GONE
                lottieAnimation.cancelAnimation()
            }

            itemView.setOnClickListener {
                onItemClick(coral)

                // Optional: Play a quick animation on click
                lottieAnimation.visibility = View.VISIBLE
                lottieAnimation.playAnimation()

                // Auto-hide animation after a delay if not selected
                itemView.postDelayed({
                    if (!selectedItems.contains(coral)) {
                        lottieAnimation.visibility = View.GONE
                    }
                }, 1000) // Hide after 1 second if not selected
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_coral, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val coral = corals[position]
        holder.bind(coral, selectedItems.contains(coral))
    }

    override fun getItemCount(): Int = corals.size

    fun updateData(newCorals: List<Coral>) {
        this.corals = newCorals
        notifyDataSetChanged()
    }

    fun updateSelection(newSelectedCorals: List<Coral>) {
        this.selectedItems = newSelectedCorals.toSet()
        notifyDataSetChanged()
    }
}