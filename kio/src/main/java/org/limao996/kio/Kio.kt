package org.limao996.kio

import android.content.Context
import android.content.Intent
import java.nio.charset.Charset
import java.util.WeakHashMap

/**
 * [Kio] 文件管理与操作
 *
 * @property context 应用上下文
 * @constructor 创建 [Kio] 对象以管理与操作文件
 */
class Kio(private val context: Context) {

    /**
     * `onActivityResult` 回调队列
     */
    private val onActivityResultCallbackList = ArrayList<OnActivityResultCallback>()

    /**
     * 转发 [Activity] 的 `onActivityResult` 回调
     *
     * @param requestCode 请求代码
     * @param resultCode 结果代码
     * @param data 返回结果
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResultCallbackList.removeIf {
            it(requestCode, resultCode, data)
            true
        }
    }

    init {
        // 为 [onActivityResultCallbackList] 建立映射到伴生对象
        onActivityResultCallbackListMap[context] = onActivityResultCallbackList
    }

    companion object {
        /**
         * `onActivityResult` 回调队列的映射表
         */
        @JvmStatic
        val onActivityResultCallbackListMap =
            WeakHashMap<Context, ArrayList<OnActivityResultCallback>>()

        /**
         * 注册 `onActivityResult` 回调
         *
         * @param context 应用上下文
         * @param callback 回调函数
         */
        @JvmStatic
        fun registerActivityResultCallback(context: Context, callback: OnActivityResultCallback) {
            onActivityResultCallbackListMap[context]!!.add(callback)
        }

        /**
         * 是否为虚拟文件
         *
         * @param path 路径
         */
        @JvmStatic
        fun isDocumentFile(path: String) = KFile.isDocumentFile(path)
    }

    /**
     * 检查权限
     *
     * @param path 路径
     * @return 权限是否完整
     */
    fun checkPermission(path: String): Boolean {
        if (isDocumentFile(path)) {
            return KDocumentFile(context, KFile.toDocumentPath(path)).checkPermission()
        }
        return KStorageFile(context, path).checkPermission()
    }

    /**
     * 请求权限
     *
     * @param path 路径
     * @param callback 请求权限回调，返回请求结果
     */
    fun requestPermission(path: String, callback: (Boolean) -> Unit) {
        if (isDocumentFile(path)) {
            return KDocumentFile(context, KFile.toDocumentPath(path)).requestPermission(
                callback
            )
        }
        return KStorageFile(context, path).requestPermission(callback)
    }

    /**
     * 释放权限
     *
     * @param path 路径
     * @return 是否释放成功
     */
    fun releasePermission(path: String): Boolean {
        if (isDocumentFile(path)) {
            return KDocumentFile(context, KFile.toDocumentPath(path)).releasePermission()
        }
        return KStorageFile(context, path).releasePermission()
    }

    /**
     * 判断是否为虚拟文件并获取对应 [KFile] 对象
     *
     * @param path 路径
     * @return [KFile] 对象
     */
    fun open(path: String): KFile {
        if (isDocumentFile(path)) {
            return KDocumentFile(context, KFile.toDocumentPath(path))
        }
        return KStorageFile(context, path)
    }

    /**
     * 创建新文件
     *
     * @param path 路径
     * @return 结果
     */
    fun createNewFile(path: String) = open(path).createNewFile()

    /**
     * 创建文件夹
     *
     * @param path 路径
     * @return 结果
     */
    fun mkdir(path: String) = open(path).mkdir()

    /**
     * 删除文件
     *
     * @param path 路径
     * @return 结果
     */
    fun delete(path: String) = open(path).delete()

    /**
     * 文件是否存在
     *
     * @param path 路径
     * @return 结果
     */
    fun exists(path: String) = open(path).exists()

    /**
     * 获取子节点路径列表
     *
     * @param path 路径
     * @return 路径列表
     */
    fun list(path: String) = open(path).list()

    /**
     * 获取子节点对象列表
     *
     * @param path 路径
     * @return 对象列表
     */
    fun listFiles(path: String) = open(path).listFiles()


    /**
     * 打开文件句柄
     *
     * @param path 路径
     * @param mode 文件模式 `"r"` `"w"` `"a"` `"t"`
     * @return 文件句柄
     */
    fun openFileDescriptor(path: String, mode: String) = open(path).openFileDescriptor(mode)

    /**
     * 打开文件输入流
     *
     * @param path 路径
     * @return 输入流
     */
    fun openInputStream(path: String) = open(path).openInputStream()

    /**
     * 打开文件输出流
     *
     * @param path 路径
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出流
     */
    fun openOutputStream(path: String, mode: String = "w") = open(path).openOutputStream(mode)

    /**
     * 打开文件输入通道
     *
     * @param path 路径
     * @return 输入通道
     */
    fun openInputChannel(path: String) = openInputStream(path).channel!!

    /**
     * 打开文件输出通道
     *
     * @param path 路径
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     * @return 输出通道
     */
    fun openOutputChannel(path: String, mode: String = "w") = openOutputStream(path, mode).channel!!

    /**
     * 是否为文件
     *
     * @param path 路径
     */
    fun isFile(path: String) = open(path).isFile

    /**
     * 是否为文件夹
     *
     * @param path 路径
     */
    fun isDirectory(path: String) = open(path).isDirectory

    /**
     * 获取显示友好名称
     *
     * @param path 路径
     */
    fun getDisplayName(path: String) = open(path).displayName

    /**
     * 获取最后修改时间
     *
     * @param path 路径
     */
    fun getLastModified(path: String) = open(path).lastModified

    /**
     * 获取文件大小
     *
     * @param path 路径
     */
    fun getSize(path: String) = open(path).size

    /**
     * 获取文件Uri
     *
     * @param path 路径
     */
    fun getUri(path: String) = open(path).uri

    /**
     * 写入内容到文件
     *
     * @param path 文件路径
     * @param bytes 字节数组
     * @param off 数组偏移
     * @param len 写入长度
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     */
    fun write(
        path: String, bytes: ByteArray, off: Int = 0, len: Int = bytes.size, mode: String = "t"
    ) {
        val file = open(path)
        file.createNewFile()
        val fos = file.openOutputStream(mode)
        fos.write(bytes, off, len)
        fos.close()
    }

    /**
     * 写入内容到文件
     *
     * @param path 文件路径
     * @param text 字符串
     * @param mode 写入模式
     * - `w`: 覆盖
     * - `a`: 追加
     * - `t`: 截断
     */
    fun write(
        path: String, text: String, charset: Charset = Charsets.UTF_8, mode: String = "t"
    ) = write(path, text.toByteArray(charset), mode = mode)

    /**
     * 读取内容到数组
     *
     * @param path 文件路径
     * @param bytes 字节数组
     * @param off 数组偏移
     * @param len 读取长度
     */
    fun read(path: String, bytes: ByteArray, off: Int = 0, len: Int = bytes.size) {
        val fis = openInputStream(path)
        fis.read(bytes, off, len)
        fis.close()
    }

    /**
     * 读取内容到数组
     *
     * @param path 文件路径
     * @param charset 内容编码
     * @return 文件内容
     */
    fun read(path: String, charset: Charset = Charsets.UTF_8): String {
        val fis = openInputStream(path)
        val fr = fis.reader(charset)
        val text = fr.readText()
        fr.close()
        fis.close()
        return text
    }

    /**
     * 清空文件内容
     *
     * @param path 文件路径
     */
    fun clear(path: String) = open(path).clear()

    /**
     * 拷贝文件或文件夹
     *
     * @param source 源路径
     * @param target 目标路径
     */
    fun copy(source: String, target: String) {
        open(source).copyTo(open(target))
    }

    /**
     * 移动文件或文件夹
     *
     * @param source 源路径
     * @param target 目标路径
     */
    fun move(source: String, target: String) {
        open(source).moveTo(open(target))
    }

    /**
     * 重命名文件或文件夹
     *
     * @param path 路径
     * @param name 新名称
     */
    fun rename(path: String, name: String) {
        open(path).rename(name)
    }
}

/**
 * `onActivityResult` 回调函数
 */
private typealias OnActivityResultCallback = (Int, Int, Intent?) -> Unit

