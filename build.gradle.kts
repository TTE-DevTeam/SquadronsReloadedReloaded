plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.7.1" // Check for new versions at https://plugins.gradle.org/plugin/io.papermc.paperweight.userdev
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    flatDir {
        dirs("lib")
    }
}

dependencies {
    //compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    implementation(files("lib/Movecraft-sign-testbuild-9.jar"))
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)