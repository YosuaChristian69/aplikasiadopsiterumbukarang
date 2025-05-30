package id.istts.aplikasiadopsiterumbukarang.data.sources.local

import android.app.Application
import retrofit2.Retrofit

class Applicatione:Application() {
    companion object{
        lateinit var db: AppDatabase
//        db = AppDatabase.getInstance(baseContext)
    }
    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(baseContext)
    }
}