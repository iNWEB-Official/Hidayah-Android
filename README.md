# হিদায়াহ — Hidayah (Android)

একটি আধুনিক ইসলামিক অ্যাপ — নামাজের সময়, কুরআন কারীম, কিবলা কম্পাস, তসবিহ, দুয়া, হিজরি ক্যালেন্ডার ও সেটিংস। UI বাংলায়, আরবি বিষয়বস্তু Amiri হরফে।

> **অবস্থা / Status (২০২৬-১১-১৬ পর্যন্ত)**: সোর্স-কোড কাজ চলমান — থিম সুইচ, নতুন আয়াত কার্ড, শেয়ার=ইমেজ, **উপরের অ্যাকশন বার সরানো**; **কোনো প্রকাশিত APK/AAB নেই** (২০২৬.০০.০৭ রিলিজ অপেক্ষমাণ — `CHANGELOG.md` দেখুন)।
>
> **প্যাকেজ / applicationId**: `com.hidayah.android`। ডিভাইসে/এমুলেটরে রান করে UI যাচাই করা হয়নি (এই পরিবেশে ডিসপ্লে নেই)।

## ডকুমেন্টেশন ও রিপোজিটরি

প্রকল্পের সকল গুরুত্বপূর্ণ নথি **[iNWEB-DOCS/Hidayah-Android](https://github.com/iNWEB-DOCS/Hidayah-Android)** রিপোজিটরিতে সংরক্ষিত (public):

- **[guides/SCREENS.md](https://github.com/iNWEB-DOCS/Hidayah-Android/blob/main/guides/SCREENS.md)** — ১৬ স্ক্রিনের সিরিয়াল তালিকা (ইন্সটল → অনবোর্ডিং → সেটআপ → পারমিশন → হোম …), কোনটি তৈরি ও মকআপের সাথে প্রতিটিতে মিল/অমিল ও সংশোধনের নোট।
- **[guides/PUBLISHING.md](https://github.com/iNWEB-DOCS/Hidayah-Android/blob/main/guides/PUBLISHING.md)** — ২১টি সম্ভাব্য প্ল্যাটফর্মে (Google Play, Galaxy Store, Amazon, AppGallery, GetApps, F-Droid, APKPure, Aptoide, Uptodown, SlideME, itch.io, GetJar, Cafe Bazaar, Myket, GitHub Releases, নিজস্ব ওয়েবসাইট…) অ্যাকাউন্ট খোলার ও প্রকাশের সম্পূর্ণ স্টেপ-বাই-স্টেপ গাইড।
- **[guides/VERSIONING.md](https://github.com/iNWEB-DOCS/Hidayah-Android/blob/main/guides/VERSIONING.md)** — `YYYY.MM.NN` ভার্সন সিস্টেম (২০২৬ = চলতি সাল, MM = মেজর, NN = মাইনর), `versionCode` নিয়ম, জিপ রিলিজের নাম `হিদায়াহ-v<ভার্সন>.zip`, প্রথম পাবলিক রিলিজ **হিদায়াহ-v2026.00.01**।
- **এপ্লিকেশন ভার্সন সিস্টেম.md** (রিপোর রুটে) — এই ভার্সনিং সিস্টেমটি সকল iNWEB অ্যাপে ব্যবহারের সাধারণ স্ট্যান্ডার্ড।
- **[design/](https://github.com/iNWEB-DOCS/Hidayah-Android/tree/main/design)** — ডিজাইন সোর্স: আয়াত-কার্ড SVG, সকল স্ক্রিনের SVG মকাপ + PNG প্রিভিউ (`design/mockups/`)।

## বৈশিষ্ট্য

| স্ক্রিন | বিবরণ |
|---|---|
| 00 কভার | নাম, আমন্ত্রণ ও সূচনা স্ক্রিন (بِسْمِ লোগো, মসজিদ ইলাস্ট্রেশন Canvas-এ আঁকা) |
| 01 হোম | আজকের ৬টি নামাজের সময় (বাস্তব জ্যোতির্বৈজ্ঞানিক হিসাব), পরবর্তী নামাজের কাউন্টডাউন, আজকের আয়াত, দ্রুত অ্যাক্সেস |
| 02 কুরআন | ১১৪টি সূরার সূচী, সার্চ (বাংলা/ইংরেজি/আরবি/নম্বর), "যেখান থেকে পড়তে ছিলেন" বুকমার্ক কার্ড |
| 03 রিডার | আরবি টেক্সট (অ-মাপ পরিবর্তনযোগ্য), বাংলা অনুবাদ টগল, বুকমার্ক, সূরা আল-ফাতিহা সম্পূর্ণ অফলাইনে |
| 04 কিবলা | সেন্সর ফিউশন কম্পাস (rotation vector, fallback accel+mag), কাবার দিক ও দূরত্ব, ক্যালিব্রেশন বার্তা |
| 05 তসবিহ | ট্যাপ-টু-কাউন্ট, ৩৩/১০০/১০০০ লক্ষ্য, আজকের মোট যিকির (ডেটাস্টোরে সংরক্ষণ), সম্পূরণ উল্লাস ডায়ালগ |
| 06 দুয়া | ৪৬টি সহীহ দুয়া, সার্চ, ক্যাটাগরি ফিল্টার, ফেভারিট (সংরক্ষিত), বিস্তারিত কার্ড |
| 07 হিজরি ক্যালেন্ডার | উম্মুল-ক্বুরা হিজরি বর্ষপঞ্জি (ICU), নির্বাচিত দিনের বিস্তারিত, গুরুত্বপূর্ণ দিন চিহ্নিত |
| 08 সেটিংস | মাযহাব (হানাফি/শাফেয়ী — আসর হিসাবে প্রভাব), শহর (৮টি বাংলাদেশি শহর), বিজ্ঞপ্তি টগল, অ্যাপ তথ্য |

## টেক স্ট্যাক

- **Kotlin + Jetpack Compose** (Material 3), single-activity, Navigation-Compose
- **DataStore Preferences** — সেটিংস ও তসবিহ স্টোর
- **android.icu** (`islamic-umalqura`) — হিজরি তারিখ (minSdk 26+)
- **কোনো নেটওয়ার্ক কল নেই** — সব কনটেন্ট অফলাইনে (নামাজের হিসাব সম্পূর্ণ ডিভাইসে)
- ফন্ট: Noto Serif/Sans Bengali + Amiri (OFL, `res/font/`-এ বান্ডল করা)

### নামাজের সময় অ্যালগরিদম

NOAA সৌর হিসাব (সৌর দ্রাঘিমা, সমীকরণ-সময়, ঘনঘটা অবনতি কোণ) থেকে সরাসরি ডিভাইসে হিসাব — বছরের পর বছর **astral** রেফারেন্সের সাপেক্ষে সাপ্তাহিক নমুনায় ±১ মিনিটের মধ্যে যাচাই করা হয়েছে (২০২৫–২০২৬, ঢাকা)। ফজর/এশা কোণ যথাক্রমে ১৮°/১৭° (MWL পদ্ধতি), মাগরিব ০.৮৩৩° প্রতিসরণের সাথে। মাযহাব অনুযায়ী আসর (হানাফি: ছায়ার দ্বিগুণ) সেটিংসে পরিবর্তনযোগ্য।

হিজরি তারিখের জন্য কোনো নিজস্ব হিসাব নেই — Android-এর ICU লাইব্রেরি (`islamic-umalqura`) ব্যবহৃত হয়।

## বিল্ড নির্দেশনা

1. **Android Studio Hedgehog (2023.1)+** ইনস্টল করুন।
2. এই ফোল্ডারটি (`hidayah-android/`) প্রজেক্ট হিসেবে খুলুন → Gradle Sync হবে।
   - যদি Android Studio wrapper নিয়ে সতর্ক করে: `gradle wrapper --gradle-version 8.4` চালানোর প্রয়োজন নেই; Studio নিজেই wrapper সেটআপ করে। কমান্ড-লাইন চাইলে স্থানীয় Gradle 8.4 থেকে `gradle wrapper` চালান।
3. **Run** → যেকোনো ডিভাইস/এমুলেটর (API 26+, অর্থাৎ Android 8.0+)।
4. রিলিজ বিল্ড: `./gradlew :app:assembleRelease` (সাইনিং কনফিগ নিজে যোগ করুন)।

প্রয়োজনীয় ভ্যারিয়েন্ট: compileSdk 34, minSdk 26, targetSdk 34, JDK 17 (Studio-তে ডিফল্ট), Compose BOM 2024.02.00, Kotlin 1.9.22।

## মডিউল ম্যাপ

```
app/src/main/
├── AndroidManifest.xml
├── assets/                 # quran_surahs.json (১১৪ সূরা), fatiha.json (সম্পূর্ণ ফাতিহা), duas.json (৪৬ দুয়া)
├── res/font/               # bundled TTFs (OFL)
├── res/values[-bn]/        # strings, colors, theme, launcher icon (adaptive)
└── java/com/hidayah/android/
    ├── MainActivity.kt             # Scaffold + NavHost (bottom bar ৫-ট্যাব)
    ├── state/HidayahViewModel.kt   # UI স্টেট (home countdown, tasbih, settings)
    ├── data/
    │   ├── Cities.kt               # ৮টি বাংলাদেশি শহর (অফলাইন কিবলা/নামাজের জন্য)
    │   ├── prayer/PrayerTimeCalculator.kt
    │   ├── hijri/HijriDates.kt     # ICU উম্মুল-ক্বুরা
    │   ├── quran/QuranRepository.kt, QiblaMath.kt
    │   ├── dua/DuaRepository.kt
    │   └── prefs/SettingsStore.kt  # DataStore
    ├── util/BnDigits.kt            # বাংলা সংখ্যা রূপান্তর
    └── ui/
        ├── theme/                  # Color, Type, Shape, Theme (emerald+gold)
        ├── navigation/Routes.kt
        ├── components/             # ScreenTitle, EmeraldHeroHeader, HidayahBottomBar, chips, dividers, নম্বর বৃত্ত
        └── screens/                # cover, home, quran (সূচী+রিডার), qibla, tasbih, dua, calendar, settings
```

## পরিচিত সীমাবদ্ধতা / যাচাই-বাকি

1. **ডিভাইসে টেস্ট করা হয়নি** — বিল্ড সফল, কিন্তু এমুлятор/ডিভাইসে রান করে UI যাচাই বাকি। API 26+ ডিভাইসে রান করুন; কিবলা কম্পাসে প্রকৃত সেন্সর দরকার।
2. **রেসিটেশন অডিও নেই** — রিডারে "মিশারি আল-আফাসি" চিপটি শুধু UI; অডিও স্ট্রিমিং/অফলাইন অডিও আলাদা ধাপ।
3. **পূর্ণ কুরআন টেক্সট নেই** — বর্তমানে শুধু সূরা আল-ফাতিহার আরবি+অনুবাদ; বাকি সূরাগুলোতে "পূর্ণ টেক্সট যোগ হবে" নোটিশ দেখায়। ভবিষ্যতে `assets/quran_text.json` বা Room DB-তে যোগ করুন।
4. **নামাজের নোটিফিকেশন নির্ধারিত হয়নি** — সেটিংসের টগলগুলো DataStore-এ সংরক্ষিত হয়, কিন্তু `AlarmManager`/`NotificationManager` সেটআপ পরের ধাপ।
5. **কিবলা দিক** — ডিভাইস `ROTATION_VECTOR` সেন্সরের ওপর নির্ভর; এমুলেটরে সঠিক মান দেখা যাবে না (বাস্তব ডিভাইসে টেস্ট করুন)। শহরের অবস্থান `Cities.kt`-তে স্থির; GPS-লোকেশন সংস্করণ চাইলে `FusedLocationProvider` + অনুমতি যোগ করতে পারেন।
6. **হিজরি তারিখ উম্মুল-ক্বুরা** — অন্য কিছু ক্যালেন্ডার অ্যালগরিদম (যেমন কিছু দেশে ব্যবহৃত চাঁদ-দেখা পদ্ধতি) এ থেকে ±১ দিন ভিন্ন হতে পারে; এটি ICU-এর মানদণ্ড অনুযায়ী "সরকারি" হিসাব।
7. মকিং-আপের নাম "রাহিম উদ্দিন" ও ডিফল্ট আল-কাহফ বুকমার্ক — ডেমো ডেটা; বাস্তব অ্যাকাউন্ট/সিঙ্ক নেই।

## লাইসেন্স ও ফন্ট

ফন্ট তিনটি SIL Open Font License: Noto Serif Bengali, Noto Sans Bengali, Amiri। যথাযথ উৎস: Google Fonts।
