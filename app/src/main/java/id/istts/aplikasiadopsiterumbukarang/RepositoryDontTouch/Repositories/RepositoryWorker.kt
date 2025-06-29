package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories

import android.content.Context
import android.net.Uri
import com.google.android.libraries.places.api.model.LocalDate
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.WorkerEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.AddWorkerRequest
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.AddWorkerRepository
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Locale

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
    suspend fun syncWithRemote(token:String,imageUri: Uri?=null,context: Context?=null): List<Worker>{
        var retainData=false
        var copyData: MutableList<WorkerEntities> = mutableListOf()
        var copyInsertData: MutableList<WorkerEntities> = mutableListOf()
        var MSRepo = AddWorkerRepository()
        local.WorkerDAO().deleteWorkerExceptIfItIsLocallyModifiedOrCreated()
        var remainingData = local.WorkerDAO().getAllWorkers()
        if (!remainingData.isEmpty()){
            remainingData.forEach {
                if(it.is_updated_locally == true){
                    val updatedData = mapOf(
                        "full_name" to it.full_name,
                        "email" to it.email,
                        "user_status" to it.user_status
                    )
                    var remoteData = remote.updateUserById(it.id_user,token,updatedData)
                    if (remoteData.isSuccessful){
                        local.WorkerDAO().deleteSingleWorker(it)
                    }else{
                        println("Hello Im here : "+it.full_name)
                        copyData.add(it)
                        retainData=true
                    }
                }
                if(it.is_inserted_locally == true){
                    val responseResult = MSRepo.addWorker(token, AddWorkerRequest(it.full_name,it.email,it.password!!), null, context)
                    if (responseResult.isSuccessful) {
                        local.WorkerDAO().deleteSingleWorker(it)
                    }else {
                        copyInsertData.add(it)
                        retainData = true
                    }
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
//                println("id worker : "+it.id_user+" status : "+it.status)
                if(it.status=="worker"){
                    println("Added into local database : "+it.id_user)
                    local.WorkerDAO().insertWorker(WorkerEntities.fromWorker(it))
                }
            }
            return local.WorkerDAO().getAllWorkers().map { it.toWorker() }
        }else{
            refetchedData.body()?.users?.forEach {
                if(!copyData.any { a -> a.id_user == it.id_user } && it.status=="worker"){
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
    suspend fun updateHybridly(id:String,token:String,updateData: Map<String,String>): Worker{
        try {
            var remoteData = remote.updateUserById(id,token,updateData)
            if (!remoteData.isSuccessful){
                throw HttpException(remoteData)
            }

            var localUpdateResult = localUpdate(id,updateData)

            return remoteData.body()?.user!!
        }catch (e: Exception){
            println("error : "+e.message)
            var worker = local.WorkerDAO().getWorkerById(id)
            var full_name=worker.full_name
            var email=worker.email
            var user_status=worker.user_status
            for ((key,value) in updateData){
                if(key=="full_name"){
                    full_name=value
                }
                if(key=="email"){
                    email=value
                }
                if(key=="user_status"){
                    user_status=value
                }
            }
//            println("full_name : "+full_name)
            var updatedWorker = worker.copy(full_name = full_name,email = email,user_status = user_status,is_updated_locally = true)
            println("full_name : "+updatedWorker.full_name+" is_updated_locally : "+updatedWorker.is_updated_locally)
            local.WorkerDAO().updateWorker(updatedWorker)
            return updatedWorker.toWorker()
        }
//        return "success"
    }

    suspend fun localUpdate(id:String,updateData: Map<String,String>): Worker{
        var worker = local.WorkerDAO().getWorkerById(id)
        var full_name=worker.full_name
        var email=worker.email
        var user_status=worker.user_status
        for ((key,value) in updateData){
            if(key=="full_name"){
                full_name=value
            }
            if(key=="email"){
                email=value
            }
            if(key=="user_status"){
                user_status=value
            }
        }
//            println("full_name : "+full_name)
        var updatedWorker = worker.copy(full_name = full_name,email = email,user_status = user_status,is_updated_locally = true)
        println("full_name : "+updatedWorker.full_name+" is_updated_locally : "+updatedWorker.is_updated_locally)
        local.WorkerDAO().updateWorker(updatedWorker)
        return updatedWorker.toWorker()
    }

    suspend fun insertHybridly(token: String, request: AddWorkerRequest, imageUri: Uri? = null, context: Context? = null):String{
        var MSRepo = AddWorkerRepository()
        try {
            val response = MSRepo.addWorker(token, request, imageUri, context)
            if (response.isSuccessful) {
                val message = response.body()?.msg ?: "Worker added successfully"
                insertLocallyIfRemoteSuccess(request)
                return message
            } else {
//                insertLocallyIfRemoteFail(request)
                throw HttpException(response)
            }
        }catch (e: Exception){
            println("error in function insertHybridly : "+e.message)
            insertLocallyIfRemoteFail(request)
            return "Worker added locally"
        }
//        return "success"
    }

    suspend fun insertLocallyIfRemoteFail(request: AddWorkerRequest){
        var WorkerWithBiggestID = local.WorkerDAO().getWorkerWithBiggestID()
        val newWorker= WorkerEntities(
            id_user = (WorkerWithBiggestID.id_user.toInt()+1).toString(),
            full_name = request.name,
            email = request.email,
            password = request.password,
            status = "worker",
            user_status = "active",
            joined_at = "2020-10-10 08:01:01",
            balance = "0",
            is_updated_locally = false,
            is_inserted_locally = true
        )
        local.WorkerDAO().insertWorker(newWorker)
    }

    suspend fun insertLocallyIfRemoteSuccess(request: AddWorkerRequest){
        var WorkerWithBiggestID = local.WorkerDAO().getWorkerWithBiggestID()
        val newWorker= WorkerEntities(
            id_user = (WorkerWithBiggestID.id_user.toInt()+1).toString(),
            full_name = request.name,
            email = request.email,
            password = request.password,
            status = "worker",
            user_status = "active",
            joined_at = "2020-10-10 08:01:01",
            balance = "0",
            is_updated_locally = false,
            is_inserted_locally = false
        )
        local.WorkerDAO().insertWorker(newWorker)
    }

    suspend fun selectOneWorkerHybridly(id: String,token:String): Worker{
        try {
            var rawData = remote.fetchUserById(id,token)
            if (!rawData.isSuccessful){
                throw HttpException(rawData)
            }
            return rawData.body()?.user!!
        }catch (e: Exception){
            println("error in function selectOneWorkerHybridly : "+e.message)
            var worker = local.WorkerDAO().getWorkerById(id)
            return worker.toWorker()
        }
    }

    suspend fun copyImageToInternalStorage(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val mimeType = context.contentResolver.getType(uri)
            val extension = when (mimeType) {
                "image/png" -> ".png"
                "image/jpeg" -> ".jpeg"
                "image/jpg" -> ".jpg"
                else -> ".img"
            }
            val fileName = "img_${System.currentTimeMillis()}.${extension}"
            val targetFile = File(context.filesDir, fileName)

            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}