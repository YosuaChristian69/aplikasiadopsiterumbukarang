package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities

@Dao
interface CorralDAO {
    @Query("SELECT * FROM terumbu_karang")
    suspend fun getAllTerumbuKarang(): List<TerumbuKarangEntities>
    @Query("SELECT * FROM terumbu_karang WHERE id_tk = :id")
    suspend fun getTerumbuKarangById(id: Int): TerumbuKarangEntities?
    @Insert
    suspend fun insertTerumbuKarang(terumbuKarang: TerumbuKarangEntities)
    @Update
    suspend fun updateTerumbuKarang(terumbuKarang: TerumbuKarangEntities)
    @Delete
    suspend fun deleteTerumbuKarang(terumbuKarang: TerumbuKarangEntities)
    @Query("DELETE FROM terumbu_karang")
    suspend fun deleteAllTerumbuKarang()

}