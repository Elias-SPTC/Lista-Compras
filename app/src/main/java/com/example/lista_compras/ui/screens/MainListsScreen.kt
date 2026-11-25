package com.example.lista_compras.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lista_compras.data.ShoppingListCategory
import com.example.lista_compras.ui.ShoppingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainListsScreen(
    viewModel: ShoppingViewModel,
    onListClick: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var listToEdit by remember { mutableStateOf<ShoppingListCategory?>(null) }
    var showSuppliersDialog by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<ShoppingListCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de compras") },
                actions = {
                    TextButton(onClick = { showSuppliersDialog = true }) {
                        Text("Fornecedores", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Criar Nova Lista")
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val categories = viewModel.categories
            if (categories.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Nenhuma lista criada.\nToque em '+' para criar uma nova lista.",
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
                    items(categories, key = { it.id }) { cat ->
                        ShoppingListCard(
                            category = cat,
                            onClick = { onListClick(cat.id) },
                            onEdit = { listToEdit = cat },
                            onDelete = { categoryToDelete = cat }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ListFormDialog(
                title = "Criar Nova Lista de Compras",
                initialName = "",
                onDismiss = { showAddDialog = false },
                onSave = { name ->
                    viewModel.addCategory(name)
                    showAddDialog = false
                }
            )
        }

        listToEdit?.let { cat ->
            ListFormDialog(
                title = "Editar Nome da Lista",
                initialName = cat.name,
                onDismiss = { listToEdit = null },
                onSave = { name ->
                    viewModel.updateCategory(cat.id, name)
                    listToEdit = null
                }
            )
        }

        categoryToDelete?.let { cat ->
            ConfirmDeleteDialog(
                title = "Apagar Lista",
                message = "Deseja realmente apagar a lista '${cat.name}' e todos os seus itens? Esta ação não pode ser desfeita.",
                onConfirm = {
                    viewModel.deleteCategory(cat.id)
                    categoryToDelete = null
                },
                onDismiss = { categoryToDelete = null }
            )
        }

        if (showSuppliersDialog) {
            SuppliersManagementDialog(
                viewModel = viewModel,
                onDismiss = { showSuppliersDialog = false }
            )
        }
    }
}

@Composable
fun ShoppingListCard(
    category: ShoppingListCategory,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${category.items.size} item(ns) cadastrado(s)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun ListFormDialog(
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
                label = { Text("Nome da Lista (ex: Remédios, Eletrônicos...)") },
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
