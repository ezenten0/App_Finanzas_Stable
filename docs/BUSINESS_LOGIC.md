# Funciones puras y de negocio

Las pantallas dependen de varios cálculos determinísticos que no requieren
frameworks de Android y que ahora cuentan con pruebas o documentación dedicada:

## `home/analytics/TransactionAnalytics`
- `calculateBalanceSummary(transactions)` agrega ingresos, gastos y saldo sin
  producir efectos secundarios.
- `calculateExpenseByCategory(transactions)` agrupa los gastos por etiqueta para
  alimentar las gráficas de estadísticas.
- `calculateBudgetProgress(transactions, monthlyBudget)` combina gastos y metas
  para estimar el avance por categoría.
- `calculateTimeSeries(transactions, range, referenceDate)` normaliza las fechas
  para construir series temporales.

## `categories/CategoryDefinitions`
- `labelForKey(key)` y `keyForLabel(label)` sincronizan los catálogos de
  categorías entre presupuestos y transacciones.
- `mergedLabels(extra)` combina las etiquetas predeterminadas con entradas
  dinámicas respetando mayúsculas/minúsculas.

## `transactions/TransactionsScreens`
- `formatAmount(transaction)` y `formatDate(raw)` encapsulan la lógica de
  formateo para garantizar que los montos, colores y fechas sean consistentes.
- `simulateTransactionLoading(delayMillis)` (extraída a
  `transactions/loading/TransactionLoading.kt`) abstrae la espera simulada antes
  de mostrar datos, lo que permite usar `coroutines-test` para validar los
  `delay`.
