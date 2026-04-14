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
        // Estado inicial: tela limpa, botão habilitado
        setIdleState()

        binding.btnLogin.setOnClickListener { attemptLogin() }

        binding.etPassword.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                attemptLogin(); true
            } else false
        }

        // D-Pad focus chain para TV
        binding.etUsername.nextFocusDownId = R.id.et_password
        binding.etPassword.nextFocusDownId = R.id.btn_login
        binding.btnLogin.nextFocusUpId    = R.id.et_password
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
            viewModel.loginState.collect { state ->
                when (state) {
                    is LoginUiState.Idle    -> setIdleState()
                    is LoginUiState.Loading -> setLoadingState()
                    is LoginUiState.Success -> navigateToMain()
                    is LoginUiState.Error   -> setErrorState(state.message)
                }
            }
        }
    }

    // ── Estado: tela inicial limpa ──────────────────────────────────
    private fun setIdleState() {
        binding.progressBar.visibility = View.GONE
        binding.tvDnsStatus.visibility = View.GONE   // ← oculto no idle
        binding.tvError.visibility     = View.GONE
        binding.btnLogin.isEnabled     = true
        binding.btnLogin.text          = getString(R.string.login_button)
        binding.btnLogin.icon          = getDrawable(R.drawable.ic_login)
    }

    // ── Estado: buscando servidor ───────────────────────────────────
    private fun setLoadingState() {
        binding.progressBar.visibility = View.VISIBLE
        binding.tvDnsStatus.visibility = View.VISIBLE
        binding.tvDnsStatus.text       = "Conectando ao servidor…"
        binding.tvError.visibility     = View.GONE
        binding.btnLogin.isEnabled     = false
        binding.btnLogin.text          = ""
        binding.btnLogin.icon          = null
    }

    // ── Estado: erro ────────────────────────────────────────────────
    private fun setErrorState(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.tvDnsStatus.visibility = View.GONE   // ← oculto no erro também
        binding.tvError.visibility     = View.VISIBLE
        binding.tvError.text           = message
        binding.btnLogin.isEnabled     = true
        binding.btnLogin.text          = getString(R.string.login_button)
        binding.btnLogin.icon          = getDrawable(R.drawable.ic_login)

        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.cardLogin.startAnimation(shake)
    }

    // ── Navegar para o app ──────────────────────────────────────────
    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private fun animateEntrance() {
        binding.ivLogo.alpha      = 0f
        binding.cardLogin.alpha   = 0f
        binding.cardLogin.translationY = 80f

        binding.ivLogo.animate().alpha(1f).setDuration(500).start()
        binding.cardLogin.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setStartDelay(250)
            .start()
    }
}
