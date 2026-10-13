package com.creatorledger.mm.data

import androidx.room.withTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONArray
import org.json.JSONObject
import com.creatorledger.mm.util.tr

// Backup ဖိုင်ထဲက Data အားလုံး
data class BackupData(
    val createdAt: String,
    val incomes: List<IncomeEntity>,
    val expenses: List<ExpenseEntity>,
    val deals: List<SponsorDealEntity>,
    val payments: List<SponsorPaymentEntity>
)

enum class CsvKind { INCOMES, EXPENSES, SPONSOR_DEALS, SPONSOR_PAYMENTS }

// Backup ဖိုင်ပုံစံ မမှန်ရင် ပစ်တဲ့ Error (မြန်မာစာနဲ့ အကြောင်းပြချက်ပါတယ်)
class BackupFormatException(message: String) : Exception(message)

private const val BACKUP_APP_NAME = "CreatorLedgerMM"
private const val BACKUP_VERSION = 1

// ---------------------------------------------------------------
// JSON Backup ထုတ်ခြင်း
// ---------------------------------------------------------------
suspend fun buildBackupJson(db: AppDatabase): String {
    val root = JSONObject()
    root.put("app", BACKUP_APP_NAME)
    root.put("backupVersion", BACKUP_VERSION)
    root.put("createdAt", LocalDateTime.now().toString())

    val incomes = JSONArray()
    for (i in db.incomeDao().getAll()) {
        val o = JSONObject()
        o.put("id", i.id)
        o.put("source", i.source)
        o.put("platform", i.platform)
        o.put("description", i.description)
        o.put("amountMmk", i.amountMmk)
        o.put("incomeDate", i.incomeDate)
        o.put("receivedDate", i.receivedDate ?: JSONObject.NULL)
        o.put("paymentStatus", i.paymentStatus)
        o.put("note", i.note)
        incomes.put(o)
    }
    root.put("incomes", incomes)

    val expenses = JSONArray()
    for (e in db.expenseDao().getAll()) {
        val o = JSONObject()
        o.put("id", e.id)
        o.put("category", e.category)
        o.put("description", e.description)
        o.put("amountMmk", e.amountMmk)
        o.put("expenseDate", e.expenseDate)
        o.put("paidDate", e.paidDate ?: JSONObject.NULL)
        o.put("paymentStatus", e.paymentStatus)
        o.put("note", e.note)
        expenses.put(o)
    }
    root.put("expenses", expenses)

    val deals = JSONArray()
    for (d in db.sponsorDao().getAllDeals()) {
        val o = JSONObject()
        o.put("id", d.id)
        o.put("brandName", d.brandName)
        o.put("campaignName", d.campaignName)
        o.put("agreedFeeMmk", d.agreedFeeMmk)
        o.put("dealDate", d.dealDate)
        o.put("deliveryDeadline", d.deliveryDeadline ?: JSONObject.NULL)
        o.put("paymentDueDate", d.paymentDueDate ?: JSONObject.NULL)
        o.put("status", d.status)
        o.put("note", d.note)
        deals.put(o)
    }
    root.put("sponsorDeals", deals)

    val payments = JSONArray()
    for (p in db.sponsorDao().getAllPayments()) {
        val o = JSONObject()
        o.put("id", p.id)
        o.put("dealId", p.dealId)
        o.put("amountMmk", p.amountMmk)
        o.put("receivedDate", p.receivedDate)
        o.put("note", p.note)
        payments.put(o)
    }
    root.put("sponsorPayments", payments)

    return root.toString(2)
}

// ---------------------------------------------------------------
// JSON Backup ဖတ်ခြင်း (Database ကို မထိခင် အကုန်စစ်ဆေးတယ်)
// ---------------------------------------------------------------
private fun JSONObject.reqString(key: String): String {
    if (isNull(key)) throw BackupFormatException("'" + key + tr("' မရှိပါ"))
    return getString(key)
}

private fun JSONObject.optStringOrEmpty(key: String): String {
    return if (isNull(key)) "" else getString(key)
}

private fun JSONObject.optStringOrNull(key: String): String? {
    return if (isNull(key)) null else getString(key)
}

private fun JSONObject.reqAmount(key: String): Long {
    if (isNull(key)) throw BackupFormatException("'" + key + tr("' မရှိပါ"))
    val v = getLong(key)
    if (v < 0L) throw BackupFormatException("'" + key + tr("' က အနှုတ်ဂဏန်း ဖြစ်နေပါတယ်"))
    return v
}

private fun checkDate(value: String?, key: String): String? {
    if (value == null) return null
    try {
        LocalDate.parse(value)
    } catch (e: Exception) {
        throw BackupFormatException("'" + key + tr("' ရက်စွဲပုံစံ မမှန်ပါ"))
    }
    return value
}

fun parseBackup(text: String): BackupData {
    val root = try {
        JSONObject(text)
    } catch (e: Exception) {
        throw BackupFormatException(tr("ဒီဖိုင်က JSON Backup ဖိုင် မဟုတ်ပါ"))
    }
    if (root.optString("app") != BACKUP_APP_NAME) {
        throw BackupFormatException(tr("ဒီဖိုင်က CreatorLedger MM ရဲ့ Backup ဖိုင် မဟုတ်ပါ"))
    }
    if (root.optInt("backupVersion", -1) != BACKUP_VERSION) {
        throw BackupFormatException(tr("Backup ဗားရှင်း မကိုက်ပါ"))
    }

    try {
        val incomes = ArrayList<IncomeEntity>()
        val incomeArr = root.getJSONArray("incomes")
        for (idx in 0 until incomeArr.length()) {
            val o = incomeArr.getJSONObject(idx)
            val status = o.reqString("paymentStatus")
            if (status != PaymentStatus.RECEIVED && status != PaymentStatus.PENDING) {
                throw BackupFormatException(tr("ဝင်ငွေ paymentStatus မမှန်ပါ"))
            }
            val receivedDate = checkDate(o.optStringOrNull("receivedDate"), "receivedDate")
            if (status == PaymentStatus.RECEIVED && receivedDate == null) {
                throw BackupFormatException(tr("လက်ခံရရှိပြီး ဝင်ငွေမှာ receivedDate မရှိပါ"))
            }
            incomes.add(
                IncomeEntity(
                    id = o.getLong("id"),
                    source = o.reqString("source"),
                    platform = o.reqString("platform"),
                    description = o.optStringOrEmpty("description"),
                    amountMmk = o.reqAmount("amountMmk"),
                    incomeDate = checkDate(o.reqString("incomeDate"), "incomeDate") ?: "",
                    receivedDate = receivedDate,
                    paymentStatus = status,
                    note = o.optStringOrEmpty("note")
                )
            )
        }

        val expenses = ArrayList<ExpenseEntity>()
        val expenseArr = root.getJSONArray("expenses")
        for (idx in 0 until expenseArr.length()) {
            val o = expenseArr.getJSONObject(idx)
            val status = o.reqString("paymentStatus")
            if (status != PaymentStatus.PAID && status != PaymentStatus.UNPAID) {
                throw BackupFormatException(tr("အသုံးစရိတ် paymentStatus မမှန်ပါ"))
            }
            val paidDate = checkDate(o.optStringOrNull("paidDate"), "paidDate")
            if (status == PaymentStatus.PAID && paidDate == null) {
                throw BackupFormatException(tr("ပေးချေပြီး အသုံးစရိတ်မှာ paidDate မရှိပါ"))
            }
            expenses.add(
                ExpenseEntity(
                    id = o.getLong("id"),
                    category = o.reqString("category"),
                    description = o.optStringOrEmpty("description"),
                    amountMmk = o.reqAmount("amountMmk"),
                    expenseDate = checkDate(o.reqString("expenseDate"), "expenseDate") ?: "",
                    paidDate = paidDate,
                    paymentStatus = status,
                    note = o.optStringOrEmpty("note")
                )
            )
        }

        val validDealStatuses = setOf(
            DealStatus.NEGOTIATING, DealStatus.ACTIVE, DealStatus.DELIVERED,
            DealStatus.COMPLETED, DealStatus.CANCELLED
        )
        val deals = ArrayList<SponsorDealEntity>()
        val dealArr = root.getJSONArray("sponsorDeals")
        for (idx in 0 until dealArr.length()) {
            val o = dealArr.getJSONObject(idx)
            val status = o.reqString("status")
            if (status !in validDealStatuses) {
                throw BackupFormatException(tr("Sponsor Deal status မမှန်ပါ"))
            }
            deals.add(
                SponsorDealEntity(
                    id = o.getLong("id"),
                    brandName = o.reqString("brandName"),
                    campaignName = o.optStringOrEmpty("campaignName"),
                    agreedFeeMmk = o.reqAmount("agreedFeeMmk"),
                    dealDate = checkDate(o.reqString("dealDate"), "dealDate") ?: "",
                    deliveryDeadline = checkDate(o.optStringOrNull("deliveryDeadline"), "deliveryDeadline"),
                    paymentDueDate = checkDate(o.optStringOrNull("paymentDueDate"), "paymentDueDate"),
                    status = status,
                    note = o.optStringOrEmpty("note")
                )
            )
        }

        val payments = ArrayList<SponsorPaymentEntity>()
        val paymentArr = root.getJSONArray("sponsorPayments")
        for (idx in 0 until paymentArr.length()) {
            val o = paymentArr.getJSONObject(idx)
            payments.add(
                SponsorPaymentEntity(
                    id = o.getLong("id"),
                    dealId = o.getLong("dealId"),
                    amountMmk = o.reqAmount("amountMmk"),
                    receivedDate = checkDate(o.reqString("receivedDate"), "receivedDate") ?: "",
                    note = o.optStringOrEmpty("note")
                )
            )
        }

        // ID ထပ်နေတာ၊ Deal မရှိဘဲ ငွေလက်ခံမှတ်တမ်းရှိတာ၊ လက်ခံငွေ ကြေးထက်ကျော်တာတွေကို စစ်တယ်
        if (incomes.map { it.id }.toSet().size != incomes.size ||
            expenses.map { it.id }.toSet().size != expenses.size ||
            deals.map { it.id }.toSet().size != deals.size ||
            payments.map { it.id }.toSet().size != payments.size
        ) {
            throw BackupFormatException(tr("ID ထပ်နေတဲ့ မှတ်တမ်းတွေ ပါနေပါတယ်"))
        }
        val dealById = deals.associateBy { it.id }
        val paidByDeal = HashMap<Long, Long>()
        for (p in payments) {
            if (dealById[p.dealId] == null) {
                throw BackupFormatException(tr("မရှိတဲ့ Deal အတွက် ငွေလက်ခံမှတ်တမ်း ပါနေပါတယ်"))
            }
            paidByDeal[p.dealId] = (paidByDeal[p.dealId] ?: 0L) + p.amountMmk
        }
        for (d in deals) {
            if ((paidByDeal[d.id] ?: 0L) > d.agreedFeeMmk) {
                throw BackupFormatException(d.brandName + tr(" Deal ရဲ့ လက်ခံငွေက သဘောတူကြေးထက် ကျော်နေပါတယ်"))
            }
        }

        return BackupData(
            createdAt = root.optString("createdAt", ""),
            incomes = incomes,
            expenses = expenses,
            deals = deals,
            payments = payments
        )
    } catch (e: BackupFormatException) {
        throw e
    } catch (e: Exception) {
        throw BackupFormatException(tr("Backup ဖိုင်ထဲက အချက်အလက်တွေ မမှန်ပါ"))
    }
}

// ---------------------------------------------------------------
// Restore - အကုန်လုံး အောင်မြင်မှ ပြောင်းတယ် (တစ်ခုပျက်ရင် မူလအတိုင်း ပြန်ကျန်တယ်)
// ---------------------------------------------------------------
suspend fun restoreBackup(db: AppDatabase, data: BackupData) {
    db.withTransaction {
        db.sponsorDao().deleteAllPayments()
        db.sponsorDao().deleteAllDeals()
        db.incomeDao().deleteAll()
        db.expenseDao().deleteAll()

        db.incomeDao().insertAll(data.incomes)
        db.expenseDao().insertAll(data.expenses)
        db.sponsorDao().insertDeals(data.deals)
        db.sponsorDao().insertPayments(data.payments)
    }
}

// ---------------------------------------------------------------
// CSV ထုတ်ခြင်း (Excel မှာ မြန်မာစာမှန်အောင် UTF-8 BOM ထည့်တယ်)
// ---------------------------------------------------------------
private fun csvCell(value: Any?): String {
    var s = value?.toString() ?: ""
    // Excel မှာ Formula အဖြစ် အလုပ်မလုပ်အောင် ကာကွယ်တယ်
    if (s.isNotEmpty() && (s[0] == '=' || s[0] == '+' || s[0] == '-' || s[0] == '@')) {
        s = "'" + s
    }
    return "\"" + s.replace("\"", "\"\"") + "\""
}

suspend fun buildCsv(db: AppDatabase, kind: CsvKind): String {
    val sb = StringBuilder()
    sb.append('\uFEFF')

    fun row(vararg cells: Any?) {
        sb.append(cells.joinToString(",") { csvCell(it) })
        sb.append("\r\n")
    }

    when (kind) {
        CsvKind.INCOMES -> {
            row("id", "source", "platform", "description", "amountMmk", "incomeDate", "receivedDate", "paymentStatus", "note")
            for (i in db.incomeDao().getAll()) {
                row(i.id, i.source, i.platform, i.description, i.amountMmk, i.incomeDate, i.receivedDate, i.paymentStatus, i.note)
            }
        }
        CsvKind.EXPENSES -> {
            row("id", "category", "description", "amountMmk", "expenseDate", "paidDate", "paymentStatus", "note")
            for (e in db.expenseDao().getAll()) {
                row(e.id, e.category, e.description, e.amountMmk, e.expenseDate, e.paidDate, e.paymentStatus, e.note)
            }
        }
        CsvKind.SPONSOR_DEALS -> {
            row("id", "brandName", "campaignName", "agreedFeeMmk", "dealDate", "deliveryDeadline", "paymentDueDate", "status", "note")
            for (d in db.sponsorDao().getAllDeals()) {
                row(d.id, d.brandName, d.campaignName, d.agreedFeeMmk, d.dealDate, d.deliveryDeadline, d.paymentDueDate, d.status, d.note)
            }
        }
        CsvKind.SPONSOR_PAYMENTS -> {
            row("id", "dealId", "amountMmk", "receivedDate", "note")
            for (p in db.sponsorDao().getAllPayments()) {
                row(p.id, p.dealId, p.amountMmk, p.receivedDate, p.note)
            }
        }
    }
    return sb.toString()
}
