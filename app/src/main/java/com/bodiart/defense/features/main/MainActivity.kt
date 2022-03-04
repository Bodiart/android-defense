package com.bodiart.defense.features.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodiart.defense.databinding.ActivityMainBinding
import com.bodiart.defense.features.main.adapter.StatisticItem
import com.bodiart.defense.features.main.adapter.StatisticsAdapter

class MainActivity : AppCompatActivity(), MainPresenter.View {

    private lateinit var binding: ActivityMainBinding
    private val presenter by lazy { MainPresenter() }

    private val statisticsAdapter by lazy { StatisticsAdapter() }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        presenter.attachView(this)

        setupUi()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    private fun setupUi() {
        binding.run {
            attackBtn.setOnClickListener { presenter.attackBtnClicked() }

            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
            recyclerView.adapter = statisticsAdapter
        }
    }

    override fun setAttackBtnText(textResId: Int) {
        binding.attackBtn.setText(textResId)
    }

    override fun showError(errorResId: Int) {
        Toast.makeText(this, errorResId, Toast.LENGTH_SHORT).show()
    }

    override fun setLoading(isLoadingVisible: Boolean) {
        binding.progressBar.isVisible = isLoadingVisible
    }

    override fun showStatistics(statistics: List<StatisticItem>) {
        statisticsAdapter.submitList(statistics)
    }
}