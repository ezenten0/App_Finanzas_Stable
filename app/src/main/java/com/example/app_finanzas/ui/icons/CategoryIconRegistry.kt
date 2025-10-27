package com.example.app_finanzas.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.app_finanzas.categories.CategoryDefinitions
import com.example.app_finanzas.categories.CategoryDefinition

/**
 * Keeps a stable mapping between category identifiers and the Material icons
 * used across the app.
 */
object CategoryIconRegistry {

    data class CategoryIconOption(
        val key: String,
        val label: String,
        val icon: ImageVector
    )

    private val iconsByKey: Map<String, ImageVector> = mapOf(
        CategoryDefinitions.HOME to Icons.Rounded.Home,
        CategoryDefinitions.INVESTMENTS to Icons.Rounded.TrendingUp,
        CategoryDefinitions.ENTERTAINMENT to Icons.Rounded.Movie,
        CategoryDefinitions.SOCIAL to Icons.Rounded.Groups,
        CategoryDefinitions.FOOD to Icons.Rounded.Restaurant,
        CategoryDefinitions.SHOPPING to Icons.Rounded.LocalMall,
        CategoryDefinitions.TRANSPORT to Icons.Rounded.DirectionsCar,
        CategoryDefinitions.HEALTH to Icons.Rounded.HealthAndSafety,
        CategoryDefinitions.EDUCATION to Icons.Rounded.School,
        CategoryDefinitions.OTHERS to Icons.Rounded.Category
    )

    private val defaultIcon = Icons.Rounded.Label

    val defaultOptions: List<CategoryIconOption> =
        CategoryDefinitions.defaults.map { it.toOption() }

    fun iconForKey(key: String?): ImageVector {
        return iconsByKey[key] ?: defaultIcon
    }

    fun iconForLabel(label: String): ImageVector {
        val key = CategoryDefinitions.keyForLabel(label)
        return iconForKey(key)
    }

    private fun CategoryDefinition.toOption(): CategoryIconOption {
        return CategoryIconOption(
            key = key,
            label = label,
            icon = iconForKey(key)
        )
    }
}

@Composable
fun CategoryIcon(
    key: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified
) {
    Icon(
        imageVector = CategoryIconRegistry.iconForKey(key),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}

@Composable
fun CategoryIconByLabel(
    label: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = CategoryIconRegistry.iconForLabel(label),
        contentDescription = contentDescription,
        modifier = modifier
    )
}
