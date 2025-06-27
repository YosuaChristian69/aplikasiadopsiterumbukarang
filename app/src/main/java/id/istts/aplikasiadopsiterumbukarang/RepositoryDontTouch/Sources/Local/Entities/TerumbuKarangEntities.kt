package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.CorralDAO
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "terumbu_karang")
data class TerumbuKarangEntities(
    @PrimaryKey(autoGenerate = false)
    val id_tk: Int,
    val tk_name: String,
    val tk_jenis: String,
    val description:String,
    val harga_tk: Int,
    val stok_tersedia: Int,
    val is_deleted: Boolean,
    val img_path: String?=null,
    val public_id: String?=null,
    val is_created_locally: Boolean?=false,
    val is_updated_locally: Boolean?=false,
    val is_deleted_locally: Boolean?=false,
    val uri: String?=null
): Parcelable {
    companion object{
        fun fromCorral(corral: Coral): TerumbuKarangEntities {
            return TerumbuKarangEntities(
                id_tk = corral.id_tk,
                tk_name = corral.tk_name,
                tk_jenis = corral.tk_jenis,
                description = corral.description,
                harga_tk = corral.harga_tk,
                stok_tersedia = corral.stok_tersedia,
                img_path = corral.img_path,
                public_id = corral.public_id,
                is_deleted = corral.is_deleted
            )
        }
    }
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