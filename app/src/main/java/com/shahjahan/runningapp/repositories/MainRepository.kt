package com.shahjahan.runningapp.repositories

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.shahjahan.runningapp.db.Run
import com.shahjahan.runningapp.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    val runDAO: RunDAO
) {

    suspend fun insertRun(run: Run) = runDAO.insertRun(run)
    suspend fun deleteRun(run: Run) = runDAO.deleteRun(run)


    fun getAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate();


    fun getAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis();


    fun getAllRunsSortedByCaloriesBurned() = runDAO.getAllRunsSortedByCaloriesBurned();


    fun getAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed();


    fun getAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance();


    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis();


    fun getTotalCaloriesBurned() = runDAO.getTotalCaloriesBurned();


    fun getTotalDistance() = runDAO.getTotalDistance();


    fun getTotalAvgSpeed() = runDAO.getTotalAvgSpeed();
}