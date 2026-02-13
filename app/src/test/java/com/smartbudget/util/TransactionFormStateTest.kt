package com.smartbudget.util

import com.smartbudget.data.entity.Recurrence
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.viewmodel.TransactionFormState
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TransactionFormStateTest {

    @Test
    fun `default form state has correct defaults`() {
        val state = TransactionFormState()
        assertEquals("", state.name)
        assertEquals(TransactionType.EXPENSE, state.type)
        assertEquals("", state.amount)
        assertNull(state.categoryId)
        assertEquals(LocalDate.now(), state.date)
        assertEquals("", state.note)
        assertEquals(Recurrence.NONE, state.recurrence)
        assertFalse(state.isEditing)
        assertNull(state.editingId)
    }

    @Test
    fun `form state copy works correctly`() {
        val state = TransactionFormState(name = "Test", amount = "100.00")
        val updated = state.copy(type = TransactionType.INCOME)
        assertEquals("Test", updated.name)
        assertEquals("100.00", updated.amount)
        assertEquals(TransactionType.INCOME, updated.type)
    }

    @Test
    fun `editing state is set correctly`() {
        val state = TransactionFormState(isEditing = true, editingId = 42L)
        assertTrue(state.isEditing)
        assertEquals(42L, state.editingId)
    }
}
