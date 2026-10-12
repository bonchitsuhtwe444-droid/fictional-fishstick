package com.creatorledger.mm.data

import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class DashboardData(
    val receivedIncome: Long = 0L,        // ဒီလ လက်ခံရရှိပြီး ဝင်ငွေ (ဝင်ငွေ + Sponsor ငွေလက်ခံမှု)
    val paidExpense: Long = 0L,           // ဒီလ ပေးချေပြီး အသုံးစရိတ်
    val remainingToReceive: Long = 0L,    // ရရန်ကျန်ငွေ စုစုပေါင်း
    val unpaidSponsorCount: Long = 0L     // ငွေမပြည့်သေးတဲ့ Sponsor Deal အရေအတွက်
) {
    val monthlyProfit: Long get() = receivedIncome - paidExpense
}

fun observeDashboard(db: AppDatabase, month: YearMonth): Flow<DashboardData> {
    val start = month.atDay(1).toString()
    val end = month.atEndOfMonth().toString()

    val flows: List<Flow<Long>> = listOf(
        db.incomeDao().observeReceivedBetween(start, end),   // 0
        db.sponsorDao().observePaymentsBetween(start, end),  // 1
        db.expenseDao().observePaidBetween(start, end),      // 2
        db.incomeDao().observePendingTotal(),                // 3
        db.sponsorDao().observeAgreedTotal(),                // 4
        db.sponsorDao().observePaymentsTotal(),              // 5
        db.sponsorDao().observeUnpaidDealCount()             // 6
    )

    return combine(flows) { v ->
        DashboardData(
            receivedIncome = v[0] + v[1],
            paidExpense = v[2],
            remainingToReceive = v[3] + (v[4] - v[5]),
            unpaidSponsorCount = v[6]
        )
    }
}
