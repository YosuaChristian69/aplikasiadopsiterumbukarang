package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application

import android.app.Application
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Database.AppDatabase
import retrofit2.Retrofit

class Applicatione: Application() {
    companion object{
//        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//        val retrofit = Retrofit.Builder().addConverterFactory(
//            MoshiConverterFactory.create(moshi)
//        ).baseUrl("http://192.168.18.35:3000").build()
//        val retrofitService = retrofit.create(webService::class.java)
        lateinit var db: AppDatabase

    }

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getInstance(this)
    }
}