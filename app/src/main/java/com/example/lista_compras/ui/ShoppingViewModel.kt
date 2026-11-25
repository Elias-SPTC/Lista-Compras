package com.example.lista_compras.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.lista_compras.data.ComparisonOption
import com.example.lista_compras.data.ShoppingItem
import com.example.lista_compras.data.ShoppingListCategory
import com.example.lista_compras.data.Supplier
import com.example.lista_compras.data.VariationOption
import org.json.JSONArray
import org.json.JSONObject

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("shopping_prefs", Context.MODE_PRIVATE)
    private val _categories = mutableStateListOf<ShoppingListCategory>()
    private val _suppliers = mutableStateListOf<Supplier>()

    init {
        loadData()
    }

    val categories: List<ShoppingListCategory> get() = _categories
    val suppliers: List<Supplier> get() = _suppliers

    private fun loadData() {
        loadCategories()
        loadSuppliers()
    }

    private fun loadSuppliers() {
        val jsonString = prefs.getString("saved_suppliers", null)
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                val loadedSuppliers = mutableListOf<Supplier>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    loadedSuppliers.add(
                        Supplier(
                            id = obj.getString("id"),
                            name = obj.getString("name")
                        )
                    )
                }
                _suppliers.clear()
                _suppliers.addAll(loadedSuppliers)
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultSuppliers()
            }
        } else {
            loadDefaultSuppliers()
        }
    }

    private fun loadDefaultSuppliers() {
        val defaultSuppliers = listOf(
            Supplier(name = "Supermercado Extra"),
            Supplier(name = "Atacadão"),
            Supplier(name = "Carrefour"),
            Supplier(name = "Droga Raia"),
            Supplier(name = "Farmácia São João")
        )
        _suppliers.clear()
        _suppliers.addAll(defaultSuppliers)
        saveSuppliers()
    }

    private fun saveSuppliers() {
        try {
            val jsonArray = JSONArray()
            for (s in _suppliers) {
                val obj = JSONObject()
                obj.put("id", s.id)
                obj.put("name", s.name)
                jsonArray.put(obj)
            }
            prefs.edit().putString("saved_suppliers", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addSupplier(name: String) {
        if (name.isBlank()) return
        if (_suppliers.any { it.name.equals(name.trim(), ignoreCase = true) }) return
        _suppliers.add(Supplier(name = name.trim()))
        saveSuppliers()
    }

    fun updateSupplier(id: String, name: String) {
        if (name.isBlank()) return
        val index = _suppliers.indexOfFirst { it.id == id }
        if (index != -1) {
            _suppliers[index] = _suppliers[index].copy(name = name.trim())
            saveSuppliers()
        }
    }

    fun deleteSupplier(id: String) {
        _suppliers.removeAll { it.id == id }
        saveSuppliers()
    }

    // Categories and Items persistence
    private fun loadCategories() {
        val jsonString = prefs.getString("saved_categories", null)
        if (jsonString != null) {
            try {
                val jsonArray = JSONArray(jsonString)
                val loadedCategories = mutableListOf<ShoppingListCategory>()
                for (i in 0 until jsonArray.length()) {
                    val catObj = jsonArray.getJSONObject(i)
                    val catId = catObj.getString("id")
                    val catName = catObj.getString("name")

                    val itemsArray = catObj.getJSONArray("items")
                    val items = mutableListOf<ShoppingItem>()
                    for (j in 0 until itemsArray.length()) {
                        val obj = itemsArray.getJSONObject(j)
                        val id = obj.getString("id")
                        val name = obj.getString("name")

                        val varArray = obj.getJSONArray("variations")
                        val variations = mutableListOf<VariationOption>()
                        for (k in 0 until varArray.length()) {
                            val vObj = varArray.getJSONObject(k)
                            variations.add(VariationOption(id = vObj.getString("id"), name = vObj.getString("name")))
                        }

                        val compArray = obj.getJSONArray("comparisonOptions")
                        val comparisonOptions = mutableListOf<ComparisonOption>()
                        for (l in 0 until compArray.length()) {
                            val cObj = compArray.getJSONObject(l)
                            comparisonOptions.add(
                                ComparisonOption(
                                    id = cObj.getString("id"),
                                    supplier = cObj.getString("supplier"),
                                    price = cObj.getDouble("price")
                                )
                            )
                        }

                        items.add(ShoppingItem(id = id, name = name, variations = variations, comparisonOptions = comparisonOptions))
                    }

                    loadedCategories.add(ShoppingListCategory(id = catId, name = catName, items = items))
                }
                _categories.clear()
                _categories.addAll(loadedCategories)
            } catch (e: Exception) {
                e.printStackTrace()
                loadDefaultCategories()
            }
        } else {
            loadDefaultCategories()
        }
    }

    private fun loadDefaultCategories() {
        val defaultList = listOf(
            ShoppingListCategory(
                name = "Cereais e Mantimentos",
                items = listOf(
                    ShoppingItem(
                        name = "Café",
                        variations = listOf(
                            VariationOption(name = "Pó"),
                            VariationOption(name = "500g"),
                            VariationOption(name = "Torra Média"),
                            VariationOption(name = "Gourmet")
                        ),
                        comparisonOptions = listOf(
                            ComparisonOption(supplier = "Supermercado Extra", price = 18.90),
                            ComparisonOption(supplier = "Atacadão", price = 16.50),
                            ComparisonOption(supplier = "Carrefour", price = 19.20)
                        )
                    ),
                    ShoppingItem(
                        name = "Arroz",
                        variations = listOf(
                            VariationOption(name = "Grãos"),
                            VariationOption(name = "Tipo 1"),
                            VariationOption(name = "Pacote 5kg")
                        ),
                        comparisonOptions = listOf(
                            ComparisonOption(supplier = "Supermercado Extra", price = 24.50),
                            ComparisonOption(supplier = "Atacadão", price = 22.00)
                        )
                    )
                )
            ),
            ShoppingListCategory(
                name = "Lista de Remédios",
                items = listOf(
                    ShoppingItem(
                        name = "Anador",
                        variations = listOf(VariationOption(name = "500mg"), VariationOption(name = "20 Comprimidos")),
                        comparisonOptions = listOf(
                            ComparisonOption(supplier = "Droga Raia", price = 12.50),
                            ComparisonOption(supplier = "Farmácia São João", price = 10.90)
                        )
                    )
                )
            ),
            ShoppingListCategory(
                name = "Eletrônicos",
                items = emptyList()
            )
        )
        _categories.clear()
        _categories.addAll(defaultList)
        saveCategories()
    }

    private fun saveCategories() {
        try {
            val jsonArray = JSONArray()
            for (cat in _categories) {
                val catObj = JSONObject()
                catObj.put("id", cat.id)
                catObj.put("name", cat.name)

                val itemsArray = JSONArray()
                for (item in cat.items) {
                    val obj = JSONObject()
                    obj.put("id", item.id)
                    obj.put("name", item.name)

                    val varArray = JSONArray()
                    for (v in item.variations) {
                        val vObj = JSONObject()
                        vObj.put("id", v.id)
                        vObj.put("name", v.name)
                        varArray.put(vObj)
                    }
                    obj.put("variations", varArray)

                    val compArray = JSONArray()
                    for (c in item.comparisonOptions) {
                        val cObj = JSONObject()
                        cObj.put("id", c.id)
                        cObj.put("supplier", c.supplier)
                        cObj.put("price", c.price)
                        compArray.put(cObj)
                    }
                    obj.put("comparisonOptions", compArray)

                    itemsArray.put(obj)
                }
                catObj.put("items", itemsArray)
                jsonArray.put(catObj)
            }
            prefs.edit().putString("saved_categories", jsonArray.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        _categories.add(ShoppingListCategory(name = name.trim()))
        saveCategories()
    }

    fun updateCategory(id: String, name: String) {
        if (name.isBlank()) return
        val index = _categories.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = _categories[index]
            _categories[index] = old.copy(name = name.trim())
            saveCategories()
        }
    }

    fun deleteCategory(id: String) {
        _categories.removeAll { it.id == id }
        saveCategories()
    }

    fun importCategory(category: ShoppingListCategory) {
        val index = _categories.indexOfFirst { it.id == category.id }
        if (index != -1) {
            _categories[index] = category
        } else {
            _categories.add(category)
        }
        saveCategories()
    }

    fun addItem(categoryId: String, name: String) {
        if (name.isBlank()) return
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items + ShoppingItem(name = name.trim())
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun updateItem(categoryId: String, itemId: String, name: String) {
        if (name.isBlank()) return
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) item.copy(name = name.trim()) else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun deleteItem(categoryId: String, itemId: String) {
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.filter { it.id != itemId }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun addVariation(categoryId: String, itemId: String, variationName: String) {
        if (variationName.isBlank()) return
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    item.copy(variations = item.variations + VariationOption(name = variationName.trim()))
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun updateVariation(categoryId: String, itemId: String, variationId: String, variationName: String) {
        if (variationName.isBlank()) return
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    val newVars = item.variations.map { v ->
                        if (v.id == variationId) v.copy(name = variationName.trim()) else v
                    }
                    item.copy(variations = newVars)
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun deleteVariation(categoryId: String, itemId: String, variationId: String) {
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    item.copy(variations = item.variations.filter { it.id != variationId })
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun addComparisonOption(categoryId: String, itemId: String, supplier: String, price: Double) {
        if (supplier.isBlank()) return
        addSupplier(supplier)

        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    item.copy(comparisonOptions = item.comparisonOptions + ComparisonOption(supplier = supplier.trim(), price = price))
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun updateComparisonOption(categoryId: String, itemId: String, optionId: String, supplier: String, price: Double) {
        if (supplier.isBlank()) return
        addSupplier(supplier)

        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    val newOpts = item.comparisonOptions.map { opt ->
                        if (opt.id == optionId) opt.copy(supplier = supplier.trim(), price = price) else opt
                    }
                    item.copy(comparisonOptions = newOpts)
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }

    fun deleteComparisonOption(categoryId: String, itemId: String, optionId: String) {
        val catIndex = _categories.indexOfFirst { it.id == categoryId }
        if (catIndex != -1) {
            val cat = _categories[catIndex]
            val newItems = cat.items.map { item ->
                if (item.id == itemId) {
                    item.copy(comparisonOptions = item.comparisonOptions.filter { it.id != optionId })
                } else item
            }
            _categories[catIndex] = cat.copy(items = newItems)
            saveCategories()
        }
    }
}
