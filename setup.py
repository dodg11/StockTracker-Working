"""
StockTracker - Автоматическая генерация проекта
Запустите: python setup.py
"""

import os

BASE = os.path.dirname(os.path.abspath(__file__))

def create_file(path, content):
    full_path = os.path.join(BASE, path)
    os.makedirs(os.path.dirname(full_path), exist_ok=True)
    with open(full_path, "w", encoding="utf-8") as f:
        f.write(content)
    print(f"✅ {path}")

# ============================================
# GRADLE FILES
# ============================================

create_file("build.gradle", """buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
""")

create_file("settings.gradle", """include ':app'
""")

create_file("gradle.properties", """org.gradle.jvmargs=-Xmx2048m
android.useAndroidX=true
android.enableJetifier=true
""")

create_file("app/build.gradle", """plugins {
    id 'com.android.application'
}

android {
    namespace 'com.stocktracker.app'
    compileSdk 34

    defaultConfig {
        applicationId "com.stocktracker.app"
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0"
    }

    buildTypes {
        release {
            minifyEnabled false
            shrinkResources false
        }
        debug {
            minifyEnabled false
        }
    }
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding true
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    implementation 'androidx.work:work-runtime:2.9.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
}
""")

create_file("gradle/wrapper/gradle-wrapper.properties", """distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.2-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
""")

print("\n🎉 Gradle файлы созданы!")
print("📋 Теперь скопируйте Java файлы и ресурсы из архива StockTracker.zip")
