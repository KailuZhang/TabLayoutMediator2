package io.kailuzhang.github.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.kailuzhang.github.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button1.setOnClickListener {
            NoOffsetActivity.start(this)
        }

        binding.button2.setOnClickListener {
            WithOffsetActivity.start(this)
        }
    }
}