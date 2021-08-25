package com.shahjahan.runningapp.ui.viewmodels.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.shahjahan.runningapp.R
import com.shahjahan.runningapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run) {

    private val viewModel: MainViewModel by viewModels();

}