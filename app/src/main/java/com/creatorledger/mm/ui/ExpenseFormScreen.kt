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
import com.creatorledger.mm.data.ExpenseEntity
import com.creatorledger.mm.data.PaymentStatus
import com.creatorledger.mm.util.formatKyat
import com.creatorledger.mm.util.parseAmount
import java.time.LocalDate
import com.creatorledger.mm.util.tr

private fun pickExpenseDate(context: Context, current: String, onPicked: (String) -> Unit) {
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
fun ExpenseFormScreen(
    initial: ExpenseEntity?,
    onSave: (ExpenseEntity) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = remember { LocalDate.now().toString() }

    var category by remember { mutableStateOf(initial?.category ?: "VIDEO_EDITING") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var amountText by remember { mutableStateOf(initial?.amountMmk?.toString() ?: "") }
    var expenseDate by remember { mutableStateOf(initial?.expenseDate ?: today) }
    var status by remember { mutableStateOf(initial?.paymentStatus ?: PaymentStatus.PAID) }
    var paidDate by remember { mutableStateOf(initial?.paidDate ?: today) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var triedSave by remember { mutableStateOf(false) }

    val amount = parseAmount(amountText)
    val amountError = amount == null || amount <= 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (initial == null) tr("အသုံးစရိတ်အသစ် ထည့်ရန်") else tr("အသုံးစရိတ် ပြင်ဆင်ရန်"),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(tr("အမျိုးအစား"), color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (code in EXPENSE_CATEGORY_CODES) {
                FilterOption(categoryLabel(code), category == code) { category = code }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text(tr("ဖော်ပြချက် (ဥပမာ - Video editor ကြေး)")) },
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
            onClick = { pickExpenseDate(context, expenseDate) { picked -> expenseDate = picked } }
        ) { Text(tr("အသုံးစရိတ်ဖြစ်သည့်ရက် - ") + expenseDate) }

        Text(tr("ငွေပေးချေမှု အခြေအနေ"), color = AppColors.TextSecondary, fontSize = 14.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterOption(
                expenseStatusLabel(PaymentStatus.PAID),
                status == PaymentStatus.PAID
            ) { status = PaymentStatus.PAID }
            FilterOption(
                expenseStatusLabel(PaymentStatus.UNPAID),
                status == PaymentStatus.UNPAID
            ) { status = PaymentStatus.UNPAID }
        }

        if (status == PaymentStatus.PAID) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { pickExpenseDate(context, paidDate) { picked -> paidDate = picked } }
            ) { Text(tr("ငွေပေးချေခဲ့သည့်ရက် - ") + paidDate) }
        } else {
            Text(
                tr("ငွေပေးချေပြီးတဲ့အခါ ဒီမှတ်တမ်းကို ပြင်ပြီး 'ပေးချေပြီး' သို့ ပြောင်းပါ။"),
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
                    if (!amountError && amount != null) {
                        onSave(
                            ExpenseEntity(
                                id = initial?.id ?: 0L,
                                category = category,
                                description = description.trim(),
                                amountMmk = amount,
                                expenseDate = expenseDate,
                                paidDate = if (status == PaymentStatus.PAID) paidDate else null,
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
