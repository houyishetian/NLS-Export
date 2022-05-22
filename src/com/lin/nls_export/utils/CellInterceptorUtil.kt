package com.lin.nls_export.utils

import com.lin.nls_export.entities.*

object CellInterceptorUtil {

    fun createKeyInterceptor(): IKeyInterceptor {
        return object : IKeyInterceptor {
            override fun handle(original: String): String {
                return original
            }
        }
    }

    private fun createCommonNlsInterceptor(): ICellInterceptor {
        return object : ICellInterceptor {
            override fun handle(original: String): String {
                return original.replace("\n", "\\n") // 处理换行
                        .replace("\\\"", "\"").replace("\"", "\\\"") // 处理双引号
                        .replace("\\'", "'").replace("'", "\\'") // 处理单引号
            }
        }
    }

    fun createEnInterceptor(): IEnInterceptor {
        return object : IEnInterceptor {
            override fun handle(original: String): String {
                return createCommonNlsInterceptor().handle(original)
            }
        }
    }

    fun createScInterceptor(): IScInterceptor {
        return object : IScInterceptor {
            override fun handle(original: String): String {
                return createCommonNlsInterceptor().handle(original)
            }
        }
    }

    fun createTcInterceptor(): ITcInterceptor {
        return object : ITcInterceptor {
            override fun handle(original: String): String {
                return createCommonNlsInterceptor().handle(original)
            }
        }
    }
}