package org.limao996.kio

import android.content.Context
import android.os.ParcelFileDescriptor
import java.io.File

/**
 * [Kio] 磁盘文件
 *
 * @property context 应用上下文
 * @property path 文件路径
 * @constructor 创建 [KStorageFile] 以操作磁盘文件
 */
class KStorageFile(
    private val context: Context, override val path: String
) : KFile(path) {

    /**
     * 文件对象
     */
    private val file = File(path)

    /**
     * 父目录路径
     */
    override val parent = file.parent!!

    /**
     * 父目录对象
     */
    override val parentFile by lazy { KStorageFile(context, parent) }

    /**
     * 打开下级节点
     *
     * @param path 相对路径
     * @return [Kio] 文件对象
     */
    override fun openFile(path: String): KFile {
        val newPath = formatPath(this.path) + "/" + formatPath(path)
        return if (isDocumentFile(newPath)) KDocumentFile(context, toDocumentPath(newPath))
        else KStorageFile(context, newPath)
    }

    /**
     * 打开文件输入流
     *
     * @return 输入流
     */
    override fun openInputStream() =
        ParcelFileDescriptor.AutoCloseInputStream(openFileDescriptor("r"))

    /**
     * 打开文件输出流
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出流
     */
    override fun openOutputStream(mode: String) =
        ParcelFileDescriptor.AutoCloseOutputStream(openFileDescriptor(mode))

    /**
     * 打开文件输入通道
     *
     * @return 输入通道
     */
    override fun openInputChannel() = openInputStream().channel!!

    /**
     * 打开文件输出通道
     *
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出通道
     */
    override fun openOutputChannel(mode: String) = openOutputStream(mode).channel!!

    /**
     * 打开文件句柄
     *
     * @param mode 文件模式 `"r"` `"w"` `"a"` `"t"`
     * @return 文件句柄
     */
    override fun openFileDescriptor(mode: String) = ParcelFileDescriptor.open(
        file, ParcelFileDescriptor.parseMode(
            when (mode) {
                "a" -> "wa"
                "t" -> "wt"
                else -> mode
            }
        )
    )!!

    /**
     * 检查权限
     *
     * @return 权限是否完整
     */
    override fun checkPermission(): Boolean {
        return file.canRead() && file.canWrite()
    }


    /**
     * 请求权限
     *
     * @param callback 请求权限回调，返回请求结果
     */
    override fun requestPermission(callback: (Boolean) -> Unit) {
        callback(
            file.setReadable(true) && file.setWritable(true)
        )
    }

    /**
     * 释放权限
     *
     * @return 是否释放成功
     */
    override fun releasePermission() = file.setReadable(false) && file.setWritable(false)

    /**
     * 是否为虚拟文件
     *
     * @return 判断结果
     */
    override fun isDocumentFile(): Boolean = false

}