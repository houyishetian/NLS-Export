package com.lin.nls_export.entities

interface ICellInterceptor {
    fun handle(original: String): String
}

// key 拦截处理
interface IKeyInterceptor : ICellInterceptor

// En 拦截处理
interface IEnInterceptor : ICellInterceptor

// Sc 拦截处理
interface IScInterceptor : ICellInterceptor

// Tc 拦截处理
interface ITcInterceptor : ICellInterceptor