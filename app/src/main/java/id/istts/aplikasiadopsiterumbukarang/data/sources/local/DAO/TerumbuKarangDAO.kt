package id.istts.aplikasiadopsiterumbukarang.data.sources.local.DAO

import androidx.room.Dao
import androidx.room.Query
import id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities.TerumbuKarangEntities

@Dao
interface TerumbuKarangDAO {
    @Query("SELECT * FROM terumbu_karang")
    fun getAllTerumbuKarang(): List<TerumbuKarangEntities>
}