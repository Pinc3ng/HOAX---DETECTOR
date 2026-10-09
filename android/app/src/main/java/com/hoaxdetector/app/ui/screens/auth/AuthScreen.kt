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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
            .background(NavyDeep)
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
            Spacer(modifier = Modifier.height(20.dp))

            // Logo & Brand Header
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF132238)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "Logo",
                    tint = ElectricBlue,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "HOAXDETECTOR",
                fontSize = 22.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cek sebelum percaya.",
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tab Selector: Masuk / Daftar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF101F38))
                    .padding(4.dp)
            ) {
                // Tab Masuk
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isLoginTab) ElectricBlue else Color.Transparent)
                        .clickable { viewModel.setTab(true) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Masuk",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLoginTab) White else TextSecondary
                    )
                }

                // Tab Daftar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isLoginTab) ElectricBlue else Color.Transparent)
                        .clickable { viewModel.setTab(false) }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Daftar",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isLoginTab) White else TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner jika ada
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF381E2B))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF4D6D),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFFF4D6D),
                            fontSize = 12.5.sp
                        )
                    }
                }
            }

            // Input Fields Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF101F38)),
                border = BorderStroke(1.dp, Color(0xFF1E3252))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nama Lengkap (Khusus Tab Daftar)
                    AnimatedVisibility(visible = !isLoginTab) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.nameInput.value = it },
                            label = { Text("Nama Lengkap", color = TextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color(0xFF1E3252),
                                focusedContainerColor = NavyLight,
                                unfocusedContainerColor = NavyLight
                            )
                        )
                    }

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.emailInput.value = it },
                        label = { Text("Email", color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = ElectricBlue)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFF1E3252),
                            focusedContainerColor = NavyLight,
                            unfocusedContainerColor = NavyLight
                        )
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.passwordInput.value = it },
                        label = { Text("Kata Sandi", color = TextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = ElectricBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color(0xFF1E3252),
                            focusedContainerColor = NavyLight,
                            unfocusedContainerColor = NavyLight
                        )
                    )

                    // Konfirmasi Password (Khusus Tab Daftar)
                    AnimatedVisibility(visible = !isLoginTab) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { viewModel.confirmPasswordInput.value = it },
                            label = { Text("Konfirmasi Kata Sandi", color = TextSecondary) },
                            leadingIcon = {
                                Icon(Icons.Default.LockClock, contentDescription = null, tint = ElectricBlue)
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color(0xFF1E3252),
                                focusedContainerColor = NavyLight,
                                unfocusedContainerColor = NavyLight
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Tombol Submit (Masuk / Daftar)
                    Button(
                        onClick = {
                            if (isLoginTab) viewModel.login(onAuthSuccess)
                            else viewModel.signUp(onAuthSuccess)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text(
                            text = if (isLoginTab) "Masuk ke Akun" else "Daftar Akun Baru",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = White
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
                    color = Color(0xFF1E3252),
                    thickness = 1.dp
                )
                Text(
                    text = "  atau  ",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF1E3252),
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol "Gunakan Aplikasi Tanpa Akun" (Masuk sebagai Tamu)
            Card(
                onClick = { viewModel.continueAsGuest(onAuthSuccess) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E33)),
                border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A5F)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonOutline,
                                contentDescription = null,
                                tint = ElectricBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "Gunakan Tanpa Akun",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Lanjut langsung ke beranda sebagai tamu",
                                fontSize = 11.5.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
