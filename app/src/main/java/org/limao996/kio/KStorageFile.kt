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
    override val context: Context, override val path: String
) : KFile(context, path) {

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
     * 绝对路径
     */
    override val absolutePath: String = file.absolutePath

    /**
     * 文件名称
     */
    override val name: String = file.name

    /**
     * 是否为文件
     */
    override val isFile by lazy { file.isFile }

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

    /**
     * 创建子级新文件
     *
     * @return 结果
     */
    override fun createNewFile(name: String) = openFile(name).createNewFile()


    /**
     * 创建新文件
     *
     * @return 结果
     */
    override fun createNewFile() = file.createNewFile()

    /**
     * 创建文件夹
     *
     * @return 结果
     */
    override fun mkdir() = file.mkdir()

    /**
     * 删除文件
     *
     * @return 结果
     */
    override fun delete() = file.delete()

}