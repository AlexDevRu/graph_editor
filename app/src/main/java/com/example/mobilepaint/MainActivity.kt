package com.example.mobilepaint

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.mobilepaint.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener, NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityMainBinding

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var navController: NavController

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            Toast.makeText(this, "account ${account.email}", Toast.LENGTH_LONG).show()
            viewModel.saveAccount(account)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGoogleSignIn.setOnClickListener(this)

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener(this)
        binding.toolbar.setupWithNavController(navController)

        observe()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            val showUp = navController.currentDestination?.id == R.id.canvasFragment
            menu.findItem(R.id.signOut)?.isVisible = viewModel.googleAccount.value != null && !showUp
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_dashboard, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.signOut -> googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    viewModel.saveAccount(null)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val showUp = destination.id == R.id.canvasFragment
        binding.btnGoogleSignIn.isVisible = viewModel.googleAccount.value == null && !showUp
        binding.profileAvatar.isVisible = viewModel.googleAccount.value != null && !showUp
        binding.profileName.isVisible = viewModel.googleAccount.value != null && !showUp
        invalidateOptionsMenu()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    private fun observe() {
        viewModel.googleAccount.observe(this) {
            val showUp = navController.currentDestination?.id == R.id.canvasFragment
            binding.btnGoogleSignIn.isVisible = it == null && !showUp
            binding.profileAvatar.isVisible = it != null && !showUp
            binding.profileName.isVisible = it != null && !showUp
            invalidateOptionsMenu()

            if (it != null) {
                val spannable = SpannableStringBuilder("${it.displayName}\n${it.email}")
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    it.displayName.orEmpty().length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this, R.color.grey2)),
                    it.displayName.orEmpty().length + 1,
                    spannable.length,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE
                )
                binding.profileName.text = spannable
            }

            if (it?.photoUrl != null) {
                Glide.with(this)
                    .load(it.photoUrl?.toString())
                    .error(R.drawable.no_photo)
                    .into(binding.profileAvatar)
            } else {
                binding.profileAvatar.setImageResource(R.drawable.no_photo)
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.message.collectLatest {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btnGoogleSignIn -> googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }
}