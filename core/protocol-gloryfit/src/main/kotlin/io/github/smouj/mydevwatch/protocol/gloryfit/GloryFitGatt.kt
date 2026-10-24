package io.github.smouj.mydevwatch.protocol.gloryfit

import io.github.smouj.mydevwatch.protocol.GattChannel

/**
 * GATT layout shared by GloryFit / UTE family watches.
 *
 * Evidence: three independent public implementations (Gadgetbridge, RyzeWaveWatch,
 * air5 reverse engineering) agree on 55FF/33F1+33F2 and 56FF/34F1+34F2, and the
 * transfer service 60FF/6001+6002 is used for watch-face uploads.
 */
object GloryFitGatt {
    const val SERVICE_PRIMARY = "000055ff-0000-1000-8000-00805f9b34fb"
    const val WRITE_PRIMARY = "000033f1-0000-1000-8000-00805f9b34fb"
    const val NOTIFY_PRIMARY = "000033f2-0000-1000-8000-00805f9b34fb"

    const val SERVICE_SECONDARY = "000056ff-0000-1000-8000-00805f9b34fb"
    const val WRITE_SECONDARY = "000034f1-0000-1000-8000-00805f9b34fb"
    const val NOTIFY_SECONDARY = "000034f2-0000-1000-8000-00805f9b34fb"

    const val SERVICE_TRANSFER = "000060ff-0000-1000-8000-00805f9b34fb"
    const val WRITE_TRANSFER = "00006001-0000-1000-8000-00805f9b34fb"
    const val NOTIFY_TRANSFER = "00006002-0000-1000-8000-00805f9b34fb"

    val ALL_SERVICES = setOf(SERVICE_PRIMARY, SERVICE_SECONDARY, SERVICE_TRANSFER)

    val primaryWrite = GattChannel(SERVICE_PRIMARY, WRITE_PRIMARY)
    val primaryNotify = GattChannel(SERVICE_PRIMARY, NOTIFY_PRIMARY)
    val secondaryWrite = GattChannel(SERVICE_SECONDARY, WRITE_SECONDARY)
    val secondaryNotify = GattChannel(SERVICE_SECONDARY, NOTIFY_SECONDARY)
    val transferWrite = GattChannel(SERVICE_TRANSFER, WRITE_TRANSFER)
    val transferNotify = GattChannel(SERVICE_TRANSFER, NOTIFY_TRANSFER)
}
