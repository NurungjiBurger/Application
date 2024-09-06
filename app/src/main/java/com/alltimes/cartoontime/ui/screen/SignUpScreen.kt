package com.alltimes.cartoontime.ui.screen

import android.widget.ImageButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldDefaults.textFieldColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alltimes.cartoontime.R
import com.alltimes.cartoontime.ui.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(viewModel: SignUpViewModel) {
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val verificationCode by viewModel.verificationCode.collectAsState()
    val name by viewModel.name.collectAsState()
    val isVerificationCodeVisible by viewModel.isVerificationCodeVisible.collectAsState()
    val isSubmitButtonEnabled by viewModel.isSubmitButtonEnabled.collectAsState()
    val isNameCorrect by viewModel.isNameCorrect.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFF4F2EE))
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 상단: 전화번호 입력 및 인증번호 받기 버튼
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                horizontalArrangement = Arrangement.End // 오른쪽 끝에 정렬
            ) {
                // 상단: 뒤로가기 버튼 finish와 연결
                IconButton(
                    onClick = { println("BACKBACKBACK") },
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back), // 사용할 이미지 리소스
                        contentDescription = "Back",
                        tint = Color.Black, // 아이콘 색상 설정
                        modifier = Modifier.size(24.dp) // 아이콘 크기 설정
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "휴대폰 본인인증",
                fontSize = 16.sp, // 텍스트 크기 설정
                fontWeight = FontWeight.Bold // 텍스트 굵기 설정
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "인증번호를 요청해주세요.",
                fontSize = 24.sp,
            )

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "휴대폰 번호",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 15.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(5.dp)
            ) {
                // 전화번호 입력 필드
                TextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(2.dp),
                    label = {
                        Text(
                            text = "전화번호",
                            color = Color.Black,
                        )
                    }
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(5.dp)

                ) {
                    // 중간: 인증번호 입력 (버튼 클릭 시 보이도록)
                    if (isVerificationCodeVisible) {
                        TextField(
                            value = phoneNumber,
                            onValueChange = { viewModel.onPhoneNumberChange(it) },
                            colors = TextFieldDefaults.textFieldColors(
                                containerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color.Black,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(2.dp),
                            label = {
                                Text(
                                    text = "인증번호",
                                    color = Color.Black,
                                )
                            }
                        )
                    }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End, // 오른쪽 끝에 정렬
                ) {
                    // 인증번호 요청 버튼 클릭시 사라져야함
                    if (!isVerificationCodeVisible) {
                        Button(
                            onClick = { viewModel.onRequestVerificationCode() },
                            shape = RoundedCornerShape(15),
                            colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                            modifier = Modifier
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "인증번호 요청",
                                color = Color.Black,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // 오른쪽 끝에 정렬
            ) {
                if (isVerificationCodeVisible) {
                    Button(
                        onClick = { println("인증번호 재요청") },
                        shape = RoundedCornerShape(15),
                        colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                        modifier = Modifier
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "인증번호 재요청",
                            color = Color.Black,
                        )
                    }

                    Button(
                        onClick = { println("시간 연장") },
                        shape = RoundedCornerShape(15),
                        colors = ButtonDefaults.buttonColors(Color(0xFFF9B912)),
                        modifier = Modifier
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "시간 연장",
                            color = Color.Black,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = "이름",
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 15.dp)
            )

            Spacer(modifier = Modifier.height(3.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                // 이름 입력 필드
                TextField(
                    value = name,
                    onValueChange = { viewModel.onPhoneNumberChange(it) },
                    colors = TextFieldDefaults.textFieldColors(
                        containerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color.Black,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = Color.Black,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(2.dp),
                    label = {
                        Text(
                            text = "이름",
                            color = Color.Black,
                        )
                    }
                )

                // 이름이 한글자라도 입력되어 있다면 버튼 활성화
                if (isNameCorrect) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        // 입력된 이름 다 지우기
                        IconButton(
                            onClick = { println("nameBACKBACK") },
                            modifier = Modifier
                                .size(16.dp)
                                .background(Color.Transparent)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back), // 사용할 이미지 리소스
                                contentDescription = "Back",
                                tint = Color.Black, // 아이콘 색상 설정
                                modifier = Modifier.size(16.dp) // 아이콘 크기 설정
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            if (isNameCorrect && isVerificationCodeVisible) {
                Button(
                    onClick = { viewModel.onSubmit() },
                    enabled = isSubmitButtonEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("인증하기")
                }
            }

        }
    }
}