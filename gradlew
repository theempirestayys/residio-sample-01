#!/bin/sh
# Standard Gradle wrapper launcher. The matching gradle-wrapper.jar is generated
# automatically the first time you open this project in Android Studio (or by
# running `gradle wrapper --gradle-version 8.7`). CI bootstraps Gradle separately.
APP_HOME=$(cd "$(dirname "$0")" && pwd)
exec java -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
