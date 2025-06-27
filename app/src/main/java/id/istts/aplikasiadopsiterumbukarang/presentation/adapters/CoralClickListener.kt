package id.istts.aplikasiadopsiterumbukarang.presentation.adapters

import id.istts.aplikasiadopsiterumbukarang.domain.models.Coral

class CoralClickListener(
    val itemClickListener: (coral: Coral) -> Unit,
    val editClickListener: (coral: Coral) -> Unit,
    val deleteClickListener: (coral: Coral) -> Unit
) {
    fun onItemClick(coral: Coral) = itemClickListener(coral)
    fun onEditClick(coral: Coral) = editClickListener(coral)
    fun onDeleteClick(coral: Coral) = deleteClickListener(coral)
}