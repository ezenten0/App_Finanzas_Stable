
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



public interface CreateTransactionMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      ExampleConnector,
      CreateTransactionMutation.Data,
      CreateTransactionMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val accountId: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID,
    val amount: Double,
    val date: @kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.TimestampSerializer::class) com.google.firebase.Timestamp,
    val description: String,
    val type: String,
    val categoryId: com.google.firebase.dataconnect.OptionalVariable<@kotlinx.serialization.Serializable(with = com.google.firebase.dataconnect.serializers.UUIDSerializer::class) java.util.UUID?>
  ) {
    
    
      
      @kotlin.DslMarker public annotation class BuilderDsl

      @BuilderDsl
      public interface Builder {
        public var accountId: java.util.UUID
        public var amount: Double
        public var date: com.google.firebase.Timestamp
        public var description: String
        public var type: String
        public var categoryId: java.util.UUID?
        
      }

      public companion object {
        @Suppress("NAME_SHADOWING")
        public fun build(
          accountId: java.util.UUID,amount: Double,date: com.google.firebase.Timestamp,description: String,type: String,
          block_: Builder.() -> Unit
        ): Variables {
          var accountId= accountId
            var amount= amount
            var date= date
            var description= description
            var type= type
            var categoryId: com.google.firebase.dataconnect.OptionalVariable<java.util.UUID?> =
                com.google.firebase.dataconnect.OptionalVariable.Undefined
            

          return object : Builder {
            override var accountId: java.util.UUID
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { accountId = value_ }
              
            override var amount: Double
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { amount = value_ }
              
            override var date: com.google.firebase.Timestamp
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { date = value_ }
              
            override var description: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { description = value_ }
              
            override var type: String
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { type = value_ }
              
            override var categoryId: java.util.UUID?
              get() = throw UnsupportedOperationException("getting builder values is not supported")
              set(value_) { categoryId = com.google.firebase.dataconnect.OptionalVariable.Value(value_) }
              
            
          }.apply(block_)
          .let {
            Variables(
              accountId=accountId,amount=amount,date=date,description=description,type=type,categoryId=categoryId,
            )
          }
        }
      }
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val transaction_insert: TransactionKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "CreateTransaction"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun CreateTransactionMutation.ref(
  
    accountId: java.util.UUID,amount: Double,date: com.google.firebase.Timestamp,description: String,type: String,
  
    block_: CreateTransactionMutation.Variables.Builder.() -> Unit = {}
  
): com.google.firebase.dataconnect.MutationRef<
    CreateTransactionMutation.Data,
    CreateTransactionMutation.Variables
  > =
  ref(
    
      CreateTransactionMutation.Variables.build(
        accountId=accountId,amount=amount,date=date,description=description,type=type,
  
    block_
      )
    
  )

public suspend fun CreateTransactionMutation.execute(
  
    accountId: java.util.UUID,amount: Double,date: com.google.firebase.Timestamp,description: String,type: String,
  
    block_: CreateTransactionMutation.Variables.Builder.() -> Unit = {}
  
  ): com.google.firebase.dataconnect.MutationResult<
    CreateTransactionMutation.Data,
    CreateTransactionMutation.Variables
  > =
  ref(
    
      accountId=accountId,amount=amount,date=date,description=description,type=type,
  
    block_
    
  ).execute()


