package com.passmanager.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passmanager.ui.theme.*
import com.passmanager.viewmodel.PasswordUiItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordListScreen(
    passwords: List<PasswordUiItem>,
    searchQuery: String,
    onAboutClick: () -> Unit,
    onSearchChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onItemClick: (PasswordUiItem) -> Unit,
    onLock: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = BgPrimary,
        topBar = {
            Column(
                modifier = Modifier
                    .background(BgSecondary)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "方寸密",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalIconButton(
                            onClick = onAboutClick,
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = BgInput
                            )
                        ) {
                            Icon(Icons.Default.Info, "关于", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        FilledTonalIconButton(
                            onClick = onLock,
                            modifier = Modifier.size(38.dp),
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = BgInput
                            )
                        ) {
                            Icon(Icons.Default.Lock, "锁定", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("搜索网站或账号...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Pink,
                        unfocusedBorderColor = GrayLight,
                        cursorColor = Pink,
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Pink,
                contentColor = BgSecondary,
                shape = CircleShape,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(Icons.Default.Add, "添加", modifier = Modifier.size(26.dp))
            }
        }
    ) { padding ->
        if (passwords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = GrayLight
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("还没有保存的密码", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("点击右下角 + 添加", fontSize = 14.sp, color = TextMuted)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(passwords, key = { _, item -> item.id }) { index, item ->
                    PasswordCard(
                        item = item,
                        colorIndex = index % CardColors.size,
                        onCopyUsername = {
                            copyToClipboard(context, "用户名", item.username)
                        },
                        onCopyPassword = {
                            copyToClipboard(context, "密码", item.password)
                        },
                        onClick = { onItemClick(item) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PasswordCard(
    item: PasswordUiItem,
    colorIndex: Int,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgSecondary),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Site initial icon
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardColors[colorIndex].copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.siteName.take(1).uppercase(),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = CardColors[colorIndex]
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.siteName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.username,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Copy username
            FilledTonalIconButton(
                onClick = onCopyUsername,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = BgInput)
            ) {
                Icon(Icons.Default.Person, "复制用户名", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Copy password
            FilledTonalIconButton(
                onClick = onCopyPassword,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PinkSoft)
            ) {
                Icon(Icons.Default.Key, "复制密码", tint = Pink, modifier = Modifier.size(16.dp))
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "${label}已复制", Toast.LENGTH_SHORT).show()
}
