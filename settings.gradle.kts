enableFeaturePreview("VERSION_CATALOGS")

includeBuild("./build-logic")

rootProject.name = "lin-bus"
include("common")
include("stream")
include("tree")
// Pending a lot of work, ignoring for now
// include("gui")
