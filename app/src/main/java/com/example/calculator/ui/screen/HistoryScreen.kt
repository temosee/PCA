package com.example.calculator.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.calculator.model.Calculation

@Composable
fun HistoryScreen(history: List<Calculation>, onItemClick: (String) -> Unit) {
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("История пуста", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(24.dp)
    ) {
        item {
            Text(
                "История расчетов",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        items(history) { calc ->
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(calc.id) }
            ) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Итог: ${calc.totalWithTip}", fontWeight = FontWeight.Bold)
                        Text("По ${calc.perPerson} с человека", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("${calc.people} чел.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
