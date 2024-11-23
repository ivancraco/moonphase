package com.ivandev.moonphase.model

import com.ivandev.moonphase.R

object GetMoonImage {
    fun imageToNew(illumination: Int): Int {
        if (illumination in 0..4) {
            return R.drawable.moon_to_full_0
        }
        if (illumination in 5..9) {
            return R.drawable.moon_to_new_5
        }
        if (illumination in 10..14) {
            return R.drawable.moon_to_new_10
        }
        if (illumination in 15..19) {
            return R.drawable.moon_to_new_15
        }
        if (illumination in 20..24) {
            return R.drawable.moon_to_new_20
        }
        if (illumination in 25..29) {
            return R.drawable.moon_to_new_25
        }
        if (illumination in 30..34) {
            return R.drawable.moon_to_new_30
        }
        if (illumination in 35..39) {
            return R.drawable.moon_to_new_35
        }
        if (illumination in 40..44) {
            return R.drawable.moon_to_new_40
        }
        if (illumination in 45..49) {
            return R.drawable.moon_to_new_45
        }
        if (illumination in 50..54) {
            return R.drawable.moon_to_new_50
        }
        if (illumination in 55..59) {
            return R.drawable.moon_to_new_55
        }
        if (illumination in 60..64) {
            return R.drawable.moon_to_new_60
        }
        if (illumination in 65..69) {
            return R.drawable.moon_to_new_65
        }
        if (illumination in 70..74) {
            return R.drawable.moon_to_new_70
        }
        if (illumination in 75..79) {
            return R.drawable.moon_to_new_75
        }
        if (illumination in 80..84) {
            return R.drawable.moon_to_new_80
        }
        if (illumination in 85..89) {
            return R.drawable.moon_to_new_85
        }
        if (illumination in 90..94) {
            return R.drawable.moon_to_new_90
        }
        if (illumination in 95..99) {
            return R.drawable.moon_to_new_95
        }
        return R.drawable.moon_100
    }

    fun imageToFull(illumination: Int): Int {
        if (illumination in 0..4) {
            return R.drawable.moon_to_full_0
        }
        if (illumination in 5..9) {
            return R.drawable.moon_to_full_5
        }
        if (illumination in 10..14) {
            return R.drawable.moon_to_full_10
        }
        if (illumination in 15..19) {
            return R.drawable.moon_to_full_15
        }
        if (illumination in 20..24) {
            return R.drawable.moon_to_full_20
        }
        if (illumination in 25..29) {
            return R.drawable.moon_to_full_25
        }
        if (illumination in 30..34) {
            return R.drawable.moon_to_full_30
        }
        if (illumination in 35..39) {
            return R.drawable.moon_to_full_35
        }
        if (illumination in 40..44) {
            return R.drawable.moon_to_full_40
        }
        if (illumination in 45..49) {
            return R.drawable.moon_to_full_45
        }
        if (illumination in 50..54) {
            return R.drawable.moon_to_full_50
        }
        if (illumination in 55..59) {
            return R.drawable.moon_to_full_55
        }
        if (illumination in 60..64) {
            return R.drawable.moon_to_full_60
        }
        if (illumination in 65..69) {
            return R.drawable.moon_to_full_65
        }
        if (illumination in 70..74) {
            return R.drawable.moon_to_full_70
        }
        if (illumination in 75..79) {
            return R.drawable.moon_to_full_75
        }
        if (illumination in 80..84) {
            return R.drawable.moon_to_full_80
        }
        if (illumination in 85..89) {
            return R.drawable.moon_to_full_85
        }
        if (illumination in 90..94) {
            return R.drawable.moon_to_full_90
        }
        if (illumination in 95..99) {
            return R.drawable.moon_to_full_95
        }
        return R.drawable.moon_100
    }
}