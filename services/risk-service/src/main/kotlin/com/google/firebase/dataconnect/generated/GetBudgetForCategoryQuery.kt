
@file:kotlin.Suppress(
  "KotlinRedundantDiagnosticSuppress",
  "LocalVariableName",
  "MayBeConstant",
  "RedundantVisibilityModifier",
  "RemoveEmptyClassBody",
  "SpellCheckingInspection",
  "LocalVariableName",
  "unused",
)

package com.google.firebase.dataconnect.generated


import kotlinx.coroutines.flow.filterNotNull as _flow_filterNotNull
import kotlinx.coroutines.flow.map as _flow_map


public interface GetBudgetForCategoryQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      ExampleConnector,
      GetBudgetForCategoryQuery.Data,
      GetBudgetForCategoryQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val categoryId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val budgets: List<BudgetsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class BudgetsItem(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val amount: Double,
    val startDate: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.TimestampSerializer::class) com.google.firebase.Timestamp,
    val endDate: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.TimestampSerializer::class) com.google.firebase.Timestamp?,
    val period: String
  ) {
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetBudgetForCategory"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetBudgetForCategoryQuery.ref(
  
    categoryId: java.util.UUID,
  
  
): com.google.firebase.dataconnect.QueryRef<
    GetBudgetForCategoryQuery.Data,
    GetBudgetForCategoryQuery.Variables
  > =
  ref(
    
      GetBudgetForCategoryQuery.Variables(
        categoryId=categoryId,
  
      )
    
  )

public suspend fun GetBudgetForCategoryQuery.execute(
  
    categoryId: java.util.UUID,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetBudgetForCategoryQuery.Data,
    GetBudgetForCategoryQuery.Variables
  > =
  ref(
    
      categoryId=categoryId,
  
    
  ).execute()


  public fun GetBudgetForCategoryQuery.flow(
    
      categoryId: java.util.UUID,
  
    
    ): kotlinx.coroutines.flow.Flow<GetBudgetForCategoryQuery.Data> =
    ref(
        
          categoryId=categoryId,
  
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

