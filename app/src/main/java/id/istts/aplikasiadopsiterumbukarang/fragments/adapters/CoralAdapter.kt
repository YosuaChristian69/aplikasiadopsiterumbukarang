package id.istts.aplikasiadopsiterumbukarang.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import id.istts.aplikasiadopsiterumbukarang.R
import id.istts.aplikasiadopsiterumbukarang.fragments.models.CoralItem

class CoralAdapter(private val coralList: List<CoralItem>) :
    RecyclerView.Adapter<CoralAdapter.CoralViewHolder>() {

    class CoralViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val speciesText: TextView = itemView.findViewById(R.id.tvSpecies)
        val stockText: TextView = itemView.findViewById(R.id.tvStock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoralViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coral_card, parent, false)
        return CoralViewHolder(view)
    }

    override fun onBindViewHolder(holder: CoralViewHolder, position: Int) {
        val coral = coralList[position]
        holder.speciesText.text = coral.species
        holder.stockText.text = coral.stock
    }

    override fun getItemCount() = coralList.size
}