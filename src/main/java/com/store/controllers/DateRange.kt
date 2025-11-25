package com.store.controllers

import java.time.LocalDateTime

class DateRange(requestedFromDate: LocalDateTime?, requestedToDate: LocalDateTime?) {
    private val toDate: LocalDateTime = requestedToDate ?: LocalDateTime.now()
    private val fromDate: LocalDateTime = requestedFromDate ?: toDate.minusWeeks(1)

    fun contains(dateTime: LocalDateTime): Boolean {
        return dateTime in fromDate..toDate
    }
}
