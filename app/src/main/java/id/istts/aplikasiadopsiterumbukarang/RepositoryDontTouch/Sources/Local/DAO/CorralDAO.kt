package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities

@Dao
interface CorralDAO {
    @Query("SELECT * FROM terumbu_karang where is_deleted = 0")
    suspend fun getAllTerumbuKarang(): List<TerumbuKarangEntities>

    @Query("SELECT * FROM terumbu_karang")
    suspend fun getAllTerumbuKarangIncludingTheDeletedOnes(): List<TerumbuKarangEntities>

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
    @Query("DELETE FROM terumbu_karang where is_deleted = 0 and is_created_locally = 0 and is_updated_locally = 0")
    suspend fun deleteAllTerumbuKarangExceptIfItIsLocallyModifiedOrCreated()


    @Query("DELETE FROM terumbu_karang where is_deleted = 1")
    suspend fun deleteAllTerumbuKarangThatAreIsDeleteTrue()

    @Query("DELETE FROM terumbu_karang where is_created_locally = 1")
    suspend fun deleteAllTerumbuKarangThatAreIsUpdatedLocallyTrue()
//
    @Query("DELETE FROM terumbu_karang where is_created_locally = 1")
    suspend fun deleteAllTerumbuKarangThatAreIsCreatedLocallyTrue()

    @Query("update terumbu_karang set is_deleted = 1 where id_tk = :id")
    suspend fun softDeleteTerumbuKarang(id: Int)

}