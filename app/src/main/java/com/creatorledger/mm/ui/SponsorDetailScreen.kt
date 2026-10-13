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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.SponsorDealEntity
import com.creatorledger.mm.data.SponsorPaymentEntity
import com.creatorledger.mm.util.formatKyat
import com.creatorledger.mm.util.parseAmount
import java.time.LocalDate

@Composable
fun SponsorDetailScreen(
    deal: SponsorDealEntity,
    payments: List<SponsorPaymentEntity>,
    onEditDeal: () -> Unit,
    onDeleteDeal: () -> Unit,
    onSavePayment: (SponsorPaymentEntity) -> Unit,
    onDeletePayment: (SponsorPaymentEntity) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPaymentDialog by remember { mutableStateOf(false) }
    var editingPayment by remember { mutableStateOf<SponsorPaymentEntity?>(null) }
    var deletePaymentTarget by remember { mutableStateOf<SponsorPaymentEntity?>(null) }
    var showDeleteDeal by remember { mutableStateOf(false) }

    val received = payments.sumOf { it.amountMmk }
    val remaining = deal.agreedFeeMmk - received

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("◀ Deal စာရင်းသို့") }

        Text(deal.brandName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        if (deal.campaignName.isNotEmpty()) {
            Text(deal.campaignName, color = AppColors.TextSecondary, fontSize = 16.sp)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("သဘောတူထားသော ကြေး", color = AppColors.TextSecondary, fontSize = 13.sp)
                Text(formatKyat(deal.agreedFeeMmk), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("လက်ခံရရှိပြီးငွေ", color = AppColors.TextSecondary, fontSize = 13.sp)
                Text(formatKyat(received), color = AppColors.Teal, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("ရရန်ကျန်ငွေ", color = AppColors.TextSecondary, fontSize = 13.sp)
                Text(
                    formatKyat(remaining),
                    color = if (remaining > 0L) AppColors.Warning else AppColors.Teal,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("အခြေအနေ - " + dealStatusLabel(deal.status), color = Color.White, fontSize = 14.sp)
                Text("Deal Date - " + deal.dealDate, color = AppColors.TextSecondary, fontSize = 13.sp)
                Text(
                    "Delivery Deadline - " + (deal.deliveryDeadline ?: "မသတ်မှတ်ထားပါ"),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    "Payment Due Date - " + (deal.paymentDueDate ?: "မသတ်မှတ်ထားပါ"),
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
                if (deal.note.isNotEmpty()) {
                    Text("မှတ်ချက် - " + deal.note, color = AppColors.TextSecondary, fontSize = 13.sp)
                }
            }
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                editingPayment = null
                showPaymentDialog = true
            }
        ) { Text("+ ငွေလက်ခံမှတ်တမ်း ထည့်မယ်") }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onEditDeal) { Text("Deal ပြင်မယ်") }
            OutlinedButton(onClick = { showDeleteDeal = true }) {
                Text("Deal ဖျက်မယ်", color = AppColors.Danger)
            }
        }

        Text("ငွေလက်ခံမှတ်တမ်းများ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (payments.isEmpty()) {
            Text("ငွေလက်ခံထားတာ မရှိသေးပါ။", color = AppColors.TextSecondary, fontSize = 14.sp)
        }
        for (p in payments) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(formatKyat(p.amountMmk), color = AppColors.Teal, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(p.receivedDate, color = AppColors.TextSecondary, fontSize = 13.sp)
                        if (p.note.isNotEmpty()) {
                            Text(p.note, color = AppColors.TextSecondary, fontSize = 13.sp)
                        }
                    }
                    TextButton(
                        onClick = {
                            editingPayment = p
                            showPaymentDialog = true
                        }
                    ) { Text("ပြင်မယ်") }
                    TextButton(onClick = { deletePaymentTarget = p }) {
                        Text("ဖျက်မယ်", color = AppColors.Danger)
                    }
                }
            }
        }
    }

    if (showPaymentDialog) {
        val editing = editingPayment
        PaymentDialog(
            dealId = deal.id,
            initial = editing,
            maxAmount = remaining + (editing?.amountMmk ?: 0L),
            onSave = { payment ->
                showPaymentDialog = false
                onSavePayment(payment)
            },
            onDismiss = { showPaymentDialog = false }
        )
    }

    val payTarget = deletePaymentTarget
    if (payTarget != null) {
        AlertDialog(
            onDismissRequest = { deletePaymentTarget = null },
            title = { Text("ဖျက်မလား?") },
            text = {
                Text(
                    formatKyat(payTarget.amountMmk) + " (" + payTarget.receivedDate +
                        ") ငွေလက်ခံမှတ်တမ်းကို ဖျက်ပါမယ်။ ရရန်ကျန်ငွေ ပြန်တက်သွားပါမယ်။"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletePaymentTarget = null
                        onDeletePayment(payTarget)
                    }
                ) { Text("ဖျက်မယ်") }
            },
            dismissButton = {
                TextButton(onClick = { deletePaymentTarget = null }) { Text("မဖျက်ဘူး") }
            }
        )
    }

    if (showDeleteDeal) {
        AlertDialog(
            onDismissRequest = { showDeleteDeal = false },
            title = { Text("Deal ကို ဖျက်မလား?") },
            text = {
                Text(
                    deal.brandName + " Deal နဲ့ ငွေလက်ခံမှတ်တမ်း " + payments.size +
                        " ခုကို အကုန်ဖျက်ပါမယ်။ ပြန်မရနိုင်ပါ။"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDeal = false
                        onDeleteDeal()
                    }
                ) { Text("ဖျက်မယ်") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDeal = false }) { Text("မဖျက်ဘူး") }
            }
        )
    }
}

@Composable
private fun PaymentDialog(
    dealId: Long,
    initial: SponsorPaymentEntity?,
    maxAmount: Long,
    onSave: (SponsorPaymentEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now().toString() }
    var amountText by remember { mutableStateOf(initial?.amountMmk?.toString() ?: "") }
    var date by remember { mutableStateOf(initial?.receivedDate ?: today) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var tried by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val error: String? = if (amount == null || amount <= 0L) {
        "ပမာဏကို ၀ ထက်ကြီးတဲ့ ဂဏန်းနဲ့ ဖြည့်ပါ"
    } else if (amount > maxAmount) {
        "ရရန်ကျန်ငွေ (" + formatKyat(maxAmount) + ") ထက် မကျော်ရပါ"
    } else {
        null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (initial == null) "ငွေလက်ခံမှတ်တမ်း ထည့်ရန်" else "ငွေလက်ခံမှတ်တမ်း ပြင်ရန်")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("လက်ခံရရှိတဲ့ပမာဏ (ကျပ်)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = tried && error != null,
                    supportingText = {
                        if (tried && error != null) {
                            Text(error)
                        } else if (amount != null && amount > 0L) {
                            Text(formatKyat(amount))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DateButton("ငွေလက်ခံရသည့်ရက်", date) { date = it }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("မှတ်ချက်") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    tried = true
                    if (error == null && amount != null) {
                        onSave(
                            SponsorPaymentEntity(
                                id = initial?.id ?: 0L,
                                dealId = dealId,
                                amountMmk = amount,
                                receivedDate = date,
                                note = note.trim()
                            )
                        )
                    }
                }
            ) { Text("သိမ်းမယ်") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("မလုပ်တော့ဘူး") }
        }
    )
}
