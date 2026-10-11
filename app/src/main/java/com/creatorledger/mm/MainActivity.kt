package com.creatorledger.mm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.creatorledger.mm.ui.CreatorLedgerApp
import com.creatorledger.mm.ui.CreatorLedgerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreatorLedgerTheme {
                CreatorLedgerApp()
            }
        }
    }
}
