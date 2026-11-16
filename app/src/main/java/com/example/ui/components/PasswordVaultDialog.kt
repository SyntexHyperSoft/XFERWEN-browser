package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EncryptedCredential
import com.example.data.security.CryptoManager
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberDanger
import com.example.ui.theme.CyberMatrixGreen
import com.example.ui.theme.CyberNeonBlue
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.CyberVoid

/**
 * Encrypted Password Vault modal sheet.
 * Stores passwords in AES-256-GCM encrypted format and decrypts on-demand for autofill or viewing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVaultDialog(
    sheetState: SheetState,
    credentials: List<EncryptedCredential>,
    currentDomain: String,
    onInsertIntoPage: (username: String, decryptedPassword: String) -> Unit,
    onSaveCredential: (domain: String, username: String, plainPassword: String) -> Unit,
    onDeleteCredential: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    // Map to track which credentials have their password revealed
    val revealedMap = remember { mutableStateMapOf<Long, Boolean>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(CyberCyan.copy(alpha = 0.6f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CyberMatrixGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Менеджер паролей",
                            color = CyberTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "AES-256-GCM шифрование",
                            color = CyberMatrixGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = CyberVoid
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("add_password_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Добавить", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Informational encryption banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, CyberMatrixGreen.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyberMatrixGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Все пароли надежно зашифрованы в базе данных. При вставке на сайт расшифровываются напрямую в память.",
                    color = CyberTextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (credentials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сохраненных паролей пока нет",
                        color = CyberTextMuted,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(credentials, key = { it.id }) { item ->
                        val isRevealed = revealedMap[item.id] == true
                        val decryptedPassword = remember(item.id, isRevealed) {
                            if (isRevealed) {
                                CryptoManager.decrypt(item.encryptedPasswordBase64, item.ivBase64)
                            } else {
                                ""
                            }
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberSurfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.domain,
                                            color = CyberCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = item.username,
                                            color = CyberTextPrimary,
                                            fontSize = 13.sp
                                        )
                                    }

                                    // Insert into page button
                                    Button(
                                        onClick = {
                                            val realPassword = CryptoManager.decrypt(
                                                item.encryptedPasswordBase64,
                                                item.ivBase64
                                            )
                                            onInsertIntoPage(item.username, realPassword)
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CyberNeonBlue,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("autofill_button_${item.id}")
                                    ) {
                                        Icon(imageVector = Icons.Default.Input, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "Вставить", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = CyberCardBorder.copy(alpha = 0.5f))
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isRevealed) decryptedPassword else "••••••••••••",
                                        color = if (isRevealed) CyberTextPrimary else CyberTextMuted,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Toggle visibility
                                    IconButton(
                                        onClick = {
                                            revealedMap[item.id] = !isRevealed
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle",
                                            tint = CyberCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Copy password
                                    IconButton(
                                        onClick = {
                                            val real = CryptoManager.decrypt(item.encryptedPasswordBase64, item.ivBase64)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("Password", real)
                                            clipboard.setPrimaryClip(clip)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = CyberTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete credential
                                    IconButton(
                                        onClick = { onDeleteCredential(item.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = CyberDanger,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Credential Dialog
    if (showAddDialog) {
        var inputDomain by remember { mutableStateOf(currentDomain.ifBlank { "ya.ru" }) }
        var inputUser by remember { mutableStateOf("") }
        var inputPass by remember { mutableStateOf("") }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Text(text = "Сохранить новый пароль", color = CyberTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = inputDomain,
                        onValueChange = { inputDomain = it },
                        label = { Text("Сайт / Домен") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputUser,
                        onValueChange = { inputUser = it },
                        label = { Text("Логин / Email") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputPass,
                        onValueChange = { inputPass = it },
                        label = { Text("Пароль") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (inputDomain.isNotBlank() && inputPass.isNotBlank()) {
                            onSaveCredential(inputDomain, inputUser, inputPass)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Сохранить (Зашифровать)", color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Отмена", color = CyberTextMuted)
                }
            },
            containerColor = CyberSurfaceVariant,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
