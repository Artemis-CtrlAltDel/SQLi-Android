package com.example.myapplication.presentation.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.myapplication.databinding.FragmentFormBinding
import com.example.myapplication.other.Utils
import com.example.myapplication.presentation.viewmodels.SharedViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.*


class FormFragment : Fragment(){

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var imgLauncher: ActivityResultLauncher<Intent>
    private lateinit var imgTempFile: File
    private lateinit var imgSourceChooser: Intent
    private var imgUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFormBinding.inflate(layoutInflater, container, false)

        sharedViewModel.onFormValidated = { user ->
            val imgFile = File(requireContext().filesDir, "IMG_${Date().time}.png")
            imgTempFile.copyTo(imgFile)

            sharedViewModel.insertUser(user.apply { image = Uri.fromFile(imgFile) })

            Navigation.findNavController(binding.root)
                .navigate(FormFragmentDirections.actionFormFragmentToListFragment())
        }

        bindViews()
        setupImagePickerLauncher()
        handleActions()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun bindViews() {
        bindErrors()
        bindPreviews()
    }

    private fun handleActions() {
        binding.fab.setOnClickListener {

            bindErrors()

            sharedViewModel.validateForm(
                fname = binding.includePersonal.fname.text.toString().trim(),
                lname = binding.includePersonal.lname.text.toString().trim(),
                email = binding.includePersonal.email.text.toString().trim(),
                phone = binding.includePersonal.phone.text.toString().trim(),
                fax = binding.includePersonal.fax.text.toString().trim(),
                country = binding.includeAbout.country.text.toString().trim(),
                city = binding.includeAbout.city.text.toString().trim(),
                job = binding.includeAbout.job.text.toString().trim(),
                bio = binding.includeAbout.bio.text.toString().trim()
            )
        }

        binding.includeAbout.image.setOnClickListener {
            imgSourceChooser = Utils.createImageSourceChooser(requireContext(), imgTempFile)
            imgLauncher.launch(imgSourceChooser)
        }
    }

    private fun setupImagePickerLauncher() {
        imgTempFile = File(requireContext().cacheDir, "temp.png")
        imgLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode != Activity.RESULT_CANCELED) {
                val tempUri = Uri.fromFile(imgTempFile)
                var uri = it.data?.data ?: tempUri
                if(uri != null){
                    if (uri.path != tempUri.path){
                        requireActivity().contentResolver.openInputStream(uri).use { inputStream ->
                            FileOutputStream(File(imgTempFile.path)).use { outputStream ->
                                inputStream?.copyTo(outputStream)
                                outputStream.close()
                            }
                        }
                        uri = tempUri
                    }
                    imgUri = uri
                }
                bindPreviews()
            }
        }

    }

    private fun bindErrors() {
        sharedViewModel.errorMessage.observe(viewLifecycleOwner) {
            binding.includeError.errorWrapper.isVisible = it.isNotBlank()
            binding.includeError.error.text = it.trim()
        }
    }

    private fun bindPreviews() {
        binding.includeAbout.imagePreview.isVisible = imgUri.toString().isNotBlank()
        binding.includeAbout.imagePreview.setImageURI(imgUri)
    }
}