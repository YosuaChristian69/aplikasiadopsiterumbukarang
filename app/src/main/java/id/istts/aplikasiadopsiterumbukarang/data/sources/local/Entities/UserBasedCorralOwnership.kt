package id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_based_corral_ownership")
class UserBasedCorralOwnership(
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id_tk") val id_tk: Int,
    @ColumnInfo(name = "id_user") val id_user: Int,
    @ColumnInfo(name = "amount") val amount: Boolean

) {

}