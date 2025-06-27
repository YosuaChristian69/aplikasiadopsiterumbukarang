package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.domain.models.UserCoral // Make sure to import your data class

/**
 * Adapter for the RecyclerView in the UserDashboardFragment.
 * This adapter is responsible for displaying the list of corals owned by the user.
 *
 * @property corals The list of UserCoral objects to be displayed.
 */
class UserCoralAdapter(
    private var corals: List<UserCoral>,
    private val onItemClicked: (Int) -> Unit
) : RecyclerView.Adapter<UserCoralAdapter.UserCoralViewHolder>() {

    /**
     * The ViewHolder holds the views for each individual item in the list.
     */
    class UserCoralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val coralImageView: ImageView = itemView.findViewById(R.id.coralImageView)
        val coralNicknameTextView: TextView = itemView.findViewById(R.id.coralNicknameTextView)
        val coralSpeciesTextView: TextView = itemView.findViewById(R.id.coralSpeciesTextView)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder. It inflates the item layout.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserCoralViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_user_coral, parent, false)
        return UserCoralViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method binds the data to the views in the ViewHolder.
     */
    override fun onBindViewHolder(holder: UserCoralViewHolder, position: Int) {
        val coral = corals[position]

        // Set the text for the nickname and species name
        holder.coralNicknameTextView.text = coral.coralNickname
        holder.coralSpeciesTextView.text = coral.coralDetails.tk_name
        holder.itemView.setOnClickListener {
            // 2. Pass the ownershipId when an item is clicked
            onItemClicked(coral.ownershipId) // <-- CHANGE: Pass the ID
        }
        // Load the coral image using Glide
        // It's good practice to have a placeholder and an error image.
        Glide.with(holder.itemView.context)
            .load(coral.coralDetails.img_path) // The URL of the image from your API
            .placeholder(R.drawable.ic_image_placeholder) // A placeholder drawable
            .error(R.drawable.ic_coral_logo) // A drawable for when the image fails to load
            .into(holder.coralImageView)
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    override fun getItemCount(): Int {
        return corals.size
    }

    /**
     * A helper function to update the data in the adapter and refresh the RecyclerView.
     */
    fun updateData(newCorals: List<UserCoral>) {
        this.corals = newCorals
        notifyDataSetChanged()
    }
}



