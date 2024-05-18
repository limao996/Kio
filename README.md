# Kio

[![](https://img.shields.io/github/license/limao996/Kio.svg)]()
[![](https://jitpack.io/v/limao996/Kio.svg)](https://jitpack.io/#limao996/Kio)

[![](https://img.shields.io/badge/Github-仓库-0969DA?logo=github)](https://github.com/limao996/Kio)
[![](https://img.shields.io/badge/Gitee-仓库-C71D23?logo=gitee)](https://gitee.com/limao996/Kio)
[![](https://img.shields.io/badge/QQ-17453684-0099FF?logo=tencentqq)](https://qm.qq.com/cgi-bin/qm/qr?k=cXJY7qL3Vm3OKtk8_PjJdgnHqoS_sfGL&noverify=0&personal_qrcode_source=3)

[![](https://img.shields.io/badge/QQ群-884183161-0099FF?logo=tencentqq)](https://qm.qq.com/q/3aHOYecyNO)
[![](https://img.shields.io/badge/Telegram-limao__lua-0099FF?logo=telegram)](https://t.me/limao_lua)

## 导入依赖

将其添加到根目录的 `build.gradle.kts` 文件中

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

在项目 `build.gradle.kts` 添加依赖

```kotlin
dependencies {
    implementation("com.github.limao996:Kio:1.1.0")
}
```