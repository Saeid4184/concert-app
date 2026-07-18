package ir.factory.entryexit.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.factory.entryexit.R
import ir.factory.entryexit.data.PersonEntity
import ir.factory.entryexit.data.PersonType
import ir.factory.entryexit.databinding.ActivityCategoryBinding
import ir.factory.entryexit.databinding.DialogAddPersonBinding
import ir.factory.entryexit.databinding.DialogVisitorCheckinBinding
import ir.factory.entryexit.viewmodel.CategoryViewModel

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private lateinit var viewModel: CategoryViewModel
    private lateinit var type: PersonType
    private lateinit var adapter: PersonAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        type = PersonType.valueOf(
            intent.getStringExtra(EXTRA_TYPE) ?: PersonType.PERSONNEL.name
        )

        viewModel = ViewModelProvider(
            this,
            CategoryViewModel.Factory(application, type)
        )[CategoryViewModel::class.java]

        setupToolbar()
        setupList()
        setupFab()
        observeData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = type.displayName
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupList() {
        adapter = PersonAdapter(type) { person -> onPersonClicked(person) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupFab() {
        if (type == PersonType.VISITOR) {
            binding.fabAdd.text = getString(R.string.new_visitor_checkin_title)
            binding.fabAdd.setOnClickListener { showVisitorCheckInDialog() }
        } else {
            binding.fabAdd.text = getString(R.string.add_new)
            binding.fabAdd.setOnClickListener { showAddPersonDialog() }
        }
    }

    private fun observeData() {
        // Visitors only ever show the list of people currently inside (transient guests);
        // the other categories show the full registered roster with live in/out status.
        val listSource = if (type == PersonType.VISITOR) viewModel.insidePersons else viewModel.allPersons

        listSource.observe(this) { list ->
            adapter.submitList(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            binding.tvEmpty.text = getString(
                if (type == PersonType.VISITOR) R.string.empty_list_visitor else R.string.empty_list_personnel
            )
        }

        viewModel.insidePersons.observe(this) { insideList ->
            binding.tvInsideCount.text = getString(R.string.inside_count_format, insideList.size)
        }
    }

    private fun onPersonClicked(person: PersonEntity) {
        if (type == PersonType.VISITOR) {
            // Visitors only appear in this list while inside, so a tap always means "check out".
            confirmCheckOut(person)
        } else {
            showCheckInOutDialog(person)
        }
    }

    private fun showCheckInOutDialog(person: PersonEntity) {
        val items = arrayOf(getString(R.string.btn_checkin), getString(R.string.btn_checkout))

        MaterialAlertDialogBuilder(this)
            .setTitle(person.name)
            .setItems(items) { _, which ->
                if (which == 0) {
                    // The repository is the single source of truth for the "already inside" rule;
                    // if the person is already checked in, checkIn() fails and we surface its message.
                    viewModel.checkIn(person.id) { result -> handleResult(result, R.string.checkin_success) }
                } else {
                    confirmCheckOut(person)
                }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun confirmCheckOut(person: PersonEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.btn_checkout))
            .setMessage(getString(R.string.confirm_checkout_message, person.name))
            .setPositiveButton(R.string.btn_confirm_checkout) { _, _ ->
                viewModel.checkOut(person.id) { result -> handleResult(result, R.string.checkout_success) }
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }

    private fun showAddPersonDialog() {
        val dialogBinding = DialogAddPersonBinding.inflate(LayoutInflater.from(this))
        dialogBinding.tilExtraInfo.hint = when (type) {
            PersonType.MACHINERY -> getString(R.string.hint_extra_info_machinery)
            PersonType.DRIVER -> getString(R.string.hint_extra_info_driver)
            else -> getString(R.string.hint_extra_info)
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_new_person_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_save, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etName.text?.toString().orEmpty()
                if (name.isBlank()) {
                    dialogBinding.tilName.error = getString(R.string.error_name_empty)
                    return@setOnClickListener
                }
                val extra = dialogBinding.etExtraInfo.text?.toString()
                viewModel.addPerson(name, extra) { result ->
                    result.onSuccess {
                        toast(getString(R.string.person_added_success))
                        dialog.dismiss()
                    }.onFailure { error ->
                        dialogBinding.tilName.error = error.message ?: getString(R.string.error_generic)
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showVisitorCheckInDialog() {
        val dialogBinding = DialogVisitorCheckinBinding.inflate(LayoutInflater.from(this))

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.new_visitor_checkin_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.btn_checkin, null)
            .setNegativeButton(R.string.btn_cancel, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etVisitorName.text?.toString().orEmpty()
                val department = dialogBinding.etDepartment.text?.toString().orEmpty()

                var hasError = false
                if (name.isBlank()) {
                    dialogBinding.tilVisitorName.error = getString(R.string.error_name_empty)
                    hasError = true
                } else {
                    dialogBinding.tilVisitorName.error = null
                }
                if (department.isBlank()) {
                    dialogBinding.tilDepartment.error = getString(R.string.error_department_empty)
                    hasError = true
                } else {
                    dialogBinding.tilDepartment.error = null
                }
                if (hasError) return@setOnClickListener

                viewModel.checkInVisitor(name, department) { result ->
                    result.onSuccess {
                        toast(getString(R.string.checkin_success))
                        dialog.dismiss()
                    }.onFailure { error ->
                        toast(error.message ?: getString(R.string.error_generic))
                    }
                }
            }
        }
        dialog.show()
    }

    private fun handleResult(result: Result<Unit>, @androidx.annotation.StringRes successMessage: Int) {
        result.onSuccess {
            toast(getString(successMessage))
        }.onFailure { error ->
            toast(error.message ?: getString(R.string.error_generic))
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_TYPE = "extra_type"
    }
}
