package ir.factory.entryexit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A registered person / machine / driver / visitor.
 * [isInside] is the single source of truth for whether they are currently inside the factory;
 * it is what enforces the "no duplicate check-in" business rule.
 */
@Entity(tableName = "persons")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // matches PersonType.name
    val extraInfo: String? = null, // e.g. plate number for machinery, phone for drivers
    val isInside: Boolean = false
)
