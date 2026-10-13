package com.creatorledger.mm.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.ReportInputs
import com.creatorledger.mm.data.computeMonthReport
import com.creatorledger.mm.util.formatKyat
import com.creatorledger.mm.util.tr
import java.time.YearMonth
import kotlinx.coroutines.flow.combine

private const val CHART_HEIGHT = 140f

private data class ChartPoint(val month: YearMonth, val income: Long, val expense: Long)

// ဘားရဲ့ အမြင့် (ပြသဖို့သာ Float သုံးတယ်၊ ငွေတွက်ချက်မှုမှာ မသုံးဘူး)
private fun barHeight(value: Long, max: Long): Dp {
    if (value <= 0L || max <= 0L) return 0.dp
    val h = CHART_HEIGHT * value.toFloat() / max.toFloat()
    return (if (h < 2f) 2f else h).dp
}

// ပင်မ Tab က Graph - နောက်ဆုံး ၆ လ၊ လက်ခံရရှိတဲ့ဝင်ငွေ နဲ့ ပေးချေပြီးတဲ့အသုံးစရိတ်
@Composable
fun MonthlyChartCard(db: AppDatabase) {
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

    val months = remember {
        val now = YearMonth.now()
        (5 downTo 0).map { now.minusMonths(it.toLong()) }
    }
    val points = remember(inputs) {
        months.map { m ->
            val report = computeMonthReport(m, inputs)
            ChartPoint(m, report.receivedIncome, report.paidExpense)
        }
    }
    val maxValue = points.maxOf { if (it.income > it.expense) it.income else it.expense }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                tr("လစဉ် ဝင်ငွေနှင့် အသုံးစရိတ် Graph"),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                tr("နောက်ဆုံး ၆ လ (လက်ခံရရှိငွေ / ပေးချေပြီးငွေ)"),
                color = AppColors.TextSecondary,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendItem(AppColors.Teal, tr("ဝင်ငွေ"))
                LegendItem(AppColors.Danger, tr("အသုံးစရိတ်"))
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (maxValue <= 0L) {
                Text(
                    tr("ဒီ ၆ လအတွင်း မှတ်တမ်း မရှိသေးပါ။"),
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    tr("အမြင့်ဆုံး ဘား = ") + formatKyat(maxValue),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (p in points) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.height(CHART_HEIGHT.dp),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(barHeight(p.income, maxValue))
                                        .background(AppColors.Teal)
                                )
                                Box(
                                    modifier = Modifier
                                        .width(14.dp)
                                        .height(barHeight(p.expense, maxValue))
                                        .background(AppColors.Danger)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                p.month.monthValue.toString().padStart(2, '0') + "/" +
                                    (p.month.year % 100).toString().padStart(2, '0'),
                                color = AppColors.TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
    }
}
