package com.example.lista_compras.data

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.UUID

data class Supplier(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class VariationOption(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class ComparisonOption(
    val id: String = UUID.randomUUID().toString(),
    val supplier: String,
    val price: Double,
    val variationName: String? = null // Optional association with a specific variation
)

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val variations: List<VariationOption> = emptyList(),
    val comparisonOptions: List<ComparisonOption> = emptyList()
) {
    val bestOption: ComparisonOption?
        get() = comparisonOptions.minByOrNull { it.price }

    val bestPrice: Double?
        get() = bestOption?.price

    val bestSupplier: String?
        get() = bestOption?.supplier
}

data class ShoppingListCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<ShoppingItem> = emptyList(),
    val suppliers: List<Supplier> = emptyList()
)

fun formatCurrency(price: Double?): String {
    if (price == null) return "R$ 0,00"
    val symbols = DecimalFormatSymbols(Locale.forLanguageTag("pt-BR")).apply {
        decimalSeparator = ','
        groupingSeparator = '.'
    }
    val formatter = DecimalFormat("R$ #,##0.00", symbols)
    return formatter.format(price)
}
