package com.snail.kotlin.core

object ReflectUtils {
    @Throws(Exception::class)
    operator fun <T> get(clazz: Class<*>?, fieldName: String?): T? {
        return ReflectFiled<T>(clazz, fieldName).get()
    }

    @Throws(Exception::class)
    operator fun <T> get(clazz: Class<*>?, fieldName: String?, instance: Any?): T? {
        return ReflectFiled<T>(clazz, fieldName)[instance]
    }

    @Throws(Exception::class)
    operator fun set(clazz: Class<*>?, fieldName: String?, `object`: Any?): Boolean {
        return ReflectFiled<Any?>(clazz, fieldName).set(`object`)
    }

    @Throws(Exception::class)
    operator fun set(clazz: Class<*>?, fieldName: String?, instance: Any?, value: Any?): Boolean {
        return ReflectFiled<Any?>(clazz, fieldName).set(instance, value)
    }

    @Throws(Exception::class)
    operator fun <T> invoke(
        clazz: Class<*>?,
        methodName: String?,
        instance: Any?,
        vararg args: Any?
    ): T? {
        return ReflectMethod(clazz, methodName).invoke(instance, *args)
    }
}