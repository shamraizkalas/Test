package com.example.data

object InitialData {
    fun getInitialFamilyMembers(): List<FamilyMember> {
        return listOf(
            // Root - Generation 1
            FamilyMember(
                id = 1L,
                name = "محمد علی",
                fatherId = null,
                fatherName = null,
                gender = Gender.MALE,
                generation = 1,
                notes = "بانیٔ شجرہ نسب (محمد علی)",
                displayOrder = 1
            ),

            // Generation 2 - Children of محمد علی
            FamilyMember(
                id = 10L,
                name = "کرم الہی",
                fatherId = 1L,
                fatherName = "محمد علی",
                gender = Gender.MALE,
                generation = 2,
                notes = "ولد محمد علی (پہلی شاخ)",
                displayOrder = 1
            ),
            FamilyMember(
                id = 20L,
                name = "کرم دین",
                fatherId = 1L,
                fatherName = "محمد علی",
                gender = Gender.MALE,
                generation = 2,
                notes = "ولد محمد علی (دوسری شاخ)",
                displayOrder = 2
            ),
            FamilyMember(
                id = 30L,
                name = "حاکم علی",
                fatherId = 1L,
                fatherName = "محمد علی",
                gender = Gender.MALE,
                generation = 2,
                notes = "ولد محمد علی (تیسری شاخ)",
                displayOrder = 3
            ),
            FamilyMember(
                id = 40L,
                name = "محمد ھاشم",
                fatherId = 1L,
                fatherName = "محمد علی",
                gender = Gender.MALE,
                generation = 2,
                notes = "ولد محمد علی (چوتھی شاخ)",
                displayOrder = 4
            ),

            // ==========================================
            // BRANCH 1: اولاد کرم الہی (Father ID = 10)
            // ==========================================
            // بیٹے (Sons)
            FamilyMember(
                id = 101L,
                name = "محمد فاضل",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.MALE,
                generation = 3,
                displayOrder = 1
            ),
            FamilyMember(
                id = 102L,
                name = "محمد صادق",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.MALE,
                generation = 3,
                displayOrder = 2
            ),
            FamilyMember(
                id = 103L,
                name = "غلام قاسم",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.MALE,
                generation = 3,
                displayOrder = 3
            ),
            FamilyMember(
                id = 104L,
                name = "غلام علی",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.MALE,
                generation = 3,
                displayOrder = 4
            ),
            FamilyMember(
                id = 105L,
                name = "نزر حسین",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.MALE,
                generation = 3,
                displayOrder = 5
            ),
            // بیٹیاں (Daughters)
            FamilyMember(
                id = 106L,
                name = "عائشہ بی بی",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.FEMALE,
                generation = 3,
                displayOrder = 6
            ),
            FamilyMember(
                id = 107L,
                name = "فاطمہ بی بی",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.FEMALE,
                generation = 3,
                spouse = "زوجہ محمد حسین سرگودھا",
                location = "سرگودھا",
                displayOrder = 7
            ),
            FamilyMember(
                id = 108L,
                name = "قاسم بی",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.FEMALE,
                generation = 3,
                spouse = "زوجہ رحمت خاں",
                displayOrder = 8
            ),
            FamilyMember(
                id = 109L,
                name = "مختوم بی بی",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.FEMALE,
                generation = 3,
                spouse = "زوجہ سردار خاں سدوال براھمنہ",
                location = "سدوال براھمنہ",
                displayOrder = 9
            ),
            FamilyMember(
                id = 110L,
                name = "مختوم",
                fatherId = 10L,
                fatherName = "کرم الہی",
                gender = Gender.FEMALE,
                generation = 3,
                isDeceased = true,
                deathNote = "عمر 2.5 سال وفات",
                displayOrder = 10
            ),

            // اولاد محمد فاضل (Father ID = 101)
            FamilyMember(
                id = 111L,
                name = "گلزار حسین",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.MALE,
                generation = 4,
                displayOrder = 1
            ),
            FamilyMember(
                id = 112L,
                name = "مختار احمد",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.MALE,
                generation = 4,
                displayOrder = 2
            ),
            FamilyMember(
                id = 113L,
                name = "ریاض احمد",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.MALE,
                generation = 4,
                displayOrder = 3
            ),
            FamilyMember(
                id = 114L,
                name = "غلام عباس",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.MALE,
                generation = 4,
                displayOrder = 4
            ),
            FamilyMember(
                id = 115L,
                name = "کبعراں بی بی",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.FEMALE,
                generation = 4,
                spouse = "زوجہ بشیر لودھراں",
                location = "لودھراں",
                displayOrder = 5
            ),
            FamilyMember(
                id = 116L,
                name = "صغراں بی بی",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.FEMALE,
                generation = 4,
                spouse = "زوجہ فضل حسین رولیہ",
                location = "رولیہ",
                displayOrder = 6
            ),
            FamilyMember(
                id = 117L,
                name = "منوره بی بی",
                fatherId = 101L,
                fatherName = "محمد فاضل",
                gender = Gender.FEMALE,
                generation = 4,
                spouse = "زوجہ ریاض سینتھل",
                location = "سینتھل",
                displayOrder = 7
            ),

            // اولاد گلزار حسین (Father ID = 111)
            FamilyMember(id = 121L, name = "تصور حسین", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 122L, name = "منور حسین", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 123L, name = "محمد عمران", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 124L, name = "طاہرہ یاسمین", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 125L, name = "فرزانہ کوثر", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 126L, name = "ریحانہ", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 6),
            FamilyMember(id = 127L, name = "مہوش", fatherId = 111L, fatherName = "گلزار حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 7),

            // اولاد مختار احمد (Father ID = 112)
            FamilyMember(id = 131L, name = "بلال احمد", fatherId = 112L, fatherName = "مختار احمد", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 132L, name = "افضال احمد", fatherId = 112L, fatherName = "مختار احمد", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 133L, name = "عرفان احمد", fatherId = 112L, fatherName = "مختار احمد", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 134L, name = "محمد عثمان", fatherId = 112L, fatherName = "مختار احمد", gender = Gender.MALE, generation = 5, displayOrder = 4),

            // اولاد ریاض احمد (Father ID = 113)
            FamilyMember(id = 141L, name = "محمد وقاص", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 142L, name = "محمد لقمان", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 143L, name = "آسیہ ریاض", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 144L, name = "سادیہ ریاض", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 145L, name = "شمسہ کنول", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 146L, name = "رمشہ کنول", fatherId = 113L, fatherName = "ریاض احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 6),

            // اولاد غلام عباس (Father ID = 114)
            FamilyMember(id = 151L, name = "شاہد عباس", fatherId = 114L, fatherName = "غلام عباس", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 152L, name = "زاہد عباس", fatherId = 114L, fatherName = "غلام عباس", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 153L, name = "طیب عباس", fatherId = 114L, fatherName = "غلام عباس", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 154L, name = "مقدس عباس", fatherId = 114L, fatherName = "غلام عباس", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد محمد صادق (Father ID = 102)
            FamilyMember(id = 161L, name = "نزیر احمد", fatherId = 102L, fatherName = "محمد صادق", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 162L, name = "اقبال بی بی", fatherId = 102L, fatherName = "محمد صادق", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ شریف گوٹرآلا", location = "گوٹرآلا", displayOrder = 2),
            FamilyMember(id = 163L, name = "نسرین بی بی", fatherId = 102L, fatherName = "محمد صادق", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ ایوب سرگودھا", location = "سرگودھا", displayOrder = 3),
            FamilyMember(id = 164L, name = "سکینہ بی بی", fatherId = 102L, fatherName = "محمد صادق", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ لطیف سدوال کلاں", location = "سدوال کلاں", displayOrder = 4),

            // اولاد نزیر احمد (Father ID = 161)
            FamilyMember(id = 171L, name = "صغیر احمد", fatherId = 161L, fatherName = "نزیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 172L, name = "سفیر احمد", fatherId = 161L, fatherName = "نزیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 173L, name = "محمد سیف", fatherId = 161L, fatherName = "نزیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 174L, name = "تنویر احمد", fatherId = 161L, fatherName = "نزیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 175L, name = "محمد عاصم", fatherId = 161L, fatherName = "نزیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 5),

            // اولاد غلام قاسم (Father ID = 103)
            FamilyMember(id = 181L, name = "مظہر اقبال", fatherId = 103L, fatherName = "غلام قاسم", gender = Gender.MALE, generation = 4, isDeceased = true, deathNote = "کم عمر وفات", displayOrder = 1),
            FamilyMember(id = 182L, name = "ثمینہ کوثر", fatherId = 103L, fatherName = "غلام قاسم", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ عبدالغفور", displayOrder = 2),
            FamilyMember(id = 183L, name = "راحیلہ کوثر", fatherId = 103L, fatherName = "غلام قاسم", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد شہباز", displayOrder = 3),

            // اولاد غلام علی (Father ID = 104)
            FamilyMember(id = 191L, name = "محمد شبیر", fatherId = 104L, fatherName = "غلام علی", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 192L, name = "بشیر احمد", fatherId = 104L, fatherName = "غلام علی", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 193L, name = "رشید احمد", fatherId = 104L, fatherName = "غلام علی", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 194L, name = "قربان علی", fatherId = 104L, fatherName = "غلام علی", gender = Gender.MALE, generation = 4, isDeceased = true, deathNote = "کم عمر وفات", displayOrder = 4),
            FamilyMember(id = 195L, name = "شہناز بی بی", fatherId = 104L, fatherName = "غلام علی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد حنیف", displayOrder = 5),
            FamilyMember(id = 196L, name = "شمشاد اختر", fatherId = 104L, fatherName = "غلام علی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد ریاض", displayOrder = 6),
            FamilyMember(id = 197L, name = "فوزیہ کوثر", fatherId = 104L, fatherName = "غلام علی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ شہزاد احمد بھاٹی", displayOrder = 7),
            FamilyMember(id = 198L, name = "صوفیہ کوثر", fatherId = 104L, fatherName = "غلام علی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ نور حسین", displayOrder = 8),
            FamilyMember(id = 199L, name = "بلقیس بی بی", fatherId = 104L, fatherName = "غلام علی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ اشفاق احمد بھاٹی", displayOrder = 9),

            // اولاد محمد شبیر (Father ID = 191)
            FamilyMember(id = 201L, name = "احسن شبیر", fatherId = 191L, fatherName = "محمد شبیر", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 202L, name = "محمد عبداللہ شبیر", fatherId = 191L, fatherName = "محمد شبیر", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 203L, name = "حرا شبیر", fatherId = 191L, fatherName = "محمد شبیر", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 204L, name = "اذا مریم", fatherId = 191L, fatherName = "محمد شبیر", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد بشیر احمد (Father ID = 192)
            FamilyMember(id = 211L, name = "حافظ محمد عمیر", fatherId = 192L, fatherName = "بشیر احمد", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 212L, name = "ثنا بشیر", fatherId = 192L, fatherName = "بشیر احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 213L, name = "سونیا بشیر", fatherId = 192L, fatherName = "بشیر احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 3),

            // اولاد رشید احمد (Father ID = 193)
            FamilyMember(id = 221L, name = "محمد کلیم اللہ", fatherId = 193L, fatherName = "رشید احمد", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 222L, name = "کائنات رشید", fatherId = 193L, fatherName = "رشید احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 223L, name = "عائشہ رشید", fatherId = 193L, fatherName = "رشید احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 224L, name = "کشف رشید", fatherId = 193L, fatherName = "رشید احمد", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد نزر حسین (Father ID = 105)
            FamilyMember(id = 231L, name = "شہباز حسین", fatherId = 105L, fatherName = "نزر حسین", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 232L, name = "ناظم حسین", fatherId = 105L, fatherName = "نزر حسین", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 233L, name = "واجد حسین", fatherId = 105L, fatherName = "نزر حسین", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 234L, name = "تلط", fatherId = 105L, fatherName = "نزر حسین", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ ظہور احمد", displayOrder = 4),
            FamilyMember(id = 235L, name = "کشور", fatherId = 105L, fatherName = "نزر حسین", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ اورنگزیب نتھو دا کوٹ", location = "نتھو دا کوٹ", displayOrder = 5),

            // اولاد شہباز حسین (Father ID = 231)
            FamilyMember(id = 241L, name = "حسنین شہباز", fatherId = 231L, fatherName = "شہباز حسین", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 242L, name = "سلمان شہباز", fatherId = 231L, fatherName = "شہباز حسین", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 243L, name = "عمر شہباز", fatherId = 231L, fatherName = "شہباز حسین", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 244L, name = "شاہمیر شہباز", fatherId = 231L, fatherName = "شہباز حسین", gender = Gender.MALE, generation = 5, displayOrder = 4),

            // ==========================================
            // BRANCH 2: اولاد کرم دین (Father ID = 20)
            // ==========================================
            FamilyMember(id = 301L, name = "دین محمد", fatherId = 20L, fatherName = "کرم دین", gender = Gender.MALE, generation = 3, displayOrder = 1),
            FamilyMember(id = 302L, name = "محمد بی بی", fatherId = 20L, fatherName = "کرم دین", gender = Gender.FEMALE, generation = 3, displayOrder = 2),
            FamilyMember(id = 303L, name = "حسین بی بی", fatherId = 20L, fatherName = "کرم دین", gender = Gender.FEMALE, generation = 3, displayOrder = 3),

            // اولاد دین محمد (Father ID = 301)
            FamilyMember(id = 311L, name = "محمد اسلم", fatherId = 301L, fatherName = "دین محمد", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 312L, name = "محمد اکرم", fatherId = 301L, fatherName = "دین محمد", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 313L, name = "محمد عارف", fatherId = 301L, fatherName = "دین محمد", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 314L, name = "ثریا بی بی", fatherId = 301L, fatherName = "دین محمد", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ رفیق نت", displayOrder = 4),
            FamilyMember(id = 315L, name = "پروین", fatherId = 301L, fatherName = "دین محمد", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد حنیف بنگیال", displayOrder = 5),

            // اولاد محمد اسلم (Father ID = 311)
            FamilyMember(id = 321L, name = "محمد بلال", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 322L, name = "محمد اویس", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 323L, name = "شازیہ", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 324L, name = "سمینہ", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.FEMALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 325L, name = "شکیلا", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.FEMALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 326L, name = "سمیرہ", fatherId = 311L, fatherName = "محمد اسلم", gender = Gender.FEMALE, generation = 5, displayOrder = 6),

            // اولاد محمد اکرم (Father ID = 312)
            FamilyMember(id = 331L, name = "محمد وسیم", fatherId = 312L, fatherName = "محمد اکرم", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 332L, name = "نازیہ بی بی", fatherId = 312L, fatherName = "محمد اکرم", gender = Gender.FEMALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 333L, name = "نسبہ", fatherId = 312L, fatherName = "محمد اکرم", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 334L, name = "صدرہ", fatherId = 312L, fatherName = "محمد اکرم", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد محمد عارف (Father ID = 313)
            FamilyMember(id = 341L, name = "نعیم عارف", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 342L, name = "وحید عارف", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 343L, name = "طیبہ", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 344L, name = "تسمیہ", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.FEMALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 345L, name = "آصفہ", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.FEMALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 346L, name = "منیبہ", fatherId = 313L, fatherName = "محمد عارف", gender = Gender.FEMALE, generation = 5, displayOrder = 6),

            // ==========================================
            // BRANCH 3: اولاد حاکم علی (Father ID = 30)
            // ==========================================
            FamilyMember(id = 401L, name = "محمد عالم", fatherId = 30L, fatherName = "حاکم علی", gender = Gender.MALE, generation = 3, displayOrder = 1),
            FamilyMember(id = 402L, name = "محمد دین", fatherId = 30L, fatherName = "حاکم علی", gender = Gender.MALE, generation = 3, displayOrder = 2),

            // اولاد محمد عالم (Father ID = 401)
            FamilyMember(id = 411L, name = "محمد اصغر", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 412L, name = "لیاقت علی", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 413L, name = "شوکت علی", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 414L, name = "منظور بی بی", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد عالم", displayOrder = 4),
            FamilyMember(id = 415L, name = "مقبول بی بی", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ شریف چک سکندر", location = "چک سکندر", displayOrder = 5),
            FamilyMember(id = 416L, name = "مقصود بی بی", fatherId = 401L, fatherName = "محمد عالم", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد تاج پھالیہ", location = "پھالیہ", displayOrder = 6),

            // اولاد محمد اصغر (Father ID = 411)
            FamilyMember(id = 421L, name = "شہزاد اصغر", fatherId = 411L, fatherName = "محمد اصغر", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 422L, name = "عنصر شہزاد", fatherId = 411L, fatherName = "محمد اصغر", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 423L, name = "رخسانہ", fatherId = 411L, fatherName = "محمد اصغر", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 424L, name = "صوبیہ", fatherId = 411L, fatherName = "محمد اصغر", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد لیاقت علی (Father ID = 412)
            FamilyMember(id = 431L, name = "شہباز", fatherId = 412L, fatherName = "لیاقت علی", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 432L, name = "سفینہ", fatherId = 412L, fatherName = "لیاقت علی", gender = Gender.FEMALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 433L, name = "نرین", fatherId = 412L, fatherName = "لیاقت علی", gender = Gender.FEMALE, generation = 5, displayOrder = 3),

            // اولاد شوکت علی (Father ID = 413)
            FamilyMember(id = 441L, name = "اویس", fatherId = 413L, fatherName = "شوکت علی", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 442L, name = "وقاص", fatherId = 413L, fatherName = "شوکت علی", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 443L, name = "سفیان", fatherId = 413L, fatherName = "شوکت علی", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 444L, name = "رابیہ", fatherId = 413L, fatherName = "شوکت علی", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد محمد دین ولد حاکم علی (Father ID = 402)
            FamilyMember(id = 451L, name = "طالب حسین", fatherId = 402L, fatherName = "محمد دین", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 452L, name = "عدالت حسین", fatherId = 402L, fatherName = "محمد دین", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 453L, name = "نزر حسین", fatherId = 402L, fatherName = "محمد دین", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 454L, name = "خادم حسین", fatherId = 402L, fatherName = "محمد دین", gender = Gender.MALE, generation = 4, displayOrder = 4),

            // اولاد عدالت حسین (Father ID = 452)
            FamilyMember(id = 461L, name = "محمد جاوید", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 462L, name = "محمد پرویز", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 463L, name = "صغیر احمد", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 464L, name = "تنویر احمد", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.MALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 465L, name = "شبیر احمد", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.MALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 466L, name = "امتل حفیظ", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 6),
            FamilyMember(id = 467L, name = "عزیز فاطمہ", fatherId = 452L, fatherName = "عدالت حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 7),

            // اولاد نزر حسین ولد محمد دین (Father ID = 453)
            FamilyMember(id = 471L, name = "عابد حسین", fatherId = 453L, fatherName = "نزر حسین", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 472L, name = "غزالہ شاہین", fatherId = 453L, fatherName = "نزر حسین", gender = Gender.FEMALE, generation = 5, displayOrder = 2),

            // ==========================================
            // BRANCH 4: اولاد محمد ھاشم (Father ID = 40)
            // ==========================================
            FamilyMember(id = 501L, name = "محمد ماہی", fatherId = 40L, fatherName = "محمد ھاشم", gender = Gender.MALE, generation = 3, displayOrder = 1),
            FamilyMember(id = 502L, name = "سردار بی بی", fatherId = 40L, fatherName = "محمد ھاشم", gender = Gender.FEMALE, generation = 3, location = "گوجرانوالہ", notes = "شادی گوجرانوالہ", displayOrder = 2),

            // اولاد محمد ماہی (Father ID = 501)
            FamilyMember(id = 511L, name = "رحمت خاں", fatherId = 501L, fatherName = "محمد ماہی", gender = Gender.MALE, generation = 4, displayOrder = 1),
            FamilyMember(id = 512L, name = "میاں خاں", fatherId = 501L, fatherName = "محمد ماہی", gender = Gender.MALE, generation = 4, displayOrder = 2),
            FamilyMember(id = 513L, name = "فتح خاں", fatherId = 501L, fatherName = "محمد ماہی", gender = Gender.MALE, generation = 4, displayOrder = 3),
            FamilyMember(id = 514L, name = "مرزا خاں", fatherId = 501L, fatherName = "محمد ماہی", gender = Gender.MALE, generation = 4, displayOrder = 4),
            FamilyMember(id = 515L, name = "نزیر بی بی", fatherId = 501L, fatherName = "محمد ماہی", gender = Gender.FEMALE, generation = 4, spouse = "زوجہ محمد فاضل", displayOrder = 5),

            // اولاد رحمت خاں (Father ID = 511)
            FamilyMember(id = 521L, name = "محمد یوسف", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 522L, name = "محمد یونس", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 523L, name = "محمد ایوب", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 524L, name = "عبدالغفور", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 4),
            FamilyMember(id = 525L, name = "ظہور احمد", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 5),
            FamilyMember(id = 526L, name = "نور حسین", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.MALE, generation = 5, displayOrder = 6),
            FamilyMember(id = 527L, name = "انور بی بی", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.FEMALE, generation = 5, spouse = "زوجہ محمد لطیف پھالیہ", location = "پھالیہ", displayOrder = 7),
            FamilyMember(id = 528L, name = "عظمت بی بی", fatherId = 511L, fatherName = "رحمت خاں", gender = Gender.FEMALE, generation = 5, spouse = "زوجہ نزر حسین", displayOrder = 8),

            // اولاد محمد یوسف (Father ID = 521)
            FamilyMember(id = 531L, name = "محمد علی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.MALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 532L, name = "سمیرہ بی بی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.FEMALE, generation = 6, displayOrder = 2),
            FamilyMember(id = 533L, name = "زنیرہ بی بی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.FEMALE, generation = 6, displayOrder = 3),
            FamilyMember(id = 534L, name = "اقراء بی بی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.FEMALE, generation = 6, displayOrder = 4),
            FamilyMember(id = 535L, name = "حاجرہ بی بی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.FEMALE, generation = 6, displayOrder = 5),
            FamilyMember(id = 536L, name = "آمنہ بی بی", fatherId = 521L, fatherName = "محمد یوسف", gender = Gender.FEMALE, generation = 6, displayOrder = 6),

            // اولاد محمد یونس (Father ID = 522)
            FamilyMember(id = 541L, name = "محمد یاسین", fatherId = 522L, fatherName = "محمد یونس", gender = Gender.MALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 542L, name = "محمد وسیم", fatherId = 522L, fatherName = "محمد یونس", gender = Gender.MALE, generation = 6, displayOrder = 2),
            FamilyMember(id = 543L, name = "محمد ندیم", fatherId = 522L, fatherName = "محمد یونس", gender = Gender.MALE, generation = 6, displayOrder = 3),
            FamilyMember(id = 544L, name = "نازیہ بی بی", fatherId = 522L, fatherName = "محمد یونس", gender = Gender.FEMALE, generation = 6, displayOrder = 4),
            FamilyMember(id = 545L, name = "لائبہ بی بی", fatherId = 522L, fatherName = "محمد یونس", gender = Gender.FEMALE, generation = 6, displayOrder = 5),

            // اولاد محمد ایوب (Father ID = 523)
            FamilyMember(id = 551L, name = "شمریز ایوب", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.MALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 552L, name = "عبدالقدوس", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.MALE, generation = 6, displayOrder = 2),
            FamilyMember(id = 553L, name = "محمد راؤف", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.MALE, generation = 6, displayOrder = 3),
            FamilyMember(id = 554L, name = "محمد جہانزیب", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.MALE, generation = 6, displayOrder = 4),
            FamilyMember(id = 555L, name = "محمد شاویز", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.MALE, generation = 6, displayOrder = 5),
            FamilyMember(id = 556L, name = "عمبرین جان", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.FEMALE, generation = 6, displayOrder = 6),
            FamilyMember(id = 557L, name = "عاصمہ بی بی", fatherId = 523L, fatherName = "محمد ایوب", gender = Gender.FEMALE, generation = 6, displayOrder = 7),

            // اولاد عبدالغفور (Father ID = 524)
            FamilyMember(id = 561L, name = "مریم غفور", fatherId = 524L, fatherName = "عبدالغفور", gender = Gender.FEMALE, generation = 6, displayOrder = 1),

            // اولاد ظہور احمد (Father ID = 525)
            FamilyMember(id = 571L, name = "شازین بی بی", fatherId = 525L, fatherName = "ظہور احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 572L, name = "سابین بی بی", fatherId = 525L, fatherName = "ظہور احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 2),

            // اولاد نور حسین (Father ID = 526)
            FamilyMember(id = 581L, name = "عبدالحنان", fatherId = 526L, fatherName = "نور حسین", gender = Gender.MALE, generation = 6, displayOrder = 1),

            // اولاد میاں خاں (Father ID = 512)
            FamilyMember(id = 601L, name = "امتیاز احمد", fatherId = 512L, fatherName = "میاں خاں", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 602L, name = "اعجاز احمد", fatherId = 512L, fatherName = "میاں خاں", gender = Gender.MALE, generation = 5, displayOrder = 2),
            FamilyMember(id = 603L, name = "انور بی بی", fatherId = 512L, fatherName = "میاں خاں", gender = Gender.FEMALE, generation = 5, displayOrder = 3),
            FamilyMember(id = 604L, name = "زہرہ بی بی", fatherId = 512L, fatherName = "میاں خاں", gender = Gender.FEMALE, generation = 5, displayOrder = 4),

            // اولاد امتیاز احمد (Father ID = 601)
            FamilyMember(id = 611L, name = "شاہد میاں", fatherId = 601L, fatherName = "امتیاز احمد", gender = Gender.MALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 612L, name = "عمر", fatherId = 601L, fatherName = "امتیاز احمد", gender = Gender.MALE, generation = 6, displayOrder = 2),
            FamilyMember(id = 613L, name = "مدیحہ", fatherId = 601L, fatherName = "امتیاز احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 3),
            FamilyMember(id = 614L, name = "زینت", fatherId = 601L, fatherName = "امتیاز احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 4),
            FamilyMember(id = 615L, name = "عروج", fatherId = 601L, fatherName = "امتیاز احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 5),

            // اولاد اعجاز احمد (Father ID = 602)
            FamilyMember(id = 621L, name = "اعتزاز حسن", fatherId = 602L, fatherName = "اعجاز احمد", gender = Gender.MALE, generation = 6, displayOrder = 1),
            FamilyMember(id = 622L, name = "ابیحہ", fatherId = 602L, fatherName = "اعجاز احمد", gender = Gender.FEMALE, generation = 6, displayOrder = 2),

            // اولاد فتح خاں (Father ID = 513)
            FamilyMember(id = 631L, name = "پروین اختر", fatherId = 513L, fatherName = "فتح خاں", gender = Gender.FEMALE, generation = 5, displayOrder = 1),

            // اولاد مرزا خاں (Father ID = 514)
            FamilyMember(id = 641L, name = "محمد پرویز", fatherId = 514L, fatherName = "مرزا خاں", gender = Gender.MALE, generation = 5, displayOrder = 1),
            FamilyMember(id = 642L, name = "محمد فاروق", fatherId = 514L, fatherName = "مرزا خاں", gender = Gender.MALE, generation = 5, isDeceased = true, deathNote = "وفات کم عمری", displayOrder = 2)
        )
    }
}
