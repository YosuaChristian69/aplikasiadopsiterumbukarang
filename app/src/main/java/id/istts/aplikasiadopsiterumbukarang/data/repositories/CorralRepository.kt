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
       return RetrofitClient.instance.getTk("asdf")
    }
}