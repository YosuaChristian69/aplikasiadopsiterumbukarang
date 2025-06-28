    package id.istts.aplikasiadopsiterumbukarang.presentation.viewmodels.admin.placeDashboard

    import id.istts.aplikasiadopsiterumbukarang.domain.models.Lokasi

    data class AdminPlaceDashboardUiState(
        val isLoading: Boolean = true,
        val userName: String = "Admin",
        val allLokasi: List<Lokasi> = emptyList(), // Daftar asli dari API
        val displayedLokasi: List<Lokasi> = emptyList(), // Daftar yang ditampilkan setelah di-filter
        val totalPlacesCount: Int = 0,
        val activePlacesCount: Int = 0, // Anda bisa tambahkan logika untuk ini nanti
        val errorMessage: String? = null
    )