package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.CorralDAO
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.LokasiDAO
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.WorkerDAO
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.LokasiEntities
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.WorkerEntities
//import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.TypeConverter.DateConverter

@Database(entities = [TerumbuKarangEntities::class, LokasiEntities::class, WorkerEntities::class], version = 2)
//@TypeConverters(DateConverter::class)

abstract class AppDatabase: RoomDatabase() {
    abstract fun CorralDAO(): CorralDAO
    abstract fun LokasiDAO(): LokasiDAO
    abstract fun WorkerDAO(): WorkerDAO
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "projectDatabase"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}