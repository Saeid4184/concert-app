package ir.factory.entryexit.data

import androidx.lifecycle.LiveData

/**
 * Single place where the app's core rule lives:
 * a person cannot be checked in again until their previous check-in has been checked out.
 */
class Repository(private val personDao: PersonDao, private val logDao: LogDao) {

    fun getPersonsByType(type: PersonType): LiveData<List<PersonEntity>> =
        personDao.getByType(type.name)

    fun getInsidePersonsByType(type: PersonType): LiveData<List<PersonEntity>> =
        personDao.getInsideByType(type.name)

    fun getLogsForPerson(personId: Long): LiveData<List<LogEntity>> =
        logDao.getLogsForPerson(personId)

    /** Registers a brand-new person/machine/driver/visitor (name-only registration). */
    suspend fun addPerson(name: String, type: PersonType, extraInfo: String? = null): Result<Long> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("نام نمی‌تواند خالی باشد"))
        }
        if (personDao.countByNameAndType(type.name, trimmed) > 0) {
            return Result.failure(IllegalStateException("این نام قبلاً ثبت شده است"))
        }
        val id = personDao.insert(
            PersonEntity(name = trimmed, type = type.name, extraInfo = extraInfo?.trim()?.ifEmpty { null })
        )
        return Result.success(id)
    }

    /**
     * Check a person **in**. Fails if they are already marked as inside — this is what
     * prevents duplicate/erroneous consecutive check-ins.
     */
    suspend fun checkIn(personId: Long, department: String? = null): Result<Unit> {
        val fresh = personDao.getById(personId)
            ?: return Result.failure(IllegalStateException("فرد یافت نشد"))

        if (fresh.isInside) {
            return Result.failure(
                IllegalStateException("${fresh.name} قبلاً ورود ثبت کرده و هنوز خروج نزده است")
            )
        }

        personDao.update(fresh.copy(isInside = true))
        logDao.insert(
            LogEntity(
                personId = fresh.id,
                personName = fresh.name,
                type = fresh.type,
                action = ACTION_IN,
                timestamp = System.currentTimeMillis(),
                department = department?.trim()?.ifEmpty { null }
            )
        )
        return Result.success(Unit)
    }

    /**
     * Check a person **out**. Fails if they are not currently inside. On success the person
     * is removed from the "currently inside" list.
     */
    suspend fun checkOut(personId: Long): Result<Unit> {
        val fresh = personDao.getById(personId)
            ?: return Result.failure(IllegalStateException("فرد یافت نشد"))

        if (!fresh.isInside) {
            return Result.failure(IllegalStateException("${fresh.name} ورودی ثبت‌شده‌ای ندارد"))
        }

        personDao.update(fresh.copy(isInside = false))
        logDao.insert(
            LogEntity(
                personId = fresh.id,
                personName = fresh.name,
                type = fresh.type,
                action = ACTION_OUT,
                timestamp = System.currentTimeMillis()
            )
        )
        return Result.success(Unit)
    }

    /**
     * One-step flow for a guest: register the visitor by name (if not already present in this
     * visit) and immediately check them in against the department they are visiting.
     */
    suspend fun checkInVisitor(name: String, department: String): Result<Unit> {
        val trimmedName = name.trim()
        val trimmedDept = department.trim()
        if (trimmedName.isEmpty()) {
            return Result.failure(IllegalArgumentException("نام مهمان نمی‌تواند خالی باشد"))
        }
        if (trimmedDept.isEmpty()) {
            return Result.failure(IllegalArgumentException("وارد کردن واحد مورد مراجعه الزامی است"))
        }
        val id = personDao.insert(PersonEntity(name = trimmedName, type = PersonType.VISITOR.name))
        return checkIn(id, trimmedDept)
    }

    companion object {
        const val ACTION_IN = "IN"
        const val ACTION_OUT = "OUT"
    }
}
