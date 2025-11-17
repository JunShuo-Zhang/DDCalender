package com.calendar.ddcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.ddcalendar.data.model.Event
import com.calendar.ddcalendar.data.repo.EventRepository
import com.calendar.ddcalendar.utils.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 事件管理 ViewModel
 * 负责事件的增删改查操作
 */
@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    // 当前编辑的事件
    private val _currentEvent = MutableStateFlow<Event?>(null)
    val currentEvent: StateFlow<Event?> = _currentEvent.asStateFlow()

    /**
     * 加载事件详情
     */
    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val event = eventRepository.getEventById(eventId)
            _currentEvent.value = event
            
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    error = if (event == null) "事件不存在" else null
                )
            }
        }
    }

    /**
     * 创建事件
     */
    fun createEvent(event: Event, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val eventId = eventRepository.addEvent(event)
                
                // 安排提醒
                if (event.reminderRule.minutesBefore >= 0) {
                    reminderScheduler.scheduleReminder(event.copy(id = eventId))
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = "创建事件失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 更新事件
     */
    fun updateEvent(event: Event, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                eventRepository.updateEvent(event)
                
                // 更新提醒
                reminderScheduler.updateReminder(event)
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = "更新事件失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 删除事件
     */
    fun deleteEvent(eventId: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            
            try {
                eventRepository.deleteEvent(eventId)
                
                // 取消提醒
                reminderScheduler.cancelReminder(eventId)
                
                _uiState.update { 
                    it.copy(
                        isDeleting = false,
                        deleteSuccess = true
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isDeleting = false,
                        error = "删除事件失败: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * 重置状态
     */
    fun resetState() {
        _uiState.value = EventUiState()
        _currentEvent.value = null
    }
}

/**
 * 事件 UI 状态
 */
data class EventUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val saveSuccess: Boolean = false,
    val deleteSuccess: Boolean = false,
    val error: String? = null
)
