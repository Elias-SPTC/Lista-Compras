package com.example.lista_compras

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lista_compras.ui.ShoppingViewModel
import com.example.lista_compras.ui.screens.CategoryItemsScreen
import com.example.lista_compras.ui.screens.ItemDetailScreen
import com.example.lista_compras.ui.screens.MainListsScreen
import com.example.lista_compras.ui.theme.ListaComprasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ListaComprasTheme {
                val viewModel: ShoppingViewModel = viewModel()
                var selectedCategoryId by remember { mutableStateOf<String?>(null) }
                var selectedItemId by remember { mutableStateOf<String?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                        val catId = selectedCategoryId
                        val itemId = selectedItemId

                        when {
                            catId == null -> {
                                MainListsScreen(
                                    viewModel = viewModel,
                                    onListClick = { id -> selectedCategoryId = id }
                                )
                            }
                            itemId == null -> {
                                CategoryItemsScreen(
                                    categoryId = catId,
                                    viewModel = viewModel,
                                    onBack = { selectedCategoryId = null },
                                    onItemClick = { id -> selectedItemId = id }
                                )
                            }
                            else -> {
                                ItemDetailScreen(
                                    categoryId = catId,
                                    itemId = itemId,
                                    viewModel = viewModel,
                                    onBack = { selectedItemId = null }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
