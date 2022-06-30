package com.loitp.viewmodels

import com.loitp.model.Data
import com.loitp.service.repository.MainRepository
import com.loitp.service.service.ApiClient
import com.loitpcore.annotation.LogTag
import com.loitpcore.core.base.BaseApplication
import com.loitpcore.core.base.BaseViewModel
import com.loitpcore.service.livedata.ActionData
import com.loitpcore.service.livedata.ActionLiveData
import kotlinx.coroutines.launch

@LogTag("MainViewModel")
class MainViewModel : BaseViewModel() {
    private val repository: MainRepository = MainRepository(ApiClient.apiService)
    val dataActionLiveData: ActionLiveData<ActionData<Data>> = ActionLiveData()

    fun getBookingDetail(orderId: String) {
        logD(">>>getBookingDetail orderId $orderId")
        dataActionLiveData.postAction(ActionData(isDoing = true))

        ioScope.launch {
            val response = repository.getBookingDetail(orderId)
            logD(">>>response " + BaseApplication.gson.toJson(response))
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
