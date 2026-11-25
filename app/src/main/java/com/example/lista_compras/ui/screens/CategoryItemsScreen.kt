package com.example.lista_compras.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lista_compras.data.ComparisonOption
import com.example.lista_compras.data.ShoppingItem
import com.example.lista_compras.data.ShoppingListCategory
import com.example.lista_compras.data.Supplier
import com.example.lista_compras.data.VariationOption
import com.example.lista_compras.data.formatCurrency
import com.example.lista_compras.ui.ShoppingViewModel
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

@Composable
fun CategoryItemsScreen(
    categoryId: String,
    viewModel: ShoppingViewModel,
    onBack: () -> Unit,
    onItemClick: (String) -> Unit
) {
    val category = viewModel.categories.find { it.id == categoryId }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ShoppingItem?>(null) }
    var itemToDelete by remember { mutableStateOf<ShoppingItem?>(null) }
    var showSummaryScreen by remember { mutableStateOf(false) }
    var showSuppliersScreen by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            try {
                if (category != null) {
                    val json = exportCategoryToJson(category)
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(json.toByteArray())
                    }
                    Toast.makeText(context, "Lista salva com sucesso!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao salvar lista: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val json = context.contentResolver.openInputStream(uri)?.bufferedReader().use { it?.readText() }
                if (json != null) {
                    val importedCat = importCategoryFromJson(json)
                    if (importedCat != null) {
                        viewModel.importCategory(importedCat)
                        Toast.makeText(context, "Lista recuperada com sucesso!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Arquivo JSON inválido.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Erro ao recuperar lista: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (category == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lista não encontrada.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Voltar") }
            }
        }
        return
    }

    if (showSummaryScreen) {
        BestPricesSummaryScreen(
            category = category,
            onBack = { showSummaryScreen = false }
        )
        return
    }

    if (showSuppliersScreen) {
        SuppliersManagementScreen(
            categoryId = category.id,
            viewModel = viewModel,
            onBack = { showSuppliersScreen = false }
        )
        return
    }

    Scaffold(
        topBar = {
            CompactTopAppBar(
                title = category.name,
                onBack = onBack,
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Melhores preços") },
                                onClick = {
                                    menuExpanded = false
                                    showSummaryScreen = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Gerenciar fornecedores") },
                                onClick = {
                                    menuExpanded = false
                                    showSuppliersScreen = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Salvar lista") },
                                onClick = {
                                    menuExpanded = false
                                    exportLauncher.launch("${category.name.replace(" ", "_")}_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recuperar lista") },
                                onClick = {
                                    menuExpanded = false
                                    importLauncher.launch(arrayOf("application/json", "*/*"))
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Item")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val items = category.items
            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhum item nesta lista.\nToque em '+' para adicionar.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        ShoppingItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onEdit = { itemToEdit = item },
                            onDelete = { itemToDelete = item }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ItemFormDialog(
                title = "Adicionar Item",
                initialName = "",
                onDismiss = { showAddDialog = false },
                onSave = { name ->
                    viewModel.addItem(category.id, name)
                    showAddDialog = false
                }
            )
        }

        itemToEdit?.let { item ->
            ItemFormDialog(
                title = "Editar Nome do Item",
                initialName = item.name,
                onDismiss = { itemToEdit = null },
                onSave = { name ->
                    viewModel.updateItem(category.id, item.id, name)
                    itemToEdit = null
                }
            )
        }

        itemToDelete?.let { item ->
            ConfirmDeleteDialog(
                title = "Apagar Item",
                message = "Deseja realmente apagar o item '${item.name}'? Esta ação não pode ser desfeita.",
                onConfirm = {
                    viewModel.deleteItem(category.id, item.id)
                    itemToDelete = null
                },
                onDismiss = { itemToDelete = null }
            )
        }
    }
}

fun exportCategoryToJson(category: ShoppingListCategory): String {
    val catObj = JSONObject().apply {
        put("id", category.id)
        put("name", category.name)

        val supArray = JSONArray()
        for (s in category.suppliers) {
            val sObj = JSONObject().put("id", s.id).put("name", s.name)
            supArray.put(sObj)
        }
        put("suppliers", supArray)

        val itemsArray = JSONArray()
        for (item in category.items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                val varArray = JSONArray()
                for (v in item.variations) {
                    varArray.put(JSONObject().put("id", v.id).put("name", v.name))
                }
                put("variations", varArray)
                val compArray = JSONArray()
                for (c in item.comparisonOptions) {
                    compArray.put(JSONObject().put("id", c.id).put("supplier", c.supplier).put("price", c.price))
                }
                put("comparisonOptions", compArray)
            }
            itemsArray.put(obj)
        }
        put("items", itemsArray)
    }
    return catObj.toString(4)
}

fun importCategoryFromJson(jsonString: String): ShoppingListCategory? {
    try {
        val catObj = JSONObject(jsonString)
        val catId = catObj.optString("id", UUID.randomUUID().toString())
        val catName = catObj.getString("name")

        val suppliers = mutableListOf<Supplier>()
        if (catObj.has("suppliers")) {
            val supArray = catObj.getJSONArray("suppliers")
            for (s in 0 until supArray.length()) {
                val sObj = supArray.getJSONObject(s)
                suppliers.add(Supplier(id = sObj.optString("id", UUID.randomUUID().toString()), name = sObj.getString("name")))
            }
        }

        val itemsArray = catObj.getJSONArray("items")
        val items = mutableListOf<ShoppingItem>()
        for (i in 0 until itemsArray.length()) {
            val obj = itemsArray.getJSONObject(i)
            val id = obj.optString("id", UUID.randomUUID().toString())
            val name = obj.getString("name")

            val varArray = obj.getJSONArray("variations")
            val variations = mutableListOf<VariationOption>()
            for (j in 0 until varArray.length()) {
                val vObj = varArray.getJSONObject(j)
                variations.add(VariationOption(id = vObj.optString("id", UUID.randomUUID().toString()), name = vObj.getString("name")))
            }

            val compArray = obj.getJSONArray("comparisonOptions")
            val comparisonOptions = mutableListOf<ComparisonOption>()
            for (k in 0 until compArray.length()) {
                val cObj = compArray.getJSONObject(k)
                comparisonOptions.add(
                    ComparisonOption(
                        id = cObj.optString("id", UUID.randomUUID().toString()),
                        supplier = cObj.getString("supplier"),
                        price = cObj.getDouble("price")
                    )
                )
            }
            items.add(ShoppingItem(id = id, name = name, variations = variations, comparisonOptions = comparisonOptions))
        }
        return ShoppingListCategory(id = catId, name = catName, items = items, suppliers = suppliers)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

@Composable
fun ShoppingItemCard(
    item: ShoppingItem,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val variationsText = item.variations.joinToString(", ") { it.name }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (variationsText.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Variações: $variationsText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Melhor Preço:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = formatCurrency(item.bestPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Melhor Fornecedor:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = item.bestSupplier ?: "Nenhum",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ItemFormDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome do Item (ex: Café, Arroz, Remédio...)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
