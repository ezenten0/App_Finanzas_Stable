package com.example.app_finanzas.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Assessment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.app_finanzas.data.budget.BudgetRepository
import com.example.app_finanzas.data.transaction.TransactionRepository
import com.example.app_finanzas.home.HomeRoute
import com.example.app_finanzas.transactions.TransactionDetailRoute
import com.example.app_finanzas.transactions.TransactionsRoute
import com.example.app_finanzas.statistics.StatisticsRoute
import com.example.app_finanzas.budgets.BudgetsRoute
import com.example.app_finanzas.transactions.form.TransactionFormRoute
import com.example.app_finanzas.insights.InsightsRoute
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

/**
 * Hosts the animated navigation graph, bottom bar and screen wiring for the
 * finance application.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FinanceApp(
    transactionRepository: TransactionRepository,
    budgetRepository: BudgetRepository,
    userName: String,
    userEmail: String,
    modifier: Modifier = Modifier
) {
    val navController = rememberAnimatedNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val bottomDestinations = FinanceDestination.bottomDestinations

    Scaffold(
        modifier = modifier,
        bottomBar = {
            FinanceBottomBar(
                navController = navController,
                currentDestination = currentDestination?.destination,
                destinations = bottomDestinations
            )
        }
    ) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = FinanceDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            composable(FinanceDestination.Home.route) {
                HomeRoute(
                    userName = userName,
                    userEmail = userEmail,
                    transactionRepository = transactionRepository,
                    onAddTransaction = {
                        navController.navigate(FinanceDestination.TransactionForm.route)
                    },
                    onTransactionSelected = { id ->
                        navController.navigate(FinanceDestination.TransactionDetail.createRoute(id))
                    },
                    onShowInsights = {
                        navController.navigate(FinanceDestination.Insights.route)
                    },
                    onShowStatistics = {
                        navController.navigate(FinanceDestination.Statistics.route)
                    }
                )
            }
            composable(FinanceDestination.Transactions.route) {
                TransactionsRoute(
                    transactionRepository = transactionRepository,
                    onTransactionSelected = { id ->
                        navController.navigate(FinanceDestination.TransactionDetail.createRoute(id))
                    },
                    onAddTransaction = {
                        navController.navigate(FinanceDestination.TransactionForm.route)
                    }
                )
            }
            composable(FinanceDestination.Statistics.route) {
                StatisticsRoute(transactionRepository = transactionRepository)
            }
            composable(FinanceDestination.Budgets.route) {
                BudgetsRoute(
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository
                )
            }
            composable(FinanceDestination.TransactionDetail.route) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString(FinanceDestination.TransactionDetail.transactionIdArg)?.toIntOrNull()
                TransactionDetailRoute(
                    transactionRepository = transactionRepository,
                    transactionId = transactionId,
                    onBack = { navController.popBackStack() },
                    onEdit = { id ->
                        navController.navigate(FinanceDestination.TransactionForm.createRoute(id))
                    }
                )
            }
            composable(FinanceDestination.TransactionForm.route) {
                TransactionFormRoute(
                    transactionRepository = transactionRepository,
                    transactionId = null,
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable(
                route = FinanceDestination.TransactionForm.editRoute,
                arguments = listOf(navArgument(FinanceDestination.TransactionForm.transactionIdArg) { type = NavType.IntType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getInt(FinanceDestination.TransactionForm.transactionIdArg)
                TransactionFormRoute(
                    transactionRepository = transactionRepository,
                    transactionId = transactionId,
                    onDismiss = { navController.popBackStack() }
                )
            }
            composable(FinanceDestination.Insights.route) {
                InsightsRoute(
                    transactionRepository = transactionRepository,
                    budgetRepository = budgetRepository,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Draws the bottom navigation bar and keeps the selected destination in sync
 * with the active screen.
 */
@Composable
private fun FinanceBottomBar(
    navController: NavHostController,
    currentDestination: NavDestination?,
    destinations: List<FinanceDestination>
) {
    val currentRoute = currentDestination?.route
    val resolvedRoute = currentRoute ?: destinations.firstOrNull()?.route
    val showBottomBar = resolvedRoute != null && destinations.any { it.route == resolvedRoute }
    if (!showBottomBar) return

    NavigationBar {
        destinations.forEach { destination ->
            val selected = resolvedRoute == destination.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                label = { Text(text = destination.label) },
                colors = NavigationBarItemDefaults.colors()
            )
        }
    }
}

/**
 * Represents each destination handled by the animated navigation graph.
 */
sealed class FinanceDestination(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Home : FinanceDestination("home", "Inicio", Icons.Rounded.Home)
    object Transactions : FinanceDestination("transactions", "Movimientos", Icons.Rounded.AccountBalanceWallet)
    object Statistics : FinanceDestination("statistics", "Estadísticas", Icons.Rounded.Assessment)
    object Budgets : FinanceDestination("budgets", "Presupuestos", Icons.Rounded.BarChart)

    object TransactionDetail : FinanceDestination("transactionDetail/{transactionId}", "Detalle", Icons.Rounded.AccountBalanceWallet) {
        const val transactionIdArg = "TRANSACTION_ID"
        fun createRoute(id: Int): String = "transactionDetail/$id"
    }

    object TransactionForm : FinanceDestination("transactionForm", "Registrar", Icons.Rounded.EditNote) {
        const val transactionIdArg = "transactionId"
        val editRoute: String = "transactionForm/{$transactionIdArg}"
        fun createRoute(id: Int? = null): String = id?.let { "transactionForm/$it" } ?: route
    }

    object Insights : FinanceDestination("insights", "Insights", Icons.Rounded.Lightbulb)

    companion object {
        val bottomDestinations = listOf(Home, Transactions, Statistics, Budgets)
    }
}
