package com.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smartbudget.R
import com.smartbudget.data.dao.TransactionWithCategory
import com.smartbudget.data.entity.TransactionType
import com.smartbudget.ui.theme.*
import com.smartbudget.ui.util.CurrencyFormatter
import com.smartbudget.ui.util.IconMapper
import com.smartbudget.ui.util.toComposeColor

@Composable
fun TransactionItem(
    transaction: TransactionWithCategory,
    onToggleValidation: (Long) -> Unit,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val amountPrefix = if (isIncome) "+" else "-"
    val categoryColor = transaction.categoryColor?.toComposeColor() ?: MaterialTheme.colorScheme.primary

    Column(modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = IconMapper.getIcon(transaction.categoryIcon ?: "more_horiz"),
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and category
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.name.ifBlank { transaction.categoryName ?: stringResource(R.string.no_category) },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (transaction.isValidated) MaterialTheme.colorScheme.onSurface
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (transaction.recurrence != "NONE") {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.Repeat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (transaction.name.isNotBlank() && transaction.categoryName != null) {
                    Text(
                        text = transaction.categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = "\u200E$amountPrefix${CurrencyFormatter.format(transaction.amount)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = amountColor
            )
        }
        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
