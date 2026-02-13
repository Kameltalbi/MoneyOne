package com.smartbudget.util

import com.smartbudget.data.entity.*
import org.junit.Assert.*
import org.junit.Test

class EntityTest {

    @Test
    fun `Account default values`() {
        val account = Account(name = "Test")
        assertEquals(0L, account.id)
        assertEquals("Test", account.name)
        assertFalse(account.isDefault)
        assertEquals("", account.currency)
    }

    @Test
    fun `Account with currency`() {
        val account = Account(name = "EUR Account", currency = "EUR")
        assertEquals("EUR", account.currency)
    }

    @Test
    fun `Transaction default values`() {
        val tx = Transaction(amount = 50.0, type = TransactionType.EXPENSE, accountId = 1, date = 1000L)
        assertEquals("", tx.name)
        assertEquals(50.0, tx.amount, 0.01)
        assertEquals(TransactionType.EXPENSE, tx.type)
        assertNull(tx.categoryId)
        assertEquals(1L, tx.accountId)
        assertEquals("", tx.note)
        assertTrue(tx.isValidated)
        assertEquals(Recurrence.NONE, tx.recurrence)
        assertNull(tx.recurrenceEndDate)
    }

    @Test
    fun `Transaction income type`() {
        val tx = Transaction(amount = 1000.0, type = TransactionType.INCOME, accountId = 1, date = 1000L)
        assertEquals(TransactionType.INCOME, tx.type)
    }

    @Test
    fun `Category default values`() {
        val cat = Category(name = "Food", icon = "restaurant", color = 0xFF000000, type = TransactionType.EXPENSE)
        assertEquals("Food", cat.name)
        assertEquals("restaurant", cat.icon)
        assertFalse(cat.isDefault)
    }

    @Test
    fun `Recurrence enum values`() {
        val values = Recurrence.values()
        assertEquals(7, values.size)
        assertTrue(values.contains(Recurrence.NONE))
        assertTrue(values.contains(Recurrence.WEEKLY))
        assertTrue(values.contains(Recurrence.MONTHLY))
        assertTrue(values.contains(Recurrence.QUARTERLY))
        assertTrue(values.contains(Recurrence.FOUR_MONTHLY))
        assertTrue(values.contains(Recurrence.SEMI_ANNUAL))
        assertTrue(values.contains(Recurrence.ANNUAL))
    }

    @Test
    fun `TransactionType enum values`() {
        val values = TransactionType.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(TransactionType.INCOME))
        assertTrue(values.contains(TransactionType.EXPENSE))
    }

    @Test
    fun `SavingsGoal default values`() {
        val goal = SavingsGoal(name = "Vacation", targetAmount = 5000.0)
        assertEquals(0L, goal.id)
        assertEquals("Vacation", goal.name)
        assertEquals(5000.0, goal.targetAmount, 0.01)
        assertEquals(0.0, goal.currentAmount, 0.01)
        assertEquals("savings", goal.icon)
        assertNull(goal.targetDate)
    }

    @Test
    fun `SavingsGoal progress calculation`() {
        val goal = SavingsGoal(name = "Car", targetAmount = 10000.0, currentAmount = 2500.0)
        val progress = goal.currentAmount / goal.targetAmount
        assertEquals(0.25, progress, 0.01)
    }
}
