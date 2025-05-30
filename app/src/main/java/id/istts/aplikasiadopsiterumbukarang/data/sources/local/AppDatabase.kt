package id.istts.aplikasiadopsiterumbukarang.data.sources.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import id.istts.aplikasiadopsiterumbukarang.data.sources.local.DAO.TerumbuKarangDAO
import id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities.TerumbuKarangEntities

@Database(entities = [TerumbuKarangEntities::class], version = 1)
abstract class AppDatabase:RoomDatabase() {
    abstract fun TerumbuKarangDAO(): TerumbuKarangDAO
    companion object{
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "localdb"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}