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
    private var navController: NavController? = null 
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
        // Correção de segurança: tenta encontrar o NavHost sem forçar o fechamento se houver atraso na inflação
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        
        navController = navHostFragment?.navController

        navController?.let { controller ->
            // Verifica se os componentes de UI estão presentes no layout antes de configurar
            if (DeviceUtils.isTV(this)) {
                binding.bottomNavigation.gone()
                binding.sideNavigation.show()
                binding.sideNavigation.setupWithNavController(controller)
            } else {
                binding.sideNavigation.gone()
                binding.bottomNavigation.show()
                binding.bottomNavigation.setupWithNavController(controller)
            }
        }
    }

    private fun setupUI() {
        binding.ivSearch.setOnClickListener {
            navController?.navigate(R.id.searchFragment)
        }

        navController?.addOnDestinationChangedListener { _, destination, _ ->
            // Mantém a barra de topo visível em tudo, exceto no Player (Layout Premium)
            val showHeader = destination.id != R.id.playerFragment
            if (showHeader) {
                binding.appBar.show()
            } else {
                binding.appBar.gone()
            }
        }
    }

    private fun observeSync() {
        lifecycleScope.launch {
            viewModel.syncState.collect { _ ->
                // Observando estado de sincronização com segurança
            }
        }
    }

    private fun triggerSync() {
        // Dispara a sincronização de conteúdo ao abrir a Home
        viewModel.syncContent()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Tratamento personalizado para o botão de voltar em controles remotos de TV
        if (RemoteKeyUtils.isBack(keyCode)) {
            if (navController?.currentDestination?.id != R.id.homeFragment) {
                navController?.navigateUp()
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
