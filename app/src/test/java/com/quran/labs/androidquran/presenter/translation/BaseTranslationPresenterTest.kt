package com.quran.labs.androidquran.presenter.translation

import com.google.common.truth.Truth.assertThat
import com.quran.data.page.provider.madani.MadaniPageProvider
import com.quran.labs.androidquran.common.LocalTranslation
import com.quran.labs.androidquran.common.QuranText
import com.quran.labs.androidquran.data.QuranInfo
import com.quran.labs.androidquran.data.VerseRange
import com.quran.labs.androidquran.database.TranslationsDBAdapter
import com.quran.labs.androidquran.model.translation.TranslationModel
import com.quran.labs.androidquran.presenter.Presenter
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap

class BaseTranslationPresenterTest {
  private lateinit var presenter: BaseTranslationPresenter<TestPresenter>

  @Before
  fun setupTest() {
    presenter = BaseTranslationPresenter(
        Mockito.mock(TranslationModel::class.java),
        Mockito.mock(TranslationsDBAdapter::class.java),
        QuranInfo(MadaniPageProvider()))
  }

  @Test
  fun testGetTranslationNames() {
    val databases = Arrays.asList("one.db", "two.db")
    val map = object : HashMap<String, LocalTranslation>() {
      init {
        put("one.db", LocalTranslation(1, "one.db", "One", "First", null, "", null, 1))
        put("two.db", LocalTranslation(2, "two.db", "Two", "Second", null, "", null, 1))
        put("three.db", LocalTranslation(2, "three.db", "Three", "Third", null, "", null, 1))
      }
    }

    val translations = presenter!!.getTranslationNames(databases, map)
    assertThat(translations).hasLength(2)
    assertThat(translations[0]).isEqualTo("First")
    assertThat(translations[1]).isEqualTo("Second")
  }

  @Test
  fun testHashlessGetTranslationNames() {
    val databases = Arrays.asList("one.db", "two.db")
    val map = HashMap<String, LocalTranslation>()

    val translations = presenter!!.getTranslationNames(databases, map)
    assertThat(translations).hasLength(2)
    assertThat(translations[0]).isEqualTo(databases[0])
    assertThat(translations[1]).isEqualTo(databases[1])
  }

  @Test
  fun testCombineAyahDataOneVerse() {
    val verseRange = VerseRange(1, 1, 1, 1, 1)
    val arabic = listOf(QuranText(1, 1, "first ayah"))
    val info = presenter!!.combineAyahData(verseRange, arabic,
        listOf(listOf(QuranText(1, 1, "translation"))))

    assertThat(info).hasSize(1)
    val first = info[0]
    assertThat(first.sura).isEqualTo(1)
    assertThat(first.ayah).isEqualTo(1)
    assertThat(first.texts).hasSize(1)
    assertThat(first.arabicText).isEqualTo("first ayah")
    assertThat(first.texts[0]).isEqualTo("translation")
  }

  @Test
  fun testCombineAyahDataOneVerseEmpty() {
    val verseRange = VerseRange(1, 1, 1, 1, 1)
    val arabic = emptyList<QuranText>()
    val info = presenter!!.combineAyahData(verseRange, arabic, emptyList())
    assertThat(info).hasSize(0)
  }

  @Test
  fun testCombineAyahDataOneVerseNoArabic() {
    val verseRange = VerseRange(1, 1, 1, 1, 1)
    val arabic = emptyList<QuranText>()
    val info = presenter!!.combineAyahData(verseRange, arabic,
        listOf(listOf(QuranText(1, 1, "translation"))))

    assertThat(info).hasSize(1)
    val first = info[0]
    assertThat(first.sura).isEqualTo(1)
    assertThat(first.ayah).isEqualTo(1)
    assertThat(first.texts).hasSize(1)
    assertThat(first.arabicText).isNull()
    assertThat(first.texts[0]).isEqualTo("translation")
  }

  @Test
  fun testCombineAyahDataArabicEmptyTranslations() {
    val verseRange = VerseRange(1, 1, 1, 2, 2)
    val arabic = Arrays.asList(
        QuranText(1, 1, "first ayah"),
        QuranText(1, 2, "second ayah")
    )
    val info = presenter!!.combineAyahData(verseRange, arabic, ArrayList())
    assertThat(info).hasSize(2)
    assertThat(info[0].sura).isEqualTo(1)
    assertThat(info[0].ayah).isEqualTo(1)
    assertThat(info[0].texts).hasSize(0)
    assertThat(info[0].arabicText).isEqualTo("first ayah")
    assertThat(info[1].sura).isEqualTo(1)
    assertThat(info[1].ayah).isEqualTo(2)
    assertThat(info[1].texts).hasSize(0)
    assertThat(info[1].arabicText).isEqualTo("second ayah")
  }

  @Test
  fun testEnsureProperTranslations() {
    val verseRange = VerseRange(1, 1, 1, 2, 2)

    val text = listOf(QuranText(1, 1, "bismillah"))
    val result = presenter.ensureProperTranslations(verseRange, text)
    assertThat(result).hasSize(2)

    val first = result[0]
    assertThat(first.sura).isEqualTo(1)
    assertThat(first.ayah).isEqualTo(1)
    assertThat(first.text).isEqualTo("bismillah")

    val second = result[1]
    assertThat(second.sura).isEqualTo(1)
    assertThat(second.ayah).isEqualTo(2)
    assertThat(second.text).isEmpty()
  }

  private class TestPresenter : Presenter<Any> {
    override fun bind(what: Any) {}

    override fun unbind(what: Any) {}
  }
}
