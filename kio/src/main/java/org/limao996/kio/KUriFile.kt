package org.limao996.kio

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract

/**
 * [Kio] Uri文件
 *
 * @property context 应用上下文
 * @property uri 文件 [Uri]
 * @constructor 创建 [KUriFile] 以操作Uri文件
 */
class KUriFile(
    override val context: Context, override val uri: Uri
) : KFile(context) {

    /**
     * 内容提供者
     */
    private val contentResolver = context.contentResolver!!

    /**
     * 查询文件属性
     *
     * @param keys key列表
     * @return 数据库光标
     */
    private fun query(vararg keys: String) = contentResolver.query(uri, keys, null, null, null)!!

    /**
     * 文件路径
     */
    override val path by lazy { uri.path!! }

    /**
     * [KFile] 类型
     *
     * @return 类型枚举对象
     */
    override val type = Type.URI

    /**
     * 父目录路径
     */
    override val parent: String
        get() = TODO("Not yet implemented")

    /**
     * 父目录对象
     */
    override val parentFile: KFile
        get() = TODO("Not yet implemented")

    /**
     * 绝对路径
     */
    override val absolutePath: String
        get() = TODO("Not yet implemented")

    /**
     * 文件名称
     */
    override val name by lazy { displayName }

    /**
     * 是否为文件
     */
    override val isFile by lazy {
        val cursor = query(DocumentsContract.Document.COLUMN_MIME_TYPE)
        if (!cursor.moveToFirst()) {
            return@lazy false
        }
        val mimeType = cursor.getString(0)
        cursor.close()
        mimeType != DocumentsContract.Document.MIME_TYPE_DIR
    }

    /**
     * 显示友好名称
     */
    override val displayName: String by lazy {
        query(DocumentsContract.Document.COLUMN_DISPLAY_NAME).use {
            it.moveToFirst()
            it.getString(0)
        }
    }

    /**
     * 最后修改时间
     */
    override val lastModified: Long by lazy {
        query(DocumentsContract.Document.COLUMN_LAST_MODIFIED).use {
            it.moveToFirst()
            it.getLong(0)
        }
    }

    /**
     * 文件大小
     */
    override val size: Long by lazy {
        query(DocumentsContract.Document.COLUMN_SIZE).use {
            it.moveToFirst()
            it.getLong(0)
        }
    }

    /**
     * 打开文件句柄
     *
     * @param mode 文件模式 `"r"` `"w"` `"a"` `"t"`
     * @return 文件句柄
     */
    override fun openFileDescriptor(mode: String) = contentResolver.openFileDescriptor(
        uri, when (mode) {
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
    override fun checkPermission() = TODO("Not yet implemented")

    /**
     * 请求权限
     *
     * @param callback 请求权限回调，返回请求结果
     */
    @SuppressLint("WrongConstant")
    override fun requestPermission(callback: ((Boolean) -> Unit)) = TODO("Not yet implemented")


    /**
     * 释放权限
     *
     * @return 是否释放成功
     */
    override fun releasePermission() = false

    /**
     * 创建子级新文件
     *
     * @return 结果
     */
    override fun createNewFile(name: String) = TODO("Not yet implemented")

    /**
     * 创建新文件
     *
     * @return 结果
     */
    override fun createNewFile() = TODO("Not yet implemented")

    /**
     * 创建文件夹
     *
     * @return 结果
     */
    override fun mkdir() = TODO("Not yet implemented")

    /**
     * 重命名并打开新的节点
     *
     * @param name 新名称
     */
    override fun renameTo(name: String): KUriFile {
        val newUri = DocumentsContract.renameDocument(context.contentResolver, uri, name)!!
        return KUriFile(context, newUri)
    }

    /**
     * 删除文件
     *
     * @return 结果
     */
    override fun delete() = DocumentsContract.deleteDocument(contentResolver, uri)

    /**
     * 文件是否存在
     *
     * @return 结果
     */
    override fun exists() = try {
        query(DocumentsContract.Document.COLUMN_DOCUMENT_ID).use {
            it.count > 0
        }
    } catch (e: Exception) {
        false
    }

    /**
     * 获取子节点路径列表
     *
     * @return 路径列表
     */
    override fun list(): Array<String> {
        val id = DocumentsContract.getDocumentId(uri)
        val tree = DocumentsContract.buildChildDocumentsUriUsingTree(uri, id)
        return contentResolver.query(
            tree, arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID), null, null, null
        ).use {
            val list = ArrayList<String>()
            while (it!!.moveToNext()) {
                list.add(
                    it.getString(0).split('/').last()
                )
            }
            list.toTypedArray()
        }
    }

}