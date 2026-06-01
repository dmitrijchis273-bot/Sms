package com.dmitrijchis273.smsapitester.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmitrijchis273.smsapitester.data.models.RequestStatus
import com.dmitrijchis273.smsapitester.presentation.viewmodel.SmsViewModel

@Composable
fun MainScreen(
    viewModel: SmsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = "Тестер SMS API",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Отправьте SMS код на свой номер",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Карточка отправки SMS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Поле ввода номера телефона
                TextField(
                    value = uiState.phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    label = { Text("Номер телефона") },
                    placeholder = { Text("+7 (999) 123-45-67") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    keyboardType = KeyboardType.Phone,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Call,
                            contentDescription = "Телефон",
                            tint = Color(0xFF3B82F6)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF9FAFB),
                        unfocusedContainerColor = Color(0xFFF9FAFB),
                        focusedIndicatorColor = Color(0xFF3B82F6),
                        unfocusedIndicatorColor = Color(0xFFE5E7EB)
                    ),
                    enabled = !uiState.isLoading
                )

                // Кнопка отправки
                Button(
                    onClick = { viewModel.sendSms() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !uiState.isLoading && uiState.phoneNumber.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6),
                        disabledContainerColor = Color(0xFFD1D5DB)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Отправить SMS",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Сообщение статуса
                if (uiState.message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (uiState.isSuccess) Color(0xDC2626).copy(alpha = 0.1f)
                                else Color(0xEF4444).copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.message,
                            fontSize = 14.sp,
                            color = if (uiState.isSuccess) Color(0xDC2626)
                            else Color(0xEF4444),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // История запросов
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "История (${uiState.history.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )

            if (uiState.history.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Очистить историю",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Список истории
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.history) { request ->
                HistoryItem(request)
            }
        }
    }
}

@Composable
fun HistoryItem(request: SmsRequest) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = request.phoneNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
                Text(
                    text = request.timestamp.toString(),
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Surface(
                modifier = Modifier
                    .background(
                        color = when (request.status) {
                            RequestStatus.SUCCESS -> Color(0x10059669)
                            RequestStatus.FAILED -> Color(0x10DC2626)
                            RequestStatus.PENDING -> Color(0x10F59E0B)
                            RequestStatus.RATE_LIMITED -> Color(0x108B5CF6)
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (request.status) {
                        RequestStatus.SUCCESS -> "✓ Успешно"
                        RequestStatus.FAILED -> "✗ Ошибка"
                        RequestStatus.PENDING -> "⏳ Ожидание"
                        RequestStatus.RATE_LIMITED -> "⛔ Лимит"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (request.status) {
                        RequestStatus.SUCCESS -> Color(0xFF059669)
                        RequestStatus.FAILED -> Color(0xFFDC2626)
                        RequestStatus.PENDING -> Color(0xFFF59E0B)
                        RequestStatus.RATE_LIMITED -> Color(0xFF8B5CF6)
                    }
                )
            }
        }
    }
}
