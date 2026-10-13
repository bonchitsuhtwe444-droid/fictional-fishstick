package com.creatorledger.mm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.DashboardData
import com.creatorledger.mm.data.RecentTransaction
import com.creatorledger.mm.util.formatKyat
import java.time.YearMonth

private val MONTH_NAMES = listOf(
    "ဇန်နဝါရီ", "ဖေဖော်ဝါရီ", "မတ်", "ဧပြီ", "မေ", "ဇွန်",
    "ဇူလိုင်", "ဩဂုတ်", "စက်တင်ဘာ", "အောက်တိုဘာ", "နိုဝင်ဘာ", "ဒီဇင်ဘာ"
)

@Composable
fun DashboardScreen(
    data: DashboardData,
    month: YearMonth,
    recent: List<RecentTransaction>,
    modifier: Modifier = Modifier
) {
    val monthLabel = MONTH_NAMES[month.monthValue - 1] + " " + month.year
    val profit = data.monthlyProfit

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(monthLabel, color = AppColors.TextSecondary, fontSize = 16.sp)

        SummaryCard(
            label = "လစဉ်အမြတ်",
            value = formatKyat(profit),
            valueColor = if (profit >= 0) AppColors.Teal else AppColors.Danger,
            caption = "လက်ခံရရှိငွေ − ပေးချေပြီးအသုံးစရိတ်",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "လက်ခံရရှိပြီး ဝင်ငွေ",
                value = formatKyat(data.receivedIncome),
                valueColor = AppColors.Teal,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "ပေးချေပြီး အသုံးစရိတ်",
                value = formatKyat(data.paidExpense),
                valueColor = AppColors.Danger,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "ရရန်ကျန်ငွေ",
                value = formatKyat(data.remainingToReceive),
                valueColor = AppColors.Warning,
                caption = "စုစုပေါင်း",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "ငွေမပြည့်သေးသော Sponsor",
                value = data.unpaidSponsorCount.toString() + " ခု",
                valueColor = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }

        RecentCard(recent)

        InfoCard(
            title = "လစဉ် ဝင်ငွေနှင့် အသုံးစရိတ် Graph",
            note = "Graph ကို Stage 7 မှာ ဆောက်ပါမယ်။"
        )
    }
}

@Composable
private fun SummaryCard(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = AppColors.TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            if (caption != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(caption, color = AppColors.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, note: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(note, color = AppColors.TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
private fun RecentCard(recent: List<RecentTransaction>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("နောက်ဆုံး မှတ်တမ်းများ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("ရက်စွဲအသစ်ဆုံး ၅ ခု", color = AppColors.TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (recent.isEmpty()) {
                Text(
                    "မှတ်တမ်း မရှိသေးပါ။ ဝင်ငွေ/အသုံးစရိတ် ထည့်လာရင် ဒီမှာ ပေါ်လာပါမယ်။",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            for (item in recent) {
                val title = if (item.title.isNotEmpty()) {
                    item.title
                } else if (item.categoryCode != null) {
                    categoryLabel(item.categoryCode)
                } else {
                    ""
                }
                val kind = if (item.isIncome) "ဝင်ငွေ" else "အသုံးစရိတ်"
                val pending = if (item.isSettled) {
                    ""
                } else if (item.isIncome) {
                    " • မရသေး"
                } else {
                    " • မပေးရသေး"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(title, color = Color.White, fontSize = 15.sp)
                        Text(
                            kind + " • " + item.date + pending,
                            color = AppColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = (if (item.isIncome) "+ " else "- ") + formatKyat(item.amountMmk),
                        color = if (item.isIncome) AppColors.Teal else AppColors.Danger,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
