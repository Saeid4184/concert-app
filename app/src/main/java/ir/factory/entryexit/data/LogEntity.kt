package ir.factory.entryexit.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * An immutable historical record of a single check-in or check-out event.
 * The [personName]/[type] are denormalized (copied at the time of the event) so that
 * history remains readable/accurate even if the person record changes later.
 */
@Entity(tableName = "logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val personName: String,
    val type: String,
    val action: String, // "IN" or "OUT"
    val timestamp: Long,
    val department: String? = null // only used for VISITOR check-ins
)
