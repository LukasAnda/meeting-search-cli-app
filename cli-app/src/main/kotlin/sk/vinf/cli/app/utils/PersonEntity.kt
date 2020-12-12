package sk.vinf.cli.app.utils

data class PersonEntity(
    val name: String,
    val birthTimestamp: Long,
    val deathTimestamp: Long
){
    fun hasMet(other: PersonEntity): Boolean{
        return this.birthTimestamp in other.birthTimestamp..other.deathTimestamp ||
                other.birthTimestamp in this.birthTimestamp..this.deathTimestamp
    }
}