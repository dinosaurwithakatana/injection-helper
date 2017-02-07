package io.dwak

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class InjectionHelperBindingClass(private val classPackage: String,
                                  private val className: String,
                                  private val targetClass: String,
                                  private val processingEnv: ProcessingEnvironment) {
  companion object {
    const val SUFFIX = "InjectionHelper"
    const val METHOD_NAME = "inject"
  }

  private val bindings = hashMapOf<String, FieldBinding>()

  fun createAndAddBinding(element: Element) {
    val binding = FieldBinding(element)
    bindings.put(binding.name, binding)
  }

  fun generate(): TypeSpec {
    val classBuilder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)

    val injectBuilder = MethodSpec.methodBuilder(METHOD_NAME)
        .addParameter(ParameterSpec.builder(ClassName.get(classPackage, targetClass), "target")
            .build())
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(TypeName.VOID)

    bindings.values
        .forEach {
          injectBuilder.addParameter(ParameterSpec.builder(ClassName.get(it.type), it.name).build())
          injectBuilder.addStatement("target.${it.name} = ${it.name}")
        }

    return classBuilder.addMethod(injectBuilder.build())
        .build()
  }

  @Throws(IOException::class)
  fun writeToFiler(filer: Filer) {
    JavaFile.builder(classPackage, generate()).build().writeTo(filer)
  }
}