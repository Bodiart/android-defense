package com.bodiart.defense.features.main

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bodiart.defense.R
import com.bodiart.defense.databinding.ActivityMainBinding
import com.bodiart.defense.features.main.adapter.StatisticItem
import com.bodiart.defense.features.main.adapter.StatisticsAdapter
import com.bodiart.defense.model.entity.AttacksStatistic
import com.bodiart.defense.model.settings.AttackMode

class MainActivity : AppCompatActivity(), MainPresenter.View {

    private lateinit var binding: ActivityMainBinding
    private val presenter by lazy { MainPresenter() }


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
            changeSettingsBtn.setOnClickListener { presenter.changeSettingsClicked() }
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

    override fun showStatistic(statistic: AttacksStatistic?) {
        binding.statisticInclude.run {
            this.root.isVisible = statistic != null

            if (statistic == null) {
                return
            }

            website.text = getString(R.string.main_statistic_website, statistic.url)
            attacksCount.text = getString(
                R.string.main_statistic_attacks_count,
                statistic.getStatisticsAttacks().size.toString()
            )
            successAttacks.text = getString(
                R.string.main_statistic_success_attacks,
                statistic.getStatisticsAttacks().filter { it.isSuccess }.size.toString()
            )
            failAttacks.text = getString(
                R.string.main_statistic_fail_attacks,
                statistic.getStatisticsAttacks().filter { !it.isSuccess }.size.toString()
            )
        }
    }

    override fun setSettingsModeName(nameResId: Int) {
        binding.changeSettingsBtn.text = getString(R.string.main_settings, getString(nameResId))
    }

    override fun showChangeSettingsDialog(items: List<AttackMode>, checkedItemIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.main_settings_dialog_title)
            .setSingleChoiceItems(
                items.map { getString(it.nameResId) }.toTypedArray(),
                checkedItemIndex
            ) { dialog, which ->
                dialog.dismiss()
                presenter.settingsModeSelected(items[which])
            }
            .show()
    }
}