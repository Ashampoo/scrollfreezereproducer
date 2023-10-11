@Suppress("TooManyFunctions")
data class Photo(

    val id: Long?,

    val uri: String

) {

    fun getIdNotNull(): Long = requireNotNull(id) { "Photo id was null." }

}