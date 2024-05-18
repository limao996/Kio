# Kio

基于Kotlin的Android通用文件操作库

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
    implementation("com.github.limao996:Kio:1.1.1")
}
```

## 使用方法/示例

在 `Activity` 中实例化 `Kio` 对象并注册 `onActivityResult` 回调

```kotlin
class MainActivity : AppCompatActivity() {
    private val kio = Kio(this)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        kio.onActivityResult(requestCode, resultCode, data)
    }
}

```

调用 `kio.open` 打开 `KFile` 对象

```kotlin
// 返回 [KStorageFile] 对象
kio.open("/sdcard/test.txt")

// 返回 [KStorageFile] 对象
kio.open(File("/sdcard/test.txt"))

// 根据情况会返回 [KStorageFile] 或 [KDocumentFile]
kio.open("/sdcard/Android/data/bin.mt.plus/test.txt")

// 返回 [KUriFile] 对象
kio.open(intent.data!!)

```

处理文件操作所需权限

```kotlin
// 检查并申请权限，回调事件返回结果
file.checkAndRequestPermission { granted -> }

// 检查权限
file.checkPermission()

// 申请权限
file.requestPermission { granted -> }

// 释放权限，返回结果
// 目前仅 [KDocumentFile] 支持
file.releasePermission()

// [KUriFile] 不支持处理权限
```