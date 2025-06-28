package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

// UBAH DARI 'class' MENJADI 'interface'
interface LokasiClickListener {
    // UBAH DARI 'val' DI CONSTRUCTOR MENJADI 'fun'
    fun onEditClick(lokasi: Lokasi)
    fun onMapsClick(lokasi: Lokasi)
    // Anda bisa menambahkan fungsi lain di sini jika perlu
}