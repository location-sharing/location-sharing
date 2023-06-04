package edu.validation

import edu.messaging.producers.GroupUserValidationRequestProducer
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object GroupUserValidationService {

    private val pendingValidations = ConcurrentHashMap<String, GroupUserValidationDeferred>()

    suspend fun sendValidationRequest(
        groupId: String,
        userId: String,
        ttlMillis: Long = 30000,
        onTtlExpired: () -> Unit = {}
    ): GroupUserValidationDeferred {
        val metadata = GroupUserValidationMetadata(
            userId,
            UUID.randomUUID().toString(),
            GroupUserValidationPurpose.CONNECTION_CREATE,
        )
        val request = GroupUserValidationRequestEvent(groupId, userId, metadata)
        val deferredValidation = CompletableDeferred<GroupUserValidationResultEvent>(null)
        pendingValidations[metadata.validationRequestId] = deferredValidation
        GroupUserValidationRequestProducer.sendValidationRequest(request)

        // remove the validation if it's still there after the TTL
        coroutineScope {
            launch {
                delay(ttlMillis)
                if (pendingValidations.containsKey(metadata.validationRequestId)) {
                    pendingValidations.remove(metadata.validationRequestId)
                    onTtlExpired()
                }
            }
        }

        return deferredValidation
    }

    fun consumeResult(result: GroupUserValidationResultEvent) {
        if (!pendingValidations.containsKey(result.metadata.validationRequestId)) {
            // validation request sent by another instance or ttl has expired
            return
        }
        if (result.valid) {
            pendingValidations[result.metadata.validationRequestId]!!.complete(result)
        }
        pendingValidations[result.metadata.validationRequestId]!!.completeExceptionally(
            GroupUserValidationException(
                result.message ?: "Unknown error while validating group ${result.groupId} for user ${result.userId}"
            )
        )
        pendingValidations.remove(result.metadata.validationRequestId)
    }
}

typealias GroupUserValidationDeferred = CompletableDeferred<GroupUserValidationResultEvent>