package com.creatorledger.mm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.creatorledger.mm.R
import com.creatorledger.mm.util.tr

// App စဖွင့်ချင်း ပြမယ့် Loading မျက်နှာပြင်
@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Navy),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier.size(220.dp)
        )
        Text(
            text = "CreatorLedger MM",
            color = AppColors.Teal,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = tr("မြန်မာ Creator များအတွက် ငွေစာရင်း"),
            color = AppColors.TextSecondary,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        CircularProgressIndicator(color = AppColors.Teal)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = tr("ဖွင့်နေပါတယ်..."),
            color = AppColors.TextSecondary,
            fontSize = 14.sp
        )
    }
}
