package ir.factory.entryexit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.factory.entryexit.R
import ir.factory.entryexit.data.PersonEntity
import ir.factory.entryexit.data.PersonType
import ir.factory.entryexit.databinding.ItemPersonBinding

class PersonAdapter(
    private val type: PersonType,
    private val onClick: (PersonEntity) -> Unit
) : ListAdapter<PersonEntity, PersonAdapter.PersonViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        val binding = ItemPersonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PersonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonViewHolder(private val binding: ItemPersonBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(person: PersonEntity) {
            val context = binding.root.context
            binding.tvName.text = person.name

            val iconRes = when (type) {
                PersonType.PERSONNEL -> R.drawable.ic_personnel
                PersonType.MACHINERY -> R.drawable.ic_machinery
                PersonType.VISITOR -> R.drawable.ic_visitor
                PersonType.DRIVER -> R.drawable.ic_driver
            }
            binding.ivTypeIcon.setImageResource(iconRes)

            binding.tvSubtitle.text = person.extraInfo?.takeIf { it.isNotBlank() }
                ?: context.getString(
                    R.string.last_status_format,
                    if (person.isInside) context.getString(R.string.status_inside)
                    else context.getString(R.string.status_outside)
                )

            if (person.isInside) {
                binding.tvStatusBadge.text = context.getString(R.string.status_inside)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_inside)
                binding.tvStatusBadge.setTextColor(context.getColor(R.color.status_green))
            } else {
                binding.tvStatusBadge.text = context.getString(R.string.status_outside)
                binding.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_outside)
                binding.tvStatusBadge.setTextColor(context.getColor(R.color.concrete_500))
            }

            binding.root.setOnClickListener { onClick(person) }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PersonEntity>() {
            override fun areItemsTheSame(oldItem: PersonEntity, newItem: PersonEntity) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: PersonEntity, newItem: PersonEntity) =
                oldItem == newItem
        }
    }
}
