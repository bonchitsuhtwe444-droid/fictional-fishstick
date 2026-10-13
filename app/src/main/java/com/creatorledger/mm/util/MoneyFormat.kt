package com.creatorledger.mm.util

import java.util.Locale

// ငွေပမာဏ (Long) ကို "1,250,000 ကျပ်" ပုံစံ ပြောင်းပေးတယ်
fun formatKyat(amount: Long): String {
    return String.format(Locale.US, "%,d", amount) + " ကျပ်"
}

// ရိုက်ထည့်တဲ့စာသားကို ငွေပမာဏ (Long) ပြောင်းတယ်
// ကော်မာ၊ Space ကို ကျော်တယ်၊ မြန်မာဂဏန်း ၀-၉ ကိုလည်း လက်ခံတယ်
// ဂဏန်းမဟုတ်တာပါရင် (သို့) ဗလာဆိုရင် (သို့) ဂဏန်း ၁၂ လုံးထက်ကျော်ရင် null ပြန်တယ်
fun parseAmount(text: String): Long? {
    val digits = StringBuilder()
    for (ch in text) {
        if (ch == ',' || ch == ' ') continue
        if (ch in '\u1040'..'\u1049') {
            digits.append('0' + (ch - '\u1040'))
        } else if (ch in '0'..'9') {
            digits.append(ch)
        } else {
            return null
        }
    }
    if (digits.isEmpty() || digits.length > 12) return null
    return digits.toString().toLongOrNull()
}
