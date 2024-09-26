package com.alltimes.cartoontime.ui.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alltimes.cartoontime.common.MessageListener
import com.alltimes.cartoontime.common.NumpadAction
import com.alltimes.cartoontime.common.PointpadAction
import com.alltimes.cartoontime.data.model.FcmMessage
import com.alltimes.cartoontime.data.model.SendUiState
import com.alltimes.cartoontime.data.model.ui.ActivityNavigationTo
import com.alltimes.cartoontime.data.model.ui.ActivityType
import com.alltimes.cartoontime.data.model.ui.ScreenNavigationTo
import com.alltimes.cartoontime.data.model.ui.ScreenType
import com.alltimes.cartoontime.data.remote.RetrofitClient
import com.alltimes.cartoontime.data.remote.TransferRequest
import com.alltimes.cartoontime.data.repository.FCMRepository
import com.alltimes.cartoontime.data.repository.UserRepository
import com.alltimes.cartoontime.ui.handler.NumPadClickHandler
import com.alltimes.cartoontime.ui.handler.PointPadClickHandler
import com.alltimes.cartoontime.utils.AccelerometerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.properties.Delegates

class SendViewModel(private val context: Context) : ViewModel(), NumpadAction, PointpadAction {

    /////////////////////////// 공용 ///////////////////////////

    private val _activityNavigationTo = MutableLiveData<ActivityNavigationTo>()
    val activityNavigationTo: LiveData<ActivityNavigationTo> get() = _activityNavigationTo

    private val _screenNavigationTo = MutableLiveData<ScreenNavigationTo>()
    val screenNavigationTo: LiveData<ScreenNavigationTo> get() = _screenNavigationTo

    private val sharedPreferences: SharedPreferences
        get() = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    val editor = sharedPreferences.edit()

    var inputEnable: Boolean = true

    private val _balance = MutableStateFlow(sharedPreferences.getLong("balance", 0L))
    val balance: StateFlow<Long> = _balance

    private val _toUserName = MutableStateFlow("")
    val toUserName: StateFlow<String> = _toUserName

    private val repository = UserRepository(RetrofitClient.apiService)

    fun goActivity(activity: ActivityType) {
        _activityNavigationTo.value = ActivityNavigationTo(activity)
    }

    fun goScreen(screen: ScreenType) {
        _screenNavigationTo.value = ScreenNavigationTo(screen)
    }

    // 각속도 측정
    private lateinit var accelerometerManager: AccelerometerManager
    private var accelerometerCount by Delegates.notNull<Int>()

    fun accelerometerStart(lifecycleOwner: LifecycleOwner) {
        println("각속도 측정 시작")
        accelerometerManager = AccelerometerManager(context)
        accelerometerCount = 0
        accelerometerManager.start()

        accelerometerManager.accelerometerData.observe(lifecycleOwner) { data ->
            // 데이터 업데이트
            println("x: ${data.x}, y: ${data.y}, z: ${data.z}")
            if (data.z <= -9.0) {
                // 아래를 보는 중
                accelerometerCount++
                if (accelerometerCount >= 10) {
                    // 승인
                    transferPoint()
                    accelerometerCount = 0
                    accelerometerStop()
                }
            } else if (data.z >= 0) {
                // 위를 보는 중
                accelerometerCount = 0
            }
        }

    }

    fun accelerometerStop() {
        println("각속도 측정 중지")
        accelerometerManager.stop()
    }

    /////////////////////////// PointInput ///////////////////////////

    override fun onPointClickedButton(type: Int) {
        pointPadClickHandler.onClickedButton(type, balance.value)
    }

    val point: StateFlow<String> get() = pointPadClickHandler.point

    // 눌리는 버튼에 대한 구현
    private val pointPadClickHandler: PointPadClickHandler by lazy {
        PointPadClickHandler(
            context = context,
            isPointExceeded = {
                if (point.value.toIntOrNull() ?: 0 > balance.value) {
                    showPointError()
                    pointPadClickHandler.setPoint(balance.value.toString())
                }
            }
        )
    }

    private fun showPointError() {
        Toast.makeText(context, "포인트가 초과되었습니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }


    /////////////////////////// PasswordInput ///////////////////////////

    override fun onClickedButton(type: Int) {
        numPadClickHandler.onClickedButton(type)
    }

    val password: StateFlow<String> get() = numPadClickHandler.password

    private val numPadClickHandler: NumPadClickHandler by lazy {
        NumPadClickHandler(
            context = context,
            onPasswordComplete = { password: String ->
                val userPassword = sharedPreferences.getString("password", null)
                if (userPassword == password) {
                    //transferPoint()
                    goScreen(ScreenType.SENDPARTNERCHECK)
                } else {
                    numPadClickHandler.clearPassword()
                    showPasswordError()
                }
            }
        )
    }

    private fun showPasswordError() {
        Toast.makeText(context, "비밀번호가 다릅니다", Toast.LENGTH_SHORT).show()
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            val vibrationEffect =
                VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(vibrationEffect)
        }
    }

    /////////////////////////// PartnerCheck ///////////////////////////

    private val bleScannerViewModel: BLEScannerViewModel = BLEScannerViewModel(context)

    private val _uiState = MutableStateFlow(SendUiState())
    val uiState = _uiState.asStateFlow()

    fun startTransaction() {
        // UWB 세션 생성 및 연결
        goScreen(ScreenType.SENDDESCRIPTION)
    }

    fun setUiState(value: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeviceConnected = value) }
        }
    }

    // 버튼 클릭 시 호출되는 함수
    fun onSendButtonClick() {
        _uiState.value = _uiState.value.copy(isSending = !_uiState.value.isSending)

        if (_uiState.value.isSending) {
            bleScannerViewModel.setMode(true)
            bleScannerViewModel.startScanningAndConnect()
        } else {
            bleScannerViewModel.disconnectDevice()
        }
    }

    /////////////////////////// Description ///////////////////////////

    // BLE 연결

    /////////////////////////// Loading ///////////////////////////

    // UWB 연결
    // 포인트 송금 함수

    // 테스트용 코드

    private val fcmMessageRepository = FCMRepository()

    fun sendMessage(senderId: String, receiverId: String, content: String) {
        println("메시지 전송을 시작합니다.")
        fcmMessageRepository.saveMessage(senderId, receiverId, content)
        // SaveMessage 메서드에서 Firestore에 데이터를 비동기로 저장하고 결과를 처리해야 합니다.
    }

    fun transferPoint() {

        CoroutineScope(Dispatchers.IO).launch {

            val fromUserId = sharedPreferences.getLong("userId", -1L)
            val toUserId = 1L
            val transferRequest = TransferRequest(fromUserId, toUserId, point.value.toLong())

            val userResponse = repository.getUserInfo(toUserId)

            if (userResponse.success)
            {
                _toUserName.value = userResponse.data?.name!!
            }

            // 상대 정보 받아오기
            val toUser = repository.getUserInfo(toUserId)
            // 송금
            val response = repository.transfer(transferRequest)

            withContext(Dispatchers.Main) {
                if (response.success) {

                    val currentBalance = sharedPreferences.getLong("balance", 0)
                    val newBalance = currentBalance - point.value.toLong()

                    editor.putLong("balance", newBalance)
                    editor.apply()

                    _balance.value = newBalance

                    val myFcmToken = sharedPreferences.getString("fcmToken", null)
                    val toFcmToken = toUser.data?.fcmtoken
                    val name = sharedPreferences.getString("name", null)

                    sendMessage(myFcmToken!!, toFcmToken!!,"${name}님 지갑에서\n${point.value} 포인트를\n받았습니다.")

                    goScreen(ScreenType.SENDCONFIRM)
                } else {

                }
            }
        }
    }

    /////////////////////////// Confirm ///////////////////////////
}
