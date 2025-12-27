package com.example.lottery

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlin.random.Random
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
            "一等奖：智能手机",
            "二等奖：平板电脑",
            "三等奖：无线耳机",
            "四等奖：充电宝",
            "五等奖：精美礼品",
            "参与奖：谢谢参与"
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
}