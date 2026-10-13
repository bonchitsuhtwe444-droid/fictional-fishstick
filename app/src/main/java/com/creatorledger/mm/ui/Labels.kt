package com.creatorledger.mm.ui

import com.creatorledger.mm.data.PaymentStatus
import java.time.YearMonth
import com.creatorledger.mm.util.tr

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
        PaymentStatus.RECEIVED -> tr("လက်ခံရရှိပြီး")
        PaymentStatus.PENDING -> tr("မရသေးပါ")
        else -> code
    }
}

private val MONTHS_MM get() = listOf(
    tr("ဇန်နဝါရီ"), tr("ဖေဖော်ဝါရီ"), tr("မတ်"), tr("ဧပြီ"), tr("မေ"), tr("ဇွန်"),
    tr("ဇူလိုင်"), tr("ဩဂုတ်"), tr("စက်တင်ဘာ"), tr("အောက်တိုဘာ"), tr("နိုဝင်ဘာ"), tr("ဒီဇင်ဘာ")
)

fun monthLabel(month: YearMonth): String {
    return MONTHS_MM[month.monthValue - 1] + " " + month.year
}

// အသုံးစရိတ် အမျိုးအစားများ
val EXPENSE_CATEGORY_CODES = listOf(
    "VIDEO_EDITING", "VOICE_OVER", "THUMBNAIL", "SOFTWARE",
    "INTERNET", "EQUIPMENT", "ADVERTISING", "OTHER"
)

fun categoryLabel(code: String): String {
    return when (code) {
        "VIDEO_EDITING" -> "Video Editing"
        "VOICE_OVER" -> "Voice-over"
        "THUMBNAIL" -> "Thumbnail Design"
        "SOFTWARE" -> "Software and Subscriptions"
        "INTERNET" -> "Internet and Mobile Data"
        "EQUIPMENT" -> "Equipment"
        "ADVERTISING" -> "Advertising"
        "OTHER" -> "Other"
        else -> code
    }
}

fun expenseStatusLabel(code: String): String {
    return when (code) {
        PaymentStatus.PAID -> tr("ပေးချေပြီး")
        PaymentStatus.UNPAID -> tr("မပေးရသေးပါ")
        else -> code
    }
}

// Sponsor Deal အခြေအနေများ
val DEAL_STATUS_CODES = listOf("NEGOTIATING", "ACTIVE", "DELIVERED", "COMPLETED", "CANCELLED")

fun dealStatusLabel(code: String): String {
    return when (code) {
        "NEGOTIATING" -> tr("ဆွေးနွေးဆဲ")
        "ACTIVE" -> tr("လုပ်ဆောင်ဆဲ")
        "DELIVERED" -> tr("ပို့ပြီး")
        "COMPLETED" -> tr("ပြီးစီး")
        "CANCELLED" -> tr("ပယ်ဖျက်")
        else -> code
    }
}

fun monthName(monthValue: Int): String {
    return MONTHS_MM[monthValue - 1]
}
