package com.project.peoplelist.presentation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.peoplelist.databinding.ItemLoadStateBinding

class UserLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<UserLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val binding = ItemLoadStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LoadStateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    inner class LoadStateViewHolder(
        private val binding: ItemLoadStateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            binding.apply {
                // Mostra loading enquanto carrega
                progressBar.isVisible = loadState is LoadState.Loading
                loadingText.isVisible = loadState is LoadState.Loading

                retryButton.isVisible = loadState is LoadState.Error

                errorText.isVisible = loadState is LoadState.Error
                if (loadState is LoadState.Error) {
                    errorText.text = "Erro ao carregar próxima página"
                }

                // Configura clique do retry
                retryButton.setOnClickListener {
                    retry()
                }

                root.setOnClickListener {
                    if (loadState is LoadState.Error) {
                        retry()
                    }
                }
            }
        }
    }
}