package id.istts.aplikasiadopsiterumbukarang.usecases

import android.net.Uri
import id.istts.aplikasiadopsiterumbukarang.domain.repositories.CoralRepository
import id.istts.aplikasiadopsiterumbukarang.utils.FileUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class AddCoralUseCase(
    private val coralRepository: CoralRepository,
    private val fileUtils: FileUtils
) {

    data class AddCoralParams(
        val token: String,
        val name: String,
        val jenis: String,
        val harga: String,
        val stok: String,
        val imageUri: Uri
    )

    suspend fun execute(params: AddCoralParams): Result<String> {
        return try {
            // Convert URI to File
            val file = fileUtils.getFileFromUri(params.imageUri)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)

            // Create request bodies
            val nameBody = params.name.toRequestBody("text/plain".toMediaTypeOrNull())
            val jenisBody = params.jenis.toRequestBody("text/plain".toMediaTypeOrNull())
            val hargaBody = params.harga.toRequestBody("text/plain".toMediaTypeOrNull())
            val stokBody = params.stok.toRequestBody("text/plain".toMediaTypeOrNull())

            // Call repository
            coralRepository.addCoral(
                token = params.token,
                name = nameBody,
                jenis = jenisBody,
                harga = hargaBody,
                stok = stokBody,
                profilePicture = body
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}