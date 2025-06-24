package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories

import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.HttpException
import retrofit2.Response

class RepostioryCorral() {
    var local = Applicatione.db
    var remote = RetrofitClient.instance

    suspend fun getAllTerumbuKarangHybridly(token: String):List<Coral>{
        try {
            var rawdata = remote.getTerumbuKarang(token)
            if(!rawdata.isSuccessful){
                throw HttpException(rawdata)
            }
            var body = rawdata.body()
            var data = body?.data
            if (data == null){data = listOf()}
//            syncFromRemote(token)
            var synchronized_data = syncWithRemote(token)
            println("take data from remote")
            return synchronized_data
        }catch (e: Exception){
            var data = local.CorralDAO().getAllTerumbuKarang()
            var a = data.map { it.toCorral() }
            println("error")
            println("error : "+e.message)
//            a.forEach {
//                println("name : "+it.tk_name)
//            }
            return a
        }
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

    suspend fun deleteHybridly(idTk:Int,token: String):String{
        try {
            var result=remote.deleteTerumbuKarang(idTk, token)
            if(!result.isSuccessful){
                throw HttpException(result)
            }
            local.CorralDAO().softDeleteTerumbuKarang(idTk)
            return "remote delete success"
        }catch (e: Exception){
            local.CorralDAO().softDeleteTerumbuKarang(idTk)
            return "local delete success"
        }
    }

    suspend fun syncWithRemote(token:String):List<Coral>{
        var retainDelete = false
        var retainUpdate = false
        var retainCreate = false
        var copyData: MutableList<Coral> = mutableListOf()

        var Rawdata = remote.getTerumbuKarang(token)
        if(!Rawdata.isSuccessful){
            return listOf()
        }
        var data = Rawdata.body()?.data
        if (data == null){
            return listOf()
        }
        local.CorralDAO().deleteAllTerumbuKarangExceptIfItIsLocallyModifiedOrCreated()
        var localData = local.CorralDAO().getAllTerumbuKarangIncludingTheDeletedOnes()
        println("localdata size : "+localData.size)
        if (localData.isEmpty()==false){
            println("localdata not empty")
        }
        if(!localData.isEmpty()){
            println("Ahaa")
            println("Ahaa")
            println("Ahaa")
            localData.forEach {
                if(it.is_created_locally == true){
//                    remote.addTerumbuKarang(token, Response<it.tk_name>, it.tk_jenis, it.harga_tk, it.stok_tersedia, it.description, null)
                }
                if(it.is_updated_locally == true){
                    var updateResult = remote.editTerumbuKarang(it.id_tk, token, editRequest = EditCoralRequest(it.tk_name, it.tk_jenis, it.harga_tk, it.stok_tersedia, it.description))
                    if(!updateResult.isSuccessful){
                        retainUpdate = true
                        copyData.add(it.toCorral())
                    }else{
                        local.CorralDAO().deleteTerumbuKarang(it)
                    }
                }
                if(it.is_deleted == true){
                    var deleteResult = remote.deleteTerumbuKarang(it.id_tk, token)
                    if(!deleteResult.isSuccessful){
                        retainDelete = true
                        copyData.add(it.toCorral())
                    }else{
                        local.CorralDAO().deleteTerumbuKarang(it)
                    }
                }
            }
        }

        var refetchedData = refetchData(token)
        if(retainDelete==false && retainUpdate==false && retainCreate==false){
//            local.CorralDAO().deleteAllTerumbuKarang()
            refetchedData.forEach {
                local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
            }
            return refetchedData
        }else{
//            local.CorralDAO().deleteAllTerumbuKarang()
            refetchedData.forEach {
                if(!copyData.any { a -> a.id_tk == it.id_tk }){
                    local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
                }
            }
            return local.CorralDAO().getAllTerumbuKarang().map { it.toCorral() }
        }

    }

    suspend fun refetchData(token: String):List<Coral>{
        var Rawdata = remote.getTerumbuKarang(token)
        if(!Rawdata.isSuccessful){
            return listOf()
        }
        var data = Rawdata.body()?.data
        if (data == null || data.isEmpty()){
            return listOf()
        }
        return data
    }

}