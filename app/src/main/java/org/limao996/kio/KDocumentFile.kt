package org.limao996.kio

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import kotlin.random.Random

/**
 * 根Uri
 */
private const val RootUriHeader = "content://com.android.externalstorage.documents/tree/primary%3A"

/**
 * SAF权限
 */
private const val SafPermission =
    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

/**
 * [Kio] 虚拟文件
 *
 * @property context 应用上下文
 * @property path 文件路径
 * @constructor 创建 [KDocumentFile] 以操作虚拟文件
 */
class KDocumentFile(
    private val context: Context, override val path: String
) : KFile(path) {

    /**
     * 根Uri
     */
    val rootUri: Uri

    /**
     * 节点Uri
     */
    val nodeUri: Uri

    /**
     * 内容提供者
     */
    private val contentResolver = context.contentResolver!!

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

    init {
        // 格式化并分割路径
        val path = formatPath(path).split('/')

        // 生成首页uri
        var uri = RootUriHeader
        uri += path.take(if (SDK_INT < 33) 2 else 3)
            .joinToString("%2F")
        rootUri = Uri.parse(uri)

        // 切割路径
        val homeSplitPath = path.take(if (SDK_INT < 33) 2 else 3)
        val nodeUriHeader = homeSplitPath.joinToString("%2F")

        // 生成节点uri
        uri = RootUriHeader + nodeUriHeader
        uri += "/document/primary%3A"
        uri += path.joinToString("%2F")
        nodeUri = Uri.parse(uri)
    }

    /**
     * 父目录路径
     */
    override val parent = formatPath(path) + "/.."

    /**
     * 父目录对象
     */
    override val parentFile: KFile
        get() = if (isDocumentFile("sdcard/$parent")) KDocumentFile(context, parent)
        else KStorageFile(context, parent)

    /**
     * 绝对路径
     */
    override val absolutePath: String
        get() = TODO("Not yet implemented")

    /**
     * 文件名称
     */
    override val name = formatPath(path).split('/')
        .last()

    /**
     * 打开下级节点
     *
     * @param path 相对路径
     * @return [Kio] 文件对象
     */
    override fun openFile(path: String): KFile {
        val newPath = formatPath(this.path) + "/" + formatPath(path)
        return KDocumentFile(context, newPath)
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
    override fun openFileDescriptor(mode: String) = contentResolver.openFileDescriptor(
        nodeUri, when (mode) {
            "a" -> "wa"
            "t" -> "wt"
            else -> mode
        }
    )!!

    /**
     * 检查权限
     *
     * @return 权限是否完整
     */
    override fun checkPermission(): Boolean {
        // 遍历权限
        for (permission in contentResolver.persistedUriPermissions) {
            // 判断权限
            if (permission.isReadPermission && permission.isWritePermission && permission.uri == rootUri) {
                return true
            }
        }
        return false
    }

    /**
     * 请求权限
     *
     * @param callback 请求权限回调，返回请求结果
     */
    @SuppressLint("WrongConstant")
    override fun requestPermission(callback: ((Boolean) -> Unit)) {
        // 限制上下文必须是 [Activity]
        assert(context is Activity) { KioException("No Activity cannot apply for permissions") }

        // 获取虚拟文件id
        val id = DocumentsContract.getTreeDocumentId(rootUri)
        // 创建 [Intent] 对象
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).setFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION.or(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            ) or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
            .putExtra(
                "android.provider.extra.INITIAL_URI",
                DocumentsContract.buildDocumentUriUsingTree(rootUri, id)
            )
        // 申请权限并回调
        val tag = Random.nextInt()
        Kio.registerActivityResultCallback(context) { requestCode: Int, _: Int, data: Intent? ->
            if (requestCode == tag && rootUri == data?.data) {
                contentResolver.takePersistableUriPermission(
                    data.data!!, data.flags and SafPermission
                )
                callback(true)
            } else callback(false)
        }
        (context as Activity).startActivityForResult(intent, tag)
    }

    /**
     * 释放权限
     *
     * @return 是否释放成功
     */
    override fun releasePermission(): Boolean {
        return try {
            contentResolver.releasePersistableUriPermission(
                rootUri, SafPermission
            )
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 是否为虚拟文件
     *
     * @return 判断结果
     */
    override fun isDocumentFile() = true

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
    override fun createNewFile() = createNewNode("*/*")

    /**
     * 创建文件夹
     *
     * @return 结果
     */
    override fun mkdir() = createNewNode("vnd.android.document/directory")

    /**
     * 创建新节点
     *
     * @param mimeType MIME类型
     * @return 结果
     */
    private fun createNewNode(mimeType: String): Boolean {
        return try {
            val parentUri = (parentFile as KDocumentFile).nodeUri
            val uri = DocumentsContract.createDocument(
                contentResolver, parentUri, mimeType, name
            )!!
            val rawName = nodeUri.path?.split("/")
                ?.last()
            val newName = uri.path?.split("/")
                ?.last()
            if (rawName != newName) {
                DocumentsContract.deleteDocument(contentResolver, uri)
            }
            rawName == newName
        } catch (e: Exception) {
            false
        }
    }

}