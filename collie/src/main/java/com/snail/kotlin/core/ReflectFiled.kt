package com.snail.kotlin.core

import kotlin.jvm.Synchronized
import kotlin.Throws
import java.lang.ClassCastException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.reflect.Field

class ReflectFiled<Type>(clazz: Class<*>?, fieldName: String?) {
    private val mClazz: Class<*>
    private val mFieldName: String
    private var mInit = false
    private var mField: Field? = null
    @Synchronized
    private fun prepare() {
        if (mInit) {
            return
        }
        var clazz: Class<*>? = mClazz
        while (clazz != null) {
            try {
                val f = clazz.getDeclaredField(mFieldName)
                f.isAccessible = true
                mField = f
                break
            } catch (e: Exception) {
            }
            clazz = clazz.superclass
        }
        mInit = true
    }

    @Synchronized
    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class
    )
    fun get(): Type? {
        return get(false)
    }

    @Synchronized
    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class
    )
    operator fun get(ignoreFieldNoExist: Boolean): Type? {
        prepare()
        if (mField == null) {
            if (!ignoreFieldNoExist) {
                throw NoSuchFieldException()
            }
            return null
        }
        var fieldVal: Type? = null
        fieldVal = try {
            mField!![null] as Type
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("unable to cast object")
        }
        return fieldVal
    }

    @Synchronized
    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class
    )
    operator fun get(ignoreFieldNoExist: Boolean, instance: Any?): Type? {
        prepare()
        if (mField == null) {
            if (!ignoreFieldNoExist) {
                throw NoSuchFieldException()
            }
            return null
        }
        var fieldVal: Type? = null
        fieldVal = try {
            mField!![instance] as Type
        } catch (e: ClassCastException) {
            throw IllegalArgumentException("unable to cast object")
        }
        return fieldVal
    }

    @Synchronized
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    operator fun get(instance: Any?): Type? {
        return get(false, instance)
    }

    @Synchronized
    fun getWithoutThrow(instance: Any?): Type? {
        var fieldVal: Type? = null
        try {
            fieldVal = get(true, instance)
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        } catch (e: IllegalArgumentException) {
        }
        return fieldVal
    }

    @get:Synchronized
    val withoutThrow: Type?
        get() {
            var fieldVal: Type? = null
            try {
                fieldVal = get(true)
            } catch (e: NoSuchFieldException) {
            } catch (e: IllegalAccessException) {
            } catch (e: IllegalArgumentException) {
            }
            return fieldVal
        }

    @Synchronized
    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class
    )
    operator fun set(instance: Any?, `val`: Type): Boolean {
        return set(instance, `val`, false)
    }

    @Synchronized
    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        IllegalArgumentException::class
    )
    operator fun set(instance: Any?, `val`: Type, ignoreFieldNoExist: Boolean): Boolean {
        prepare()
        if (mField == null) {
            if (!ignoreFieldNoExist) {
                throw NoSuchFieldException("Method $mFieldName is not exists.")
            }
            return false
        }
        mField!![instance] = `val`
        return true
    }

    @Synchronized
    fun setWithoutThrow(instance: Any?, `val`: Type): Boolean {
        var result = false
        try {
            result = set(instance, `val`, true)
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        } catch (e: IllegalArgumentException) {
        }
        return result
    }

    @Synchronized
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun set(`val`: Type): Boolean {
        return set(null, `val`, false)
    }

    @Synchronized
    fun setWithoutThrow(`val`: Type): Boolean {
        var result = false
        try {
            result = set(null, `val`, true)
        } catch (e: NoSuchFieldException) {
        } catch (e: IllegalAccessException) {
        } catch (e: IllegalArgumentException) {
        }
        return result
    }

    companion object {
        private const val TAG = "ReflectFiled"
    }

    init {
        require(!(clazz == null || fieldName == null || fieldName.length == 0)) { "Both of invoker and fieldName can not be null or nil." }
        mClazz = clazz
        mFieldName = fieldName
    }
}