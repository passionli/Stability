package com.example.stability.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stability.data_structures.DataStructuresMain
import com.example.stability.kotlin_learning.KotlinLearningMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel : ViewModel() {

    fun runKotlinLearningExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val kotlinLearningMain = KotlinLearningMain()
            kotlinLearningMain.runAllExamples()
        }
    }

    fun runDataStructuresExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val dataStructuresMain = DataStructuresMain()
            dataStructuresMain.runAllExamples()
        }
    }

    fun runBasicThreadExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val basicExample = com.example.stability.multithreading.basic.BasicThreadExample()
            basicExample.runAllExamples()
        }
    }

    fun runIntermediateThreadExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val intermediateExample = com.example.stability.multithreading.intermediate.IntermediateThreadExample()
            intermediateExample.runAllExamples()
        }
    }

    fun runAdvancedThreadExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val advancedExample = com.example.stability.multithreading.advanced.AdvancedThreadExample()
            advancedExample.runAllExamples()
        }
    }

    fun runBasicCppExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val basicExample = com.example.stability.cpp.basic.BasicCppExample()
            basicExample.runAllExamples()
        }
    }

    fun runIntermediateCppExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val intermediateExample = com.example.stability.cpp.intermediate.IntermediateCppExample()
            intermediateExample.runAllExamples()
        }
    }

    fun runAdvancedCppExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val advancedExample = com.example.stability.cpp.advanced.AdvancedCppExample()
            advancedExample.runAllExamples()
        }
    }

    fun runBasicCExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val basicExample = com.example.stability.c.basic.BasicCExample()
            basicExample.runAllExamples()
        }
    }

    fun runIntermediateCExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val intermediateExample = com.example.stability.c.intermediate.IntermediateCExample()
            intermediateExample.runAllExamples()
        }
    }

    fun runAdvancedCExamples() {
        viewModelScope.launch(Dispatchers.IO) {
            val advancedExample = com.example.stability.c.advanced.AdvancedCExample()
            advancedExample.runAllExamples()
        }
    }
}
