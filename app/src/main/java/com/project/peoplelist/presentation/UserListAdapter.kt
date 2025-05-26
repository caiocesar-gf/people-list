package com.project.peoplelist.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.project.core.User
import com.project.peoplelist.databinding.ItemUserBinding

class UserListAdapter(
    private val onUserClick: (User) -> Unit
) : PagingDataAdapter<User, UserListAdapter.UserViewHolder>(USER_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        if (user != null) {
            holder.bind(user)
        }
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.apply {
                // Informações pessoais
                textUserName.text = user.name
                textUserUsername.text = "@${user.username}"
                textUserEmail.text = user.email
                textUserPhone.text = user.phone
                textUserWebsite.text = user.website

                // Endereço completo
                val fullAddress = "${user.address.street}, ${user.address.suite}\n" +
                        "${user.address.city}, ${user.address.zipcode}"
                textUserAddress.text = fullAddress

                // Informações da empresa
                textCompanyName.text = user.company.name
                textCompanyCatchPhrase.text = user.company.catchPhrase
                textCompanyBs.text = user.company.bs

                // Click listener
                root.setOnClickListener {
                    onUserClick(user)
                }
            }
        }
    }

    companion object {
        private val USER_COMPARATOR = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }
}