package org.limao996.kio

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * [Kio] 磁盘文件
 *
 * @property context 应用上下文
 * @property path 文件路径
 * @constructor 创建 [KStorageFile] 以操作磁盘文件
 */
class KStorageFile(override val path: String) : KFile(path) {

    /**
     * 文件对象
     */
    private val file = File(path)

    /**
     * 文件输入流
     */
    override val inputStream by lazy { FileInputStream(file) }

    /**
     * 文件输出流
     */
    override val outputStream by lazy { FileOutputStream(file) }

    /**
     * 文件输入通道
     */
    override val inputChannel by lazy { inputStream.channel!! }

    /**
     * 文件输出通道
     */
    override val outputChannel by lazy { outputStream.channel!! }

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