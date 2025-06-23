package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "lokasi")
data class LokasiEntities(
    @PrimaryKey(autoGenerate = false)
    val id_lokasi: Int,
    val lokasi_name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val initial_tk_population: Int
) {
}