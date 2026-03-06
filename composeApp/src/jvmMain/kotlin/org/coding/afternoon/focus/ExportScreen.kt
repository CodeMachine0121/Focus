package org.coding.afternoon.focus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExportScreen(viewModel: ExportViewModel) {
    LaunchedEffect(Unit) { viewModel.generateReport() }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("📤 Export & Reports", fontWeight = FontWeight.Bold, fontSize = 22.sp)

        // Period Selector
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Report Period", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportPeriod.entries.forEach { period ->
                        FilterChip(
                            selected = viewModel.selectedPeriod == period,
                            onClick = { viewModel.selectPeriod(period) },
                            label = { Text(period.label, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Report Preview
        val report = viewModel.report
        if (report != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📊 Report Preview", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatBox("Sessions", "${report.totalSessions}")
                        StatBox("Hours", "${"%.1f".format(report.totalHours)}")
                        StatBox("Avg", "${report.avgSessionMinutes.toInt()} min")
                        StatBox("Best Day", "${report.bestDayMinutes} min")
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1E1E1E),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            report.formattedText,
                            modifier = Modifier.padding(12.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF00FF41)
                        )
                    }
                }
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No sessions found for this period", color = Color.Gray)
                }
            }
        }

        // Action Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.exportCsv() }, modifier = Modifier.weight(1f)) {
                Text("💾 Export CSV")
            }
            OutlinedButton(
                onClick = { viewModel.copyReport() },
                modifier = Modifier.weight(1f),
                enabled = report != null
            ) {
                Text("📋 Copy Report")
            }
        }

        // Status Message
        if (viewModel.statusMessage.isNotEmpty()) {
            Text(
                viewModel.statusMessage,
                color = if (viewModel.statusMessage.startsWith("✅")) Color(0xFF4CAF50) else Color.Red
            )
        }
    }
}

@Composable
private fun StatBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1976D2))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}
