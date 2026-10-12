package com.creatorledger.mm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.DealStatus
import com.creatorledger.mm.data.ExpenseEntity
import com.creatorledger.mm.data.IncomeEntity
import com.creatorledger.mm.data.PaymentStatus
import com.creatorledger.mm.data.SponsorDealEntity
import com.creatorledger.mm.data.SponsorPaymentEntity
import java.time.LocalDate
import kotlinx.coroutines.launch

// Stage 3 စမ်းသပ်ရန်အတွက် ယာယီမျက်နှာပြင်။ Stage 8 မှာ တကယ့် Settings နဲ့ အစားထိုးမယ်။
// ဒီခလုတ်တွေက Database ထဲကို အမှန်တကယ် မှတ်တမ်းရေးသွင်းတာဖြစ်ပြီး "TEST" အမှတ်အသားပါတယ်။
@Composable
fun DebugScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var showConfirm by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("ဆက်တင်နှင့် Backup", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        Text(
            "စမ်းသပ်ရန် ယာယီအပိုင်း (Stage 3)",
            color = AppColors.TextSecondary,
            fontSize = 16.sp
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val today = LocalDate.now().toString()
                    db.incomeDao().insert(
                        IncomeEntity(
                            source = "TEST",
                            platform = "YOUTUBE",
                            amountMmk = 100_000L,
                            incomeDate = today,
                            receivedDate = today,
                            paymentStatus = PaymentStatus.RECEIVED,
                            note = "TEST"
                        )
                    )
                    message = "ဝင်ငွေ 100,000 ကျပ် ထည့်ပြီးပါပြီ"
                }
            }
        ) { Text("စမ်းသပ်ဝင်ငွေ 100,000 ကျပ် ထည့်မယ်") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val today = LocalDate.now().toString()
                    db.expenseDao().insert(
                        ExpenseEntity(
                            category = "VIDEO_EDITING",
                            amountMmk = 30_000L,
                            expenseDate = today,
                            paidDate = today,
                            paymentStatus = PaymentStatus.PAID,
                            note = "TEST"
                        )
                    )
                    message = "အသုံးစရိတ် 30,000 ကျပ် ထည့်ပြီးပါပြီ"
                }
            }
        ) { Text("စမ်းသပ်အသုံးစရိတ် 30,000 ကျပ် ထည့်မယ်") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val today = LocalDate.now().toString()
                    val dealId = db.sponsorDao().insertDeal(
                        SponsorDealEntity(
                            brandName = "TEST Brand",
                            agreedFeeMmk = 500_000L,
                            dealDate = today,
                            status = DealStatus.ACTIVE,
                            note = "TEST"
                        )
                    )
                    db.sponsorDao().insertPayment(
                        SponsorPaymentEntity(
                            dealId = dealId,
                            amountMmk = 200_000L,
                            receivedDate = today,
                            note = "TEST"
                        )
                    )
                    message = "Sponsor Deal (ကြေး 500,000 / လက်ခံပြီး 200,000) ထည့်ပြီးပါပြီ"
                }
            }
        ) { Text("စမ်းသပ် Sponsor Deal ထည့်မယ်") }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showConfirm = true }
        ) { Text("စမ်းသပ်ဒေတာအားလုံး ဖျက်မယ်") }

        if (message.isNotEmpty()) {
            Text(message, color = AppColors.Teal, fontSize = 15.sp)
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("စမ်းသပ်ဒေတာ ဖျက်မလား?") },
            text = { Text("TEST အမှတ်အသားပါတဲ့ မှတ်တမ်းတွေကိုပဲ ဖျက်ပါမယ်။") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirm = false
                        scope.launch {
                            db.incomeDao().deleteTestRows()
                            db.expenseDao().deleteTestRows()
                            db.sponsorDao().deleteTestDeals()
                            message = "စမ်းသပ်ဒေတာ ဖျက်ပြီးပါပြီ"
                        }
                    }
                ) { Text("ဖျက်မယ်") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("မဖျက်ဘူး") }
            }
        )
    }
}
