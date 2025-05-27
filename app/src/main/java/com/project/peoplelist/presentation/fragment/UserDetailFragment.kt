package com.project.peoplelist.presentation.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.project.core.User
import com.project.peoplelist.databinding.FragmentUserDetailBinding
import com.project.peoplelist.presentation.viewmodel.UserDetailViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    private val binding get() = _binding!!

    private val args: UserDetailFragmentArgs by navArgs()
    private val viewModel: UserDetailViewModel by viewModel { parametersOf(args.userId) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUser()
        setupClickListeners()
    }

    private fun observeUser() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { bindUserData(it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                // TODO: Show error message
                Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
            }
        }

        // Observer para mensagens de cache
        viewModel.cacheMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.clearCacheMessage()
            }
        }
    }

    private fun bindUserData(user: User) {
        binding.apply {
            // Personal Information
            textUserName.text = user.name
            textUserUsername.text = "@${user.username}"
            textUserEmail.text = user.email
            textUserPhone.text = user.phone
            textUserWebsite.text = user.website

            // Address Information
            textAddressStreet.text = "${user.address.street}, ${user.address.suite}"
            textAddressCity.text = user.address.city
            textAddressZipcode.text = user.address.zipcode
            textAddressCoordinates.text = "Lat: ${user.address.geo.lat}, Lng: ${user.address.geo.lng}"

            // Company Information
            textCompanyName.text = user.company.name
            textCompanyCatchPhrase.text = user.company.catchPhrase
            textCompanyBs.text = user.company.bs
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            cardEmail.setOnClickListener {
                val user = viewModel.user.value
                user?.let { openEmail(it.email) }
            }

            cardPhone.setOnClickListener {
                val user = viewModel.user.value
                user?.let { openPhone(it.phone) }
            }

            cardWebsite.setOnClickListener {
                val user = viewModel.user.value
                user?.let { openWebsite(it.website) }
            }

            cardAddress.setOnClickListener {
                val user = viewModel.user.value
                user?.let { openMaps(it.address.geo.lat, it.address.geo.lng) }
            }
        }
    }

    private fun openEmail(email: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        startActivity(Intent.createChooser(intent, "Enviar email"))
    }

    private fun openPhone(phone: String) {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$phone")
        }
        startActivity(intent)
    }

    private fun openWebsite(website: String) {
        val url = if (website.startsWith("http")) website else "http://$website"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun openMaps(lat: String, lng: String) {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}