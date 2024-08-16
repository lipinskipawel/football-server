import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

fun Project.findLibrary(libName: String) = extensions
    .getByType<VersionCatalogsExtension>()
    .named("libs")
    .findLibrary(libName)
    .get()
