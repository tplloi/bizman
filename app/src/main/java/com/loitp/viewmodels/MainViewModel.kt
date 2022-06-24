package com.loitp.viewmodels

import com.annotation.LogTag
import com.core.base.BaseApplication
import com.core.base.BaseViewModel
import com.loitp.model.Data
import com.loitp.service.repository.MainRepository
import com.loitp.service.service.ApiClient
import com.service.livedata.ActionData
import com.service.livedata.ActionLiveData
import kotlinx.coroutines.launch

@LogTag("loitppMainViewModel")
class MainViewModel : BaseViewModel() {
    private val repository: MainRepository = MainRepository(ApiClient.apiService)
    val dataActionLiveData: ActionLiveData<ActionData<Data>> = ActionLiveData()

    fun getBookingDetail(orderId: String) {
        logD(">>>getBookingDetail orderId $orderId")
        dataActionLiveData.postAction(ActionData(isDoing = true))

        ioScope.launch {
            val response = repository.getBookingDetail(orderId)
            logD(">>>response " + BaseApplication.gson.toJson(response.data))
            if (response.data != null) {
                dataActionLiveData.post(
                    ActionData(
                        isDoing = false,
                        isSuccess = true,
                        data = response.data
                    )
                )
            } else {
                dataActionLiveData.postAction(getErrorRequest(response))
            }
        }
    }
}
