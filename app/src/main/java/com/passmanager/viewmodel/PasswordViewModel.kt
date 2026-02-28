package com.passmanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.passmanager.PasswordApp
import com.passmanager.data.CryptoHelper
import com.passmanager.data.PasswordEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PasswordUiItem(
    val id: Long,
    val siteName: String,
    val siteUrl: String,
    val username: String,
    val password: String, // decrypted
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
)

@OptIn(ExperimentalCoroutinesApi::class)
class PasswordViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = (application as PasswordApp).database.passwordDao()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val passwords: StateFlow<List<PasswordUiItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) dao.getAll() else dao.search(query)
        }
        .map { list ->
            list.map { entity ->
                val decrypted = try {
                    CryptoHelper.decrypt(entity.encryptedPassword, entity.passwordIv)
                } catch (e: Exception) {
                    "••••••"
                }
                PasswordUiItem(
                    id = entity.id,
                    siteName = entity.siteName,
                    siteUrl = entity.siteUrl,
                    username = entity.username,
                    password = decrypted,
                    notes = entity.notes,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun addPassword(siteName: String, siteUrl: String, username: String, password: String, notes: String) {
        viewModelScope.launch {
            val (encrypted, iv) = CryptoHelper.encrypt(password)
            dao.insert(
                PasswordEntity(
                    siteName = siteName,
                    siteUrl = siteUrl,
                    username = username,
                    encryptedPassword = encrypted,
                    passwordIv = iv,
                    notes = notes
                )
            )
        }
    }

    fun updatePassword(id: Long, siteName: String, siteUrl: String, username: String, password: String, notes: String) {
        viewModelScope.launch {
            val existing = dao.getById(id) ?: return@launch
            val (encrypted, iv) = CryptoHelper.encrypt(password)
            dao.update(
                existing.copy(
                    siteName = siteName,
                    siteUrl = siteUrl,
                    username = username,
                    encryptedPassword = encrypted,
                    passwordIv = iv,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deletePassword(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }
}
