package com.example.voiceledger.util

object VoiceParser {
    // 分类关键词映射
    private val categoryKeywords = mapOf(
        "餐饮" to listOf("吃", "餐", "饭", "食", "外卖", "奶茶", "咖啡", "火锅", "烧烤", "面", "粉", "早餐", "午餐", "晚餐", "宵夜", "零食", "水果", "饮料", "蛋糕", "汉堡", "披萨", "寿司"),
        "交通" to listOf("车", "打车", "出租", "地铁", "公交", "高铁", "火车", "飞机", "机票", "油费", "加油", "停车", "过路费", "滴滴", "骑行", "单车"),
        "购物" to listOf("买", "购", "淘宝", "京东", "拼多多", "衣服", "鞋", "包", "化妆品", "护肤", "日用品", "超市", "商场"),
        "娱乐" to listOf("电影", "游戏", "KTV", "唱歌", "旅游", "门票", "演出", "健身", "运动", "书籍", "书"),
        "住房" to listOf("房租", "水电", "物业", "维修", "家具", "装修"),
        "医疗" to listOf("医院", "药", "看病", "体检", "挂号"),
        "教育" to listOf("学费", "课程", "培训", "考试")
    )

    // 收入关键词
    private val incomeKeywords = listOf("收入", "工资", "奖金", "红包", "转账", "理财", "利息", "退款", "报销")

    data class ParseResult(
        val amount: Double?,
        val category: String,
        val isExpense: Boolean,
        val note: String,
        val rawText: String
    )

    fun parse(text: String): ParseResult {
        val amount = extractAmount(text)
        val isExpense = !isIncome(text)
        val category = if (isExpense) extractCategory(text) else "收入"
        val note = text.trim()

        return ParseResult(
            amount = amount,
            category = category,
            isExpense = isExpense,
            note = note,
            rawText = text
        )
    }

    private fun extractAmount(text: String): Double? {
        // 匹配各种金额格式：35、35.5、35块、35元、三十五
        val patterns = listOf(
            Regex("(\\d+\\.?\\d*)\\s*[块元圆]"),
            Regex("[花了费了用了付了收了赚了]?\\s*(\\d+\\.\\d+)"),
            Regex("[花了费了用了付了收了赚了]?\\s*(\\d+)"),
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }
        return null
    }

    private fun isIncome(text: String): Boolean {
        return incomeKeywords.any { text.contains(it) }
    }

    private fun extractCategory(text: String): String {
        for ((category, keywords) in categoryKeywords) {
            if (keywords.any { text.contains(it) }) {
                return category
            }
        }
        return "其他"
    }
}
