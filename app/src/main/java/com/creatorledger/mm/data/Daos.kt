package com.creatorledger.mm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)

    @Delete
    suspend fun delete(income: IncomeEntity)

    // ဝင်ငွေမှတ်တမ်းအားလုံး (ရက်စွဲအသစ်ဆုံးက အပေါ်)
    @Query("SELECT * FROM incomes ORDER BY incomeDate DESC, id DESC")
    fun observeAll(): Flow<List<IncomeEntity>>

    // လက်ခံရရှိပြီး ဝင်ငွေ (ရက်အပိုင်းအခြားတစ်ခုအတွင်း)
    @Query(
        "SELECT COALESCE(SUM(amountMmk), 0) FROM incomes " +
            "WHERE paymentStatus = 'RECEIVED' AND receivedDate BETWEEN :start AND :end"
    )
    fun observeReceivedBetween(start: String, end: String): Flow<Long>

    // မရသေးတဲ့ ဝင်ငွေ စုစုပေါင်း
    @Query("SELECT COALESCE(SUM(amountMmk), 0) FROM incomes WHERE paymentStatus = 'PENDING'")
    fun observePendingTotal(): Flow<Long>

    @Query("DELETE FROM incomes WHERE note = 'TEST'")
    suspend fun deleteTestRows()
}

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    // အသုံးစရိတ်မှတ်တမ်းအားလုံး (ရက်စွဲအသစ်ဆုံးက အပေါ်)
    @Query("SELECT * FROM expenses ORDER BY expenseDate DESC, id DESC")
    fun observeAll(): Flow<List<ExpenseEntity>>

    // ပေးချေပြီး အသုံးစရိတ် (ရက်အပိုင်းအခြားတစ်ခုအတွင်း)
    @Query(
        "SELECT COALESCE(SUM(amountMmk), 0) FROM expenses " +
            "WHERE paymentStatus = 'PAID' AND paidDate BETWEEN :start AND :end"
    )
    fun observePaidBetween(start: String, end: String): Flow<Long>

    @Query("DELETE FROM expenses WHERE note = 'TEST'")
    suspend fun deleteTestRows()
}

@Dao
interface SponsorDao {
    @Insert
    suspend fun insertDeal(deal: SponsorDealEntity): Long

    @Insert
    suspend fun insertPayment(payment: SponsorPaymentEntity): Long

    // ရက်အပိုင်းအခြားတစ်ခုအတွင်း Sponsor ဆီက လက်ခံရတဲ့ငွေ
    @Query(
        "SELECT COALESCE(SUM(amountMmk), 0) FROM sponsor_payments " +
            "WHERE receivedDate BETWEEN :start AND :end"
    )
    fun observePaymentsBetween(start: String, end: String): Flow<Long>

    // ပယ်ဖျက်မထားတဲ့ Deal တွေရဲ့ သဘောတူကြေး စုစုပေါင်း
    @Query("SELECT COALESCE(SUM(agreedFeeMmk), 0) FROM sponsor_deals WHERE status != 'CANCELLED'")
    fun observeAgreedTotal(): Flow<Long>

    // ပယ်ဖျက်မထားတဲ့ Deal တွေအတွက် လက်ခံရပြီးငွေ စုစုပေါင်း
    @Query(
        "SELECT COALESCE(SUM(p.amountMmk), 0) FROM sponsor_payments p " +
            "INNER JOIN sponsor_deals d ON p.dealId = d.id WHERE d.status != 'CANCELLED'"
    )
    fun observePaymentsTotal(): Flow<Long>

    // ငွေမပြည့်သေးတဲ့ Deal အရေအတွက်
    @Query(
        "SELECT COUNT(*) FROM sponsor_deals d WHERE d.status != 'CANCELLED' AND d.agreedFeeMmk > " +
            "(SELECT COALESCE(SUM(p.amountMmk), 0) FROM sponsor_payments p WHERE p.dealId = d.id)"
    )
    fun observeUnpaidDealCount(): Flow<Long>

    @Query("DELETE FROM sponsor_deals WHERE note = 'TEST'")
    suspend fun deleteTestDeals()
}
