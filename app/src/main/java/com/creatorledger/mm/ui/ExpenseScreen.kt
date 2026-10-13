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
import com.creatorledger.mm.data.ExpenseEntity
import com.creatorledger.mm.data.PaymentStatus
import com.creatorledger.mm.util.formatKyat
import java.time.YearMonth
import kotlinx.coroutines.launch
import com.creatorledger.mm.util.tr

// မှတ်တမ်းတစ်ခုရဲ့ ခေါင်းစဉ် - ဖော်ပြချက်ရှိရင် ဖော်ပြချက်၊ မရှိရင် အမျိုးအစားနာမည်
private fun expenseTitle(expense: ExpenseEntity): String {
    return if (expense.description.isNotEmpty()) expense.description else categoryLabel(expense.category)
}

@Composable
fun ExpenseScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val allExpenses by remember { db.expenseDao().observeAll() }
        .collectAsState(initial = emptyList<ExpenseEntity>())

    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ExpenseEntity?>(null) }
    var deleteTarget by remember { mutableStateOf<ExpenseEntity?>(null) }

    var monthFilter by remember { mutableStateOf<YearMonth?>(YearMonth.now()) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }
    var statusFilter by remember { mutableStateOf<String?>(null) }

    if (showForm) {
        BackHandler { showForm = false }
        ExpenseFormScreen(
            initial = editing,
            onSave = { entity ->
                scope.launch {
                    if (entity.id == 0L) {
                        db.expenseDao().insert(entity)
                    } else {
                        db.expenseDao().update(entity)
                    }
                    showForm = false
                }
            },
            onCancel = { showForm = false },
            modifier = modifier
        )
    } else {
        val mf = monthFilter
        val cf = categoryFilter
        val sf = statusFilter

        val filtered = allExpenses.filter { item ->
            (mf == null || item.expenseDate.startsWith(mf.toString())) &&
                (cf == null || item.category == cf) &&
                (sf == null || item.paymentStatus == sf)
        }
        val paidSum = filtered
            .filter { it.paymentStatus == PaymentStatus.PAID }
            .sumOf { it.amountMmk }
        val unpaidSum = filtered
            .filter { it.paymentStatus == PaymentStatus.UNPAID }
            .sumOf { it.amountMmk }

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(tr("အသုံးစရိတ်"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        editing = null
                        showForm = true
                    }
                ) { Text(tr("+ အသုံးစရိတ်အသစ် ထည့်မယ်")) }
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

            // အမျိုးအစား အလိုက် စစ်ထုတ်ခြင်း
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOption(tr("အမျိုးအစားအားလုံး"), cf == null) { categoryFilter = null }
                    for (code in EXPENSE_CATEGORY_CODES) {
                        FilterOption(categoryLabel(code), cf == code) { categoryFilter = code }
                    }
                }
            }

            // ငွေပေးချေမှု အခြေအနေ အလိုက် စစ်ထုတ်ခြင်း
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterOption(tr("အားလုံး"), sf == null) { statusFilter = null }
                    FilterOption(
                        expenseStatusLabel(PaymentStatus.PAID),
                        sf == PaymentStatus.PAID
                    ) { statusFilter = PaymentStatus.PAID }
                    FilterOption(
                        expenseStatusLabel(PaymentStatus.UNPAID),
                        sf == PaymentStatus.UNPAID
                    ) { statusFilter = PaymentStatus.UNPAID }
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
                            tr("ပေးချေပြီး - ") + formatKyat(paidSum),
                            color = AppColors.Danger,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            tr("မပေးရသေးပါ - ") + formatKyat(unpaidSum),
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
                        tr("ဒီစစ်ထုတ်မှုနဲ့ ကိုက်တဲ့ အသုံးစရိတ်မှတ်တမ်း မရှိပါ။"),
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            items(filtered, key = { it.id }) { expense ->
                ExpenseCard(
                    expense = expense,
                    onEdit = {
                        editing = expense
                        showForm = true
                    },
                    onDelete = { deleteTarget = expense }
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
                    expenseTitle(target) + " - " + formatKyat(target.amountMmk) +
                        tr(" မှတ်တမ်းကို ဖျက်ပါမယ်။ ပြန်မရနိုင်ပါ။")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTarget = null
                        scope.launch { db.expenseDao().delete(target) }
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
private fun ExpenseCard(expense: ExpenseEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
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
                    text = expenseTitle(expense),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatKyat(expense.amountMmk),
                    color = AppColors.Danger,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = categoryLabel(expense.category) + " • " + expense.expenseDate,
                color = AppColors.TextSecondary,
                fontSize = 13.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (expense.paymentStatus == PaymentStatus.PAID) {
                    Text(
                        tr("ပေးချေပြီး (") + (expense.paidDate ?: "") + ")",
                        color = AppColors.Teal,
                        fontSize = 13.sp
                    )
                } else {
                    Text(tr("မပေးရသေးပါ"), color = AppColors.Warning, fontSize = 13.sp)
                }
                TextButton(onClick = onDelete) {
                    Text(tr("ဖျက်မယ်"), color = AppColors.Danger)
                }
            }
        }
    }
}
