package com.creatorledger.mm.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.DashboardData
import com.creatorledger.mm.data.observeDashboard
import java.time.YearMonth

private data class TabItem(val label: String, val icon: String)

private val tabs = listOf(
    TabItem("ပင်မ", "🏠"),
    TabItem("ဝင်ငွေ", "💰"),
    TabItem("ကုန်ကျ", "💸"),
    TabItem("Sponsor", "🤝"),
    TabItem("Report", "📊"),
    TabItem("ဆက်တင်", "⚙️")
)

@Composable
fun CreatorLedgerApp() {
    var selected by rememberSaveable { mutableStateOf(0) }

    val context = LocalContext.current
    val db = remember { AppDatabase.get(context) }
    val month = remember { YearMonth.now() }
    val dashboardFlow = remember { observeDashboard(db, month) }
    val dashboard by dashboardFlow.collectAsState(initial = DashboardData())

    Scaffold(
        containerColor = AppColors.Navy,
        bottomBar = {
            NavigationBar(containerColor = AppColors.NavyLight) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Text(tab.icon, fontSize = 20.sp) },
                        label = { Text(tab.label, fontSize = 10.sp, maxLines = 1) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = AppColors.Teal,
                            unselectedTextColor = AppColors.TextSecondary,
                            indicatorColor = AppColors.TealDark
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (selected) {
            0 -> DashboardScreen(dashboard, month, contentModifier)
            1 -> IncomeScreen(db, contentModifier)
            2 -> PlaceholderScreen("အသုံးစရိတ် စီမံခန့်ခွဲမှု", "Stage 5 မှာ ဆောက်ပါမယ်", contentModifier)
            3 -> PlaceholderScreen("Sponsorship Manager", "Stage 6 မှာ ဆောက်ပါမယ်", contentModifier)
            4 -> PlaceholderScreen("လစဉ် Report", "Stage 7 မှာ ဆောက်ပါမယ်", contentModifier)
            else -> DebugScreen(db, contentModifier)
        }
    }
}
