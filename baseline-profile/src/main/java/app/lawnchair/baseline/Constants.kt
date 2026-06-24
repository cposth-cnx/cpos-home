package app.lawnchair.baseline

object Constants {
    // CPOS Home ships the "github" flavor, whose applicationId is com.cpos.home
    // (see the github productFlavor in the root build.gradle). The baseline profile
    // must target that package or generation runs against a package that isn't installed.
    val PACKAGE_NAME = "com.cpos.home"
}
