package ir.factory.entryexit.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.factory.entryexit.data.PersonType
import ir.factory.entryexit.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tilePersonnel.setOnClickListener { openCategory(PersonType.PERSONNEL) }
        binding.tileMachinery.setOnClickListener { openCategory(PersonType.MACHINERY) }
        binding.tileVisitor.setOnClickListener { openCategory(PersonType.VISITOR) }
        binding.tileDriver.setOnClickListener { openCategory(PersonType.DRIVER) }
    }

    private fun openCategory(type: PersonType) {
        val intent = Intent(this, CategoryActivity::class.java)
            .putExtra(CategoryActivity.EXTRA_TYPE, type.name)
        startActivity(intent)
    }
}
