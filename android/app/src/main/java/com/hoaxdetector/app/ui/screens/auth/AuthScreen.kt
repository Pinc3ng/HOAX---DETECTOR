package com.hoaxdetector.app.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hoaxdetector.app.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isLoginTab by viewModel.isLoginTab.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val password by viewModel.passwordInput.collectAsState()
    val name by viewModel.nameInput.collectAsState()
    val confirmPassword by viewModel.confirmPasswordInput.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showPassword by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StitchSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo & Brand Header (Stitch Deep Teal Edition)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(StitchPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Logo",
                    tint = StitchOnPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "HOAX DETECTOR",
                fontSize = 24.sp,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                color = StitchPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pemeriksaan Fakta Berbasis Natural Language Inference",
                fontSize = 13.sp,
                color = StitchOnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Selector: Masuk / Daftar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(StitchSurfaceContainer)
                    .border(1.dp, StitchOutlineVariant.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(3.dp)
            ) {
                // Tab Masuk
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isLoginTab) StitchSurfaceContainerLowest else Color.Transparent)
                        .clickable { viewModel.setTab(true) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Masuk",
                        fontSize = 13.5.sp,
                        fontWeight = if (isLoginTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (isLoginTab) StitchPrimary else StitchOutline
                    )
                }

                // Tab Daftar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isLoginTab) StitchSurfaceContainerLowest else Color.Transparent)
                        .clickable { viewModel.setTab(false) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daftar",
                        fontSize = 13.5.sp,
                        fontWeight = if (!isLoginTab) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isLoginTab) StitchPrimary else StitchOutline
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Error Banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = StitchErrorContainer.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, StitchError.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = StitchError,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            color = StitchError,
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLowest),
                border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nama Lengkap (Khusus Tab Daftar)
                    AnimatedVisibility(visible = !isLoginTab) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.nameInput.value = it },
                            label = { Text("Nama Lengkap", color = StitchOutline) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = StitchPrimaryContainer)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = StitchOnSurface,
                                unfocusedTextColor = StitchOnSurface,
                                focusedBorderColor = StitchPrimaryContainer,
                                unfocusedBorderColor = StitchOutlineVariant,
                                focusedContainerColor = StitchSurfaceContainerLowest,
                                unfocusedContainerColor = StitchSurfaceContainerLowest
                            )
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.emailInput.value = it },
                        label = { Text("Email", color = StitchOutline) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = StitchPrimaryContainer)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StitchOnSurface,
                            unfocusedTextColor = StitchOnSurface,
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchOutlineVariant,
                            focusedContainerColor = StitchSurfaceContainerLowest,
                            unfocusedContainerColor = StitchSurfaceContainerLowest
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.passwordInput.value = it },
                        label = { Text("Kata Sandi", color = StitchOutline) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = StitchPrimaryContainer)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = StitchOutline
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = StitchOnSurface,
                            unfocusedTextColor = StitchOnSurface,
                            focusedBorderColor = StitchPrimaryContainer,
                            unfocusedBorderColor = StitchOutlineVariant,
                            focusedContainerColor = StitchSurfaceContainerLowest,
                            unfocusedContainerColor = StitchSurfaceContainerLowest
                        )
                    )

                    // Konfirmasi Password (Khusus Tab Daftar)
                    AnimatedVisibility(visible = !isLoginTab) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { viewModel.confirmPasswordInput.value = it },
                            label = { Text("Konfirmasi Kata Sandi", color = StitchOutline) },
                            leadingIcon = {
                                Icon(Icons.Default.LockClock, contentDescription = null, tint = StitchPrimaryContainer)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = StitchOnSurface,
                                unfocusedTextColor = StitchOnSurface,
                                focusedBorderColor = StitchPrimaryContainer,
                                unfocusedBorderColor = StitchOutlineVariant,
                                focusedContainerColor = StitchSurfaceContainerLowest,
                                unfocusedContainerColor = StitchSurfaceContainerLowest
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Tombol Submit (Masuk / Daftar)
                    Button(
                        onClick = {
                            if (isLoginTab) viewModel.login(onAuthSuccess)
                            else viewModel.signUp(onAuthSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StitchPrimaryContainer)
                    ) {
                        Text(
                            text = if (isLoginTab) "Masuk ke Akun" else "Daftar Akun Baru",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StitchOnPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Divider: atau
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = StitchOutlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                Text(
                    text = "  atau  ",
                    fontSize = 12.sp,
                    color = StitchOutline
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = StitchOutlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol "Gunakan Aplikasi Tanpa Akun" (Masuk sebagai Tamu)
            Card(
                onClick = { viewModel.continueAsGuest(onAuthSuccess) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = StitchSurfaceContainerLow),
                border = BorderStroke(1.dp, StitchOutlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StitchSurfaceContainerHighest),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = StitchPrimary,
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Gunakan Tanpa Akun",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "Lanjut langsung ke beranda sebagai peninjau",
                                fontSize = 11.5.sp,
                                color = StitchOnSurfaceVariant
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = StitchOutline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
