package com.example.lista_compras.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lista_compras.data.ComparisonOption
import com.example.lista_compras.data.Supplier
import com.example.lista_compras.data.VariationOption
import com.example.lista_compras.data.formatCurrency
import com.example.lista_compras.ui.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    categoryId: String,
    itemId: String,
    viewModel: ShoppingViewModel,
    onBack: () -> Unit
) {
    val category = viewModel.categories.find { it.id == categoryId }
    val item = category?.items?.find { it.id == itemId }

    var showAddVariationDialog by remember { mutableStateOf(false) }
    var variationToEdit by remember { mutableStateOf<VariationOption?>(null) }
    var variationToDelete by remember { mutableStateOf<VariationOption?>(null) }

    var showAddOptionDialog by remember { mutableStateOf(false) }
    var optionToEdit by remember { mutableStateOf<ComparisonOption?>(null) }
    var optionToDelete by remember { mutableStateOf<ComparisonOption?>(null) }

    if (item == null || category == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Item não encontrado.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Voltar") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes: ${item.name}") },
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Summary Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Melhor Preço:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = formatCurrency(item.bestPrice),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Melhor Fornecedor:", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = item.bestSupplier ?: "Nenhum",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Button: Adicionar variações
            item {
                Button(
                    onClick = { showAddVariationDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar variações", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (item.variations.isEmpty()) {
                item {
                    Text(
                        text = "Nenhuma variação cadastrada.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(item.variations, key = { "var_${it.id}" }) { variation ->
                    VariationRow(
                        variation = variation,
                        onEdit = { variationToEdit = variation },
                        onDelete = { variationToDelete = variation }
                    )
                }
            }

            // Button: Adicionar preços
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Button(
                    onClick = { showAddOptionDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Adicionar preços", style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (item.comparisonOptions.isEmpty()) {
                item {
                    Text(
                        text = "Nenhum fornecedor cadastrado.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
                items(item.comparisonOptions, key = { "comp_${it.id}" }) { option ->
                    val isBest = option.id == item.bestOption?.id
                    ComparisonOptionRow(
                        option = option,
                        isBest = isBest,
                        onEdit = { optionToEdit = option },
                        onDelete = { optionToDelete = option }
                    )
                }
            }
        }

        if (showAddVariationDialog) {
            VariationFormDialog(
                title = "Adicionar Variação",
                initialName = "",
                onDismiss = { showAddVariationDialog = false },
                onSave = { name ->
                    viewModel.addVariation(category.id, item.id, name)
                    showAddVariationDialog = false
                }
            )
        }

        variationToEdit?.let { variation ->
            VariationFormDialog(
                title = "Editar Variação",
                initialName = variation.name,
                onDismiss = { variationToEdit = null },
                onSave = { name ->
                    viewModel.updateVariation(category.id, item.id, variation.id, name)
                    variationToEdit = null
                }
            )
        }

        variationToDelete?.let { variation ->
            ConfirmDeleteDialog(
                title = "Apagar Variação",
                message = "Deseja realmente apagar a variação '${variation.name}'?",
                onConfirm = {
                    viewModel.deleteVariation(category.id, item.id, variation.id)
                    variationToDelete = null
                },
                onDismiss = { variationToDelete = null }
            )
        }

        if (showAddOptionDialog) {
            ComparisonOptionFormDialog(
                title = "Adicionar Fornecedor e Preço",
                initialSupplier = "",
                initialPrice = "",
                suppliers = viewModel.suppliers,
                onDismiss = { showAddOptionDialog = false },
                onSave = { supplier, price ->
                    viewModel.addComparisonOption(category.id, item.id, supplier, price)
                    showAddOptionDialog = false
                }
            )
        }

        optionToEdit?.let { option ->
            ComparisonOptionFormDialog(
                title = "Editar Fornecedor e Preço",
                initialSupplier = option.supplier,
                initialPrice = option.price.toString(),
                suppliers = viewModel.suppliers,
                onDismiss = { optionToEdit = null },
                onSave = { supplier, price ->
                    viewModel.updateComparisonOption(category.id, item.id, option.id, supplier, price)
                    optionToEdit = null
                }
            )
        }

        optionToDelete?.let { option ->
            ConfirmDeleteDialog(
                title = "Apagar Opção de Preço",
                message = "Deseja realmente apagar o preço de ${formatCurrency(option.price)} no fornecedor '${option.supplier}'?",
                onConfirm = {
                    viewModel.deleteComparisonOption(category.id, item.id, option.id)
                    optionToDelete = null
                },
                onDismiss = { optionToDelete = null }
            )
        }
    }
}

@Composable
fun VariationRow(
    variation: VariationOption,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "• ${variation.name}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun VariationFormDialog(
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
                label = { Text("Variação (ex: Pó, 500g, Gourmet...)") },
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

@Composable
fun ComparisonOptionRow(
    option: ComparisonOption,
    isBest: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isBest) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = option.supplier,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isBest) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "MELHOR PREÇO",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = formatCurrency(option.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(2.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComparisonOptionFormDialog(
    title: String,
    initialSupplier: String,
    initialPrice: String,
    suppliers: List<Supplier>,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var supplier by remember { mutableStateOf(initialSupplier) }
    var priceText by remember { mutableStateOf(initialPrice) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = supplier,
                    onValueChange = { supplier = it },
                    label = { Text("Fornecedor (ex: Supermercado X)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (suppliers.isNotEmpty()) {
                    Text(text = "Sugestões de fornecedores:", style = MaterialTheme.typography.labelSmall)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        suppliers.forEach { s ->
                            SuggestionChip(
                                onClick = { supplier = s.name },
                                label = { Text(s.name, style = MaterialTheme.typography.bodySmall) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it.replace(',', '.') },
                    label = { Text("Preço (ex: 15.90)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    if (supplier.isBlank()) {
                        errorMessage = "O fornecedor não pode estar vazio."
                    } else if (price == null || price < 0) {
                        errorMessage = "Insira um preço válido."
                    } else {
                        onSave(supplier, price)
                    }
                }
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
