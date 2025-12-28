package com.example.lottery

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlin.random.Random
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

class LotteryViewModel(private val context: Context) : ViewModel() {
    private val _prizePool = mutableListOf<String>()
    val prizePool: List<String> get() = _prizePool
    private val prizeFile = File(context.filesDir, "prizes.txt")

    init {
        // 从本地文件加载奖品列表
        loadPrizesFromFile()
        
        // 如果文件为空，添加默认奖品
        if (_prizePool.isEmpty()) {
            addDefaultPrizes()
        }
    }

    private fun addDefaultPrizes() {
        _prizePool.addAll(listOf(
            "None"
        ))
        savePrizesToFile()
    }

    // 从本地文件加载奖品列表
    private fun loadPrizesFromFile() {
        try {
            if (prizeFile.exists()) {
                _prizePool.clear()
                prizeFile.forEachLine { line ->
                    if (line.isNotBlank()) {
                        _prizePool.add(line)
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 保存奖品列表到本地文件
    private fun savePrizesToFile() {
        try {
            prizeFile.writeText(_prizePool.joinToString("\n"))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 抽奖方法
    fun drawLottery(): String? {
        if (_prizePool.isEmpty()) {
            return null
        }
        val randomIndex = Random.nextInt(0, _prizePool.size)
        return _prizePool[randomIndex]
    }

    // 添加奖品到奖池
    fun addPrize(prize: String) {
        _prizePool.add(prize)
        savePrizesToFile()
    }

    // 从奖池移除奖品
    fun removePrize(index: Int) {
        if (index in 0 until _prizePool.size) {
            _prizePool.removeAt(index)
            savePrizesToFile()
        }
    }

    // 清空奖池
    fun clearPrizePool() {
        _prizePool.clear()
        savePrizesToFile()
    }

    // 从JSON字符串导入奖品列表
    fun importPrizesFromJson(jsonString: String): Boolean {
        return try {
            // 解析JSON数组
            val prizes = Json.decodeFromString<List<String>>(jsonString)
            
            // 清空现有奖池，添加新奖品，保持原顺序
            _prizePool.clear()
            _prizePool.addAll(prizes)
            
            // 保存到本地文件
            savePrizesToFile()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}