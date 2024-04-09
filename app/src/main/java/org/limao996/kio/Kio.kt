package org.limao996.kio

import android.content.Context
import android.content.Intent
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
         * @param path 文件路径
         */
        @JvmStatic
        fun isDocumentFile(path: String) = KFile.isDocumentFile(path)
    }

    /**
     * 检查权限
     *
     * @param path 文件路径
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
     * @param path 文件路径
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
     * @param path 文件路径
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
     * @param path 文件路径
     * @return [KFile] 对象
     */
    fun open(path: String): KFile {
        if (isDocumentFile(path)) {
            return KDocumentFile(context, KFile.toDocumentPath(path))
        }
        return KStorageFile(context, path)
    }
}

/**
 * `onActivityResult` 回调函数
 */
private typealias OnActivityResultCallback = (Int, Int, Intent?) -> Unit

