package ir.factory.entryexit.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LogDao {

    @Insert
    suspend fun insert(log: LogEntity)

    @Query("SELECT * FROM logs WHERE personId = :personId ORDER BY timestamp DESC")
    fun getLogsForPerson(personId: Long): LiveData<List<LogEntity>>

    @Query("SELECT * FROM logs WHERE type = :type ORDER BY timestamp DESC")
    fun getLogsByType(type: String): LiveData<List<LogEntity>>

    @Query("SELECT * FROM logs ORDER BY timestamp DESC")
    fun getAllLogs(): LiveData<List<LogEntity>>
}
