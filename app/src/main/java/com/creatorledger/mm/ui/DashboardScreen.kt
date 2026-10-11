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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.util.formatKyat
import java.time.LocalDate

private val MONTH_NAMES = listOf(
    "ဇန်နဝါရီ", "ဖေဖော်ဝါရီ", "မတ်", "ဧပြီ", "မေ", "ဇွန်",
    "ဇူလိုင်", "ဩဂုတ်", "စက်တင်ဘာ", "အောက်တိုဘာ", "နိုဝင်ဘာ", "ဒီဇင်ဘာ"
)

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    val monthLabel = MONTH_NAMES[today.monthValue - 1] + " " + today.year

    // Stage 3 မှာ Database ကနေ အမှန်တကယ်ဖတ်မယ့် တန်ဖိုးများ။ အခုတော့ Database မရှိသေးလို့ သုည ထားထားတယ်။
    val receivedIncome = 0L
    val totalExpense = 0L
    val remainingToReceive = 0L
    val unpaidSponsorCount = 0
    val monthlyProfit = receivedIncome - totalExpense

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
            value = formatKyat(monthlyProfit),
            valueColor = if (monthlyProfit >= 0) AppColors.Teal else AppColors.Danger,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                label = "လက်ခံရရှိပြီး ဝင်ငွေ",
                value = formatKyat(receivedIncome),
                valueColor = AppColors.Teal,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "အသုံးစရိတ်",
                value = formatKyat(totalExpense),
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
                value = formatKyat(remainingToReceive),
                valueColor = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "ငွေမရသေးသော Sponsor",
                value = unpaidSponsorCount.toString() + " ခု",
                valueColor = AppColors.Warning,
                modifier = Modifier.weight(1f)
            )
        }

        InfoCard(
            title = "နောက်ဆုံး မှတ်တမ်းများ",
            note = "မှတ်တမ်း မရှိသေးပါ။ ဝင်ငွေ/အသုံးစရိတ် ထည့်လာရင် ဒီမှာ ပေါ်လာပါမယ်။"
        )

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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, color = AppColors.TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
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
