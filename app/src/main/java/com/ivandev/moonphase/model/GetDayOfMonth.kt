package com.ivandev.moonphase.model

object GetDayOfMonth {
    fun daysOfMonth(year: Int, month: Int):Int {
        val isLeapYear = year % 4 == 0
        when(month) {
            0 -> return 31
            1 -> return if (isLeapYear) 29 else 28
            2 -> return 31
            3 -> return 30
            4 -> return 31
            5 -> return 30
            6 -> return 31
            7 -> return 31
            8 -> return 30
            9 -> return 31
            10 -> return 30
            11 -> return 31
            else -> return 0
        }
    }
    fun getNameOfMonth(month: Int): String {
        when(month) {
            0 -> return "January"
            1 -> return "February"
            2 -> return "March"
            3 -> return "April"
            4 -> return "May"
            5 -> return "June"
            6 -> return "July"
            7 -> return "August"
            8 -> return "September"
            9 -> return "October"
            10 -> return "November"
            11 -> return "December"
            else -> return ""
        }
    }
}