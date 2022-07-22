/*
This file is part of Intake24.

Copyright 2015, 2016, 2017 Newcastle University.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

// name := "intake24-survey-site-abs"
val organization = "uk.ac.ncl.openlab.intake24"

// name := "intake24-survey-site-abs"

name := "intake24-survey-site"


//organization := organizationVal

description := "Intake24 GWT survey client"

maintainer := "Ivan Poliakov <ivan.poliakov@ncl.ac.uk>"

version := "3.2.0-SNAPSHOT"

scalaVersion := "2.12.14"

resolvers += Resolver.mavenLocal

lazy val packageManagerSettings = Seq(
  rpmRelease := "3.0.11",
  rpmVendor := "uk.ac.ncl.openlab.intake24",
  rpmUrl := Some("https://github.com/intake24/survey-frontend"),
  rpmLicense := Some("ASL 2.0")
)

libraryDependencies ++= Seq(
  ws,
  guice,
  "org.webjars" % "font-awesome" % "5.7.2",
  "org.webjars.npm" % "cookieconsent" % "3.1.0",
  "org.webjars" %% "webjars-play" % "2.7.0",
  "org.webjars" % "jquery" % "3.6.0",
  "org.webjars" % "bootstrap" % "3.4.1",
  "uk.ac.ncl.openlab.intake24" % "survey-client" % "3.1.0-SNAPSHOT",
  "com.lihaoyi" %% "upickle" % "0.4.4",
  "com.google.gwt" % "gwt-user" % "2.8.2" // for stack trace deobfuscator
)

dependencyOverrides ++= Set(
  "org.apache.commons" % "commons-compress" % "1.21",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.10.8"
)

javaOptions in Universal ++= Seq(
  // JVM memory tuning
  "-J-Xmx320m",
  "-J-Xms128m",

  // Since play uses separate pidfile we have to provide it with a proper path
  s"-Dpidfile.path=/dev/null",

  // Use separate configuration file for production environment
  s"-Dconfig.file=/usr/share/${packageName.value}/conf/production.conf",

  // Use separate logger configuration file for production environment
  s"-Dlogger.file=/usr/share/${packageName.value}/conf/production-logger.xml"
)


//lazy val root = (project in file(".")).enablePlugins(PlayScala, SystemdPlugin, JDebPackaging)

// lazy val root = (project in file(".")).
//   enablePlugins(PlayScala, SystemdPlugin, RpmPlugin).
//   settings(
//     name := "intake24-survey-site",
//     scalaVersion := "2.12.14",
//     defaultLinuxInstallLocation := "/opt",
//     rpmPrefix := Some("/opt"),
//     linuxPackageSymlinks := Seq.empty,
//     defaultLinuxLogsLocation := defaultLinuxInstallLocation + "/" + name,
//     packageName in Rpm := "intake24-survey-site",
//     packageArchitecture in Rpm := "noarch",
//     rpmRelease := "1",
//     rpmVendor := "Monash",
//     rpmUrl := Some("http://www.intake24.com"),
//     rpmLicense := Some("MIT"),
//     rpmGroup := Some("Applications/intake24")
//   )

lazy val root = (project in file(".")).settings(packageManagerSettings: _*).enablePlugins(PlayScala, SystemdPlugin, JDebPackaging, RpmPlugin)

