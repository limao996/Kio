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
private const val RootUri = "content://com.android.externalstorage.documents/tree/primary%3A"

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
     * 首页Uri
     */
    private val homeUri: Uri

    /**
     * 节点Uri
     */
    private val nodeUri: Uri

    /**
     * 首页路径
     */
    private val home: String

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
        var uri = RootUri
        uri += path.take(if (SDK_INT < 33) 2 else 3)
            .joinToString("%2F")
        homeUri = Uri.parse(uri)

        // 切割路径
        val homeSplitPath = path.take(if (SDK_INT < 33) 2 else 3)
        val nodeUriHeader = homeSplitPath.joinToString("%2F")
        home = homeSplitPath.joinToString("/")

        // 生成节点uri
        uri = RootUri + nodeUriHeader
        uri += "/document/primary%3A"
        uri += path.joinToString("%2F")
        nodeUri = Uri.parse(uri)
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
            if (permission.isReadPermission && permission.isWritePermission && permission.uri == homeUri) {
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
        val id = DocumentsContract.getTreeDocumentId(homeUri)
        // 创建 [Intent] 对象
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).setFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION.or(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            ) or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
        )
            .putExtra(
                "android.provider.extra.INITIAL_URI",
                DocumentsContract.buildDocumentUriUsingTree(homeUri, id)
            )
        // 申请权限并回调
        val tag = Random.nextInt()
        Kio.registerActivityResultCallback(context) { requestCode: Int, _: Int, data: Intent? ->
            if (requestCode == tag && homeUri == data?.data) {
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
                homeUri, SafPermission
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
    override fun isDocumentFile(): Boolean = true

}