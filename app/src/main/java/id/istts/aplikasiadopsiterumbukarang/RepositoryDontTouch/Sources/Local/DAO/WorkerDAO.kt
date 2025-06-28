package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.WorkerEntities

@Dao
interface WorkerDAO {
    @Query("SELECT * FROM workers order by id_user asc")
    suspend fun getAllWorkers(): List<WorkerEntities>

    @Query("SELECT * FROM workers order by id_user desc limit 1")
    suspend fun getWorkerWithBiggestID(): WorkerEntities

    @Query("SELECT * FROM workers WHERE id_user = :id")
    suspend fun getWorkerById(id: String): WorkerEntities?
    @Insert
    suspend fun insertWorker(worker: WorkerEntities)
    @Update
    suspend fun updateWorker(worker: WorkerEntities)
    @Query("DELETE FROM workers")
    suspend fun deleteAllWorkers()
    @Query("DELETE FROM workers WHERE password !=null and is_updated_locally = 1  ")
    suspend fun deleteWorkerExceptIfItIsLocallyModifiedOrCreated()
}