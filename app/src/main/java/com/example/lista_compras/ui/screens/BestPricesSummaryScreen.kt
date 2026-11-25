package com.example.lista_compras.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.lista_compras.data.ShoppingItem
import com.example.lista_compras.data.ShoppingListCategory
import com.example.lista_compras.data.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BestPricesSummaryScreen(
    category: ShoppingListCategory,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val summaryText = remember(category) {
        generateBestPricesSummary(category)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Melhores preços: ${category.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(summaryText))
                            Toast.makeText(context, "Lista copiada para a área de transferência!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copiar Texto da Lista")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = summaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

fun generateBestPricesSummary(category: ShoppingListCategory): String {
    val itemsWithSuppliers = category.items.map { item ->
        val best = item.bestOption
        Triple(item, best?.supplier ?: "Sem fornecedor definido", best?.price ?: Double.MAX_VALUE)
    }

    val sorted = itemsWithSuppliers.sortedWith(
        compareBy<Triple<ShoppingItem, String, Double>> { it.second }
            .thenBy { it.first.name }
    )

    val groupedBySupplier = sorted.groupBy { it.second }

    val sb = StringBuilder()
    sb.append("🛒 Lista de Compras: ${category.name}\n\n")

    var grandTotal = 0.0

    for ((supplier, triples) in groupedBySupplier) {
        sb.append("📍 Fornecedor: $supplier\n")
        for ((item, _, price) in triples) {
            val priceStr = if (price == Double.MAX_VALUE) "Preço não definido" else formatCurrency(price)
            if (price != Double.MAX_VALUE) grandTotal += price

            val variationsStr = item.variations.joinToString(", ") { itemVar -> itemVar.name }
            val varInfo = if (variationsStr.isNotBlank()) " ($variationsStr)" else ""

            sb.append("  • ${item.name}$varInfo: $priceStr\n")
        }
        sb.append("\n")
    }

    if (grandTotal > 0.0) {
        sb.append("💰 Total Estimado: ${formatCurrency(grandTotal)}")
    }

    return sb.toString()
}
