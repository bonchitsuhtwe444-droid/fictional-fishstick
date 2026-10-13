package com.creatorledger.mm.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.data.BackupData
import com.creatorledger.mm.data.BackupFormatException
import com.creatorledger.mm.data.CsvKind
import com.creatorledger.mm.data.buildBackupJson
import com.creatorledger.mm.data.buildCsv
import com.creatorledger.mm.data.parseBackup
import com.creatorledger.mm.data.restoreBackup
import com.creatorledger.mm.util.AppLanguage
import com.creatorledger.mm.util.LANG_EN
import com.creatorledger.mm.util.LANG_MY
import com.creatorledger.mm.util.saveLanguage
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.creatorledger.mm.util.tr

private suspend fun writeTextToUri(context: Context, uri: Uri, text: String) {
    withContext(Dispatchers.IO) {
        val out = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw java.io.IOException(tr("ဖိုင်ဖွင့်လို့ မရပါ"))
        out.use { stream -> stream.write(text.toByteArray(Charsets.UTF_8)) }
    }
}

private suspend fun readTextFromUri(context: Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        val input = context.contentResolver.openInputStream(uri)
            ?: throw java.io.IOException(tr("ဖိုင်ဖွင့်လို့ မရပါ"))
        input.use { stream ->
            val bytes = stream.readBytes()
            if (bytes.size > 20_000_000) {
                throw BackupFormatException(tr("ဖိုင်က ကြီးလွန်းပါတယ်"))
            }
            String(bytes, Charsets.UTF_8)
        }
    }
}

@Composable
fun SettingsScreen(db: AppDatabase, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("") }
    var pendingCsv by remember { mutableStateOf<CsvKind?>(null) }
    var restorePreview by remember { mutableStateOf<BackupData?>(null) }

    // JSON Backup သိမ်းမယ့် နေရာရွေးတဲ့ စနစ် (Permission မလိုပါ)
    val jsonSaver = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                message = try {
                    writeTextToUri(context, uri, buildBackupJson(db))
                    tr("Backup ဖိုင် သိမ်းပြီးပါပြီ")
                } catch (e: Exception) {
                    tr("Backup သိမ်းလို့ မရပါ")
                }
            }
        }
    }

    // CSV သိမ်းမယ့် နေရာရွေးတဲ့ စနစ်
    val csvSaver = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        val kind = pendingCsv
        if (uri != null && kind != null) {
            scope.launch {
                message = try {
                    writeTextToUri(context, uri, buildCsv(db, kind))
                    tr("CSV ဖိုင် သိမ်းပြီးပါပြီ")
                } catch (e: Exception) {
                    tr("CSV သိမ်းလို့ မရပါ")
                }
            }
        }
    }

    // Restore လုပ်မယ့် Backup ဖိုင်ရွေးတဲ့ စနစ်
    val restoreOpener = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                message = try {
                    val text = readTextFromUri(context, uri)
                    restorePreview = parseBackup(text)
                    ""
                } catch (e: BackupFormatException) {
                    e.message ?: tr("Backup ဖိုင် မမှန်ပါ")
                } catch (e: Exception) {
                    tr("ဖိုင်ဖတ်လို့ မရပါ")
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(tr("ဆက်တင်နှင့် Backup"), color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)

        Text(tr("ဘာသာစကား / Language"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterOption(tr("မြန်မာ"), AppLanguage.code != LANG_EN) {
                AppLanguage.code = LANG_MY
                saveLanguage(context, LANG_MY)
            }
            FilterOption("English", AppLanguage.code == LANG_EN) {
                AppLanguage.code = LANG_EN
                saveLanguage(context, LANG_EN)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = AppColors.NavyLight)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(tr("သတိပြုရန်"), color = AppColors.Warning, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    tr("သင့်ရဲ့ ငွေစာရင်း Data တွေက ဖုန်းထဲမှာပဲ သိမ်းထားပါတယ်။ ") +
                        tr("Backup မလုပ်ဘဲ App ကို ဖျက်ရင် (Uninstall) Data အားလုံး ပျောက်သွားနိုင်ပါတယ်။ ") +
                        tr("ပုံမှန် Backup လုပ်ပြီး ဖိုင်ကို ဖုန်းအပြင်ဘက် (ဥပမာ Google Drive) မှာလည်း သိမ်းထားပါ။"),
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Text(tr("Backup (Data အကုန်သိမ်းခြင်း)"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { jsonSaver.launch("creatorledger-backup-" + LocalDate.now() + ".json") }
        ) { Text(tr("Backup ဖိုင် (JSON) သိမ်းမယ်")) }

        Text(tr("Restore (Backup ဖိုင်ကနေ ပြန်ယူခြင်း)"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = { restoreOpener.launch(arrayOf("*/*")) }
        ) { Text(tr("Backup ဖိုင်ရွေးပြီး Restore လုပ်မယ်")) }

        Text(tr("CSV ထုတ်ယူခြင်း (Excel အတွက်)"), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                pendingCsv = CsvKind.INCOMES
                csvSaver.launch("creatorledger-incomes-" + LocalDate.now() + ".csv")
            }
        ) { Text(tr("ဝင်ငွေ CSV")) }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                pendingCsv = CsvKind.EXPENSES
                csvSaver.launch("creatorledger-expenses-" + LocalDate.now() + ".csv")
            }
        ) { Text(tr("အသုံးစရိတ် CSV")) }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                pendingCsv = CsvKind.SPONSOR_DEALS
                csvSaver.launch("creatorledger-sponsor-deals-" + LocalDate.now() + ".csv")
            }
        ) { Text("Sponsor Deal CSV") }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                pendingCsv = CsvKind.SPONSOR_PAYMENTS
                csvSaver.launch("creatorledger-sponsor-payments-" + LocalDate.now() + ".csv")
            }
        ) { Text(tr("Sponsor ငွေလက်ခံမှတ်တမ်း CSV")) }

        if (message.isNotEmpty()) {
            Text(message, color = AppColors.Teal, fontSize = 15.sp)
        }

        Text(tr("CreatorLedger MM  ဗားရှင်း 0.1"), color = AppColors.TextSecondary, fontSize = 12.sp)
    }

    val preview = restorePreview
    if (preview != null) {
        AlertDialog(
            onDismissRequest = { restorePreview = null },
            title = { Text(tr("Restore လုပ်မလား?")) },
            text = {
                Text(
                    tr("ဒီ Backup ထဲမှာ ဝင်ငွေ ") + preview.incomes.size +
                        tr(" ခု၊ အသုံးစရိတ် ") + preview.expenses.size +
                        tr(" ခု၊ Sponsor Deal ") + preview.deals.size +
                        tr(" ခု၊ ငွေလက်ခံမှတ်တမ်း ") + preview.payments.size + tr(" ခု ရှိပါတယ်။\n\n") +
                        tr("Restore လုပ်ရင် လက်ရှိ App ထဲက Data အားလုံးကို ဒီ Backup နဲ့ အစားထိုးပါမယ်။ ") +
                        tr("လက်ရှိ Data ကို ပြန်ယူချင်ရင် အရင် Backup လုပ်ထားပါ။")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        restorePreview = null
                        scope.launch {
                            message = try {
                                restoreBackup(db, preview)
                                tr("Restore ပြီးပါပြီ")
                            } catch (e: Exception) {
                                tr("Restore မအောင်မြင်ပါ၊ လက်ရှိ Data မပြောင်းလဲပါ")
                            }
                        }
                    }
                ) { Text(tr("အစားထိုးမယ်")) }
            },
            dismissButton = {
                TextButton(onClick = { restorePreview = null }) { Text(tr("မလုပ်တော့ဘူး")) }
            }
        )
    }
}
