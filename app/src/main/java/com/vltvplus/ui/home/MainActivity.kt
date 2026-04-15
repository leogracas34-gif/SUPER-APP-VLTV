package com.vltvplus.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.vltvplus.R
import com.vltvplus.databinding.ActivityMainBinding
import com.vltvplus.ui.auth.LoginActivity
import com.vltvplus.utils.DeviceUtils
import com.vltvplus.utils.RemoteKeyUtils
import com.vltvplus.utils.extensions.gone
import com.vltvplus.utils.extensions.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupUI()
        observeSync()
        triggerSync()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (DeviceUtils.isTV(this)) {
            binding.bottomNavigation.gone()
            binding.sideNavigation.show()
            binding.sideNavigation.setupWithNavController(navController)
        } else {
            binding.sideNavigation.gone()
            binding.bottomNavigation.show()
            binding.bottomNavigation.setupWithNavController(navController)
        }
    }

    private fun setupUI() {
        binding.ivSearch.setOnClickListener {
            navController.navigate(R.id.searchFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val showHeader = destination.id != R.id.playerFragment
            if (showHeader) binding.appBar.show() else binding.appBar.gone()
        }
    }

    private fun observeSync() {
        lifecycleScope.launch {
            viewModel.syncState.collect { state ->
                // Handle sync state if needed (show loading indicator on first load)
            }
        }
    }

    private fun triggerSync() {
        viewModel.syncContent()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (RemoteKeyUtils.isBack(keyCode)) {
            if (navController.currentDestination?.id != R.id.homeFragment) {
                navController.navigateUp()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun logout() {
        viewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
