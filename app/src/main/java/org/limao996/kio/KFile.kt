package org.limao996.kio

import android.os.Build.VERSION.SDK_INT
import android.os.ParcelFileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.channels.FileChannel

/**
 * [Kio] 文件抽象类
 *
 * @property path 文件路径
 * @constructor 创建 [KFile] 对象以操作文件
 */
abstract class KFile(open val path: String) {

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
     * 打开下级节点
     *
     * @param path 相对路径
     * @return [Kio] 文件对象
     */
    abstract fun openFile(path: String): KFile

    /**
     * 打开文件输入流
     *
     * @return 输入流
     */
    abstract fun openInputStream(): FileInputStream

    /**
     * 打开文件输出流
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出流
     */
    abstract fun openOutputStream(mode: String = "w"): FileOutputStream

    /**
     * 打开文件输入通道
     *
     * @return 输入通道
     */
    abstract fun openInputChannel(): FileChannel

    /**
     * 打开文件输出通道
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出通道
     */
    abstract fun openOutputChannel(mode: String = "w"): FileChannel

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
     * 是否为虚拟文件
     *
     * @return 判断结果
     */
    abstract fun isDocumentFile(): Boolean

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
         * 是否为虚拟文件
         *
         * @param file [Kio] 文件对象
         */
        @JvmStatic
        fun isDocumentFile(file: KFile) = file.isDocumentFile()

        /**
         * 是否为虚拟文件
         *
         * @param path 文件路径
         */
        @JvmStatic
        fun isDocumentFile(path: String): Boolean {
            // 低版本不需要 `Saf`
            if (SDK_INT < 30) return false
            // 遍历匹配并判断
            for (doc in DocumentPaths) {
                if (formatPath(path).startsWith(doc, true)) {
                    return true
                }
            }
            return false
        }

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
                    val header = doc.split('/')
                        .takeLast(2)
                        .joinToString("/")
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
    }

    override fun toString(): String = (this::class.simpleName ?: "KFile") + ": /" + formatPath(path)
    override fun equals(other: Any?) =
        other is KFile && formatPath(absolutePath) == formatPath(other.absolutePath)

}