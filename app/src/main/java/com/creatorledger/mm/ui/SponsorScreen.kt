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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.creatorledger.mm.data.SponsorDealEntity
import com.creatorledger.mm.data.SponsorPaymentEntity
import com.creatorledger.mm.util.formatKyat
import kotlinx.coroutines.launch

private enum class SponsorMode { LIST, DEAL_FORM, DETAIL }

@Composable
fun SponsorScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val deals by remember { db.sponsorDao().observeDeals() }
        .collectAsState(initial = emptyList<SponsorDealEntity>())
    val payments by remember { db.sponsorDao().observeAllPayments() }
        .collectAsState(initial = emptyList<SponsorPaymentEntity>())

    var mode by remember { mutableStateOf(SponsorMode.LIST) }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var editingDeal by remember { mutableStateOf<SponsorDealEntity?>(null) }
    var filter by remember { mutableStateOf(0) }

    val paymentsByDeal = payments.groupBy { it.dealId }
    val selectedDeal = deals.find { it.id == selectedId }
    val effectiveMode = if (mode == SponsorMode.LIST || selectedDeal == null && mode == SponsorMode.DETAIL) {
        SponsorMode.LIST
    } else {
        mode
    }

    BackHandler(enabled = effectiveMode != SponsorMode.LIST) {
        mode = if (effectiveMode == SponsorMode.DEAL_FORM && selectedDeal != null) {
            SponsorMode.DETAIL
        } else {
            SponsorMode.LIST
        }
    }

    if (effectiveMode == SponsorMode.DEAL_FORM) {
        val editing = editingDeal
        val receivedSoFar = if (editing == null) {
            0L
        } else {
            (paymentsByDeal[editing.id] ?: emptyList()).sumOf { it.amountMmk }
        }
        SponsorDealFormScreen(
            initial = editing,
            receivedSoFar = receivedSoFar,
            onSave = { deal ->
                scope.launch {
                    if (deal.id == 0L) {
                        val newId = db.sponsorDao().insertDeal(deal)
                        selectedId = newId
                    } else {
                        db.sponsorDao().updateDeal(deal)
                    }
                    mode = SponsorMode.DETAIL
                }
            },
            onCancel = {
                mode = if (selectedDeal != null && editing != null) SponsorMode.DETAIL else SponsorMode.LIST
            },
            modifier = modifier
        )
    } else if (effectiveMode == SponsorMode.DETAIL && selectedDeal != null) {
        SponsorDetailScreen(
            deal = selectedDeal,
            payments = paymentsByDeal[selectedDeal.id] ?: emptyList(),
            onEditDeal = {
                editingDeal = selectedDeal
                mode = SponsorMode.DEAL_FORM
            },
            onDeleteDeal = {
                scope.launch {
                    db.sponsorDao().deleteDeal(selectedDeal)
                    selectedId = null
                    mode = SponsorMode.LIST
                }
            },
            onSavePayment = { payment ->
                scope.launch {
                    if (payment.id == 0L) {
                        db.sponsorDao().insertPayment(payment)
                    } else {
                        db.sponsorDao().updatePayment(payment)
                    }
                }
            },
            onDeletePayment = { payment ->
                scope.launch { db.sponsorDao().deletePayment(payment) }
            },
            onBack = { mode = SponsorMode.LIST },
            modifier = modifier
        )
    } else {
        SponsorList(
            deals = deals,
            paymentsByDeal = paymentsByDeal,
            filter = filter,
            onFilter = { filter = it },
            onAdd = {
                editingDeal = null
                mode = SponsorMode.DEAL_FORM
            },
            onOpen = { deal ->
                selectedId = deal.id
                mode = SponsorMode.DETAIL
            },
            modifier = modifier
        )
    }
}

@Composable
private fun SponsorList(
    deals: List<SponsorDealEntity>,
    paymentsByDeal: Map<Long, List<SponsorPaymentEntity>>,
    filter: Int,
    onFilter: (Int) -> Unit,
    onAdd: () -> Unit,
    onOpen: (SponsorDealEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    fun receivedOf(deal: SponsorDealEntity): Long {
        return (paymentsByDeal[deal.id] ?: emptyList()).sumOf { it.amountMmk }
    }

    val active = deals.filter { it.status != DealStatus.CANCELLED }
    val totalAgreed = active.sumOf { it.agreedFeeMmk }
    val totalReceived = active.sumOf { receivedOf(it) }
    val totalRemaining = totalAgreed - totalReceived

    val shown = deals.filter { deal ->
        val remaining = deal.agreedFeeMmk - receivedOf(deal)
        val cancelled = deal.status == DealStatus.CANCELLED
        when (filter) {
            1 -> !cancelled && remaining > 0L
            2 -> !cancelled && remaining <= 0L
            3 -> cancelled
            else -> true
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Sponsorship", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Button(modifier = Modifier.fillMaxWidth(), onClick = onAdd) {
                Text("+ Sponsor Deal အသစ် ထည့်မယ်")
            }
        }

        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterOption("အားလုံး", filter == 0) { onFilter(0) }
                FilterOption("ငွေမပြည့်သေး", filter == 1) { onFilter(1) }
                FilterOption("ငွေပြည့်ပြီး", filter == 2) { onFilter(2) }
                FilterOption("ပယ်ဖျက်", filter == 3) { onFilter(3) }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ပယ်ဖျက်မထားတဲ့ Deal အားလုံး", color = AppColors.TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("သဘောတူကြေး - " + formatKyat(totalAgreed), color = Color.White, fontSize = 15.sp)
                    Text("လက်ခံရရှိပြီး - " + formatKyat(totalReceived), color = AppColors.Teal, fontSize = 15.sp)
                    Text(
                        "ရရန်ကျန်ငွေ - " + formatKyat(totalRemaining),
                        color = AppColors.Warning,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (shown.isEmpty()) {
            item {
                Text(
                    "ဒီစစ်ထုတ်မှုနဲ့ ကိုက်တဲ့ Deal မရှိပါ။",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        items(shown, key = { it.id }) { deal ->
            val received = receivedOf(deal)
            val remaining = deal.agreedFeeMmk - received
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(deal) },
                colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(deal.brandName, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    if (deal.campaignName.isNotEmpty()) {
                        Text(deal.campaignName, color = AppColors.TextSecondary, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("သဘောတူကြေး - " + formatKyat(deal.agreedFeeMmk), color = Color.White, fontSize = 14.sp)
                    Text("လက်ခံပြီး - " + formatKyat(received), color = AppColors.Teal, fontSize = 14.sp)
                    Text(
                        text = if (remaining > 0L) "ရရန်ကျန် - " + formatKyat(remaining) else "ငွေပြည့်ပါပြီ",
                        color = if (remaining > 0L) AppColors.Warning else AppColors.Teal,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        dealStatusLabel(deal.status) +
                            (if (deal.paymentDueDate != null) " • ငွေရက်သတ် " + deal.paymentDueDate else ""),
                        color = AppColors.TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
