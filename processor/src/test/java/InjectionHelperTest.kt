import com.google.testing.compile.CompilationSubject
import com.google.testing.compile.Compiler
import com.google.testing.compile.JavaFileObjects
import io.dwak.InjectionHelperProcessor
import org.junit.Test

class InjectionHelperTest {

  @Test
  fun injectTestOneField() {
    val inputFile
        = JavaFileObjects.forSourceLines("test.Test",
        "package test;",
        "import java.lang.String;",
        "import javax.inject.Inject;",
        "public class Test {",
        "",
        "  @Inject String myString;",
        "",
        "}")

    val expectedOutput
        = JavaFileObjects.forSourceLines("test.TestInjectionHelper",
        "package test;",
        "import java.lang.String;",
        "",
        "public final class TestInjectionHelper {",
        "  public static void inject(test.Test target, String myString) {",
        "    target.myString = myString;",
        "  }",
        "}")

    val compilation = Compiler.javac().withProcessors(InjectionHelperProcessor())
        .withOptions("-Xlint:-processing")
        .compile(inputFile)

    CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
    CompilationSubject.assertThat(compilation).generatedSourceFile("test.TestInjectionHelper")
        .hasSourceEquivalentTo(expectedOutput)
  }

  @Test
  fun injectTestMultiField() {
    val inputFile
        = JavaFileObjects.forSourceLines("test.Test",
        "package test;",
        "import java.lang.String;",
        "import java.lang.Integer;",
        "import javax.inject.Inject;",
        "public class Test {",
        "",
        "  @Inject String myString;",
        "  @Inject String myString2;",
        "  @Inject Integer myInt;",
        "",
        "}")

    val expectedOutput
        = JavaFileObjects.forSourceLines("test.TestInjectionHelper",
        "package test;",
        "import java.lang.Integer;",
        "import java.lang.String;",
        "",
        "public final class TestInjectionHelper {",
        "  public static void inject(test.Test target, String myString, String myString2, Integer myInt) {",
        "    target.myString = myString;",
        "    target.myString2 = myString2;",
        "    target.myInt = myInt;",
        "  }",
        "}")

    val compilation = Compiler.javac().withProcessors(InjectionHelperProcessor())
        .withOptions("-Xlint:-processing")
        .compile(inputFile)

    CompilationSubject.assertThat(compilation).succeededWithoutWarnings()
    CompilationSubject.assertThat(compilation).generatedSourceFile("test.TestInjectionHelper")
        .hasSourceEquivalentTo(expectedOutput)
  }
}