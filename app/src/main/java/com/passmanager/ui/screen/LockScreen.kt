package com.passmanager.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passmanager.ui.theme.*

@Composable
fun LockScreen(
    isFirstTime: Boolean,
    biometricAvailable: Boolean = false,
    onUnlock: (String) -> Boolean,
    onSetup: (String) -> Unit,
    onBiometricClick: () -> Unit = {}
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var isSetupMode by remember { mutableStateOf(isFirstTime) }

    // Auto-trigger biometric on first compose if available and not setup mode
    LaunchedEffect(biometricAvailable, isSetupMode) {
        if (biometricAvailable && !isSetupMode) {
            onBiometricClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(PinkLight, Lavender))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = BgSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "方寸密",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isSetupMode) "设置主密码以保护你的数据" else "输入主密码或指纹解锁",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Card container
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = "" },
                        label = { Text(if (isSetupMode) "设置主密码" else "输入主密码") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isSetupMode) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (!isSetupMode) {
                                    if (!onUnlock(password)) {
                                        error = "密码错误，请重试"
                                        password = ""
                                    }
                                }
                            }
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "切换显示",
                                    tint = TextMuted
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = GrayLight,
                            focusedLabelColor = Pink,
                            cursorColor = Pink
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (isSetupMode) {
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it; error = "" },
                            label = { Text("确认主密码") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    when {
                                        password.length < 4 -> error = "密码至少4位"
                                        password != confirmPassword -> error = "两次输入不一致"
                                        else -> onSetup(password)
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Pink,
                                unfocusedBorderColor = GrayLight,
                                focusedLabelColor = Pink,
                                cursorColor = Pink
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = error, color = Danger, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isSetupMode) {
                                when {
                                    password.length < 4 -> error = "密码至少4位"
                                    password != confirmPassword -> error = "两次输入不一致"
                                    else -> onSetup(password)
                                }
                            } else {
                                if (!onUnlock(password)) {
                                    error = "密码错误，请重试"
                                    password = ""
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Pink)
                    ) {
                        Text(
                            text = if (isSetupMode) "设置密码" else "密码解锁",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BgSecondary
                        )
                    }

                    // Biometric button
                    if (biometricAvailable && !isSetupMode) {
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = onBiometricClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink)
                        ) {
                            Icon(
                                Icons.Default.Fingerprint,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "指纹解锁",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
