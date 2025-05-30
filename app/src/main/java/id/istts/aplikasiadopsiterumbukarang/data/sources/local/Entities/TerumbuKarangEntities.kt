package id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "terumbu_karang")
data class TerumbuKarangEntities(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id_tk") val id:Int,
    @ColumnInfo(name = "tk_name") val tk_name:String,
    @ColumnInfo(name = "tk_jenis") val tk_jenis:String,
    @ColumnInfo(name = "harga_tk") val harga_tk:Int,
    @ColumnInfo(name = "stok_tersedia") val stok_tersedia:Int,
    @ColumnInfo(name = "is_deleted") val is_deleted:Boolean,
    @ColumnInfo(name = "img_path") val img_path:String?=null,
) {
}