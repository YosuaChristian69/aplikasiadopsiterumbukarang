package id.istts.aplikasiadopsiterumbukarang.domain.models.worker

data class UnfinishedTasksResponse(
    val msg: String,
    val allUnfinishedHt: List<TaskItem>
)
