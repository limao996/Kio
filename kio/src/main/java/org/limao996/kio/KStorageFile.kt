package org.limao996.kio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.Settings
import androidx.core.net.toUri
import java.io.File
import kotlin.random.Random

/**
 * [Kio] 磁盘文件
 *
 * @property context 应用上下文
 * @property file 文件对象
 * @constructor 创建 [KStorageFile] 以操作磁盘文件
 */
class KStorageFile(
    override val context: Context, val file: File
) : KFile(context) {

    constructor(context: Context, path: String) : this(context, File(path))

    /**
     * 文件路径
     */
    override val path: String = file.path

    /**
     * [KFile] 类型
     *
     * @return 类型枚举对象
     */
    override val type = Type.STORAGE

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
     * 显示友好名称
     */
    override val displayName = name

    /**
     * 最后修改时间
     */
    override val lastModified by lazy { file.lastModified() }

    /**
     * 文件大小
     */
    override val size = file.length()

    /**
     * 文件Uri
     */
    override val uri: Uri by lazy { file.toUri() }

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
    override fun checkPermission() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) and (context.checkSelfPermission(
        Manifest.permission.READ_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED)


    /**
     * 请求权限
     *
     * @param callback 请求权限回调，返回请求结果
     */
    override fun requestPermission(callback: (Boolean) -> Unit) {
        // 限制上下文必须是 [Activity]
        assert(context is Activity) { KioException("No Activity cannot apply for permissions") }

        // 注册回调
        val tag = Random.nextInt()
        Kio.registerActivityResultCallback(context) { requestCode: Int, _: Int, _: Intent? ->
            if (requestCode == tag) {
                callback(checkPermission())
            } else callback(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.setData(Uri.parse("package:" + context.packageName))
            (context as Activity).startActivityForResult(intent, tag)
        } else {
            (context as Activity).requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), tag
            )
        }
    }

    /**
     * 释放权限
     *
     * @return 是否释放成功
     */
    override fun releasePermission() = file.setReadable(false) && file.setWritable(false)

    /**
     * 创建子级新文件
     *
     * @return 结果
     */
    override fun createNewFile(name: String) = openSubFile(name).createNewFile()


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
     * 重命名并打开新的节点
     *
     * @param name 新名称
     */
    override fun renameTo(name: String): KStorageFile {
        val path = resolvePath(parent, name)
        file.renameTo(File(path))
        return KStorageFile(context, path)
    }

    /**
     * 删除文件
     *
     * @return 结果
     */
    override fun delete() = file.delete()

    /**
     * 文件是否存在
     *
     * @return 结果
     */
    override fun exists() = file.exists()

    /**
     * 获取子节点路径列表
     *
     * @return 路径列表
     */
    override fun list(): Array<String> = file.list()!!

}