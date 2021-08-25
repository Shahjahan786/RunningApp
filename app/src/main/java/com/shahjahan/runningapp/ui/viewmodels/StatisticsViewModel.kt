package com.shahjahan.runningapp.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.shahjahan.runningapp.repositories.MainRepository

class StatisticsViewModel @ViewModelInject constructor(
    var mainRepository: MainRepository
) : ViewModel(){

}