package id.istts.aplikasiadopsiterumbukarang.domain.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

//data class Coral(
//    val id_tk: Int,
//    val tk_name: String,
//    val tk_jenis: String,
//    val harga_tk: Int,
//    val stok_tersedia: Int,
//    val description: String,
//    val is_deleted: Boolean,
//    val img_path: String?,
//    val public_id: String?
//)
@Parcelize // <-- TAMBAHKAN ANOTASI INI
data class Coral(
    val id_tk: Int,
    val tk_name: String,
    val tk_jenis: String,
    val harga_tk: Int,
    val stok_tersedia: Int,
    val description: String,
    val is_deleted: Boolean,
    val img_path: String?,
    val public_id: String?
) : Parcelable
//data class Coral(
//    @SerializedName("id_tk")
//    val id_tk: Int,
//
//    @SerializedName("tk_name")
//    val tk_name: String,
//
//    @SerializedName("harga_tk")
//    val harga_tk: Int,
//
//    // --- Nullable fields that might not always be present ---
//    @SerializedName("tk_jenis")
//    val tk_jenis: String? = null,
//
//    @SerializedName("stok_tersedia")
//    val stok_tersedia: Int? = null,
//
//    @SerializedName("description")
//    val description: String? = null,
//
//    @SerializedName("is_deleted")
//    val is_deleted: Boolean? = null,
//
//    @SerializedName("img_path")
//    val img_path: String? = null,
//
//    @SerializedName("public_id")
//    val public_id: String? = null
//)