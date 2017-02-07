package io.dwak

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.inject.Inject
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@AutoService(InjectionHelperProcessor::class)
class InjectionHelperProcessor: AbstractProcessor() {
  private lateinit var env: ProcessingEnvironment
  private val filer: Filer by lazy { env.filer }
  private val messager: Messager by lazy { env.messager }
  private val elementUtils: Elements by lazy { env.elementUtils }

  override fun init(p0: ProcessingEnvironment) {
    super.init(p0)
    env = p0
  }

  override fun getSupportedSourceVersion() = SourceVersion.latestSupported()

  override fun getSupportedAnnotationTypes() = mutableSetOf(Inject::class.java.canonicalName)

  override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
    if (annotations.isNotEmpty()) {
      val injectionHelperTargetClassMap = hashMapOf<TypeElement, InjectionHelperBindingClass>()
      val erasedTargetNames =mutableSetOf<String>()
      annotations.map { roundEnv.getElementsAnnotatedWith(it) }
          .flatMap { it.filter { it.hasAnnotationWithName(Inject::class.java.simpleName) } }
          .forEach {
            try {
              val enclosingTypeElement = it.enclosingElement as TypeElement
              val injectionHelper = getOrCreateInjectionHelper(injectionHelperTargetClassMap,
                  enclosingTypeElement, erasedTargetNames)
              injectionHelper.createAndAddBinding(it)
            } catch (e: Exception) {
            }
          }

      injectionHelperTargetClassMap.values
          .forEach {
            try {
              it.writeToFiler(filer)
            } catch (e: Exception) {
              messager.printMessage(Diagnostic.Kind.ERROR, e.message)
            }
          }
    }

    return true
  }

  private fun getOrCreateInjectionHelper(targetClassMap: MutableMap<TypeElement, InjectionHelperBindingClass>,
                                         enclosingElement: TypeElement,
                                         erasedTargetNames: MutableSet<String>) : InjectionHelperBindingClass {
    var injectionHelper = targetClassMap[enclosingElement]

    if (injectionHelper == null) {
      val targetCLass = enclosingElement.qualifiedName.toString()
      val classPackage = enclosingElement.packageName(elementUtils)
      val className = enclosingElement.className(classPackage) + InjectionHelperBindingClass.SUFFIX
      injectionHelper = InjectionHelperBindingClass(classPackage, className, targetCLass,
          processingEnv)
      targetClassMap.put(enclosingElement, injectionHelper)
      erasedTargetNames.add(enclosingElement.toString())
    }

    return injectionHelper
  }

  @Suppress("unused")
  private fun error(element: Element, message: String, vararg args: Any)
      = messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args), element)

  @Suppress("unused")
  private fun note(note: String) = messager.printMessage(Diagnostic.Kind.NOTE, note)

  @Suppress("unused")
  private fun warning(note: String) = messager.printMessage(Diagnostic.Kind.WARNING, note)
}

/**
 * Returns {@code true} if the an annotation is found on the given element with the given class
 * name (not fully qualified).
 */
fun Element.hasAnnotationWithName(simpleName: String): Boolean {
  annotationMirrors.forEach {
    val annotationElement = it.annotationType.asElement()
    val annotationName = annotationElement.simpleName.toString()
    if (simpleName.equals(annotationName)) {
      return true
    }
  }
  return false
}

fun TypeElement.packageName(elementUtils: Elements) =
    elementUtils.getPackageOf(this).qualifiedName.toString()

fun TypeElement.className(packageName: String): String {
  val packageLen = packageName.length + 1
  return this.qualifiedName.toString().substring(packageLen).replace('.', '$')
}

