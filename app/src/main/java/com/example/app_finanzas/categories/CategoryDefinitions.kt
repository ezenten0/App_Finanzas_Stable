package com.example.app_finanzas.categories

/**
 * Centralizes the predefined categories offered across the app so both the
 * transaction form and the budgets flow stay in sync with the same labels and
 * identifiers.
 */
data class CategoryDefinition(
    val key: String,
    val label: String
)

object CategoryDefinitions {
    const val HOME = "home"
    const val INVESTMENTS = "investments"
    const val ENTERTAINMENT = "entertainment"
    const val SOCIAL = "social"
    const val OTHERS = "others"
    const val FOOD = "food"
    const val TRANSPORT = "transport"
    const val HEALTH = "health"
    const val EDUCATION = "education"
    const val SHOPPING = "shopping"

    val defaults: List<CategoryDefinition> = listOf(
        CategoryDefinition(HOME, "Hogar"),
        CategoryDefinition(INVESTMENTS, "Inversiones"),
        CategoryDefinition(ENTERTAINMENT, "Entretenimiento"),
        CategoryDefinition(SOCIAL, "Social"),
        CategoryDefinition(FOOD, "Alimentos"),
        CategoryDefinition(SHOPPING, "Compras"),
        CategoryDefinition(TRANSPORT, "Transporte"),
        CategoryDefinition(HEALTH, "Salud"),
        CategoryDefinition(EDUCATION, "Educación"),
        CategoryDefinition(OTHERS, "Otros")
    )

    private val labelToDefinition: Map<String, CategoryDefinition> =
        defaults.associateBy { it.label.lowercase() }

    fun labelForKey(key: String): String {
        return defaults.firstOrNull { it.key == key }?.label ?: key
    }

    fun keyForLabel(label: String): String? {
        val normalized = label.trim().lowercase()
        return labelToDefinition[normalized]?.key
    }

    fun mergedLabels(extra: List<String>): List<String> {
        return (defaults.map { it.label } + extra)
            .distinctBy { it.lowercase() }
            .sortedBy { it.lowercase() }
    }
}
