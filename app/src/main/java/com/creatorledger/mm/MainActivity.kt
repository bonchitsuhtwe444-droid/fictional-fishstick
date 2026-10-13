package com.creatorledger.mm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.creatorledger.mm.data.AppDatabase
import com.creatorledger.mm.ui.CreatorLedgerApp
import com.creatorledger.mm.ui.CreatorLedgerTheme
import com.creatorledger.mm.ui.SplashScreen
import com.creatorledger.mm.util.AppLanguage
import com.creatorledger.mm.util.loadLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // သိမ်းထားတဲ့ ဘာသာစကားကို အရင်ဖတ်တယ်
        AppLanguage.code = loadLanguage(this)

        setContent {
            CreatorLedgerTheme {
                var ready by remember { mutableStateOf(false) }

                // Loading: Database ကို အမှန်တကယ်ဖွင့်ပြီး အနည်းဆုံး ၁.၂ စက္ကန့် Splash ပြတယ်
                LaunchedEffect(Unit) {
                    val started = System.currentTimeMillis()
                    withContext(Dispatchers.IO) {
                        AppDatabase.get(applicationContext).openHelper.writableDatabase
                    }
                    val elapsed = System.currentTimeMillis() - started
                    if (elapsed < 1200L) {
                        delay(1200L - elapsed)
                    }
                    ready = true
                }

                if (ready) {
                    CreatorLedgerApp()
                } else {
                    SplashScreen()
                }
            }
        }
    }
}
