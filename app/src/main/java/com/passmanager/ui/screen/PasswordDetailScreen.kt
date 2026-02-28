package com.passmanager.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passmanager.ui.theme.*
import com.passmanager.viewmodel.PasswordUiItem

@Composable
fun PasswordDetailScreen(
    item: PasswordUiItem,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showPassword by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                text = item.siteName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Detail card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgSecondary),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Site name
                    DetailField(label = "网站名称", value = item.siteName)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = GrayLight)

                    // URL
                    if (item.siteUrl.isNotBlank()) {
                        DetailField(label = "网站地址", value = item.siteUrl)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = GrayLight)
                    }

                    // Username with copy
                    DetailFieldWithCopy(
                        label = "用户名",
                        value = item.username,
                        onCopy = { copyToClip(context, "用户名", item.username) }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = GrayLight)

                    // Password with copy and toggle
                    Column {
                        Text("密码", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (showPassword) item.password else "••••••••••",
                                fontSize = 16.sp,
                                color = if (showPassword) TextPrimary else TextSecondary,
                                letterSpacing = if (showPassword) 0.sp else 3.sp,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalIconButton(
                                onClick = { showPassword = !showPassword },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BgInput)
                            ) {
                                Icon(
                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    "显示/隐藏", tint = TextSecondary, modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            FilledTonalIconButton(
                                onClick = { copyToClip(context, "密码", item.password) },
                                modifier = Modifier.size(34.dp),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PinkSoft)
                            ) {
                                Icon(Icons.Default.ContentCopy, "复制", tint = Pink, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Notes
                    if (item.notes.isNotBlank()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = GrayLight)
                        DetailField(label = "备注", value = item.notes)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Pink),
                    border = ButtonDefaults.outlinedButtonBorder(true)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑", fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Danger)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = BgSecondary,
            title = { Text("确认删除", color = TextPrimary) },
            text = { Text("确定要删除「${item.siteName}」的密码记录吗？此操作不可撤销。", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) {
                    Text("删除", color = Danger, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailField(label: String, value: String) {
    Column {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 15.sp, color = TextPrimary)
    }
}

@Composable
private fun DetailFieldWithCopy(label: String, value: String, onCopy: () -> Unit) {
    Column {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(value, fontSize = 15.sp, color = TextPrimary, modifier = Modifier.weight(1f))
            FilledTonalIconButton(
                onClick = onCopy,
                modifier = Modifier.size(34.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PinkSoft)
            ) {
                Icon(Icons.Default.ContentCopy, "复制", tint = Pink, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun copyToClip(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "${label}已复制", Toast.LENGTH_SHORT).show()
}
