package com.team2898.engine.async.util

import com.team2898.engine.async.pools.ComputePool
import com.team2898.engine.async.pools.IOPool
import com.team2898.engine.extensions.Unit
import com.team2898.engine.logging.crashTrack
import kotlinx.coroutines.*
import java.util.concurrent.ForkJoinPool
import kotlin.system.measureTimeMillis

fun go(
        pool: ExecutorCoroutineDispatcher = ComputePool,
        lazy: Boolean = false,
        throwOnException: Boolean = true,
        block: suspend () -> Unit
): Job =
        GlobalScope.launch(pool, if (lazy) CoroutineStart.LAZY else CoroutineStart.DEFAULT) {
            try {
                block()
            } catch (e: Exception) {
                crashTrack(e)
                if (throwOnException) throw e
            }
        }

fun goIO(block: suspend () -> Unit): Job = go(IOPool) { block() }
fun goLazy(block: suspend () -> Unit): Job = go(lazy = true) { block() }

class WaitGroup {
    val joinTime: Long by lazy { measureTimeMillis { runBlocking<Unit> { jobs.forEach { it.join() } } } }
    val jobs = mutableListOf<Job>()
    fun add(job: Job): WaitGroup = this.apply { jobs.add(job) }
    fun add(block: suspend () -> Unit): WaitGroup = this.apply { jobs.add(go { block() }) }
    fun join() = this.apply { joinTime }
}

