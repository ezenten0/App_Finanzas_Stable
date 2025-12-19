
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



public interface CreateUserMutation :
    com.google.firebase.dataconnect.generated.GeneratedMutation<
      ExampleConnector,
      CreateUserMutation.Data,
      CreateUserMutation.Variables
    >
{
  
    @kotlinx.serialization.Serializable
  public data class Variables(
  
    val displayName: String,
    val email: String
  ) {
    
    
  }
  

  
    @kotlinx.serialization.Serializable
  public data class Data(
  
    val user_insert: UserKey
  ) {
    
    
  }
  

  public companion object {
    public val operationName: String = "CreateUser"

    public val dataDeserializer: kotlinx.serialization.DeserializationStrategy<Data> =
      kotlinx.serialization.serializer()

    public val variablesSerializer: kotlinx.serialization.SerializationStrategy<Variables> =
      kotlinx.serialization.serializer()
  }
}

public fun CreateUserMutation.ref(
  
    displayName: String,email: String,
  
  
): com.google.firebase.dataconnect.MutationRef<
    CreateUserMutation.Data,
    CreateUserMutation.Variables
  > =
  ref(
    
      CreateUserMutation.Variables(
        displayName=displayName,email=email,
  
      )
    
  )

public suspend fun CreateUserMutation.execute(
  
    displayName: String,email: String,
  
  
  ): com.google.firebase.dataconnect.MutationResult<
    CreateUserMutation.Data,
    CreateUserMutation.Variables
  > =
  ref(
    
      displayName=displayName,email=email,
  
    
  ).execute()


