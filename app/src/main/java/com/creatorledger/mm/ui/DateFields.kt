package com.creatorledger.mm.ui

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate

fun showDatePicker(context: Context, current: String?, onPicked: (String) -> Unit) {
    val date = try {
        if (current == null) LocalDate.now() else LocalDate.parse(current)
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

// ရက်စွဲရွေးတဲ့ ခလုတ် (ဖြည့်ရမယ့်ရက်)
@Composable
fun DateButton(label: String, value: String, onChange: (String) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showDatePicker(context, value, onChange) }
    ) { Text(label + " - " + value) }
}

// ရက်စွဲရွေးတဲ့ ခလုတ် (မဖြည့်လည်းရတဲ့ရက်)
@Composable
fun OptionalDateField(label: String, value: String?, onChange: (String?) -> Unit) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { showDatePicker(context, value) { picked -> onChange(picked) } }
        ) { Text(label + " - " + (value ?: "မသတ်မှတ်ထားပါ")) }
        if (value != null) {
            TextButton(onClick = { onChange(null) }) { Text("ဖယ်ရှားမယ်") }
        }
    }
}
