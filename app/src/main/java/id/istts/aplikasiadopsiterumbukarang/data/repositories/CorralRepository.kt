package id.istts.aplikasiadopsiterumbukarang.data.repositories

import id.istts.aplikasiadopsiterumbukarang.data.sources.local.Applicatione
import id.istts.aplikasiadopsiterumbukarang.data.sources.local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient

class CorralRepository() {
    var local = Applicatione.db
    suspend fun localGetAllCorral(): List<TerumbuKarangEntities> {
        return local.TerumbuKarangDAO().getAllTerumbuKarang()
    }
    suspend fun remoteGetAllCorral(): List<TerumbuKarangEntities> {
        val response = RetrofitClient.instance.getTk("asdf")
       return response.body()!!
    }
    suspend fun hybridGetAllCorral(): List<TerumbuKarangEntities>{
        try {
            val response = RetrofitClient.instance.getTk("asdf")
            if(response.isSuccessful){
                return response.body()!!
            }else{
                return local.TerumbuKarangDAO().getAllTerumbuKarang()
            }
        }catch(e: Exception){
            println(e.message)
            return local.TerumbuKarangDAO().getAllTerumbuKarang()
        }
    }
}