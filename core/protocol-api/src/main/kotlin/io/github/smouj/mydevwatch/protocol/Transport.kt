package io.github.smouj.mydevwatch.protocol

import io.github.smouj.mydevwatch.model.Capability
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.Instant

/** A GATT channel, kept as strings so captures stay portable across platforms. */
data class GattChannel(val service: String, val characteristic: String)

/** One notification pushed by the watch. */
class WatchNotification(
    val channel: GattChannel,
    val payload: ByteArray,
    val receivedAt: Instant = Instant.now()
) {
    val opcode: Int? get() = payload.firstOrNull()?.toInt()?.and(0xFF)
}

/**
 * The only thing a protocol implementation is allowed to know about Bluetooth.
 *
 * Implementations exist for Android (GATT), for replay from a capture file, and for
 * the test fakes. A protocol that cannot run against all three is not finished.
 */
interface WatchTransport {
    val mtu: Int
    val notifications: Flow<WatchNotification>

    suspend fun write(channel: GattChannel, payload: ByteArray)
    suspend fun subscribe(channel: GattChannel)
    suspend fun requestMtu(desired: Int): Int
    suspend fun close()

    /** Writes a command and waits for the first notification on [channel] matching [predicate]. */
    suspend fun request(
        channel: GattChannel,
        payload: ByteArray,
        predicate: (WatchNotification) -> Boolean = { true }
    ): WatchNotification {
        val reply = notifications.first(predicate) // subscribed before the write by the caller
        write(channel, payload)
        return reply
    }
}

sealed interface ProtocolResult<out T> {
    data class Success<T>(val value: T) : ProtocolResult<T>
    data class Unsupported(val capability: Capability) : ProtocolResult<Nothing>
    data class Failure(val reason: String, val cause: Throwable? = null) : ProtocolResult<Nothing>
}

/** Connection lifecycle. The UI renders this value directly; it never guesses. */
enum class ConnectionState {
    IDLE,
    SCANNING,
    FOUND,
    CONNECTING,
    DISCOVERING,
    IDENTIFYING,
    INITIALIZING,
    READY,
    SYNCING,
    RECONNECTING,
    DISCONNECTED,
    ERROR;

    val isBusy: Boolean
        get() = this in setOf(SCANNING, CONNECTING, DISCOVERING, IDENTIFYING, INITIALIZING, SYNCING, RECONNECTING)
}
