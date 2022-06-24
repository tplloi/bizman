package com.loitp.viewmodels

import com.annotation.LogTag
import com.core.base.BaseApplication
import com.core.base.BaseViewModel
import com.loitp.service.repository.MainRepository
import com.loitp.service.service.TestApiClient
import com.service.livedata.ActionData
import com.service.livedata.ActionLiveData
import com.service.model.UserTest
import kotlinx.coroutines.launch

@LogTag("MainViewModel")
class MainViewModel : BaseViewModel() {
    private val repository: MainRepository = MainRepository(TestApiClient.apiService)
    val userActionLiveData: ActionLiveData<ActionData<ArrayList<UserTest>>> = ActionLiveData()

    fun getUserTestListByPage(page: Int, isRefresh: Boolean) {
        logD(">>>getUserTestListByPage")
        userActionLiveData.set(ActionData(isDoing = true))

        ioScope.launch {
            val response = repository.getUserTest(page = page)
            logD(">>>response " + BaseApplication.gson.toJson(response.data))
            if (response.data != null) {
                userActionLiveData.post(
                    ActionData(
                        isDoing = false,
                        isSuccess = true,
                        isSwipeToRefresh = isRefresh,
                        data = response.data
                    )
                )
            } else {
                userActionLiveData.postAction(getErrorRequest(response))
            }
        }
    }
}
