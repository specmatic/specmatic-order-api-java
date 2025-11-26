package com.store.controllers

import java.time.LocalDate

class DateRange(requestedFromDate: LocalDate?, requestedToDate: LocalDate?) {
    private val toDate: LocalDate = requestedToDate ?: LocalDate.now()
    private val fromDate: LocalDate = requestedFromDate ?: toDate.minusWeeks(1)

    fun contains(dateTime: LocalDate): Boolean {
        return dateTime in fromDate..toDate
    }
}
