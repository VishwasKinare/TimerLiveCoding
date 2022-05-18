package com.example.livecoding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.livecoding.Constants.ACTION_STOP_SERVICE
import com.example.livecoding.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isTracking = false
    private var currentTimeMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setClickListeners()
        setObseervers()
    }

    private fun setObseervers(){
        CountDownTimerService.isTracking.observe(this) {
            updateTracking(it)
        }

        CountDownTimerService.timeRunInMillis.observe(this) {
            currentTimeMillis = it
            updateTime(currentTimeMillis)
        }
    }

    private fun updateTracking(isTracking: Boolean){
        this.isTracking = isTracking
        if (!isTracking){
            showStartView()
            if (currentTimeMillis > 0L)
                showStartView()
        } else {
            showStopView()
        }
    }

    private fun setClickListeners(){
        binding.startBtn.setOnClickListener {
            if (validateField()){
                toggleTime()
//                startCountDownTimer()
//                showStopView()
            }
        }

        binding.stopBtn.setOnClickListener {
            sendCommandToService(ACTION_STOP_SERVICE)
            showStartView()
        }
    }

    private fun toggleTime(){
        if (isTracking){
            showStopView()
            sendCommandToService(Constants.ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun sendCommandToService(action: String){
        Intent(this, CountDownTimerService::class.java).also {
            it.action = action
            if (binding.time.text.toString().trim().isNotEmpty())
                it.putExtra("Time", (binding.time.text.toString().trim().toInt() * 60000).toLong())
           startService(it)
        }
    }

//    private var timer: CountDownTimer? = null
//    private fun startCountDownTimer(){
//        val time = binding.time.text.toString().trim().toInt()
//        timer = object : CountDownTimer((time * 60000).toLong(), 1000){
//            override fun onTick(millisUntilFinished: Long) {
//                updateTime(millisUntilFinished)
//            }
//
//            override fun onFinish() {
//                showStartView()
//            }
//        }
//        timer?.start()
//    }

    private fun updateTime(millisUntilFinished: Long){
        val seconds = (millisUntilFinished / 1000) % 60
        val minutes = (millisUntilFinished / (1000 * 60) % 60)
        binding.countDownTv.text = "$minutes:$seconds"
    }

    private fun validateField(): Boolean{
        var isValid = true
        binding.timeLayout.error = null
        if (binding.time.text.toString().isEmpty()){
            binding.timeLayout.error = "Enter Time"
            isValid = false
        }
        return isValid
    }

    private fun showStartView(){
        binding.timeLayout.visibility = View.VISIBLE
        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.countDownTv.visibility = View.GONE
    }

    private fun showStopView(){
        binding.timeLayout.visibility = View.GONE
        binding.startBtn.visibility = View.GONE
        binding.stopBtn.visibility = View.VISIBLE
        binding.countDownTv.visibility = View.VISIBLE
    }
}