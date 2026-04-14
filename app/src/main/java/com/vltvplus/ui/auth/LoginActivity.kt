package com.vltvplus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.vltvplus.R
import com.vltvplus.databinding.ActivityLoginBinding
import com.vltvplus.ui.home.MainActivity
import com.vltvplus.utils.Resource
import com.vltvplus.utils.extensions.gone
import com.vltvplus.utils.extensions.show
import com.vltvplus.utils.extensions.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeState()
        animateEntrance()
    }

    private fun setupUI() {
        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.etPassword.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                attemptLogin()
                true
            } else false
        }

        // TV remote D-pad focus chain
        binding.etUsername.nextFocusDownId = R.id.et_password
        binding.etPassword.nextFocusDownId = R.id.btn_login
        binding.btnLogin.nextFocusUpId = R.id.et_password
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (username.isEmpty()) {
            binding.tilUsername.error = "Digite seu usuário"
            binding.etUsername.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Digite sua senha"
            binding.etPassword.requestFocus()
            return
        }

        binding.tilUsername.error = null
        binding.tilPassword.error = null
        viewModel.login(username, password)
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.dnsStatus.collect { status ->
                binding.tvDnsStatus.text = status
            }
        }

        lifecycleScope.launch {
            viewModel.loginState.collect { state ->
                when (state) {
                    is Resource.Loading -> showLoading()
                    is Resource.Success -> navigateToMain()
                    is Resource.Error -> {
                        hideLoading()
                        binding.tvError.text = state.message
                        binding.tvError.show()
                        shakeForm()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.show()
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = ""
        binding.tvDnsStatus.show()
        binding.tvError.gone()
    }

    private fun hideLoading() {
        binding.progressBar.gone()
        binding.btnLogin.isEnabled = true
        binding.btnLogin.text = getString(R.string.login_button)
        binding.tvDnsStatus.gone()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun shakeForm() {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.cardLogin.startAnimation(shake)
    }

    private fun animateEntrance() {
        binding.ivLogo.alpha = 0f
        binding.cardLogin.translationY = 100f
        binding.cardLogin.alpha = 0f

        binding.ivLogo.animate().alpha(1f).setDuration(600).start()
        binding.cardLogin.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(300)
            .start()
    }
}
