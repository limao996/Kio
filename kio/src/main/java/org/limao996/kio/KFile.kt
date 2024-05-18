package org.limao996.kio

import android.content.Context
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * 内置存储
 */
val Sdcard: String = Environment.getExternalStorageDirectory().path

/**
 * [Kio] 文件抽象类
 *
 * @property context 应用上下文
 * @constructor 创建 [KFile] 对象以操作文件
 */
abstract class KFile(open val context: Context) {

    /**
     * 文件路径
     */
    abstract val path: String

    /**
     * [KFile] 类型
     *
     * @return 类型枚举对象
     */
    abstract val type: Type


    /**
     * 父目录路径
     */
    abstract val parent: String

    /**
     * 父目录对象
     */
    abstract val parentFile: KFile

    /**
     * 绝对路径
     */
    abstract val absolutePath: String

    /**
     * 文件名称
     */
    abstract val name: String

    /**
     * 是否为文件
     */
    abstract val isFile: Boolean

    /**
     * 显示友好名称
     */
    abstract val displayName: String

    /**
     * 最后修改时间
     */
    abstract val lastModified: Long

    /**
     * 文件大小
     */
    abstract val size: Long

    /**
     * 文件Uri
     */
    abstract val uri: Uri

    /**
     * 是否为文件夹
     */
    open val isDirectory by lazy { !isFile }

    /**
     * 打开下级节点
     *
     * @param path 相对路径
     * @return [Kio] 文件对象
     */
    open fun openSubFile(path: String): KFile {
        val target = absolutePath + "/" + formatPath(path)
        return openFile(context, target)
    }

    /**
     * 打开文件输入流
     *
     * @return 输入流
     */
    open fun openInputStream() = ParcelFileDescriptor.AutoCloseInputStream(openFileDescriptor("r"))

    /**
     * 打开文件输出流
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出流
     */
    open fun openOutputStream(mode: String = "w") =
        ParcelFileDescriptor.AutoCloseOutputStream(openFileDescriptor(mode))

    /**
     * 打开文件输入通道
     *
     * @return 输入通道
     */
    open fun openInputChannel() = openInputStream().channel!!

    /**
     * 打开文件输出通道
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出通道
     */
    open fun openOutputChannel(mode: String = "w") = openOutputStream(mode).channel!!


    /**
     * 打开文件句柄
     *
     * @param mode 文件模式 `"r"` `"w"` `"a"` `"t"`
     * @return 文件句柄
     */
    abstract fun openFileDescriptor(mode: String): ParcelFileDescriptor

    /**
     * 检查权限
     *
     * @return 权限是否完整
     */
    abstract fun checkPermission(): Boolean

    /**
     * 请求权限
     *
     * @param callback 请求权限回调，返回请求结果
     */
    abstract fun requestPermission(callback: (Boolean) -> Unit = {})

    /**
     * 释放权限
     *
     * @return 是否释放成功
     */
    abstract fun releasePermission(): Boolean

    /**
     * 创建子级新文件
     *
     * @return 结果
     */
    abstract fun createNewFile(name: String): Boolean

    /**
     * 创建新文件
     *
     * @return 结果
     */
    abstract fun createNewFile(): Boolean

    /**
     * 创建文件夹
     *
     * @return 结果
     */
    abstract fun mkdir(): Boolean

    /**
     * 重命名并打开新的节点
     *
     * @param name 新名称
     */
    abstract fun renameTo(name: String): KFile

    /**
     * 删除文件
     *
     * @return 结果
     */
    abstract fun delete(): Boolean

    /**
     * 文件是否存在
     *
     * @return 结果
     */
    abstract fun exists(): Boolean

    /**
     * 获取子节点路径列表
     *
     * @return 路径列表
     */
    abstract fun list(): Array<String>

    /**
     * 获取子节点对象列表
     *
     * @return 对象列表
     */
    open fun listFiles() = list().map {
        openSubFile(it)
    }.toTypedArray()

    /**
     * 检查或请求权限
     *
     * @param callback 回调，返回是否拥有权限
     */
    open fun checkOrRequestPermission(callback: (Boolean) -> Unit = {}) {
        if (checkPermission()) callback(true)
        else requestPermission(callback)
    }

    /**
     * 复制内容到目标文件并清空原内容
     *
     * @param file 目标文件
     */
    open fun copyContentTo(file: KFile) {
        val fis = openInputStream()
        val fos = file.openOutputStream("t")
        val fic = fis.channel
        val foc = fos.channel
        val size = fic.size()
        var left = size
        while (left > 0) {
            left -= fic.transferTo(size - left, left, foc)
        }
        fic.close()
        foc.close()
        fis.close()
        fos.close()
    }

    /**
     * 拷贝到目标节点
     *
     * @param target 目标节点
     */
    fun copyTo(target: KFile) {
        // 复制文件
        if (isFile) {
            target.createNewFile()
            copyContentTo(target)
            return
        }

        // 创建文件夹
        target.mkdir()
        // 遍历文件夹并复制
        for (path in list()) {
            openSubFile(path).copyTo(target.openSubFile(path))
        }
    }


    /**
     * 移动到目标节点
     *
     * @param target 目标节点
     */
    fun moveTo(target: KFile) {
        copyTo(target)
        delete()
    }

    /**
     * 清空文件内容
     *
     */
    open fun clear() = openOutputStream("t").close()


    companion object {
        /**
         * 虚拟目录列表
         */
        @JvmStatic
        private val DocumentPaths = arrayOf(
            "sdcard/Android/data", "storage/emulated/0/Android/data",
            "sdcard/Android/obb", "storage/emulated/0/Android/obb",
            "sdcard/Android/sandbox", "storage/emulated/0/Android/sandbox",
        )

        /**
         * [KFile] 类型
         *
         * @param file [Kio] 文件对象
         */
        @JvmStatic
        fun getType(file: KFile) = file.type

        /**
         * [KFile] 类型
         *
         * @param path 文件路径
         */
        @JvmStatic
        fun getType(path: String): Type {
            // 低版本不需要 `Saf`
            if (SDK_INT < 30) return Type.STORAGE
            // 遍历匹配并判断
            for (doc in DocumentPaths) {
                if (formatPath(path).startsWith(doc, true)) {
                    return Type.DOCUMENT
                }
            }
            return Type.STORAGE
        }

        /**
         * [KFile] 类型
         *
         * @param uri 文件Uri
         */
        @JvmStatic
        fun getType(uri: Uri) = Type.URI

        /**
         * 将绝对路径转换为虚拟路径
         *
         * @param path 绝对路径
         * @return 虚拟路径
         */
        @JvmStatic
        fun toDocumentPath(path: String): String {
            // 格式化路径
            val rawPath = formatPath(path)
            // 遍历匹配
            for (doc in DocumentPaths) {
                if (rawPath.startsWith(doc, true)) {
                    // 截取路径
                    val header = doc.split('/').takeLast(2).joinToString("/")
                    return "$header/" + rawPath.drop(doc.length + 1)
                }
            }
            return rawPath
        }

        /**
         * 格式化路径
         *
         * @param path 文件路径
         * @return 结果
         */
        @JvmStatic
        fun formatPath(path: String): String {
            var newPath = path
            // 去头
            if (newPath.startsWith("/")) {
                newPath = newPath.drop(1)
            }
            // 去尾
            if (newPath.endsWith("/")) {
                newPath = newPath.dropLast(1)
            }
            return newPath
        }

        /**
         * 拼接路径
         *
         * @param parent 父路径
         * @param child 子路径
         */
        @JvmStatic
        fun resolvePath(parent: String, child: String) =
            formatPath(parent) + "/" + formatPath(child)

        /**
         * 将绕过 `Saf` 的特殊字符添加给绝对路径
         *
         * @param path 绝对路径
         */
        @JvmStatic
        private fun toBypassSafPath(path: String): String {
            // 格式化路径
            val rawPath = formatPath(path)
            // 遍历匹配
            for (doc in DocumentPaths) {
                if (rawPath.startsWith(doc, true)) {
                    val head = rawPath.take(doc.length - 1)
                    val body = rawPath.drop(doc.length - 1)
                    return "$head\u200d$body"
                }
            }
            return rawPath
        }

        /**
         * 打开文件节点
         *
         * @param path 绝对路径
         * @return [Kio] 文件对象
         */
        @JvmStatic
        fun openFile(context: Context, path: String): KFile {
            return if (getType(path) == Type.STORAGE) KStorageFile(context, path)
            else if (useBypassSaf and canBypassSaf) KStorageFile(context, toBypassSafPath(path))
            else KDocumentFile(context, toDocumentPath(path))

        }

        /**
         * 打开文件节点
         *
         * @param uri 文件 [Uri]
         * @return [Kio] 文件对象
         */
        @JvmStatic
        fun openFile(context: Context, uri: Uri) = KUriFile(context, uri)

        /**
         * 打开文件节点
         *
         * @param file 文件 [File]
         * @return [Kio] 文件对象
         */
        @JvmStatic
        fun openFile(context: Context, file: File) = KStorageFile(context, file)

        /**
         * 是否支持绕过Saf
         */
        @JvmStatic
        val canBypassSaf by lazy {
            File("$Sdcard/Android\u200d/data").canRead()
        }

        /**
         * 是否绕过Saf
         */
        @JvmStatic
        var useBypassSaf = true
    }

    override fun toString(): String = (this::class.simpleName ?: "KFile") + ": /" + formatPath(path)
    override fun equals(other: Any?) =
        other is KFile && formatPath(absolutePath) == formatPath(other.absolutePath)

    override fun hashCode(): Int {
        return absolutePath.hashCode()
    }

    enum class Type {
        STORAGE, DOCUMENT, URI
    }
}