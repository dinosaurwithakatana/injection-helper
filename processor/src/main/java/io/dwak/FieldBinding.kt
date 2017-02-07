package io.dwak

import javax.lang.model.element.Element
import javax.lang.model.type.TypeMirror

class FieldBinding(element: Element) {
  val name: String
  val type: TypeMirror

  init {
    name = element.simpleName.toString()
    type = element.asType()
  }
}