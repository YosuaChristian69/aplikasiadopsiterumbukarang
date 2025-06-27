package id.istts.aplikasiadopsiterumbukarang.utils // Pastikan package ini benar

import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.istts.aplikasiadopsiterumbukarang.R // Pastikan import R benar
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.CoralAdapter
import id.istts.aplikasiadopsiterumbukarang.presentation.adapters.LokasiAdapter

// Adapter untuk RecyclerView
@BindingAdapter("coralListData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Coral>?) {
    val adapter = recyclerView.adapter as? CoralAdapter
    adapter?.submitList(data)
}

// Adapter untuk gambar
@BindingAdapter("coralImage")
fun bindImage(imgView: ImageView, imgUrl: String?) {
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
@BindingAdapter("lokasiListData")
fun bindLokasiRecyclerView(recyclerView: RecyclerView, data: List<Lokasi>?) {
    val adapter = recyclerView.adapter as? LokasiAdapter
    adapter?.submitList(data)
}

// Adapter untuk WARNA BACKGROUND indikator stok
@BindingAdapter("stockStatusIndicatorColor")
fun bindStockIndicatorColor(cardView: CardView, stock: Int) {
    // Pastikan Anda memiliki warna coral_accent di res/values/colors.xml
    val colorRes = when {
        stock == 0 -> android.R.color.holo_red_dark
        stock < 5 -> android.R.color.holo_orange_dark
        stock < 10 -> android.R.color.holo_orange_light
        else -> R.color.coral_accent
    }
    cardView.setCardBackgroundColor(ContextCompat.getColor(cardView.context, colorRes))
}

@BindingAdapter("stockStatusTextColor")
fun bindStockStatusTextColor(textView: TextView, stock: Int) {
    // Pastikan Anda memiliki warna coral_dark_text di res/values/colors.xml
    val colorRes = when {
        stock == 0 || stock < 5 -> android.R.color.white
        else -> R.color.coral_dark_text
    }
    textView.setTextColor(ContextCompat.getColor(textView.context, colorRes))
}