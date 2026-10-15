signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    // اگر debug.keystore فائل موجود ہو تو وہ استعمال کرے، ورنہ ڈیفالٹ
    getByName("debug") {
      val customDebugKeystore = file("${rootDir}/debug.keystore")
      if (customDebugKeystore.exists()) {
        storeFile = customDebugKeystore
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // یہاں ڈیفالٹ "debug" کی کا استعمال کریں
      signingConfig = signingConfigs.getByName("debug")
    }
  }
