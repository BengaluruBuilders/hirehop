import com.tailormyresume.buildlogic.configureSpotlessForRootProject
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class RootPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target.path == ":")
        target.configureSpotlessForRootProject()
    }
}
