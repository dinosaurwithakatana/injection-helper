package io.dwak.injectionhelper.sample;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FooTest {

    @Test
    public void methodPass() throws Exception {
        Foo foo = new Foo();
        FooInjectionHelper.inject(foo, new Bar(){
            @Override
            String doSomething() {
                return "NewSomething";
            }
        });

        assertThat(foo.method()).isEqualTo("NewSomething");
    }

    @Test
    public void methodPass2() throws Exception {
        Foo foo = new Foo();
        FooInjectionHelper.inject(foo, new Bar());

        assertThat(foo.method()).isEqualTo("Something");
    }

    @Test
    public void methodFail2() throws Exception {
        Foo foo = new Foo();
        FooInjectionHelper.inject(foo, new Bar() {
            @Override
            String doSomething() {
                return "lol";
            }
        });

        assertThat(foo.method()).isNotEqualTo("DoSomething");
    }
}