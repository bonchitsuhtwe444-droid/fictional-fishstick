package com.creatorledger.mm.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// ငွေပမာဏအားလုံးကို MMK အပြည့်ဂဏန်း (Long) နဲ့ သိမ်းတယ် (ဒသမ မသုံးဘူး)
// ရက်စွဲအားလုံးကို "2026-10-12" ပုံစံ စာသားနဲ့ သိမ်းတယ်

// တန်ဖိုး (Status) များ
object PaymentStatus {
    const val RECEIVED = "RECEIVED"   // ဝင်ငွေ - လက်ခံရရှိပြီး
    const val PENDING = "PENDING"     // ဝင်ငွေ - မရသေး
    const val PAID = "PAID"           // အသုံးစရိတ် - ပေးချေပြီး
    const val UNPAID = "UNPAID"       // အသုံးစရိတ် - မပေးရသေး
}

object DealStatus {
    const val NEGOTIATING = "NEGOTIATING"
    const val ACTIVE = "ACTIVE"
    const val DELIVERED = "DELIVERED"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val platform: String,          // YOUTUBE, FACEBOOK, TIKTOK, SPONSORSHIP, FREELANCE, OTHER
    val description: String = "",
    val amountMmk: Long,
    val incomeDate: String,        // ဝင်ငွေရရှိပိုင်ခွင့်ရတဲ့ရက်
    val receivedDate: String? = null, // ငွေအမှန်တကယ်လက်ခံရတဲ့ရက် (မရသေးရင် null)
    val paymentStatus: String,
    val note: String = ""
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,          // VIDEO_EDITING, VOICE_OVER, THUMBNAIL, SOFTWARE, INTERNET, EQUIPMENT, ADVERTISING, OTHER
    val description: String = "",
    val amountMmk: Long,
    val expenseDate: String,       // အသုံးစရိတ်ဖြစ်တဲ့ရက်
    val paidDate: String? = null,  // ငွေအမှန်တကယ်ပေးချေတဲ့ရက် (မပေးရသေးရင် null)
    val paymentStatus: String,
    val note: String = ""
)

@Entity(tableName = "sponsor_deals")
data class SponsorDealEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val brandName: String,
    val campaignName: String = "",
    val agreedFeeMmk: Long,        // သဘောတူထားတဲ့ကြေး
    val dealDate: String,
    val deliveryDeadline: String? = null,
    val paymentDueDate: String? = null,
    val status: String,
    val note: String = ""
)

// Sponsor Deal တစ်ခုအတွက် ငွေလက်ခံတဲ့ အကြိမ်တစ်ခုချင်း
// လက်ခံရရှိပြီးငွေ = ဒီမှတ်တမ်းတွေရဲ့ ပေါင်းလဒ်၊ ရရန်ကျန်ငွေ = agreedFeeMmk - ပေါင်းလဒ်
@Entity(
    tableName = "sponsor_payments",
    foreignKeys = [
        ForeignKey(
            entity = SponsorDealEntity::class,
            parentColumns = ["id"],
            childColumns = ["dealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dealId")]
)
data class SponsorPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dealId: Long,
    val amountMmk: Long,
    val receivedDate: String,
    val note: String = ""
)
