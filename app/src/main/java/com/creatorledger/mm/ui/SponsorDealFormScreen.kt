package com.creatorledger.mm.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.creatorledger.mm.data.DealStatus
import com.creatorledger.mm.data.SponsorDealEntity
import com.creatorledger.mm.util.formatKyat
import com.creatorledger.mm.util.parseAmount
import java.time.LocalDate

@Composable
fun SponsorDealFormScreen(
    initial: SponsorDealEntity?,
    receivedSoFar: Long,
    onSave: (SponsorDealEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val today = remember { LocalDate.now().toString() }

    var brand by remember { mutableStateOf(initial?.brandName ?: "") }
    var campaign by remember { mutableStateOf(initial?.campaignName ?: "") }
    var feeText by remember { mutableStateOf(initial?.agreedFeeMmk?.toString() ?: "") }
    var dealDate by remember { mutableStateOf(initial?.dealDate ?: today) }
    var deadline by remember { mutableStateOf(initial?.deliveryDeadline) }
    var dueDate by remember { mutableStateOf(initial?.paymentDueDate) }
    var status by remember { mutableStateOf(initial?.status ?: DealStatus.ACTIVE) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var triedSave by remember { mutableStateOf(false) }

    val fee = parseAmount(feeText)
    val brandError = brand.trim().isEmpty()
    val feeError = fee == null || fee <= 0L
    val belowReceived = fee != null && fee < receivedSoFar

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initial == null) "Sponsor Deal အသစ်" else "Sponsor Deal ပြင်ဆင်ရန်",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = brand,
            onValueChange = { brand = it },
            label = { Text("Brand / Client နာမည်") },
            singleLine = true,
            isError = triedSave && brandError,
            supportingText = { if (triedSave && brandError) Text("Brand နာမည်ကို ဖြည့်ပါ") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = campaign,
            onValueChange = { campaign = it },
            label = { Text("Campaign နာမည်") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = feeText,
            onValueChange = { feeText = it },
            label = { Text("သဘောတူထားသော ကြေး (ကျပ်)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = (triedSave && feeError) || belowReceived,
            supportingText = {
                if (triedSave && feeError) {
                    Text("ကြေးကို ၀ ထက်ကြီးတဲ့ ဂဏန်းနဲ့ ဖြည့်ပါ")
                } else if (belowReceived) {
                    Text("လက်ခံပြီးငွေ " + formatKyat(receivedSoFar) + " ထက် မနည်းရပါ")
                } else if (fee != null && fee > 0L) {
                    Text(formatKyat(fee))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        DateButton("Deal Date", dealDate) { dealDate = it }
        OptionalDateField("Delivery Deadline", deadline) { deadline = it }
        OptionalDateField("Payment Due Date", dueDate) { dueDate = it }

        Text("Deal အခြေအနေ", color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (code in DEAL_STATUS_CODES) {
                FilterOption(dealStatusLabel(code), status == code) { status = code }
            }
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("မှတ်ချက်") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    triedSave = true
                    if (!brandError && !feeError && !belowReceived && fee != null) {
                        onSave(
                            SponsorDealEntity(
                                id = initial?.id ?: 0L,
                                brandName = brand.trim(),
                                campaignName = campaign.trim(),
                                agreedFeeMmk = fee,
                                dealDate = dealDate,
                                deliveryDeadline = deadline,
                                paymentDueDate = dueDate,
                                status = status,
                                note = note.trim()
                            )
                        )
                    }
                }
            ) { Text("သိမ်းမယ်") }

            TextButton(onClick = onCancel) { Text("မလုပ်တော့ဘူး") }
        }
    }
}
