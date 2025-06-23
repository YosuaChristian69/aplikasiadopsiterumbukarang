package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.CorralDAO
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.DAO.LokasiDAO
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.LokasiEntities
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities

@Database(entities = [TerumbuKarangEntities::class, LokasiEntities::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun CorralDAO(): CorralDAO
    abstract fun LokasiDAO(): LokasiDAO
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