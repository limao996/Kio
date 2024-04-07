package org.limao996.kio

import android.os.Build.VERSION.SDK_INT
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.FileChannel

/**
 * 虚拟目录列表
 */
private val DocumentPaths = arrayOf(
    "sdcard/Android/data", "storage/emulated/0/Android/data",
    "sdcard/Android/obb", "storage/emulated/0/Android/obb",
)

/**
 * [Kio] 文件抽象类
 *
 * @property context 应用上下文
 * @property path 文件路径
 * @constructor 创建 [KFile] 对象以操作文件
 */
abstract class KFile(open val path: String) {

    /**
     * 文件输入流
     */
    abstract val inputStream: InputStream

    /**
     * 文件输出流
     */
    abstract val outputStream: OutputStream

    /**
     * 文件输入通道
     */
    abstract val inputChannel: FileChannel

    /**
     * 文件输出通道
     */
    abstract val outputChannel: FileChannel

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
    abstract fun requestPermission(callback: (Boolean) -> Unit)

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

    companion object {
        /**
         * 是否为虚拟文件
         *
         * @param file [Kio] 文件对象
         */
        fun isDocumentFile(file: KFile) = file.isDocumentFile()

        /**
         * 是否为虚拟文件
         *
         * @param path 文件路径
         */
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
        private fun formatPath(path: String): String {
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
}