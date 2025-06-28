package id.istts.aplikasiadopsiterumbukarang.RepositoryDontTouch.Sources.Local.Entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import id.istts.aplikasiadopsiterumbukarang.domain.models.Worker
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//@Parcelize
@Entity(tableName = "workers")
data class WorkerEntities(
    @PrimaryKey(autoGenerate = false)
    val id_user: String,
    val full_name: String,
    val email: String,
    val password:String?=null,
    val status: String,
    val balance: String,
    val user_status:String,
    val joined_at: String?,
    val img_path:String="",
    val public_id:String="",
    val is_updated_locally: Boolean? = false
) {
    companion object{
//        val dateFormate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        fun fromWorker(worker: Worker): WorkerEntities {
            return WorkerEntities(
                id_user = worker.id_user,
                full_name = worker.full_name,
                email = worker.email,
                status = worker.status,
                balance = worker.balance,
                user_status = worker.user_status,
                joined_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(worker.joined_at),
                img_path = worker.img_path ?: "",
                public_id = worker.public_id ?: "",
            )
        }
    }

    fun toWorker(): Worker {
        return Worker(
            id_user = id_user,
            full_name = full_name,
            email = email,
            status = status,
            balance = balance,
            user_status = user_status,
            joined_at = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(joined_at),
            img_path = img_path,
            public_id = public_id
        )
    }

}