package com.creatorledger.mm.ui

import android.app.DatePickerDialog
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.IncomeEntity
import com.creatorledger.mm.data.PaymentStatus
import com.creatorledger.mm.util.formatKyat
import com.creatorledger.mm.util.parseAmount
import java.time.LocalDate
import com.creatorledger.mm.util.tr

private fun pickDate(context: Context, current: String, onPicked: (String) -> Unit) {
    val date = try {
        LocalDate.parse(current)
    } catch (e: Exception) {
        LocalDate.now()
    }
    DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day).toString()) },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    ).show()
}

@Composable
fun IncomeFormScreen(
    initial: IncomeEntity?,
    onSave: (IncomeEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now().toString() }

    var source by remember { mutableStateOf(initial?.source ?: "") }
    var platform by remember { mutableStateOf(initial?.platform ?: "YOUTUBE") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var amountText by remember { mutableStateOf(initial?.amountMmk?.toString() ?: "") }
    var incomeDate by remember { mutableStateOf(initial?.incomeDate ?: today) }
    var status by remember { mutableStateOf(initial?.paymentStatus ?: PaymentStatus.RECEIVED) }
    var receivedDate by remember { mutableStateOf(initial?.receivedDate ?: today) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var triedSave by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val sourceError = source.trim().isEmpty()
    val amountError = amount == null || amount <= 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initial == null) tr("ဝင်ငွေအသစ် ထည့်ရန်") else tr("ဝင်ငွေ ပြင်ဆင်ရန်"),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = source,
            onValueChange = { source = it },
            label = { Text(tr("ဝင်ငွေရင်းမြစ် (ဥပမာ - YouTube AdSense)")) },
            singleLine = true,
            isError = triedSave && sourceError,
            supportingText = { if (triedSave && sourceError) Text(tr("ဝင်ငွေရင်းမြစ်ကို ဖြည့်ပါ")) },
            modifier = Modifier.fillMaxWidth()
        )

        Text("Platform", color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (code in INCOME_PLATFORM_CODES) {
                FilterOption(platformLabel(code), platform == code) { platform = code }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(tr("ဖော်ပြချက်")) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text(tr("ပမာဏ (ကျပ်)")) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = triedSave && amountError,
            supportingText = {
                if (triedSave && amountError) {
                    Text(tr("ပမာဏကို ၀ ထက်ကြီးတဲ့ ဂဏန်းနဲ့ ဖြည့်ပါ"))
                } else if (amount != null && amount > 0L) {
                    Text(formatKyat(amount))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { pickDate(context, incomeDate) { picked -> incomeDate = picked } }
        ) { Text(tr("ဝင်ငွေရရှိသည့်ရက် - ") + incomeDate) }

        Text(tr("ငွေပေးချေမှု အခြေအနေ"), color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterOption(
                incomeStatusLabel(PaymentStatus.RECEIVED),
                status == PaymentStatus.RECEIVED
            ) { status = PaymentStatus.RECEIVED }
            FilterOption(
                incomeStatusLabel(PaymentStatus.PENDING),
                status == PaymentStatus.PENDING
            ) { status = PaymentStatus.PENDING }
        }

        if (status == PaymentStatus.RECEIVED) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { pickDate(context, receivedDate) { picked -> receivedDate = picked } }
            ) { Text(tr("ငွေလက်ခံရရှိသည့်ရက် - ") + receivedDate) }
        } else {
            Text(
                tr("ငွေရတဲ့အခါ ဒီမှတ်တမ်းကို ပြင်ပြီး 'လက်ခံရရှိပြီး' သို့ ပြောင်းပါ။"),
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
        }

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(tr("မှတ်ချက်")) },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    triedSave = true
                    if (!sourceError && !amountError && amount != null) {
                        onSave(
                            IncomeEntity(
                                id = initial?.id ?: 0L,
                                source = source.trim(),
                                platform = platform,
                                description = description.trim(),
                                amountMmk = amount,
                                incomeDate = incomeDate,
                                receivedDate = if (status == PaymentStatus.RECEIVED) receivedDate else null,
                                paymentStatus = status,
                                note = note.trim()
                            )
                        )
                    }
                }
            ) { Text(tr("သိမ်းမယ်")) }

            TextButton(onClick = onCancel) { Text(tr("မလုပ်တော့ဘူး")) }
        }
    }
}
