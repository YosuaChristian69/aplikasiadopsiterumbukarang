package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class CoralAdapter(
    private val onItemClick: (Coral) -> Unit,
    private val onEditClick: (Coral) -> Unit,
    private val onDeleteClick: (Coral, Int) -> Unit
) : RecyclerView.Adapter<CoralAdapter.CoralViewHolder>() {

    private var coralList = mutableListOf<Coral>()

    fun updateData(newList: List<Coral>) {
        coralList.clear()
        coralList.addAll(newList)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in 0 until coralList.size) {
            coralList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, coralList.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoralViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coral_card, parent, false)
        return CoralViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoralViewHolder, position: Int) {
        holder.bind(coralList[position], position)
    }

    override fun getItemCount(): Int = coralList.size

    inner class CoralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardContainer: CardView = itemView.findViewById(R.id.cardContainer)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvCoralName)
        private val typeTextView: TextView = itemView.findViewById(R.id.tvCoralType)
        private val priceTextView: TextView = itemView.findViewById(R.id.tvCoralPrice)
        private val stockTextView: TextView = itemView.findViewById(R.id.tvCoralStock)
        private val stockStatusTextView: TextView = itemView.findViewById(R.id.tvStockStatus)
        private val stockIndicator: View = itemView.findViewById(R.id.stockIndicator)
        private val imageView: ImageView = itemView.findViewById(R.id.ivCoralImage)

        // Menu and action elements
        private val menuOptions: ImageView = itemView.findViewById(R.id.ivMenuOptions)
        private val quickActionsLayout: LinearLayout = itemView.findViewById(R.id.quickActionsLayout)
        private val btnEdit: TextView = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: TextView = itemView.findViewById(R.id.btnDelete)
        private val btnEditCard: CardView = itemView.findViewById(R.id.btnEditCard)
        private val btnDeleteCard: CardView = itemView.findViewById(R.id.btnDeleteCard)

        private var isExpanded = false
        private val animationDuration = 300L

        fun bind(coral: Coral, position: Int) {
            bindCoralData(coral)
            setupClickListeners(coral, position)
        }

        private fun bindCoralData(coral: Coral) {
            nameTextView.text = coral.tk_name
            typeTextView.text = coral.tk_jenis
            priceTextView.text = formatPrice(coral.harga_tk)
            stockTextView.text = "${coral.stok_tersedia} units"

            // Update stock status based on availability
            updateStockStatus(coral.stok_tersedia)

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
        }

        private fun setupClickListeners(coral: Coral, position: Int) {
            // Card click - show details
            cardContainer.setOnClickListener {
                onItemClick(coral)
            }

            // Long click - toggle quick actions
            cardContainer.setOnLongClickListener {
                toggleQuickActions()
                true
            }

            // Menu options click - show popup menu
            menuOptions.setOnClickListener { view ->
                showPopupMenu(view, coral, position)
            }

            // Edit button click
            btnEdit.setOnClickListener {
                hideQuickActions()
                onEditClick(coral)
            }

            btnEditCard.setOnClickListener {
                hideQuickActions()
                onEditClick(coral)
            }

            // Delete button click
            btnDelete.setOnClickListener {
                hideQuickActions()
                showDeleteConfirmation(coral, position)
            }

            btnDeleteCard.setOnClickListener {
                hideQuickActions()
                showDeleteConfirmation(coral, position)
            }
        }

        private fun toggleQuickActions() {
            if (isExpanded) {
                hideQuickActions()
            } else {
                showQuickActions()
            }
        }

        private fun showQuickActions() {
            if (isExpanded) return

            isExpanded = true
            quickActionsLayout.visibility = View.VISIBLE

            // Animate appearance
            quickActionsLayout.alpha = 0f
            quickActionsLayout.animate()
                .alpha(1f)
                .setDuration(animationDuration)
                .start()

            // Rotate menu icon
            menuOptions.animate()
                .rotation(180f)
                .setDuration(animationDuration)
                .start()
        }

        private fun hideQuickActions() {
            if (!isExpanded) return

            isExpanded = false

            // Animate disappearance
            quickActionsLayout.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .withEndAction {
                    quickActionsLayout.visibility = View.GONE
                }
                .start()

            // Rotate menu icon back
            menuOptions.animate()
                .rotation(0f)
                .setDuration(animationDuration)
                .start()
        }

        private fun showPopupMenu(view: View, coral: Coral, position: Int) {
            val popup = PopupMenu(view.context, view)

            // Create menu programmatically
            popup.menu.add(0, 1, 0, "Edit")
            popup.menu.add(0, 2, 0, "Delete")
            popup.menu.add(0, 3, 0, "View Details")

            popup.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    1 -> { // Edit
                        onEditClick(coral)
                        true
                    }
                    2 -> { // Delete
                        showDeleteConfirmation(coral, position)
                        true
                    }
                    3 -> { // View Details
                        onItemClick(coral)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }

        private fun showDeleteConfirmation(coral: Coral, position: Int) {
            val context = itemView.context

            AlertDialog.Builder(context)
                .setTitle("Delete Coral")
                .setMessage("Are you sure you want to delete \"${coral.tk_name}\"?\n\nThis action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteClick(coral, position)
                }
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        private fun updateStockStatus(stock: Int) {
            val context = itemView.context

            when {
                stock == 0 -> {
                    // Out of Stock - Deep coral red
                    stockStatusTextView.text = "Out of Stock"
                    stockStatusTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    stockIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_dark))
                }
                stock < 5 -> {
                    // Critical Low - Orange coral
                    stockStatusTextView.text = "Critical Low"
                    stockStatusTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    stockIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                }
                stock < 10 -> {
                    // Low Stock - Amber/Yellow coral
                    stockStatusTextView.text = "Low Stock"
                    stockStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.coral_dark_text)) // Dark teal text
                    stockIndicator.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light))
                }
                else -> {
                    stockStatusTextView.text = "In Stock"
                    stockStatusTextView.setTextColor(ContextCompat.getColor(context, R.color.coral_dark_text)) // Dark teal text
                    stockIndicator.setBackgroundColor(ContextCompat.getColor(context, R.color.coral_accent)) // #4CDDCF
                }
            }
        }

        private fun formatPrice(price: Int): String {
            return "Rp ${String.format("%,d", price).replace(',', '.')}"
        }
    }
}