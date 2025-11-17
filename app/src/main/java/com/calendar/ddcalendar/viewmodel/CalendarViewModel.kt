package com.calendar.ddcalendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calendar.ddcalendar.data.model.CalendarDate
import com.calendar.ddcalendar.data.model.Event
import com.calendar.ddcalendar.data.model.LunarDate
import com.calendar.ddcalendar.data.repo.EventRepository
import com.calendar.ddcalendar.utils.DateUtils
import com.calendar.ddcalendar.utils.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 日历视图 ViewModel
 * 负责管理日历状态、日期选择、视图切换等
 */
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val eventRepository: EventRepository
) : ViewModel() {

    // 当前年月
    private val _currentYearMonth = MutableStateFlow(
        Pair(CalendarDate.today().year, CalendarDate.today().month)
    )
    val currentYearMonth: StateFlow<Pair<Int, Int>> = _currentYearMonth.asStateFlow()

    // 当前选中的日期
    private val _selectedDate = MutableStateFlow(CalendarDate.today())
    val selectedDate: StateFlow<CalendarDate> = _selectedDate.asStateFlow()

    // 当前视图模式
    private val _viewMode = MutableStateFlow(ViewMode.MONTH)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    // UI 状态
    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    // 数据加载协程Job，用于取消之前的监听
    private var dataLoadJob: Job? = null

    init {
        loadMonthData()
    }

    /**
     * 将事件列表按日期分组
     */
    private fun groupEventsByDate(events: List<Event>): Map<CalendarDate, List<Event>> {
        return events.groupBy { event ->
            CalendarDate(
                event.startTime.year,
                event.startTime.monthValue,
                event.startTime.dayOfMonth
            )
        }
    }

    /**
     * 加载月视图数据
     */
    private fun loadMonthData() {
        dataLoadJob?.cancel()
        dataLoadJob = viewModelScope.launch {
            val (year, month) = _currentYearMonth.value
            val dates = DateUtils.getMonthDates(year, month)
            val lunarDates = LunarCalendarUtil.batchSolarToLunar(dates)
            
            val firstDate = dates.first()
            val lastDate = dates.last()
            eventRepository.getEventsInRangeFlow(firstDate, lastDate).collect { events ->
                _uiState.update {
                    it.copy(
                        monthDates = dates,
                        lunarDates = lunarDates,
                        eventsByDate = groupEventsByDate(events),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 加载周视图数据
     */
    private fun loadWeekData() {
        dataLoadJob?.cancel()
        dataLoadJob = viewModelScope.launch {
            val dates = DateUtils.getWeekDates(_selectedDate.value)
            val lunarDates = LunarCalendarUtil.batchSolarToLunar(dates)
            
            val firstDate = dates.first()
            val lastDate = dates.last()
            eventRepository.getEventsInRangeFlow(firstDate, lastDate).collect { events ->
                _uiState.update {
                    it.copy(
                        weekDates = dates,
                        lunarDates = lunarDates,
                        eventsByDate = groupEventsByDate(events),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * 加载日视图数据
     */
    private fun loadDayData() {
        // 取消之前的数据加载任务
        dataLoadJob?.cancel()
        dataLoadJob = viewModelScope.launch {
            eventRepository.getEventsForDateFlow(_selectedDate.value).collect { events: List<Event> ->
                val lunarDate = LunarCalendarUtil.solarToLunar(_selectedDate.value)
                _uiState.update { state ->
                    state.copy(
                        dayEvents = events,
                        selectedDateLunar = lunarDate,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    /**
     * 刷新当前视图数据
     */
    fun refreshCurrentView() {
        when (_viewMode.value) {
            ViewMode.MONTH -> loadMonthData()
            ViewMode.WEEK -> loadWeekData()
            ViewMode.DAY -> loadDayData()
        }
    }

    /**
     * 选择日期
     */
    fun onDateSelected(date: CalendarDate) {
        _selectedDate.value = date
        
        // 如果选择的日期不在当前月份，切换月份
        val (currentYear, currentMonth) = _currentYearMonth.value
        if (date.year != currentYear || date.month != currentMonth) {
            _currentYearMonth.value = Pair(date.year, date.month)
        }
        
        when (_viewMode.value) {
            ViewMode.MONTH -> loadMonthData()
            ViewMode.WEEK -> loadWeekData()
            ViewMode.DAY -> loadDayData()
        }
    }

    /**
     * 切换到下个月
     */
    fun goToNextMonth() {
        val (year, month) = _currentYearMonth.value
        val (nextYear, nextMonth) = DateUtils.getNextMonth(year, month)
        _currentYearMonth.value = Pair(nextYear, nextMonth)
        loadMonthData()
    }

    /**
     * 切换到上个月
     */
    fun goToPreviousMonth() {
        val (year, month) = _currentYearMonth.value
        val (prevYear, prevMonth) = DateUtils.getPreviousMonth(year, month)
        _currentYearMonth.value = Pair(prevYear, prevMonth)
        loadMonthData()
    }

    /**
     * 切换视图模式
     */
    fun switchViewMode(mode: ViewMode) {
        _viewMode.value = mode
        when (mode) {
            ViewMode.MONTH -> loadMonthData()
            ViewMode.WEEK -> loadWeekData()
            ViewMode.DAY -> loadDayData()
        }
    }

    /**
     * 回到今天
     */
    fun goToToday() {
        val today = CalendarDate.today()
        _selectedDate.value = today
        _currentYearMonth.value = Pair(today.year, today.month)
        when (_viewMode.value) {
            ViewMode.MONTH -> loadMonthData()
            ViewMode.WEEK -> loadWeekData()
            ViewMode.DAY -> loadDayData()
        }
    }

    /**
     * 跳转到指定年月
     */
    fun goToYearMonth(year: Int, month: Int) {
        _currentYearMonth.value = Pair(year, month)
        // 如果当前是月视图，重新加载数据
        if (_viewMode.value == ViewMode.MONTH) {
            loadMonthData()
        }
        // 更新选中日期为指定年月的第一天
        _selectedDate.value = CalendarDate(year, month, 1)
    }
}

/**
 * 视图模式枚举
 */
enum class ViewMode {
    MONTH,
    WEEK,
    DAY
}

/**
 * 日历 UI 状态
 */
data class CalendarUiState(
    val monthDates: List<CalendarDate> = emptyList(),
    val weekDates: List<CalendarDate> = emptyList(),
    val dayEvents: List<Event> = emptyList(),
    val lunarDates: Map<CalendarDate, LunarDate> = emptyMap(),
    val eventsByDate: Map<CalendarDate, List<Event>> = emptyMap(),
    val selectedDateLunar: LunarDate? = null,
    val isLoading: Boolean = true
)
