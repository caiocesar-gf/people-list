package com.project.peoplelist.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            performRefresh()
        }
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                updateUI(state)
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

    private fun updateUI(state: UserListState) {
        // Atualiza SwipeRefreshLayout
        binding.swipeRefreshLayout.isRefreshing = state.isLoading

        // Mostra erro se houver
        state.error?.let { error ->
            Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG)
                .setAction("Tentar novamente") {
                    viewModel.retry()
                }
                .show()
            viewModel.clearError()
        }
    }

    private fun performRefresh() {
        viewModel.refresh()
        adapter.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}