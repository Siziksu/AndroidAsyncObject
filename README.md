# AndroidAsyncObject

Object to do async calls in Android.

## How it works

This objest uses 3 main methods, `action()`, `done()` and `subscribe()`. The `action()` method is mandatory, the other ones are optional.

This object will run the `action()` method in a new thread. Once finished, it will give feedback through the `done()` method. If we want to get track of the response and the possible errors we will need to use the `subscribe()` method.

The `subscribe()` method executes internally the `run()` method. If you don't use the `subscribe()` method, you must use the `run()` method.

## Functional Interfaces used

This object can run actions in background using threads. It uses 3 Functional Interfaces:

- Provider
```java
public interface Provider<O> {

    void provide(O object);
}
```
- Consumer
```java
public interface Consumer {

    void consume();
}
```
- Function
```java
public interface Function<O> {

    O execute() throws Exception;
}
```

## Examples

```java
new AsyncObject<Void>()
                .action(() -> {
                    Log.d(Constants.TAG, "AsyncObject");
                    return null;
                })
                .run();
```

```java
new AsyncObject<String>()
                .action(() -> "AsyncObject")
                .done(() -> Log.d(Constants.TAG, "Request finished"))
                .subscribe(
                        response -> Log.d(Constants.TAG, "Response: " + response),
                        e -> Log.d(Constants.TAG, e.getMessage(), e)
                );
```

## License
    Copyright 2016 Esteban Latre

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
