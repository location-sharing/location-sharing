import io.gitlab.arturbosch.detekt.Detekt

plugins {
  kotlin("jvm") version "1.8.21"
  id("io.gitlab.arturbosch.detekt") version("1.23.4")
}

repositories {
  mavenCentral()
}

val reportMergeXml by tasks.registering(io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
  output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.xml"))
}
val reportMergeSarif by tasks.registering(io.gitlab.arturbosch.detekt.report.ReportMergeTask::class) {
  output.set(rootProject.layout.buildDirectory.file("reports/detekt/merge.sarif"))
}

subprojects {

  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "io.gitlab.arturbosch.detekt")

  detekt {
    config.setFrom(file("${project.rootDir}/detekt/custom_config.yaml"))
    buildUponDefaultConfig = true
  }

  tasks.withType<Detekt> {
    reports {
      xml.required.set(true)
      html.required.set(true)
      txt.required.set(true)
      sarif.required.set(true)
      md.required.set(true)
    }
  }

  tasks.withType<Detekt>().configureEach {
    finalizedBy(reportMergeXml)
    finalizedBy(reportMergeSarif)
  }

//  reportMergeXml.configure {
//    input.from(tasks.withType(Detekt::class))
//  }

  dependencies {
    // ktlint wrapper, brings the "formatting" rule set, active by default
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.4")
  }
}
