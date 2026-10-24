package io.github.smouj.mydevwatch.model

/**
 * A capability is a promise the UI is allowed to make.
 *
 * Screens never ask "which protocol is this?"; they ask "does this device claim
 * WATCHFACE?" That keeps the interface honest and the code free of model checks.
 */
enum class Capability(val category: Category, val label: String) {
    BATTERY(Category.SYSTEM, "Battery"),
    TIME_SYNC(Category.SYSTEM, "Clock sync"),
    DEVICE_INFO(Category.SYSTEM, "Device information"),

    STEPS(Category.ACTIVITY, "Steps"),
    DISTANCE(Category.ACTIVITY, "Distance"),
    CALORIES(Category.ACTIVITY, "Calories"),
    WORKOUTS(Category.ACTIVITY, "Workouts"),

    HEART_RATE(Category.HEALTH, "Heart rate"),
    HEART_RATE_LIVE(Category.HEALTH, "Live heart rate"),
    SPO2(Category.HEALTH, "Blood oxygen"),
    SLEEP(Category.HEALTH, "Sleep"),
    STRESS(Category.HEALTH, "Stress"),
    TEMPERATURE(Category.HEALTH, "Temperature"),

    ALARMS(Category.COMPANION, "Alarms"),
    GOALS(Category.COMPANION, "Goals"),
    NOTIFICATIONS(Category.COMPANION, "Notifications"),
    CALLS(Category.COMPANION, "Calls"),
    MUSIC(Category.COMPANION, "Music control"),
    WEATHER(Category.COMPANION, "Weather"),
    FIND_PHONE(Category.COMPANION, "Find phone"),
    FIND_WATCH(Category.COMPANION, "Find watch"),

    WATCHFACE(Category.DESIGN, "Watch faces"),
    WATCHFACE_CUSTOM(Category.DESIGN, "Custom watch faces"),
    THEME(Category.DESIGN, "Themes"),
    ICONS(Category.DESIGN, "Icons"),
    MENU_LAYOUT(Category.DESIGN, "Menu layout"),

    FIRMWARE_BACKUP(Category.FIRMWARE, "Firmware backup"),
    FIRMWARE_UPDATE(Category.FIRMWARE, "Firmware update"),
    CUSTOM_FIRMWARE(Category.FIRMWARE, "Custom firmware"),
    OPEN_OS(Category.FIRMWARE, "Open operating system");

    enum class Category(val label: String) {
        SYSTEM("System"),
        ACTIVITY("Activity"),
        HEALTH("Health"),
        COMPANION("Companion"),
        DESIGN("Design"),
        FIRMWARE("Firmware")
    }
}
