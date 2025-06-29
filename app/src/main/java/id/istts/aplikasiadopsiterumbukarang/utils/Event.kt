// File: utils/Event.kt

package id.istts.aplikasiadopsiterumbukarang.utils

/**
 * Wrapper untuk data yang diobservasi melalui LiveData yang merepresentasikan sebuah event.
 * Ini memastikan event hanya ditangani sekali.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Hanya bisa dibaca dari luar, tidak bisa diubah

    /**
     * Mengembalikan konten dan mencegahnya digunakan kembali.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Mengembalikan konten, bahkan jika sudah ditangani sebelumnya.
     */
    fun peekContent(): T = content
}