package com.creatorledger.mm.util

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

const val LANG_MY = "my"
const val LANG_EN = "en"

// လက်ရှိ ရွေးထားတဲ့ ဘာသာစကား။ ဒါပြောင်းရင် tr() သုံးထားတဲ့ မျက်နှာပြင်တွေ အလိုအလျောက် ပြန်ဆွဲတယ်
object AppLanguage {
    var code by mutableStateOf(LANG_MY)
}

// မြန်မာစာတစ်ခုကို ရွေးထားတဲ့ဘာသာစကားနဲ့ ပြောင်းပေးတယ်
fun tr(my: String): String {
    if (AppLanguage.code != LANG_EN) return my
    return EN_STRINGS[my] ?: my
}

fun loadLanguage(context: Context): String {
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    return if (prefs.getString("lang", LANG_MY) == LANG_EN) LANG_EN else LANG_MY
}

fun saveLanguage(context: Context, code: String) {
    context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit()
        .putString("lang", code)
        .apply()
}
