package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories

import android.content.Context
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.WorkerEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.AddWorkerRepository
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import retrofit2.HttpException

class RepositoryWorker {
    var remote = RetrofitClient.instance
    var local = Applicatione.db
    suspend fun getAllWorkerHybridly(token:String): List<Worker>{
        try {
            var remoteData = remote.fetchAllUsers(token)
            if (!remoteData.isSuccessful){
                throw HttpException(remoteData)
            }
            var synchro_Data = syncWithRemote(token = token)
            println("take data from remote")
            return synchro_Data
        }catch (e: Exception){
            println("error : "+e.message)
            var lo = local.WorkerDAO().getAllWorkers().map { it.toWorker() }
            return lo
        }
        return null!!
    }
    suspend fun syncWithRemote(token:String,context: Context?=null): List<Worker>{
        var retainData=false
        var copyData: MutableList<Worker> = mutableListOf()
        var copyInsertData: MutableList<WorkerEntities> = mutableListOf()
        var MSRepo = AddWorkerRepository()
        local.WorkerDAO().deleteWorkerExceptIfItIsLocallyModifiedOrCreated()
        var remainingData = local.WorkerDAO().getAllWorkers()
        if (!remainingData.isEmpty()){
            remainingData.forEach {
                if(it.is_updated_locally == true){

                }
            }
        }
        var refetchedData = remote.fetchAllUsers(token)
        if (!refetchedData.isSuccessful){
            println("Error inside syncWithRemote : ")
            throw HttpException(refetchedData)
        }
        if (retainData==false){
            local.WorkerDAO().deleteAllWorkers()
            refetchedData.body()?.users?.forEach {
                println("id worker : "+it.id_user+" status : "+it.status)
                if(it.status=="worker"){
                    println("Added into local database : "+it.id_user)
                    local.WorkerDAO().insertWorker(WorkerEntities.fromWorker(it))
                }
            }
            return local.WorkerDAO().getAllWorkers().map { it.toWorker() }
        }else{
            refetchedData.body()?.users?.forEach {
                if(copyData.any { a -> a.id_user != it.id_user }){

                    local.WorkerDAO().insertWorker(WorkerEntities.fromWorker(it))
                }
            }
            copyInsertData.forEach {
                val workerWithBiggestID = local.WorkerDAO().getWorkerWithBiggestID()
                var id = workerWithBiggestID.id_user+1
                local.WorkerDAO().insertWorker(it.copy(id_user = id))
            }
            return local.WorkerDAO().getAllWorkers().map { it.toWorker() }
        }
//        return null!!
    }
    suspend fun updateHybridly():String{
        return "success"
    }
    suspend fun insertHybridly():String{
        return "success"
    }
}