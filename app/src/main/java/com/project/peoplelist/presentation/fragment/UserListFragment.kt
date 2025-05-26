package com.project.peoplelist.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.project.peoplelist.databinding.FragmentUserListBinding
import com.project.peoplelist.presentation.UserListAdapter
import com.project.peoplelist.presentation.viewmodel.UserListViewModel
import com.project.peoplelist.presentation.viewmodel.UserListState
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
    }

    private fun setupAdapter() {
        adapter = UserListAdapter { user ->
            val action = UserListFragmentDirections.actionUserListToUserDetail(user.id)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        binding.userRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@UserListFragment.adapter
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
                binding.swipeRefreshLayout.isRefreshing = state.isLoading

                state.error?.let { error ->
                    Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                        .setAction("Tentar novamente") {
                            viewModel.retry()
                        }
                        .show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun observeUsers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}