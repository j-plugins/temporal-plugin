package com.github.xepozz.temporal.common

import com.intellij.spellchecker.BundledDictionaryProvider

class SpellcheckingDictionaryProvider : BundledDictionaryProvider {
    override fun getBundledDictionaries(): Array<String> = arrayOf("/temporal.dic")
}