package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Using Glide for image loading is highly recommended
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class UserCoralSelectionAdapter(
    private var corals: List<Coral>,
    private val onItemClick: (Coral) -> Unit
) : RecyclerView.Adapter<UserCoralSelectionAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: CardView = itemView.findViewById(R.id.cardViewItem) // Assuming the root is a CardView with this ID
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoral)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCoralSpecies)

        fun bind(coral: Coral, isSelected: Boolean) {
            nameTextView.text = coral.tk_name
            // Use Glide to load images from a URL
            // Glide.with(itemView.context).load(coral.imageUrl).into(imageView)

            // Change appearance based on selection
            cardView.setCardBackgroundColor(
                if (isSelected) Color.parseColor("#C1C7C1") // Highlight color
                else Color.WHITE
            )

            nameTextView.setBackgroundColor(
                if (isSelected) Color.parseColor("#C1C7C1")
                else Color.parseColor("#4FFFB3")
            )

            itemView.setOnClickListener {
                onItemClick(coral)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Use your single item layout file here
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_coral, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(corals[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = corals.size

    // Method to update the adapter's data
    fun updateData(newCorals: List<Coral>) {
        this.corals = newCorals
        notifyDataSetChanged()
    }

    // Method to update the selection and refresh the item views
    fun setSelectedCoral(coral: Coral?) {
        val oldSelectedPosition = selectedPosition
        selectedPosition = if (coral == null) {
            RecyclerView.NO_POSITION
        } else {
            corals.indexOfFirst { it.id_tk == coral.id_tk }
        }

        // Refresh the old and new selected items to update their appearance
        if (oldSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(oldSelectedPosition)
        }
        if (selectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPosition)
        }
    }
}