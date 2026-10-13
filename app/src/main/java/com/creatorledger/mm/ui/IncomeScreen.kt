package com.creatorledger.mm.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.IncomeEntity
import com.creatorledger.mm.data.PaymentStatus
import com.creatorledger.mm.util.formatKyat
import java.time.YearMonth
import kotlinx.coroutines.launch
import com.creatorledger.mm.util.tr

@Composable
fun IncomeScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val allIncomes by remember { db.incomeDao().observeAll() }
        .collectAsState(initial = emptyList<IncomeEntity>())

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<IncomeEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<IncomeEntity?>(null) }

    var monthFilter by remember { mutableStateOf<YearMonth?>(YearMonth.now()) }
    var platformFilter by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }

    if (showForm) {
        BackHandler { showForm = false }
        IncomeFormScreen(
            initial = editing,
            onSave = { entity ->
                scope.launch {
                    if (entity.id == 0L) {
                        db.incomeDao().insert(entity)
                    } else {
                        db.incomeDao().update(entity)
                    }
                    showForm = false
                }
            },
            onCancel = { showForm = false },
            modifier = modifier
        )
    } else {
        val mf = monthFilter
        val pf = platformFilter
        val sf = statusFilter

        val filtered = allIncomes.filter { item ->
            (mf == null || item.incomeDate.startsWith(mf.toString())) &&
                (pf == null || item.platform == pf) &&
                (sf == null || item.paymentStatus == sf)
        }
        val receivedSum = filtered
            .filter { it.paymentStatus == PaymentStatus.RECEIVED }
            .sumOf { it.amountMmk }
        val pendingSum = filtered
            .filter { it.paymentStatus == PaymentStatus.PENDING }
            .sumOf { it.amountMmk }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(tr("ဝင်ငွေ"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        editing = null
                        showForm = true
                    }
                ) { Text(tr("+ ဝင်ငွေအသစ် ထည့်မယ်")) }
            }

            // လ အလိုက် စစ်ထုတ်ခြင်း
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { monthFilter = (mf ?: YearMonth.now()).minusMonths(1) }) {
                        Text("◀")
                    }
                    Text(
                        text = if (mf == null) tr("လအားလုံး") else monthLabel(mf),
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    TextButton(onClick = { monthFilter = (mf ?: YearMonth.now()).plusMonths(1) }) {
                        Text("▶")
                    }
                    FilterOption(tr("လအားလုံး"), mf == null) { monthFilter = null }
                }
            }

            // Platform အလိုက် စစ်ထုတ်ခြင်း
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption(tr("Platform အားလုံး"), pf == null) { platformFilter = null }
                    for (code in INCOME_PLATFORM_CODES) {
                        FilterOption(platformLabel(code), pf == code) { platformFilter = code }
                    }
                }
            }

            // ငွေပေးချေမှု အခြေအနေ အလိုက် စစ်ထုတ်ခြင်း
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterOption(tr("အားလုံး"), sf == null) { statusFilter = null }
                    FilterOption(
                        incomeStatusLabel(PaymentStatus.RECEIVED),
                        sf == PaymentStatus.RECEIVED
                    ) { statusFilter = PaymentStatus.RECEIVED }
                    FilterOption(
                        incomeStatusLabel(PaymentStatus.PENDING),
                        sf == PaymentStatus.PENDING
                    ) { statusFilter = PaymentStatus.PENDING }
                }
            }

            // စစ်ထုတ်ထားတဲ့ စာရင်းရဲ့ စုစုပေါင်း
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            tr("မှတ်တမ်း ") + filtered.size + tr(" ခု"),
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            tr("လက်ခံရရှိပြီး - ") + formatKyat(receivedSum),
                            color = AppColors.Teal,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            tr("မရသေးပါ - ") + formatKyat(pendingSum),
                            color = AppColors.Warning,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        tr("ဒီစစ်ထုတ်မှုနဲ့ ကိုက်တဲ့ ဝင်ငွေမှတ်တမ်း မရှိပါ။"),
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            items(filtered, key = { it.id }) { income ->
                IncomeCard(
                    income = income,
                    onEdit = {
                        editing = income
                        showForm = true
                    },
                    onDelete = { deleteTarget = income }
                )
            }
        }
    }

    val target = deleteTarget
    if (target != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(tr("ဖျက်မလား?")) },
            text = {
                Text(
                    target.source + " - " + formatKyat(target.amountMmk) +
                        tr(" မှတ်တမ်းကို ဖျက်ပါမယ်။ ပြန်မရနိုင်ပါ။")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        scope.launch { db.incomeDao().delete(target) }
                    }
                ) { Text(tr("ဖျက်မယ်")) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(tr("မဖျက်ဘူး")) }
            }
        )
    }
}

@Composable
private fun IncomeCard(income: IncomeEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = income.source,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatKyat(income.amountMmk),
                    color = AppColors.Teal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = platformLabel(income.platform) + " • " + income.incomeDate,
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            if (income.description.isNotEmpty()) {
                Text(income.description, color = AppColors.TextSecondary, fontSize = 13.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (income.paymentStatus == PaymentStatus.RECEIVED) {
                    Text(
                        tr("လက်ခံရရှိပြီး (") + (income.receivedDate ?: "") + ")",
                        color = AppColors.Teal,
                        fontSize = 13.sp
                    )
                } else {
                    Text(tr("မရသေးပါ"), color = AppColors.Warning, fontSize = 13.sp)
                }
                TextButton(onClick = onDelete) {
                    Text(tr("ဖျက်မယ်"), color = AppColors.Danger)
                }
            }
        }
    }
}
