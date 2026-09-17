package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMember
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthDialog(
    allMembers: List<FamilyMember>,
    initialTab: Int = 0, // 0 = Login, 1 = Sign Up
    onDismiss: () -> Unit,
    onLogin: (identifier: String, password: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onRegister: (
        fullName: String,
        identifier: String,
        password: String,
        linkedMemberId: Long?,
        adminPinCode: String?,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onForgotPassword: ((email: String, onResult: (Boolean, String) -> Unit) -> Unit)? = null,
    onRequestOtpReset: ((identifier: String, onResult: (Boolean, String, String?) -> Unit) -> Unit)? = null,
    onResetPasswordWithOtp: ((identifier: String, otp: String, newPass: String, onResult: (Boolean, String) -> Unit) -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    // Login Form State
    var loginIdentifier by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }
    var isLoggingIn by remember { mutableStateOf(false) }

    // Forgot Password State (2-step OTP + Email reset)
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordError by remember { mutableStateOf<String?>(null) }
    var forgotPasswordSuccess by remember { mutableStateOf<String?>(null) }
    var isSendingResetEmail by remember { mutableStateOf(false) }
    var forgotPasswordStep by remember { mutableIntStateOf(1) } // 1: request, 2: verify OTP & set new pass
    var resetOtpInput by remember { mutableStateOf("") }
    var resetNewPassword by remember { mutableStateOf("") }
    var resetConfirmPassword by remember { mutableStateOf("") }
    var resetNewPasswordVisible by remember { mutableStateOf(false) }
    var generatedOtpHint by remember { mutableStateOf<String?>(null) }

    // Sign Up Form State
    var regFullName by remember { mutableStateOf("") }
    var regIdentifier by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordConfirm by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regAdminPin by remember { mutableStateOf("") }
    var showAdminPinField by remember { mutableStateOf(false) }
    var selectedLinkedMemberId by remember { mutableStateOf<Long?>(null) }
    var memberDropdownExpanded by remember { mutableStateOf(false) }
    var memberSearchQuery by remember { mutableStateOf("") }
    var regError by remember { mutableStateOf<String?>(null) }
    var isRegistering by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val selectedLinkedMember = remember(selectedLinkedMemberId, allMembers) {
        allMembers.find { it.id == selectedLinkedMemberId }
    }

    val filteredMembersForLink = remember(memberSearchQuery, allMembers) {
        if (memberSearchQuery.isBlank()) allMembers.take(25)
        else allMembers.filter { it.name.contains(memberSearchQuery, ignoreCase = true) }.take(25)
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoggingIn && !isRegistering) onDismiss()
        },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedTab == 0) "اکاؤنٹ لاگ ان" else "نیا اکاؤنٹ بنائیں (سائن اپ)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = EmeraldDark
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "شجرہ نسب - اولاد محمد علی",
                    style = MaterialTheme.typography.labelMedium.copy(color = GoldDark)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab Row for switching between Login and Sign Up
                TabRow(
                    selectedTabIndex = selectedTab,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = EmeraldPrimary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            loginError = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("لاگ ان", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            regError = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("سائن اپ", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ================= TAB 0: LOGIN =================
                if (selectedTab == 0) {
                    // Email or Phone / Username
                    OutlinedTextField(
                        value = loginIdentifier,
                        onValueChange = {
                            loginIdentifier = it
                            loginError = null
                        },
                        label = { Text("فون نمبر، ای میل یا صارف نام") },
                        placeholder = { Text("مثلاً: 03001234567 یا admin") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary)
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_identifier_input")
                    )

                    // Password
                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            loginError = null
                        },
                        label = { Text("پاس ورڈ") },
                        placeholder = { Text("پاس ورڈ درج کریں") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                Icon(
                                    imageVector = if (loginPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (loginPasswordVisible) "پاس ورڈ چھپائیں" else "پاس ورڈ دکھائیں"
                                )
                            }
                        },
                        visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (loginIdentifier.isNotBlank() && loginPassword.isNotBlank() && !isLoggingIn) {
                                isLoggingIn = true
                                loginError = null
                                onLogin(loginIdentifier, loginPassword) { success, msg ->
                                    isLoggingIn = false
                                    if (!success) {
                                        loginError = msg
                                    }
                                }
                            }
                        }),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    // Forgot Password Link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                val initialEmail = if (loginIdentifier.contains("@")) loginIdentifier.trim() else loginIdentifier.trim()
                                forgotPasswordEmail = initialEmail
                                forgotPasswordError = null
                                forgotPasswordSuccess = null
                                forgotPasswordStep = 1
                                resetOtpInput = ""
                                resetNewPassword = ""
                                resetConfirmPassword = ""
                                generatedOtpHint = null
                                showForgotPasswordDialog = true
                            },
                            modifier = Modifier.testTag("forgot_password_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "پاس ورڈ بھول گئے؟",
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Error display
                    if (loginError != null) {
                        Text(
                            text = loginError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    // Dedicated Keyboard Dismiss Button (ڈاؤن ایرو بٹن)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.08f))
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "کی پیڈ بند کریں",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "کی پیڈ بند کریں (Hide Keyboard)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Login Action Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (loginIdentifier.isBlank() || loginPassword.isBlank()) {
                                loginError = "براہ کرم یوزر نام/فون اور پاس ورڈ دونوں درج کریں"
                                return@Button
                            }
                            isLoggingIn = true
                            loginError = null
                            onLogin(loginIdentifier, loginPassword) { success, msg ->
                                isLoggingIn = false
                                if (!success) {
                                    loginError = msg
                                }
                            }
                        },
                        enabled = !isLoggingIn,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button")
                    ) {
                        if (isLoggingIn) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "لاگ ان ہو رہا ہے (Firebase Auth)...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("لاگ ان کریں", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // Switch to Sign Up
                    TextButton(
                        onClick = {
                            selectedTab = 1
                            regError = null
                            nameError = null
                            identifierError = null
                            passwordError = null
                            confirmPasswordError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("اکاؤنٹ نہیں ہے؟ نیا اکاؤنٹ بنائیں (سائن اپ)", color = EmeraldDark, fontSize = 13.sp)
                    }
                }

                // ================= TAB 1: SIGN UP =================
                if (selectedTab == 1) {
                    // Registration Welcoming Banner
                    Card(
                        colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "خاندانی اکاؤنٹ کی رجسٹریشن",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                                )
                                Text(
                                    text = "معلومات درج کر کے فوری اکاؤنٹ بنائیں اور شجرہ سے جڑیں",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray, fontSize = 11.5.sp)
                                )
                            }
                        }
                    }

                    // Full Name
                    OutlinedTextField(
                        value = regFullName,
                        onValueChange = {
                            regFullName = it
                            nameError = null
                            regError = null
                        },
                        label = { Text("پورا نام (Full Name) *") },
                        placeholder = { Text("مثلاً: شمریز ایوب") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary)
                        },
                        isError = nameError != null,
                        supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_name_input")
                    )

                    // Email Address or Phone
                    OutlinedTextField(
                        value = regIdentifier,
                        onValueChange = {
                            regIdentifier = it
                            identifierError = null
                            regError = null
                        },
                        label = { Text("ای میل ایڈریس یا فون نمبر *") },
                        placeholder = { Text("مثلاً: 03001234567 یا user@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary)
                        },
                        isError = identifierError != null,
                        supportingText = identifierError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_identifier_input")
                    )

                    // Link to Family Member in the tree
                    ExposedDropdownMenuBox(
                        expanded = memberDropdownExpanded,
                        onExpandedChange = { memberDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedLinkedMember?.name ?: if (selectedLinkedMemberId == null) "اختیاری: شجرہ میں اپنا پروفائل منتخب کریں" else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("شجرہ نسب میں اپنا فرد منتخب کریں") },
                            leadingIcon = {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = GoldDark)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = memberDropdownExpanded,
                            onDismissRequest = { memberDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("کوئی نہیں / بعد میں لنک کریں", color = Color.Gray) },
                                onClick = {
                                    selectedLinkedMemberId = null
                                    memberDropdownExpanded = false
                                }
                            )
                            allMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(member.name, fontWeight = FontWeight.SemiBold)
                                            val fatherStr = member.fatherName?.let { "ولدیت: $it | " } ?: ""
                                            Text(
                                                text = "${fatherStr}نسل ${member.generation}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedLinkedMemberId = member.id
                                        memberDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Password
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = {
                            regPassword = it
                            passwordError = null
                            regError = null
                        },
                        label = { Text("پاس ورڈ (کم از کم 4 حروف/ہندسے) *") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                        },
                        trailingIcon = {
                            IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                Icon(
                                    imageVector = if (regPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_password_input")
                    )

                    // Confirm Password
                    OutlinedTextField(
                        value = regPasswordConfirm,
                        onValueChange = {
                            regPasswordConfirm = it
                            confirmPasswordError = null
                            regError = null
                        },
                        label = { Text("پاس ورڈ کی دوبارہ تصدیق *") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                        },
                        isError = confirmPasswordError != null,
                        supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signup_confirm_password_input")
                    )

                    // Admin Code Option (Expandable)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdminPinField = !showAdminPinField }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = GoldDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showAdminPinField) "ایڈمن سیکیورٹی کوڈ بند کریں" else "کیا آپ ایڈمن ہیں؟ سیکیورٹی پن درج کریں (اختیاری)",
                            style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold)
                        )
                    }

                    if (showAdminPinField) {
                        OutlinedTextField(
                            value = regAdminPin,
                            onValueChange = { regAdminPin = it },
                            label = { Text("ایڈمن ماسٹر پن (اختیاری)") },
                            placeholder = { Text("اگر آپ ایڈمن اختیارات چاہتے ہیں") },
                            leadingIcon = {
                                Icon(Icons.Default.Security, contentDescription = null, tint = GoldDark)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Error display
                    if (regError != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = regError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Dedicated Keyboard Dismiss Button (ڈاؤن ایرو بٹن)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.08f))
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "کی پیڈ بند کریں",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "کی پیڈ بند کریں (Hide Keyboard)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = EmeraldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        )
                    }

                    // Register Action Button
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            nameError = null
                            identifierError = null
                            passwordError = null
                            confirmPasswordError = null
                            regError = null

                            var hasErr = false
                            if (regFullName.isBlank()) {
                                nameError = "پورا نام درج کرنا ضروری ہے"
                                hasErr = true
                            }
                            if (regIdentifier.isBlank()) {
                                identifierError = "ای میل یا فون نمبر درج کرنا ضروری ہے"
                                hasErr = true
                            }
                            if (regPassword.isBlank()) {
                                passwordError = "پاس ورڈ درج کرنا ضروری ہے"
                                hasErr = true
                            } else if (regPassword.length < 4) {
                                passwordError = "پاس ورڈ کم از کم 4 حروف یا ہندسوں کا ہونا ضروری ہے"
                                hasErr = true
                            }
                            if (regPasswordConfirm.isBlank() && regPassword.isNotBlank()) {
                                confirmPasswordError = "پاس ورڈ کی تصدیق درج کریں"
                                hasErr = true
                            } else if (regPassword != regPasswordConfirm) {
                                confirmPasswordError = "دونوں پاس ورڈز میں مطابقت نہیں ہے!"
                                hasErr = true
                            }

                            if (hasErr) return@Button

                            isRegistering = true
                            regError = null
                            onRegister(
                                regFullName,
                                regIdentifier,
                                regPassword,
                                selectedLinkedMemberId,
                                regAdminPin.ifBlank { null }
                            ) { success, msg ->
                                isRegistering = false
                                if (!success) {
                                    regError = msg
                                }
                            }
                        },
                        enabled = !isRegistering,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("signup_submit_button")
                    ) {
                        if (isRegistering) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "اکاؤنٹ بن رہا ہے...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Icon(Icons.Default.PersonAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اکاؤنٹ بنائیں (سائن اپ)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }

                    // Switch to Login
                    TextButton(
                        onClick = {
                            selectedTab = 0
                            loginError = null
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("پہلے سے اکاؤنٹ موجود ہے؟ لاگ ان کریں", color = EmeraldDark, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoggingIn && !isRegistering
            ) {
                Text("منسوخ")
            }
        }
    )

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isSendingResetEmail) {
                    showForgotPasswordDialog = false
                    forgotPasswordError = null
                    forgotPasswordSuccess = null
                }
            },
            icon = {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = EmeraldPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (forgotPasswordStep == 1) "پاس ورڈ ری سیٹ کریں" else "نیا پاس ورڈ سیٹ کریں",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (forgotPasswordStep == 1) "ای میل یا فون نمبر کے ذریعے تصدیق" else "مرحلہ 2: تصدیقی OTP کوڈ اور نیا پاس ورڈ",
                        style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Medium)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (forgotPasswordSuccess != null) {
                        // Success View
                        Card(
                            colors = CardDefaults.cardColors(containerColor = EmeraldPrimary.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.25f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "عمل مکمل ہو گیا ہے!",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = forgotPasswordSuccess ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.DarkGray,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else if (forgotPasswordStep == 1) {
                        // Step 1: Input Identifier
                        Text(
                            text = "اپنا رجسٹرڈ ای میل، فون نمبر یا صارف نام درج کریں۔ ہم تصدیقی کوڈ (OTP) اور پاس ورڈ ری سیٹ کا عمل شروع کریں گے۔",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.DarkGray,
                                fontSize = 13.sp,
                                lineHeight = 19.sp
                            )
                        )

                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = {
                                forgotPasswordEmail = it
                                forgotPasswordError = null
                            },
                            label = { Text("ای میل، فون نمبر یا صارف نام") },
                            placeholder = { Text("مثلاً: user@example.com یا 03001234567") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = EmeraldPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }),
                            isError = forgotPasswordError != null,
                            enabled = !isSendingResetEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_email_input")
                        )

                        if (forgotPasswordError != null) {
                            Text(
                                text = forgotPasswordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GoldLight.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GoldDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "اگر ای میل رجسٹرڈ ہے تو ری سیٹ لنک ای میل پر بھیج دیا جائے گا اور ساتھ ہی 6 ہندسوں کا سیکیورٹی OTP جاری ہو گا۔",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = EmeraldDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    } else {
                        // Step 2: Enter OTP & New Password
                        if (generatedOtpHint != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "آپ کا سیکیورٹی OTP کوڈ:",
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF92400E),
                                            fontSize = 12.5.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = generatedOtpHint ?: "",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFB45309),
                                        letterSpacing = 4.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "اگر ای میل موصول ہونے میں تاخیر ہو، تو آپ یہ کوڈ نیچے درج کر سکتے ہیں۔",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF92400E),
                                            fontSize = 10.5.sp
                                        )
                                    )
                                }
                            }
                        }

                        // OTP input
                        OutlinedTextField(
                            value = resetOtpInput,
                            onValueChange = {
                                if (it.length <= 6) resetOtpInput = it
                                forgotPasswordError = null
                            },
                            label = { Text("6 ہندسوں کا OTP کوڈ") },
                            placeholder = { Text("مثلاً: 123456") },
                            leadingIcon = {
                                Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            isError = forgotPasswordError != null,
                            enabled = !isSendingResetEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_otp_input")
                        )

                        // New Password input
                        OutlinedTextField(
                            value = resetNewPassword,
                            onValueChange = {
                                resetNewPassword = it
                                forgotPasswordError = null
                            },
                            label = { Text("نیا پاس ورڈ") },
                            placeholder = { Text("کم از کم 4 حروف یا ہندسے") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { resetNewPasswordVisible = !resetNewPasswordVisible }) {
                                    Icon(
                                        imageVector = if (resetNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (resetNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            enabled = !isSendingResetEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_new_pass_input")
                        )

                        // Confirm New Password input
                        OutlinedTextField(
                            value = resetConfirmPassword,
                            onValueChange = {
                                resetConfirmPassword = it
                                forgotPasswordError = null
                            },
                            label = { Text("نئے پاس ورڈ کی تصدیق") },
                            placeholder = { Text("پاس ورڈ دوبارہ درج کریں") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary)
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                            }),
                            enabled = !isSendingResetEmail,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forgot_password_confirm_pass_input")
                        )

                        if (forgotPasswordError != null) {
                            Text(
                                text = forgotPasswordError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (forgotPasswordSuccess != null) {
                    Button(
                        onClick = {
                            showForgotPasswordDialog = false
                            forgotPasswordSuccess = null
                            forgotPasswordError = null
                            forgotPasswordStep = 1
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("forgot_password_ok_button")
                    ) {
                        Text("ٹھیک ہے (مکمل ہوا)")
                    }
                } else if (forgotPasswordStep == 1) {
                    Button(
                        onClick = {
                            val ident = forgotPasswordEmail.trim()
                            if (ident.isBlank()) {
                                forgotPasswordError = "براہ کرم ای میل یا فون نمبر درج کریں"
                                return@Button
                            }
                            isSendingResetEmail = true
                            forgotPasswordError = null
                            if (onRequestOtpReset != null) {
                                onRequestOtpReset(ident) { success, msg, otp ->
                                    isSendingResetEmail = false
                                    if (success) {
                                        generatedOtpHint = otp
                                        forgotPasswordStep = 2
                                        forgotPasswordError = null
                                    } else {
                                        forgotPasswordError = msg
                                    }
                                }
                            } else if (onForgotPassword != null) {
                                onForgotPassword(ident) { success, msg ->
                                    isSendingResetEmail = false
                                    if (success) {
                                        forgotPasswordSuccess = msg
                                    } else {
                                        forgotPasswordError = msg
                                    }
                                }
                            } else {
                                isSendingResetEmail = false
                                forgotPasswordError = "پاس ورڈ ری سیٹ سروس دستیاب نہیں ہے"
                            }
                        },
                        enabled = !isSendingResetEmail && forgotPasswordEmail.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("forgot_password_send_button")
                    ) {
                        if (isSendingResetEmail) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("کوڈ تیار ہو رہا ہے...", fontSize = 13.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تصدیقی کوڈ حاصل کریں", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    // Step 2 Confirm button: Verify OTP and save new password
                    Button(
                        onClick = {
                            val ident = forgotPasswordEmail.trim()
                            val otp = resetOtpInput.trim()
                            val pass = resetNewPassword.trim()
                            val confirmPass = resetConfirmPassword.trim()

                            if (otp.length != 6) {
                                forgotPasswordError = "براہ کرم 6 ہندسوں کا درست OTP کوڈ درج کریں"
                                return@Button
                            }
                            if (pass.length < 4) {
                                forgotPasswordError = "پاس ورڈ کم از کم 4 حروف یا ہندسوں کا ہونا چاہیے"
                                return@Button
                            }
                            if (pass != confirmPass) {
                                forgotPasswordError = "دونوں پاس ورڈ مماثل نہیں ہیں"
                                return@Button
                            }

                            isSendingResetEmail = true
                            forgotPasswordError = null
                            if (onResetPasswordWithOtp != null) {
                                onResetPasswordWithOtp(ident, otp, pass) { success, msg ->
                                    isSendingResetEmail = false
                                    if (success) {
                                        forgotPasswordSuccess = msg
                                    } else {
                                        forgotPasswordError = msg
                                    }
                                }
                            } else {
                                isSendingResetEmail = false
                                forgotPasswordError = "سروس دستیاب نہیں ہے"
                            }
                        },
                        enabled = !isSendingResetEmail && resetOtpInput.length == 6 && resetNewPassword.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("forgot_password_save_new_pass_button")
                    ) {
                        if (isSendingResetEmail) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("محفوظ ہو رہا ہے...", fontSize = 13.sp)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("نیا پاس ورڈ محفوظ کریں", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            dismissButton = {
                if (forgotPasswordSuccess == null) {
                    OutlinedButton(
                        onClick = {
                            if (forgotPasswordStep == 2) {
                                forgotPasswordStep = 1
                                forgotPasswordError = null
                            } else {
                                showForgotPasswordDialog = false
                                forgotPasswordError = null
                            }
                        },
                        enabled = !isSendingResetEmail,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("forgot_password_cancel_button")
                    ) {
                        Text(if (forgotPasswordStep == 2) "واپس" else "منسوخ")
                    }
                }
            }
        )
    }
}
