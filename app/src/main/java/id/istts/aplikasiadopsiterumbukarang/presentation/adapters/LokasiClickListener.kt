package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

class LokasiClickListener(
    val onEditClick: (lokasi: Lokasi) -> Unit,
    val onMapsClick: (lokasi: Lokasi) -> Unit
    // Tambahkan listener lain jika perlu, misal onDelete
)