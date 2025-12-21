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

class PhpWorkflowMethodIndex : AbstractIndex<String>() {
    companion object {
        val NAME = ID.create<String, String>("temporal.workflow.methods")
        val ATTRIBUTES = listOf(
            TemporalClasses.WORKFLOW_METHOD,
            TemporalClasses.WORKFLOW_INIT,
            TemporalClasses.UPDATE_VALIDATOR_METHOD,
            TemporalClasses.UPDATE_METHOD,
            TemporalClasses.SIGNAL_METHOD,
            TemporalClasses.QUERY_METHOD
        )
    }

    override fun getName(): ID<String, String> = NAME

    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return DataIndexer { fileContent ->
            val phpFile = fileContent.psiFile as? PhpFile ?: return@DataIndexer emptyMap()
            val result = mutableMapOf<String, String>()
            val classes = PsiTreeUtil.findChildrenOfType(phpFile, PhpClass::class.java)
            for (phpClass in classes) {
                val classFqn = phpClass.fqn
                for (method in phpClass.methods) {
                    for (attribute in ATTRIBUTES) {
                        if (method.hasAttribute(attribute)) {
                            result["$classFqn::${method.name}"] = attribute
                            break
                        }
                    }
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
