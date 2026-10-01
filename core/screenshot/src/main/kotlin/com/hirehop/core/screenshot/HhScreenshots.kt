package com.hirehop.core.screenshot

import androidx.compose.ui.test.junit4.ComposeTestRule
import com.dropbox.differ.SimpleImageComparator
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.RoborazziTaskType
import com.github.takahirom.roborazzi.captureScreenRoboImage
import java.io.File

const val HH_THEME_LIGHT = "light"
const val HH_THEME_DARK = "dark"

const val HH_SCREENSHOT_DIRECTORY_PROPERTY = "roborazzi.output.dir"

private const val HH_PIXEL_TOLERANCE = 0.02f
private const val HH_DIFF_SCALE = 10_000L
private const val HH_MAX_DIFFERING_PER_TEN_THOUSAND = 5L
private const val HH_ENABLED = "true"
private const val HH_DEFAULT_OUTPUT_DIRECTORY = "build/outputs/roborazzi"
private const val HH_DEFAULT_RESULT_DIRECTORY = "build/test-results/roborazzi/results/"
private const val HH_RECORD_PROPERTY = "roborazzi.test.record"
private const val HH_COMPARE_PROPERTY = "roborazzi.test.compare"
private const val HH_VERIFY_PROPERTY = "roborazzi.test.verify"
private const val HH_COMPARE_DIRECTORY_PROPERTY = "roborazzi.compare.output.dir"

private fun propertyIsSet(name: String): Boolean = System.getProperty(name) == HH_ENABLED

private fun propertyOrDefault(name: String, fallback: String): String =
    System.getProperty(name) ?: fallback

@OptIn(ExperimentalRoborazziApi::class)
private fun hhRoborazziOptions(): RoborazziOptions = RoborazziOptions(
    captureType = RoborazziOptions.CaptureType.Screenshot(),
    taskType = RoborazziTaskType.of(
        isRecording = propertyIsSet(HH_RECORD_PROPERTY),
        isComparing = propertyIsSet(HH_COMPARE_PROPERTY),
        isVerifying = propertyIsSet(HH_VERIFY_PROPERTY),
    ),
    compareOptions = RoborazziOptions.CompareOptions(
        outputDirectoryPath = propertyOrDefault(HH_COMPARE_DIRECTORY_PROPERTY, HH_DEFAULT_OUTPUT_DIRECTORY),
        imageComparator = SimpleImageComparator(
            maxDistance = HH_PIXEL_TOLERANCE,
            hShift = 0,
            vShift = 0,
        ),
        resultValidator = { result ->
            result.pixelDifferences.toLong() * HH_DIFF_SCALE <=
                result.pixelCount.toLong() * HH_MAX_DIFFERING_PER_TEN_THOUSAND
        },
    ),
)

@OptIn(ExperimentalRoborazziApi::class)
fun captureScreenHh(filePath: String) {
    captureScreenRoboImage(filePath, hhRoborazziOptions())
}

fun HhTestDevice.imageFileName(
    screenName: String,
    theme: String,
): String = "${screenName}_${name}_$theme.png"

fun HhTestDevice.imageFile(
    outputDirectory: String,
    screenName: String,
    theme: String,
): File = File(outputDirectory, imageFileName(screenName, theme))

@OptIn(ExperimentalRoborazziApi::class)
suspend fun ComposeTestRule.captureForDevice(
    outputDirectory: String,
    screenName: String,
    device: HhTestDevice,
    theme: String = HH_THEME_LIGHT,
): String {
    val file = device.imageFile(outputDirectory, screenName, theme)
    captureScreenRoboImage(file.path, hhRoborazziOptions())
    return file.path
}

suspend fun ComposeTestRule.captureForDevices(
    outputDirectory: String,
    screenName: String,
    devices: List<HhTestDevice> = HhTestDevices.all,
    theme: String = HH_THEME_LIGHT,
): List<String> = devices.map { captureForDevice(outputDirectory, screenName, it, theme) }

@OptIn(ExperimentalRoborazziApi::class)
suspend fun ComposeTestRule.captureMultiTheme(
    outputDirectory: String,
    screenName: String,
    device: HhTestDevice = HhTestDevices.board,
    setTheme: suspend (Boolean) -> Unit,
): List<String> {
    val paths = mutableListOf<String>()
    setTheme(false)
    waitForIdle()
    paths += captureForDevice(outputDirectory, screenName, device, HH_THEME_LIGHT)
    setTheme(true)
    waitForIdle()
    paths += captureForDevice(outputDirectory, screenName, device, HH_THEME_DARK)
    return paths
}
