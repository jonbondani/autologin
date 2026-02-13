package com.autologin.app.ui.history

import androidx.lifecycle.ViewModel
import com.autologin.app.data.local.AuthEvent
import com.autologin.app.domain.repository.HistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    historyRepository: HistoryRepository,
) : ViewModel() {

    val events: Flow<List<AuthEvent>> = historyRepository.getAllEvents()
}
