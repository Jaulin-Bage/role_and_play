package com.example.lottery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lottery.databinding.ActivityMainBinding
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: LotteryViewModel
    private var isPrizePoolVisible = false
    private val READ_JSON_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LotteryViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return LotteryViewModel(applicationContext) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        })[LotteryViewModel::class.java]

        setupViews()
        updatePrizePoolDisplay()
    }

    // ViewModel工厂类，用于创建带Context参数的LotteryViewModel
    class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LotteryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LotteryViewModel(context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    private fun setupViews() {
        // 设置抽奖按钮点击事件
        binding.lotteryButton.setOnClickListener {
            performLottery()
        }

        // 设置添加奖品按钮点击事件
        binding.addPrizeButton.setOnClickListener {
            showAddPrizeOptionsDialog()
        }

        // 设置清空奖池按钮点击事件
        binding.clearPrizeButton.setOnClickListener {
            showClearPrizeDialog()
        }

        // 设置展开/收起奖品池按钮点击事件
        binding.togglePrizePoolButton.setOnClickListener {
            togglePrizePoolVisibility()
        }
    }

    private fun openFilePicker() {
        // 创建文件选择意图
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/json"
        
        // 启动文件选择器
        startActivityForResult(intent, READ_JSON_REQUEST_CODE)
    }

    private fun togglePrizePoolVisibility() {
        isPrizePoolVisible = !isPrizePoolVisible
        if (isPrizePoolVisible) {
            // 显示奖品池和操作按钮
            binding.prizePoolScroll.visibility = View.VISIBLE
            binding.addPrizeButton.visibility = View.VISIBLE
            binding.clearPrizeButton.visibility = View.VISIBLE
            binding.buttonLayout.visibility = View.VISIBLE
            binding.togglePrizePoolButton.text = "收起"
        } else {
            // 隐藏奖品池和操作按钮
            binding.prizePoolScroll.visibility = View.GONE
            binding.addPrizeButton.visibility = View.GONE
            binding.clearPrizeButton.visibility = View.GONE
            binding.buttonLayout.visibility = View.GONE
            binding.togglePrizePoolButton.text = "管理奖品"
        }
    }

    /**
     * 显示添加奖品选项对话框
     */
    private fun showAddPrizeOptionsDialog() {
        AlertDialog.Builder(this)
            .setTitle("添加奖品")
            .setItems(arrayOf("手动添加单个奖品", "从JSON文件导入奖品")) { dialog, which ->
                when (which) {
                    0 -> showAddPrizeDialog() // 手动添加单个奖品
                    1 -> openFilePicker() // 从JSON文件导入奖品
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /**
     * 显示清空奖池确认对话框
     */
    private fun showClearPrizeDialog() {
        AlertDialog.Builder(this)
            .setTitle("清空奖池")
            .setMessage("确定要清空所有奖品吗？此操作不可恢复。")
            .setPositiveButton("确定") { dialog, _ ->
                viewModel.clearPrizePool()
                updatePrizePoolDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun performLottery() {
        // 执行抽奖动画
        startLotteryAnimation()

        // 延迟显示结果，让动画有时间执行
        binding.lotteryButton.postDelayed({
            val result = viewModel.drawLottery()
            if (result != null) {
                showResultDialog(result)
            } else {
                showErrorDialog(getString(R.string.empty_prize_pool))
            }
        }, 1000) // 1秒后显示结果
    }

    private fun startLotteryAnimation() {
        // 旋转动画
        val rotateAnimation = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 1000
        rotateAnimation.repeatCount = 0

        // 透明度动画
        val alphaAnimation = AlphaAnimation(1f, 0.5f)
        alphaAnimation.duration = 500
        alphaAnimation.repeatCount = 1
        alphaAnimation.repeatMode = Animation.REVERSE

        // 应用动画
        binding.cardView.startAnimation(rotateAnimation)
        binding.resultText.startAnimation(alphaAnimation)
        binding.lotteryButton.isEnabled = false

        // 动画结束后恢复按钮状态
        rotateAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                binding.lotteryButton.isEnabled = true
            }
            override fun onAnimationRepeat(animation: Animation?) {}
        })
    }

    private fun showResultDialog(result: String) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.result_title))
            .setMessage("${getString(R.string.result_subtitle)}\n\n$result")
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun showAddPrizeDialog() {
        val input = TextInputEditText(this)
        input.hint = "请输入奖品名称"

        AlertDialog.Builder(this)
            .setTitle("添加奖品")
            .setView(input)
            .setPositiveButton("添加") { dialog, _ ->
                val prize = input.text.toString().trim()
                if (prize.isNotEmpty()) {
                    viewModel.addPrize(prize)
                    updatePrizePoolDisplay()
                }
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updatePrizePoolDisplay() {
        binding.prizePoolLayout.removeAllViews()

        viewModel.prizePool.forEachIndexed { index, prize ->
            val prizeView = TextView(this).apply {
                text = "${index + 1}. $prize"
                textSize = 16f
                setTextColor(resources.getColor(R.color.black, theme))
                setPadding(10, 10, 10, 10)
                setOnLongClickListener {
                    showDeletePrizeDialog(index)
                    true
                }
            }
            binding.prizePoolLayout.addView(prizeView)
        }
    }

    private fun showDeletePrizeDialog(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("删除奖品")
            .setMessage("确定要删除这个奖品吗？")
            .setPositiveButton("删除") { dialog, _ ->
                viewModel.removePrize(index)
                updatePrizePoolDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == READ_JSON_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // 读取JSON文件内容
                val jsonString = readJsonFile(uri)
                if (jsonString != null) {
                    // 导入奖品
                    val success = viewModel.importPrizesFromJson(jsonString)
                    if (success) {
                        // 导入成功，更新UI
                        updatePrizePoolDisplay()
                        showSuccessDialog("奖品导入成功！")
                    } else {
                        // 导入失败
                        showErrorDialog("JSON文件格式错误，导入失败！")
                    }
                } else {
                    // 文件读取失败
                    showErrorDialog("无法读取JSON文件！")
                }
            }
        }
    }

    private fun readJsonFile(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSuccessDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("成功")
            .setMessage(message)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }
}