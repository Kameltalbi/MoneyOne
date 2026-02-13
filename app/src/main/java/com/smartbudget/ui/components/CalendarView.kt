package com.smartbudget.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudget.R
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarView(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    dayBalances: Map<LocalDate, Double>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.Filled.ChevronLeft,
                        contentDescription = stringResource(R.string.previous_month),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = DateUtils.formatMonthYear(yearMonth),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = stringResource(R.string.next_month),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day of week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = listOf(
                    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
                )
                daysOfWeek.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                            .replaceFirstChar { it.uppercase() }
                            .take(3),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid
            val firstDayOfMonth = yearMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek
            val daysInMonth = yearMonth.lengthOfMonth()
            val startOffset = (firstDayOfWeek.value - 1) // Monday = 0

            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - startOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNumber)
                            val isToday = date == LocalDate.now()
                            val isSelected = date == selectedDate
                            val balance = dayBalances[date]

                            CalendarDayCell(
                                day = dayNumber,
                                isToday = isToday,
                                isSelected = isSelected,
                                balance = balance,
                                onClick = { onDateSelected(date) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            // Empty cell for days outside month
                            Box(modifier = Modifier.weight(1f).height(52.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    balance: Double?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        else -> Color.Transparent
    }

    val textColor = when {
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = if (isToday) {
                Modifier
                    .size(28.dp)
                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            } else {
                Modifier.size(28.dp)
            }
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isToday || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }

        if (balance != null && balance != 0.0) {
            Text(
                text = CurrencyFormatter.formatCompact(balance),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = if (balance >= 0) IncomeGreen else ExpenseRed,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}
