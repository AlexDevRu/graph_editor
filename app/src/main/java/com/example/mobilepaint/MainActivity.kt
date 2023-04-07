package com.example.mobilepaint

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.mobilepaint.adapters.CanvasAdapter
import com.example.mobilepaint.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding

    private val canvasAdapter by lazy {
        CanvasAdapter(this, viewModel.canvases)
    }

    private val viewModel by viewModels<MainViewModel>()

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

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

        //binding.etType.setAdapter(PenTypesAdapter(this, viewModel.options))
        //binding.etType.onItemClickListener = this

        binding.btnGoogleSignIn.setOnClickListener(this)
        binding.btnSignOut.setOnClickListener(this)

        observe()

        /*if (savedInstanceState == null) {
            binding.viewPager.post {
                viewModel.setFirstCanvas(binding.viewPager.width, binding.viewPager.height)
                canvasAdapter.setCanvases(viewModel.canvases)
            }
        }*/

        setSupportActionBar(binding.toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun observe() {
        viewModel.googleAccount.observe(this) {
            binding.profileAvatar.isVisible = it != null
            binding.btnGoogleSignIn.isVisible = it == null
            binding.btnSignOut.isVisible = it != null
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
            R.id.btnSignOut -> googleSignInClient.signOut().addOnCompleteListener {
                if (it.isSuccessful) {
                    viewModel.saveAccount(null)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.saveCanvasParameters()
    }
}