
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


public interface GetTransactionsForAccountQuery :
    com.google.firebase.dataconnect.generated.GeneratedQuery<
      ExampleConnector,
      GetTransactionsForAccountQuery.Data,
      GetTransactionsForAccountQuery.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val accountId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val transactions: List<TransactionsItem>
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class TransactionsItem(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val amount: Double,
    val date: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.TimestampSerializer::class) com.google.firebase.Timestamp,
    val description: String?,
    val type: String,
    val category: Category?
  ) {
    
      
        @kotlinx.serialization.Serializable
  public data class Category(
  
    val id: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val name: String
  ) {
    
    
  }
      
    
    
  }
      
    
    
  }
  

  public companion object {
    public val operationName: String = "GetTransactionsForAccount"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun GetTransactionsForAccountQuery.ref(
  
    accountId: java.util.UUID,
  
  
): com.google.firebase.dataconnect.QueryRef<
    GetTransactionsForAccountQuery.Data,
    GetTransactionsForAccountQuery.Variables
  > =
  ref(
    
      GetTransactionsForAccountQuery.Variables(
        accountId=accountId,
  
      )
    
  )

public suspend fun GetTransactionsForAccountQuery.execute(
  
    accountId: java.util.UUID,
  
  
  ): com.google.firebase.dataconnect.QueryResult<
    GetTransactionsForAccountQuery.Data,
    GetTransactionsForAccountQuery.Variables
  > =
  ref(
    
      accountId=accountId,
  
    
  ).execute()


  public fun GetTransactionsForAccountQuery.flow(
    
      accountId: java.util.UUID,
  
    
    ): kotlinx.coroutines.flow.Flow<GetTransactionsForAccountQuery.Data> =
    ref(
        
          accountId=accountId,
  
        
      ).subscribe()
      .flow
      ._flow_map { querySubscriptionResult -> querySubscriptionResult.result.getOrNull() }
      ._flow_filterNotNull()
      ._flow_map { it.data }

