package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Repositories

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Application.Applicatione
import id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities.TerumbuKarangEntities
import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral
import id.istts.aplikasiadopsiterumbukarang.domain.models.EditCoralRequest
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.repositories.CoralRepositoryImpl
import id.istts.aplikasiadopsiterumbukarang.service.RetrofitClient
import id.istts.aplikasiadopsiterumbukarang.usecases.AddCoralUseCase
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class RepostioryCorral() {
    var local = Applicatione.db
    var remote = RetrofitClient.instance

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
    suspend fun getAllTerumbuKarangHybridly(token: String,fileUtils: FileUtils?=null,context: Context?=null):List<Coral>{
        try {
            var rawdata = remote.getTerumbuKarang(token)
            if(!rawdata.isSuccessful){
                throw HttpException(rawdata)
            }
            var body = rawdata.body()
            var data = body?.data
            if (data == null){data = listOf()}
            var synchronized_data = syncWithRemote(token =  token, fileUtils =  fileUtils, context = context)
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
    suspend fun updateHybridly(idTk:Int,token: String,editRequest: EditCoralRequest): String{
        try {
            var editResult = remote.editTerumbuKarang(idTk, token, editRequest)
            if(!editResult.isSuccessful){
                throw HttpException(editResult)
            }

            var coral = local.CorralDAO().getTerumbuKarangById(idTk)
            if(coral == null){
                return "No Data"
            }
            var updatedCoral = coral.copy(tk_name = editRequest.name!!, tk_jenis = editRequest.jenis!!, harga_tk = editRequest.harga!!, stok_tersedia = editRequest.stok!!, description = editRequest.description!!, is_updated_locally = true)
            local.CorralDAO().updateTerumbuKarang(updatedCoral)

            return "Update Success"
        }catch (e: Exception){
            var coral = local.CorralDAO().getTerumbuKarangById(idTk)
            if(coral == null){
                return "No Data"
            }
            var updatedCoral = coral.copy(tk_name = editRequest.name!!, tk_jenis = editRequest.jenis!!, harga_tk = editRequest.harga!!, stok_tersedia = editRequest.stok!!, description = editRequest.description!!, is_updated_locally = true)
            local.CorralDAO().updateTerumbuKarang(updatedCoral)
            return "Update Success"
        }
    }

    suspend fun insertHybridly(token: String,
                                           name: String,
                                           type: String,
                                           description: String,
                                           total: Int,
                                           harga: Int,
                                           uri: Uri,
                                           fileUtils: FileUtils,
                                context: Context?=null):String{
        try {
            var AddCoralUC = AddCoralUseCase(CoralRepositoryImpl(), fileUtils)
            var params = AddCoralUseCase.AddCoralParams(
                token = token,
                name = name.trim(),
                jenis = type.trim(),
                harga = harga.toString().trim(),
                stok = total.toString().trim(),
                description = description.trim(),
                imageUri = uri
            )
            var result = AddCoralUC.execute(params)
            result.onSuccess {
                return "success remotely"
            }
            result.onFailure {
                var idTk = local.CorralDAO().getAllTerumbuKarang()
                var id = idTk.last().id_tk+1
                var path = ""
                if (context!=null){
                    val safeFile = copyImageToInternalStorage(context, uri)
                    path = FileProvider.getUriForFile(context, "${context.packageName}.provider", safeFile).toString()
                }
                val data = TerumbuKarangEntities(id_tk = id,tk_name = name, tk_jenis = type, harga_tk = harga, stok_tersedia = total, description = description, is_created_locally = true, is_deleted = false, img_path = path)
//                val data = TerumbuKarangEntities(id_tk = id,tk_name = name, tk_jenis = type, harga_tk = harga, stok_tersedia = total, description = description, is_created_locally = true, is_deleted = false, img_path = uri.toString())
                local.CorralDAO().insertTerumbuKarang(data)
                return "inserted locally"
            }
            return "success remotely"
        }catch (e: Exception){
            var idTk = local.CorralDAO().getAllTerumbuKarangIncludingTheDeletedOnes()
            var id = idTk.last().id_tk+1
            val data = TerumbuKarangEntities(id_tk = id,tk_name = name, tk_jenis = type, harga_tk = harga, stok_tersedia = total, description = description, is_created_locally = true, is_deleted = false, img_path = uri.toString())
            local.CorralDAO().insertTerumbuKarang(data)
            return "inserted locally"
        }
    }

    suspend fun syncWithRemote(token:String,fileUtils: FileUtils?=null, context: Context?=null):List<Coral>{
        var retainDelete = false
        var retainUpdate = false
        var retainCreate = false
        var copyData: MutableList<Coral> = mutableListOf()
        var copyInsertData: MutableList<TerumbuKarangEntities> = mutableListOf()
        var MSrepo = CoralRepositoryImpl()

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
            localData.forEach {
                if(it.is_created_locally == true){
                    val executedCodes = AddCoralUseCase.AddCoralParams(
                        token = token,
                        name = it.tk_name.trim(),
                        jenis = it.tk_jenis.trim(),
                        harga = it.harga_tk.toString().trim(),
                        stok = it.stok_tersedia.toString().trim(),
                        description = it.description.trim(),
                        imageUri = Uri.parse(it.img_path)
//                        imageUri = File(it.img_path)
                    )
                    if(fileUtils!=null && context!=null){
                        val result = AddCoralUseCase(MSrepo, fileUtils).execute(executedCodes)
                        result.onSuccess { success ->
//                            context.contentResolver?.delete(Uri.parse(it.img_path), null, null)
                            println("deleted inserted tk : "+it.id_tk)
                            local.CorralDAO().deleteTerumbuKarang(it)
                        }
                        result.onFailure { fail ->
                            retainCreate = true
                            copyInsertData.add(it)
                            local.CorralDAO().deleteTerumbuKarang(it)
                        }
                    }else{
                        retainCreate = true
                        copyInsertData.add(it)
                        local.CorralDAO().deleteTerumbuKarang(it)
                    }
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
            local.CorralDAO().deleteAllTerumbuKarang()
            try {
                var a = local.CorralDAO().getAllTerumbuKarangIncludingTheDeletedOnes()
                a.forEach {
                    println("a id : "+it.id_tk)
                }
                var refetch_id = 0
                refetchedData.forEach {
                    println("refetched id : "+it.id_tk)
                    if(refetch_id!=it.id_tk){
                        refetch_id = it.id_tk
                        local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
                    }
                }
            }catch (e: Exception){
                println("retain false error:"+e.message)
                println("Ahaa the error is in retain false")
                println("Ahaa the error is in retain false")
//                refetchedData.forEach {
//                    local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
//                }
            }

            return refetchedData
        }else{
//            local.CorralDAO().deleteAllTerumbuKarang()
            try {
                refetchedData.forEach {
                    if(!copyData.any { a -> a.id_tk == it.id_tk}){
                        local.CorralDAO().insertTerumbuKarang(TerumbuKarangEntities.fromCorral(it))
                    }
                }
            }catch (e: Exception){
                println("Ahaa the error is in inserting retain update delete")
                println("Ahaa the error is in inserting retain update delete")
            }

            if(copyInsertData.isNotEmpty()){
                copyInsertData.forEach {
                    var idTk = local.CorralDAO().getAllTerumbuKarangIncludingTheDeletedOnes()
                    var id = idTk.last().id_tk+1
                    local.CorralDAO().insertTerumbuKarang(it.copy(id_tk = id))
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