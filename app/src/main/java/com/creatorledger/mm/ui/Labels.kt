package com.creatorledger.mm.ui

import com.creatorledger.mm.data.PaymentStatus
import java.time.YearMonth

// ဝင်ငွေဖောင်မှာ ရွေးခွင့်ပေးမယ့် Platform များ (Sponsorship ကို Sponsor Manager မှာပဲ မှတ်တယ်)
val INCOME_PLATFORM_CODES = listOf("YOUTUBE", "FACEBOOK", "TIKTOK", "FREELANCE", "OTHER")

fun platformLabel(code: String): String {
    return when (code) {
        "YOUTUBE" -> "YouTube"
        "FACEBOOK" -> "Facebook"
        "TIKTOK" -> "TikTok"
        "SPONSORSHIP" -> "Sponsorship"
        "FREELANCE" -> "Freelance"
        "OTHER" -> "Other"
        else -> code
    }
}

fun incomeStatusLabel(code: String): String {
    return when (code) {
        PaymentStatus.RECEIVED -> "လက်ခံရရှိပြီး"
        PaymentStatus.PENDING -> "မရသေးပါ"
        else -> code
    }
}

private val MONTHS_MM = listOf(
    "ဇန်နဝါရီ", "ဖေဖော်ဝါရီ", "မတ်", "ဧပြီ", "မေ", "ဇွန်",
    "ဇူလိုင်", "ဩဂုတ်", "စက်တင်ဘာ", "အောက်တိုဘာ", "နိုဝင်ဘာ", "ဒီဇင်ဘာ"
)

fun monthLabel(month: YearMonth): String {
    return MONTHS_MM[month.monthValue - 1] + " " + month.year
}
