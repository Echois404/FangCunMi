package com.passmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passmanager.ui.theme.*
import com.passmanager.viewmodel.PasswordUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    editItem: PasswordUiItem? = null,
    onSave: (siteName: String, siteUrl: String, username: String, password: String, notes: String) -> Unit,
    onBack: () -> Unit
) {
    val isEdit = editItem != null
    var siteName by remember { mutableStateOf(editItem?.siteName ?: "") }
    var siteUrl by remember { mutableStateOf(editItem?.siteUrl ?: "") }
    var username by remember { mutableStateOf(editItem?.username ?: "") }
    var password by remember { mutableStateOf(editItem?.password ?: "") }
    var notes by remember { mutableStateOf(editItem?.notes ?: "") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSecondary)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = TextPrimary)
            }
            Text(
                text = if (isEdit) "编辑密码" else "添加密码",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    when {
                        siteName.isBlank() -> error = "请输入网站名称"
                        username.isBlank() -> error = "请输入用户名"
                        password.isBlank() -> error = "请输入密码"
                        else -> onSave(siteName, siteUrl, username, password, notes)
                    }
                }
            ) {
                Text("保存", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Pink)
            }
        }

        // Form
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Main card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormField(label = "网站名称", value = siteName, onChange = { siteName = it; error = "" }, placeholder = "例如: GitHub")
                    FormField(label = "网站地址", value = siteUrl, onChange = { siteUrl = it }, placeholder = "例如: https://github.com", keyboardType = KeyboardType.Uri)
                    FormField(label = "用户名 / 邮箱", value = username, onChange = { username = it; error = "" }, placeholder = "例如: user@email.com", keyboardType = KeyboardType.Email)

                    // Password field
                    Column {
                        Text("密码", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; error = "" },
                            placeholder = { Text("输入密码", color = TextMuted) },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            "切换显示", tint = TextMuted
                                        )
                                    }
                                    IconButton(onClick = { password = generatePassword() }) {
                                        Icon(Icons.Default.Casino, "生成密码", tint = Pink)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Pink,
                                unfocusedBorderColor = GrayLight,
                                cursorColor = Pink
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Strength indicator
                        if (password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val strength = getPasswordStrength(password)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(GrayLight)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(strength.first)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(strength.second)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strength.third, fontSize = 12.sp, color = strength.second)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("备注", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("可选备注信息", color = TextMuted) },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = GrayLight,
                            cursorColor = Pink
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = error, color = Danger, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(placeholder, color = TextMuted) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Pink,
                unfocusedBorderColor = GrayLight,
                cursorColor = Pink
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun generatePassword(length: Int = 16): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#\$%^&*"
    return (1..length).map { chars.random() }.joinToString("")
}

private fun getPasswordStrength(password: String): Triple<Float, androidx.compose.ui.graphics.Color, String> {
    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 1 -> Triple(0.2f, Danger, "弱")
        score <= 2 -> Triple(0.4f, Warning, "一般")
        score <= 3 -> Triple(0.6f, Warning, "中等")
        score <= 4 -> Triple(0.8f, Success, "强")
        else -> Triple(1f, Success, "非常强")
    }
}
