package com.github.xepozz.temporal.languages.php.index

import com.github.xepozz.temporal.common.index.AbstractIndex
import com.github.xepozz.temporal.languages.php.TemporalClasses
import com.github.xepozz.temporal.languages.php.hasAttribute
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.jetbrains.php.lang.PhpFileType
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.PhpClass

class PhpActivityClassIndex : AbstractIndex<String>() {
    companion object {
        val NAME = ID.create<String, String>("temporal.activity.classes")
    }

    override fun getName(): ID<String, String> = NAME

    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return DataIndexer { fileContent ->
            val phpFile = fileContent.psiFile as? PhpFile ?: return@DataIndexer emptyMap()
            val result = mutableMapOf<String, String>()
            val classes = PsiTreeUtil.findChildrenOfType(phpFile, PhpClass::class.java)
            for (phpClass in classes) {
                if (phpClass.hasAttribute(TemporalClasses.ACTIVITY)) {
                    val fqn = phpClass.fqn
                    result[fqn] = fqn
                }
            }
            result
        }
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return FileBasedIndex.InputFilter { file -> file.fileType == PhpFileType.INSTANCE }
    }

    override fun getValueExternalizer(): DataExternalizer<String> = EnumeratorStringDescriptor.INSTANCE
}
