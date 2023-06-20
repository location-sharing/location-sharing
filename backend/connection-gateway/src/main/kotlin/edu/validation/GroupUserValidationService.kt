package edu.validation

import edu.location.sharing.events.validation.group.user.GroupUserValidationMetadata
import edu.location.sharing.events.validation.group.user.GroupUserValidationPurpose
import edu.location.sharing.events.validation.group.user.GroupUserValidationRequestEvent
import edu.location.sharing.events.validation.group.user.GroupUserValidationResultEvent
import edu.location.sharing.util.logger
import edu.messaging.producers.GroupUserValidationRequestProducer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object GroupUserValidationService {

    private val log = logger()
    private val pendingValidations = ConcurrentHashMap<String, Pair<Instant, GroupUserValidationState>>()
    private const val ttlMillis = 300000L
    private const val cleanupIntervalMillis = 180000L

    enum class ValidationState {
        PENDING,
        VALID,
        INVALID
    }

    init {
        Thread {
            runBlocking {
                while (true) {
                    log.info("running group user validation cleanup")

                    val snapshot = mutableMapOf<String, Pair<Instant, GroupUserValidationState>>()
                    snapshot.putAll(pendingValidations)

                    val now = Instant.now().toEpochMilli()
                    var count = 0
                    pendingValidations
                        .filter { (_, timestampAndState) ->
                            val elapsed = now - timestampAndState.first.toEpochMilli()
                            elapsed >= ttlMillis
                        }
                        .forEach {
                            pendingValidations.remove(it.key)
                            count += 1
                        }

                    log.info("removed $count group user validation entries")
                    delay(cleanupIntervalMillis)
                }
            }
        }.start()
    }

    fun sendValidationRequest(
        groupId: String,
        userId: String,
    ): GroupUserValidationState {
        val metadata = GroupUserValidationMetadata(
            userId,
            UUID.randomUUID().toString(),
            GroupUserValidationPurpose.CONNECTION_CREATE,
        )
        val request = GroupUserValidationRequestEvent(groupId, userId, metadata)

        val state = GroupUserValidationState(ValidationState.PENDING)

        val pair = Pair(Instant.now(), state)

        pendingValidations[metadata.validationRequestId] = pair
        GroupUserValidationRequestProducer.sendValidationRequest(request)

        return state
    }

    fun consumeResult(result: GroupUserValidationResultEvent) {
        if (!pendingValidations.containsKey(result.metadata.validationRequestId)) {
            log.info("pending group user validations don't contain the request id")
            // validation request sent by another instance or ttl has expired
            return
        }
        val pairRef = pendingValidations[result.metadata.validationRequestId]!!.second
        if (result.valid) {
            log.info("group user membership valid")
            pairRef.set(ValidationState.VALID)
        } else {
            log.info("group user membership invalid")
            pairRef.set(ValidationState.INVALID)
        }
        log.info("removing group user validation ${result.metadata.validationRequestId}")
        pendingValidations.remove(result.metadata.validationRequestId)
    }
}

typealias GroupUserValidationState = AtomicReference<GroupUserValidationService.ValidationState>
fun GroupUserValidationState.isValid() : Boolean = get() == GroupUserValidationService.ValidationState.VALID
fun GroupUserValidationState.isInvalid() : Boolean = get() == GroupUserValidationService.ValidationState.INVALID
fun GroupUserValidationState.isPending() : Boolean = get() == GroupUserValidationService.ValidationState.PENDING