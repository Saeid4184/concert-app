package ir.factory.entryexit.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ir.factory.entryexit.data.AppDatabase
import ir.factory.entryexit.data.PersonEntity
import ir.factory.entryexit.data.PersonType
import ir.factory.entryexit.data.Repository
import kotlinx.coroutines.launch

class CategoryViewModel(app: Application, private val type: PersonType) : AndroidViewModel(app) {

    private val repository: Repository = run {
        val db = AppDatabase.getInstance(app)
        Repository(db.personDao(), db.logDao())
    }

    /** All registered entries for this category (used by personnel/machinery/driver screens). */
    val allPersons: LiveData<List<PersonEntity>> = repository.getPersonsByType(type)

    /** Only the ones currently inside (used by the visitor screen, and the "inside count" badge). */
    val insidePersons: LiveData<List<PersonEntity>> = repository.getInsidePersonsByType(type)

    fun addPerson(name: String, extraInfo: String?, onResult: (Result<Long>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.addPerson(name, type, extraInfo))
        }
    }

    fun checkIn(personId: Long, department: String? = null, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.checkIn(personId, department))
        }
    }

    fun checkOut(personId: Long, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.checkOut(personId))
        }
    }

    fun checkInVisitor(name: String, department: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.checkInVisitor(name, department))
        }
    }

    class Factory(private val app: Application, private val type: PersonType) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CategoryViewModel(app, type) as T
        }
    }
}
