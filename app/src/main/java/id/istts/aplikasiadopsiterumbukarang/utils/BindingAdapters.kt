package id.istts.aplikasiadopsiterumbukarang.utils // Pastikan package ini benar

import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import id.istts.aplikasiadopsiterumbukarang.R // Pastikan import R benar
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.domain.models.worker.PendingPlanting
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.LokasiAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.fragments.worker.MissionAdapter

// === Existing Adapters from your file ===

// Adapter untuk RecyclerView Coral
@BindingAdapter("coralListData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Coral>?) {
    val adapter = recyclerView.adapter as? CoralAdapter
    adapter?.submitList(data)
}

// Adapter untuk gambar Coral
@BindingAdapter("coralImage")
fun bindCoralImage(imgView: ImageView, imgUrl: String?) {
    Glide.with(imgView.context)
        .load(imgUrl)
        .placeholder(R.drawable.ic_coral_logo)
        .error(R.drawable.ic_coral_logo)
        .centerCrop()
        .into(imgView)
}

// Adapter untuk format harga
@BindingAdapter("coralPrice")
fun bindPrice(textView: TextView, price: Int) {
    textView.text = "Rp ${String.format("%,d", price).replace(',', '.')}"
}

// Adapter untuk format stok
@BindingAdapter("coralStock")
fun bindStock(textView: TextView, stock: Int) {
    textView.text = "$stock units"
}

// Adapter untuk teks status stok
@BindingAdapter("stockStatusText")
fun bindStockStatusText(textView: TextView, stock: Int) {
    textView.text = when {
        stock == 0 -> "Out of Stock"
        stock < 5 -> "Critical Low"
        stock < 10 -> "Low Stock"
        else -> "In Stock"
    }
}

// Adapter untuk RecyclerView Lokasi
@BindingAdapter("lokasiListData")
fun bindLokasiRecyclerView(recyclerView: RecyclerView, data: List<Lokasi>?) {
    val adapter = recyclerView.adapter as? LokasiAdapter
    adapter?.submitList(data)
}

// Adapter untuk WARNA BACKGROUND indikator stok
@BindingAdapter("stockStatusIndicatorColor")
fun bindStockIndicatorColor(cardView: CardView, stock: Int) {
    val colorRes = when {
        stock == 0 -> android.R.color.holo_red_dark
        stock < 5 -> android.R.color.holo_orange_dark
        stock < 10 -> android.R.color.holo_orange_light
        else -> R.color.coral_accent
    }
    cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, colorRes))
}

// Adapter untuk WARNA TEKS status stok
@BindingAdapter("stockStatusTextColor")
fun bindStockStatusTextColor(textView: TextView, stock: Int) {
    val colorRes = when {
        stock == 0 || stock < 5 -> android.R.color.white
        else -> R.color.coral_dark_text
    }
    textView.setTextColor(ContextCompat.getColor(textView.context, colorRes))
}

// === New Adapters from Refactoring ===

// Adapter to handle view visibility based on a boolean
@BindingAdapter("android:visibility")
fun setVisibility(view: View, value: Boolean) {
    view.visibility = if (value) View.VISIBLE else View.GONE
}

// Adapter to load a list of worker missions into the RecyclerView
@BindingAdapter("missionListData")
fun bindMissionRecyclerView(recyclerView: RecyclerView, data: List<PendingPlanting>?) {
    val adapter = recyclerView.adapter as? MissionAdapter
    adapter?.submitList(data)
}

// Adapter for loading a profile image from a URL with a circular crop
@BindingAdapter("profileImageUrl")
fun bindProfileImage(imgView: ImageView, imgUrl: String?) {
    Glide.with(imgView.context)
        .load(imgUrl)
        .transform(CircleCrop())
        .placeholder(R.drawable.ic_person_placeholder)
        .error(R.drawable.ic_person_placeholder)
        .into(imgView)
}

// Adapter for loading any image from a URL (general purpose)
@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    Glide.with(imgView.context)
        .load(imgUrl)
        .placeholder(R.drawable.ic_coral_logo)
        .error(R.drawable.ic_coral_logo)
        .centerCrop()
        .into(imgView)
}

// Adapter for loading an image from a local URI
@BindingAdapter("imageUri")
fun bindImageUri(imgView: ImageView, imgUri: Uri?) {
    imgUri?.let {
        Glide.with(imgView.context)
            .load(it)
            .placeholder(R.drawable.ic_coral_logo)
            .error(R.drawable.ic_coral_logo)
            .centerCrop()
            .into(imgView)
    }
}