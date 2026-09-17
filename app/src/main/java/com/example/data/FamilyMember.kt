package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Gender {
    MALE,
    FEMALE
}

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val fatherId: Long? = null,
    val fatherName: String? = null,
    val motherName: String? = null,
    val gender: Gender = Gender.MALE,
    val generation: Int = 1,
    val spouse: String? = null,
    val location: String? = null,
    val isDeceased: Boolean = false,
    val deathNote: String? = null,
    val notes: String? = null,
    val displayOrder: Int = 0,
    val phone: String? = null,
    val occupation: String? = null,
    val birthYear: String? = null,
    val deathYear: String? = null
) {
    val isMale: Boolean get() = gender == Gender.MALE
    val isFemale: Boolean get() = gender == Gender.FEMALE
    val genderLabelUrdu: String get() = if (isMale) "بیٹا / مرد" else "بیٹی / خاتون"
    val statusLabelUrdu: String get() = if (isDeceased) "مرحوم" else "حیات"
    val generationLabelUrdu: String get() = when (generation) {
        1 -> "پہلی نسل (بانی)"
        2 -> "دوسری نسل (شاخ)"
        3 -> "تیسری نسل"
        4 -> "چوتھی نسل"
        5 -> "پانچویں نسل"
        6 -> "چھٹی نسل"
        7 -> "ساتویں نسل"
        else -> "نسل $generation"
    }
}
