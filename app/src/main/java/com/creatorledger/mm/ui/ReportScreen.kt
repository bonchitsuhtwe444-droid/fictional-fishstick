package com.creatorledger.mm.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.MonthReport
import com.creatorledger.mm.data.ReportInputs
import com.creatorledger.mm.data.computeMonthReport
import com.creatorledger.mm.util.formatKyat
import java.time.YearMonth
import kotlinx.coroutines.flow.combine

private data class CompareRow(
    val label: String,
    val current: Long,
    val previous: Long,
    val higherIsBetter: Boolean
)

private fun formatSigned(amount: Long): String {
    return if (amount > 0L) "+ " + formatKyat(amount) else formatKyat(amount)
}

@Composable
fun ReportScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val inputsFlow = remember {
        combine(
            db.incomeDao().observeAll(),
            db.expenseDao().observeAll(),
            db.sponsorDao().observeDeals(),
            db.sponsorDao().observeAllPayments()
        ) { incomes, expenses, deals, payments ->
            ReportInputs(incomes, expenses, deals, payments)
        }
    }
    val inputs by inputsFlow.collectAsState(
        initial = ReportInputs(emptyList(), emptyList(), emptyList(), emptyList())
    )

    var month by remember { mutableStateOf(YearMonth.now()) }
    val current = remember(month, inputs) { computeMonthReport(month, inputs) }
    val previous = remember(month, inputs) { computeMonthReport(month.minusMonths(1), inputs) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("လစဉ် Report", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        // နှစ်ရွေးချယ်ခြင်း
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { month = month.minusYears(1) }) { Text("◀") }
            Text(month.year.toString(), color = Color.White, fontSize = 18.sp)
            TextButton(onClick = { month = month.plusYears(1) }) { Text("▶") }
        }

        // လရွေးချယ်ခြင်း
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (m in 1..12) {
                FilterOption(monthName(m), month.monthValue == m) {
                    month = YearMonth.of(month.year, m)
                }
            }
        }

        // အမြတ်အရှုံးနဲ့ Cash Flow
        ReportCard("ရလဒ် - " + monthLabel(month)) {
            ReportRow(
                "အမြတ်အရှုံး (Profit)",
                formatKyat(current.profit),
                if (current.profit >= 0L) AppColors.Teal else AppColors.Danger
            )
            Text(
                "ရရှိပိုင်ခွင့်ရှိတဲ့ ဝင်ငွေ − ဒီလ ကုန်ကျစရိတ် (ငွေရ/မရ၊ ပေး/မပေး မခွဲ)",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ReportRow(
                "ငွေလည်ပတ်မှု (Cash Flow)",
                formatKyat(current.cashFlow),
                if (current.cashFlow >= 0L) AppColors.Teal else AppColors.Danger
            )
            Text(
                "ဒီလ လက်ခံရတဲ့ငွေ − ဒီလ ပေးချေခဲ့တဲ့ငွေ (ကိုယ့်လက်ထဲ ငွေတက်/ကျ)",
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
        }

        ReportCard("ဝင်ငွေ") {
            ReportRow("လက်ခံရရှိပြီးသော ဝင်ငွေ", formatKyat(current.receivedIncome), AppColors.Teal)
            ReportRow("ရရှိပိုင်ခွင့်ရှိပြီးသော ဝင်ငွေ", formatKyat(current.earnedIncome), Color.White)
            ReportRow("ရရန်ကျန်ငွေ", formatKyat(current.receivableOutstanding), AppColors.Warning)
        }

        ReportCard("အသုံးစရိတ်") {
            ReportRow("ပေးချေပြီးသော အသုံးစရိတ်", formatKyat(current.paidExpense), AppColors.Danger)
            ReportRow("ဒီလ ကုန်ကျစရိတ် စုစုပေါင်း", formatKyat(current.incurredExpense), Color.White)
            ReportRow("ပေးချေရန်ကျန်သော အသုံးစရိတ်", formatKyat(current.payableOutstanding), AppColors.Warning)
        }

        ReportCard("Platform အလိုက် ဝင်ငွေ (ရရှိပိုင်ခွင့်)") {
            if (current.incomeByPlatform.isEmpty()) {
                Text("မှတ်တမ်း မရှိပါ။", color = AppColors.TextSecondary, fontSize = 14.sp)
            }
            for (entry in current.incomeByPlatform.entries.sortedByDescending { it.value }) {
                ReportRow(platformLabel(entry.key), formatKyat(entry.value), Color.White)
            }
        }

        ReportCard("Category အလိုက် အသုံးစရိတ်") {
            if (current.expenseByCategory.isEmpty()) {
                Text("မှတ်တမ်း မရှိပါ။", color = AppColors.TextSecondary, fontSize = 14.sp)
            }
            for (entry in current.expenseByCategory.entries.sortedByDescending { it.value }) {
                ReportRow(categoryLabel(entry.key), formatKyat(entry.value), Color.White)
            }
        }

        ReportCard("ယခင်လ (" + monthLabel(month.minusMonths(1)) + ") နှင့် နှိုင်းယှဉ်ချက်") {
            val rows = comparisonRows(current, previous)
            for (row in rows) {
                val diff = row.current - row.previous
                val good = if (row.higherIsBetter) diff >= 0L else diff <= 0L
                Text(row.label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("ဒီလ - " + formatKyat(row.current), color = AppColors.TextSecondary, fontSize = 13.sp)
                Text("ယခင်လ - " + formatKyat(row.previous), color = AppColors.TextSecondary, fontSize = 13.sp)
                Text(
                    "ကွာခြားချက် - " + formatSigned(diff),
                    color = if (diff == 0L) AppColors.TextSecondary else if (good) AppColors.Teal else AppColors.Danger,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun comparisonRows(current: MonthReport, previous: MonthReport): List<CompareRow> {
    return listOf(
        CompareRow("ရရှိပိုင်ခွင့်ရှိတဲ့ ဝင်ငွေ", current.earnedIncome, previous.earnedIncome, true),
        CompareRow("လက်ခံရရှိတဲ့ ဝင်ငွေ", current.receivedIncome, previous.receivedIncome, true),
        CompareRow("ကုန်ကျစရိတ်", current.incurredExpense, previous.incurredExpense, false),
        CompareRow("အမြတ်အရှုံး (Profit)", current.profit, previous.profit, true),
        CompareRow("ငွေလည်ပတ်မှု (Cash Flow)", current.cashFlow, previous.cashFlow, true)
    )
}

@Composable
private fun ReportCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ReportRow(label: String, value: String, valueColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, color = AppColors.TextSecondary, fontSize = 13.sp)
        Text(value, color = valueColor, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}
