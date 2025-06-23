package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.LokasiEntities
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities

@Dao
interface LokasiDAO {
    @Query("SELECT * FROM lokasi")
    suspend fun getAllLokasi(): List<LokasiEntities>
    @Query("SELECT * FROM lokasi WHERE id_lokasi = :id")
    suspend fun getLokasiById(id: Int): LokasiEntities?
    @Insert
    suspend fun insertLokasi(lokasi: LokasiEntities)
    @Update
    suspend fun updateLokasi(lokasi: LokasiEntities)
    @Delete
    suspend fun deleteLokasi(lokasi: LokasiEntities)
    @Query("DELETE FROM lokasi")
    suspend fun deleteAllLokasi()
}