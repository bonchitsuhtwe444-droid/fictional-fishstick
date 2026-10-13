package com.creatorledger.mm.data

import java.time.YearMonth

// Report တွက်ချက်ဖို့ လိုတဲ့ Data အားလုံး
data class ReportInputs(
    val incomes: List<IncomeEntity>,
    val expenses: List<ExpenseEntity>,
    val deals: List<SponsorDealEntity>,
    val payments: List<SponsorPaymentEntity>
)

// လတစ်လရဲ့ Report ရလဒ်
data class MonthReport(
    val earnedIncome: Long,            // ရရှိပိုင်ခွင့်ရှိပြီးသော ဝင်ငွေ (ဒီလရက်စွဲနဲ့ ဝင်ငွေ + Sponsor Deal ကြေး)
    val receivedIncome: Long,          // ဒီလအတွင်း လက်ခံရရှိပြီးသော ငွေ (Cash)
    val incurredExpense: Long,         // ဒီလရက်စွဲနဲ့ အသုံးစရိတ် (ပေးချေ/မပေးချေ မခွဲ)
    val paidExpense: Long,             // ဒီလအတွင်း ပေးချေခဲ့တဲ့ ငွေ (Cash)
    val receivableOutstanding: Long,   // ဒီလဝင်ငွေထဲက ရရန်ကျန်ငွေ
    val payableOutstanding: Long,      // ဒီလအသုံးစရိတ်ထဲက ပေးချေရန်ကျန်ငွေ
    val incomeByPlatform: Map<String, Long>,
    val expenseByCategory: Map<String, Long>
) {
    // Profit = ရရှိပိုင်ခွင့်ရှိတဲ့ဝင်ငွေ - ဒီလကုန်ကျစရိတ်
    val profit: Long get() = earnedIncome - incurredExpense

    // Cash Flow = ဒီလလက်ခံရတဲ့ငွေ - ဒီလပေးချေခဲ့တဲ့ငွေ
    val cashFlow: Long get() = receivedIncome - paidExpense
}

fun computeMonthReport(month: YearMonth, input: ReportInputs): MonthReport {
    val prefix = month.toString()

    var earned = 0L
    var received = 0L
    var incurred = 0L
    var paid = 0L
    var receivable = 0L
    var payable = 0L
    val byPlatform = LinkedHashMap<String, Long>()
    val byCategory = LinkedHashMap<String, Long>()

    for (i in input.incomes) {
        if (i.incomeDate.startsWith(prefix)) {
            earned += i.amountMmk
            byPlatform[i.platform] = (byPlatform[i.platform] ?: 0L) + i.amountMmk
            if (i.paymentStatus == PaymentStatus.PENDING) {
                receivable += i.amountMmk
            }
        }
        val rd = i.receivedDate
        if (i.paymentStatus == PaymentStatus.RECEIVED && rd != null && rd.startsWith(prefix)) {
            received += i.amountMmk
        }
    }

    // Sponsor: ငွေလက်ခံတဲ့ရက်အလိုက် Cash၊ Deal ရက်စွဲအလိုက် ရရှိပိုင်ခွင့်
    val paidByDeal = HashMap<Long, Long>()
    for (p in input.payments) {
        paidByDeal[p.dealId] = (paidByDeal[p.dealId] ?: 0L) + p.amountMmk
        if (p.receivedDate.startsWith(prefix)) {
            received += p.amountMmk
        }
    }
    for (d in input.deals) {
        if (d.status != DealStatus.CANCELLED && d.dealDate.startsWith(prefix)) {
            earned += d.agreedFeeMmk
            byPlatform["SPONSORSHIP"] = (byPlatform["SPONSORSHIP"] ?: 0L) + d.agreedFeeMmk
            receivable += d.agreedFeeMmk - (paidByDeal[d.id] ?: 0L)
        }
    }

    for (e in input.expenses) {
        if (e.expenseDate.startsWith(prefix)) {
            incurred += e.amountMmk
            byCategory[e.category] = (byCategory[e.category] ?: 0L) + e.amountMmk
            if (e.paymentStatus == PaymentStatus.UNPAID) {
                payable += e.amountMmk
            }
        }
        val pd = e.paidDate
        if (e.paymentStatus == PaymentStatus.PAID && pd != null && pd.startsWith(prefix)) {
            paid += e.amountMmk
        }
    }

    return MonthReport(
        earnedIncome = earned,
        receivedIncome = received,
        incurredExpense = incurred,
        paidExpense = paid,
        receivableOutstanding = receivable,
        payableOutstanding = payable,
        incomeByPlatform = byPlatform,
        expenseByCategory = byCategory
    )
}
