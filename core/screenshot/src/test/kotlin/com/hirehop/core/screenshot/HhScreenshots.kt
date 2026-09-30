package com.hirehop.core.screenshot

import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File

const val HH_THEME_LIGHT = "light"
const val HH_THEME_DARK = "dark"

fun HhTestDevice.imageFileName(
    screenName: String,
    theme: String,
): String = "${screenName}_${name}_$theme.png"

fun HhTestDevice.imageFile(
    outputDirectory: String,
    screenName: String,
    theme: String,
): File = File(outputDirectory, imageFileName(screenName, theme))

suspend fun ComposeTestRule.captureForDevice(
    outputDirectory: String,
    screenName: String,
    device: HhTestDevice,
    theme: String = HH_THEME_LIGHT,
): String {
    val file = device.imageFile(outputDirectory, screenName, theme)
    onRoot().captureRoboImage(file.path)
    return file.path
}

suspend fun ComposeTestRule.captureForDevices(
    outputDirectory: String,
    screenName: String,
    devices: List<HhTestDevice> = HhTestDevices.all,
    theme: String = HH_THEME_LIGHT,
): List<String> = devices.map { captureForDevice(outputDirectory, screenName, it, theme) }

suspend fun ComposeTestRule.captureMultiTheme(
    outputDirectory: String,
    screenName: String,
    device: HhTestDevice = HhTestDevices.board,
): List<String> = listOf(HH_THEME_LIGHT, HH_THEME_DARK).map { theme ->
    captureForDevice(outputDirectory, screenName, device, theme)
}
