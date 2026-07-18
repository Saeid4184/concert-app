package ir.factory.entryexit.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PersonDao {

    @Insert
    suspend fun insert(person: PersonEntity): Long

    @Update
    suspend fun update(person: PersonEntity)

    @Query("SELECT * FROM persons WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): LiveData<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE type = :type AND isInside = 1 ORDER BY name ASC")
    fun getInsideByType(type: String): LiveData<List<PersonEntity>>

    @Query("SELECT * FROM persons WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PersonEntity?

    @Query("SELECT COUNT(*) FROM persons WHERE type = :type AND name = :name")
    suspend fun countByNameAndType(type: String, name: String): Int
}
