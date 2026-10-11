package com.creatorledger.mm.util

import java.util.Locale

// ငွေပမာဏ (Long) ကို "1,250,000 ကျပ်" ပုံစံ ပြောင်းပေးတယ်
fun formatKyat(amount: Long): String {
    return String.format(Locale.US, "%,d", amount) + " ကျပ်"
}
