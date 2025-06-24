package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories

import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.HttpException

class RepostioryCorral() {
    var local = Applicatione.db
    var remote = RetrofitClient.instance

    suspend fun getAllTerumbuKarangHybridly(token: String):List<Coral>{
        try {
            var rawdata = remote.getTerumbuKarang(token)
            var body = rawdata.body()
            var data = body?.data
//            println("data: "+data)
            if (data == null){
//                data?.forEach { println("name : "+it.tk_name) }
//                for (i in 1..100){
//                    println("data null")
//                }
                data = listOf()
            }
            if(!rawdata.isSuccessful){
                println("error : "+rawdata.errorBody())
                throw HttpException(rawdata)
            }
            syncFromRemote(token)
            println("take data from remote")
//            println("message "+rawdata?.msg)
            return data
        }catch (e: Exception){
            var data = local.CorralDAO().getAllTerumbuKarang()
            println("error : "+e.message)
            println("local")
            println("data size : "+data.size)
            if (data==null || data.isEmpty()){
                for (i in 1..10){
                    println("local null")
                }
//                data = listOf(TerumbuKarangEntities(id_tk = 1, tk_name = "abc", tk_jenis = "abc", harga_tk = 100, stok_tersedia = 100, description = "abc", is_deleted = false))
            }
            var a = data.map { it.toCorral() }
            a.forEach {
                println("name : "+it.tk_name)
            }
            return a
        }
//        var data = remote.getTerumbuKarang(token).body()?.data
//        if (data == null){
//            data = listOf()
//        }
//        return data
    }

    suspend fun syncFromRemote(token:String){
        var data = remote.getTerumbuKarang(token).body()?.data
        if (data == null || data.isEmpty()){
            return
        }
        local.CorralDAO().deleteAllTerumbuKarang()
        data.forEach {
            local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
        }
    }


}