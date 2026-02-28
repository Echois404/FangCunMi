package com.passmanager

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.passmanager.ui.screen.*
import com.passmanager.ui.theme.SecretAppTheme
import com.passmanager.viewmodel.PasswordUiItem
import com.passmanager.viewmodel.PasswordViewModel
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : FragmentActivity() {

    companion object {
        private const val BIOMETRIC_KEY_ALIAS = "fangcunmi_biometric_key"
    }

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            this,
            "secret_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private var onBiometricSuccess: (() -> Unit)? = null

    private val biometricPrompt by lazy {
        BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    // Only unlock if the CryptoObject was successfully authenticated
                    val cipher = result.cryptoObject?.cipher
                    if (cipher != null) {
                        onBiometricSuccess?.invoke()
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_CANCELED) {
                        Toast.makeText(this@MainActivity, "认证失败: $errString", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    Toast.makeText(this@MainActivity, "指纹不匹配，请重试", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private val promptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("方寸密")
            .setSubtitle("验证指纹以解锁")
            .setNegativeButtonText("使用密码")
            .build()
    }

    private fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Generate or retrieve a biometric-bound key from the Android Keystore.
     * This key requires user biometric authentication to use.
     */
    private fun getOrCreateBiometricKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        // Return existing key if available
        keyStore.getKey(BIOMETRIC_KEY_ALIAS, null)?.let {
            return it as SecretKey
        }

        // Create a new biometric-bound key
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            BIOMETRIC_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    /**
     * Show biometric prompt with a CryptoObject.
     * The fingerprint must be verified by the hardware before
     * the CryptoObject becomes usable and onAuthenticationSucceeded is called.
     */
    private fun showBiometricPrompt(onSuccess: () -> Unit) {
        onBiometricSuccess = onSuccess
        try {
            val key = getOrCreateBiometricKey()
            val cipher = Cipher.getInstance(
                "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
            )
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val cryptoObject = BiometricPrompt.CryptoObject(cipher)
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } catch (e: Exception) {
            // Key invalidated (e.g., new fingerprint enrolled), recreate
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                keyStore.deleteEntry(BIOMETRIC_KEY_ALIAS)
                val key = getOrCreateBiometricKey()
                val cipher = Cipher.getInstance(
                    "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}"
                )
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val cryptoObject = BiometricPrompt.CryptoObject(cipher)
                biometricPrompt.authenticate(promptInfo, cryptoObject)
            } catch (e2: Exception) {
                Toast.makeText(this, "指纹初始化失败，请使用密码", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SecretAppTheme {
                AppNavigation(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun AppNavigation(modifier: Modifier = Modifier) {
        val vm: PasswordViewModel = viewModel()
        val passwords by vm.passwords.collectAsStateWithLifecycle()
        val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

        var isLocked by remember { mutableStateOf(true) }
        var currentScreen by remember { mutableStateOf<Screen>(Screen.List) }
        var previousScreen by remember { mutableStateOf<Screen>(Screen.List) }
        var selectedItem by remember { mutableStateOf<PasswordUiItem?>(null) }
        var editItem by remember { mutableStateOf<PasswordUiItem?>(null) }

        val isFirstTime = !prefs.contains("master_hash")
        val biometricAvailable = !isFirstTime && isBiometricAvailable()

        fun navigateTo(screen: Screen) {
            previousScreen = currentScreen
            currentScreen = screen
        }

        fun goBack() {
            previousScreen = currentScreen
            when (currentScreen) {
                Screen.AddEdit -> {
                    if (editItem != null && selectedItem != null) {
                        editItem = null
                        currentScreen = Screen.Detail
                    } else {
                        editItem = null
                        currentScreen = Screen.List
                    }
                }
                Screen.Detail -> {
                    selectedItem = null
                    currentScreen = Screen.List
                }
                Screen.About -> {
                    currentScreen = Screen.List
                }
                Screen.List -> { /* already at root */ }
            }
        }

        BackHandler(enabled = !isLocked && currentScreen != Screen.List) {
            goBack()
        }

        if (isLocked) {
            LockScreen(
                isFirstTime = isFirstTime,
                biometricAvailable = biometricAvailable,
                onUnlock = { pwd ->
                    val hash = hashPassword(pwd)
                    if (hash == prefs.getString("master_hash", "")) {
                        isLocked = false
                        true
                    } else false
                },
                onSetup = { pwd ->
                    prefs.edit().putString("master_hash", hashPassword(pwd)).apply()
                    isLocked = false
                },
                onBiometricClick = {
                    showBiometricPrompt { isLocked = false }
                }
            )
        } else {
            val isForward = when {
                currentScreen == Screen.List -> false
                previousScreen == Screen.List && currentScreen != Screen.List -> true
                previousScreen == Screen.Detail && currentScreen == Screen.AddEdit -> true
                previousScreen == Screen.AddEdit && currentScreen == Screen.Detail -> false
                else -> true
            }

            AnimatedContent(
                targetState = currentScreen,
                modifier = modifier,
                transitionSpec = {
                    if (isForward) {
                        (slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { -it / 3 } + fadeOut(tween(200)))
                    } else {
                        (slideInHorizontally(tween(300)) { -it / 3 } + fadeIn(tween(300)))
                            .togetherWith(slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(200)))
                    }
                },
                label = "screenAnim"
            ) { screen ->
                when (screen) {
                    Screen.List -> {
                        PasswordListScreen(
                            passwords = passwords,
                            searchQuery = searchQuery,
                            onAboutClick = { navigateTo(Screen.About) },
                            onSearchChange = { vm.updateSearch(it) },
                            onAddClick = {
                                editItem = null
                                navigateTo(Screen.AddEdit)
                            },
                            onItemClick = { item ->
                                selectedItem = item
                                navigateTo(Screen.Detail)
                            },
                            onLock = { isLocked = true }
                        )
                    }
                    Screen.AddEdit -> {
                        AddEditScreen(
                            editItem = editItem,
                            onSave = { siteName, siteUrl, username, password, notes ->
                                val e = editItem
                                if (e != null) {
                                    vm.updatePassword(e.id, siteName, siteUrl, username, password, notes)
                                } else {
                                    vm.addPassword(siteName, siteUrl, username, password, notes)
                                }
                                editItem = null
                                selectedItem = null
                                previousScreen = currentScreen
                                currentScreen = Screen.List
                            },
                            onBack = { goBack() }
                        )
                    }
                    Screen.Detail -> {
                        val item = selectedItem
                        if (item != null) {
                            val freshItem = passwords.find { it.id == item.id } ?: item
                            PasswordDetailScreen(
                                item = freshItem,
                                onBack = { goBack() },
                                onEdit = {
                                    editItem = freshItem
                                    navigateTo(Screen.AddEdit)
                                },
                                onDelete = {
                                    vm.deletePassword(freshItem.id)
                                    selectedItem = null
                                    previousScreen = currentScreen
                                    currentScreen = Screen.List
                                }
                            )
                        }
                    }
                    Screen.About -> {
                        AboutScreen(onBack = { goBack() })
                    }
                }
            }
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

private enum class Screen { List, AddEdit, Detail, About }
