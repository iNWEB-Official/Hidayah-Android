package com.hidayah.android.ui.components

import com.hidayah.android.R

/**
 * অ্যাপে হাতে থাকা ডিফল্ট প্রোফাইল ছবিগুলো। সেটিংসে `profileImagePath = "default:N"` হিসেবে সংরক্ষিত হয়,
 * তাই ডিফল্টের ক্ষেত্রে কোনো ফাইল কপি হয় না।
 */
object DefaultAvatars {
    data class Avatar(val id: String, val resId: Int, val label: String)

    val ALL = listOf(
        Avatar("default:1", R.drawable.avatar_default_1, "তরুণ পুরুষ"),
        Avatar("default:2", R.drawable.avatar_default_2, "তরুণী"),
        Avatar("default:3", R.drawable.avatar_default_3, "বয়োজ্যেষ্ঠ পুরুষ"),
        Avatar("default:4", R.drawable.avatar_default_4, "বয়োজ্যেষ্ঠা নারী"),
        Avatar("default:5", R.drawable.avatar_default_5, "শিশু"),
        Avatar("default:6", R.drawable.avatar_default_6, "মসজিদ-চাঁদের নিদর্শন")
    )

    fun resIdFor(path: String): Int? = ALL.firstOrNull { it.id == path }?.resId
}
