import com.android.build.api.dsl.ApplicationExtension
import com.tailormyresume.buildlogic.configureAndroidLint
import com.tailormyresume.buildlogic.configureKotlinAndroid
import com.tailormyresume.buildlogic.configureSpotlessForAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

abstract class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")

            extensions.configure<ApplicationExtension> {
                configureKotlinAndroid(this)
                configureAndroidLint(lint)
                defaultConfig.targetSdk = 36
                testOptions.animationsDisabled = true
            }
            configureSpotlessForAndroid()
        }
    }
}
