package io.dwak.injectionhelper.sample;

import javax.inject.Inject;

public class Foo {
    @Inject Bar bar;

    String method() {
        return bar.doSomething();
    }
}
