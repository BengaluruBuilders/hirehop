import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure

class HireHopKoverPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "org.jetbrains.kotlinx.kover")

            extensions.configure<KoverProjectExtension> {
                reports {
                    verify {
                        rule {
                            bound {
                                minValue = 80
                                coverageUnits = CoverageUnit.LINE
                                aggregationForGroup = AggregationType.COVERED_PERCENTAGE
                            }
                        }
                    }
                }
            }
        }
    }
}
