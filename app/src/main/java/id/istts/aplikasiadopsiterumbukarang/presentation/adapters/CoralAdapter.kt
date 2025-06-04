package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class CoralAdapter(
    private val onItemClick: (Coral) -> Unit
) : RecyclerView.Adapter<CoralAdapter.CoralViewHolder>() {

    private var coralList = mutableListOf<Coral>()

    fun updateData(newList: List<Coral>) {
        coralList.clear()
        coralList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoralViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coral_card, parent, false)
        return CoralViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoralViewHolder, position: Int) {
        holder.bind(coralList[position])
    }

    override fun getItemCount(): Int = coralList.size

    inner class CoralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCoralName)
        private val typeTextView: TextView = itemView.findViewById(R.id.tvCoralType)
        private val priceTextView: TextView = itemView.findViewById(R.id.tvCoralPrice)
        private val stockTextView: TextView = itemView.findViewById(R.id.tvCoralStock)
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoralImage)

        fun bind(coral: Coral) {
            nameTextView.text = coral.tk_name
            typeTextView.text = coral.tk_jenis
            priceTextView.text = formatPrice(coral.harga_tk)
            stockTextView.text = "Stock: ${coral.stok_tersedia}"

            // Load image using Glide with proper error handling
            coral.img_path?.let { imageUrl ->
                if (imageUrl.isNotEmpty()) {
                    Glide.with(itemView.context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_coral_logo)
                        .error(R.drawable.ic_coral_logo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(imageView)
                } else {
                    imageView.setImageResource(R.drawable.ic_coral_logo)
                }
            } ?: run {
                imageView.setImageResource(R.drawable.ic_coral_logo)
            }

            itemView.setOnClickListener {
                onItemClick(coral)
            }
        }

        private fun formatPrice(price: Int): String {
            return "Rp ${String.format("%,d", price).replace(',', '.')}"
        }
    }
}