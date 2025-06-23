package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.CorralDAO
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral


@Entity(tableName = "terumbu_karang")
data class TerumbuKarangEntities(
    @PrimaryKey(autoGenerate = false)
    val id_tk: String,
    val tk_name: String,
    val tk_jenis: String,
    val description:String,
    val harga_tk: Int,
    val stok_tersedia: Int,
    val is_deleted: Boolean,
    val img_path: String?=null,
    val public_id: String?=null
) {
    fun toCorral(): Coral {
        return Coral(
            id_tk = id_tk.toInt(),
            tk_name = tk_name,
            tk_jenis = tk_jenis,
            description = description,
            harga_tk = harga_tk,
            stok_tersedia = stok_tersedia,
            img_path = img_path,
            public_id = public_id,
            is_deleted = is_deleted
        )
    }
}