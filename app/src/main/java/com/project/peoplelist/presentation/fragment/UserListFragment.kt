package com.project.peoplelist.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.project.peoplelist.databinding.FragmentUserListBinding
import com.project.peoplelist.presentation.UserListAdapter
import com.project.peoplelist.presentation.UserLoadStateAdapter
import com.project.peoplelist.presentation.viewmodel.UserListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserListViewModel by viewModel()
    private lateinit var adapter: UserListAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()
        setupSwipeToRefresh()
        setupSearch()
        observeState()
        observeUsers()
        observeLoadState()
    }

    private fun setupAdapter() {
        adapter = UserListAdapter { user ->
            val action = UserListFragmentDirections.actionUserListToUserDetail(user.id)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        val loadStateAdapter = UserLoadStateAdapter {
            adapter.retry()
        }

        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@UserListFragment.adapter.withLoadStateFooter(loadStateAdapter)
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            adapter.refresh()
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener { text ->
            val query = text?.toString()?.trim() ?: ""
            viewModel.search(query)
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                // Handle error messages
                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                        .setAction("Tentar novamente") {
                            viewModel.retry()
                        }
                        .show()
                    viewModel.clearError()
                }

                // Handle cache messages
                state.cacheMessage?.let { message ->
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    viewModel.clearCacheMessage()
                }
            }
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->
                val error = loadState.refresh as? LoadState.Error
                error?.let {
                    viewModel.setError("Erro ao carregar usuários")
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    private fun observeLoadState() {
        viewLifecycleOwner.lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadState ->
                binding.swipeRefreshLayout.isRefreshing =
                    loadState.refresh is LoadState.Loading

                val refreshError = loadState.refresh as? LoadState.Error
                refreshError?.let {
                    Snackbar.make(binding.root, "Erro ao atualizar lista", Snackbar.LENGTH_LONG)
                        .setAction("Tentar novamente") {
                            adapter.refresh()
                        }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}