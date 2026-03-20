# bermain.game

prereq:
- [jank](https://book.jank-lang.org/getting-started/01-installation.html)
- and jank's cpp toolchain (windows: [msys2](https://www.msys2.org/))
- [babashka](https://book.babashka.org/#getting_started)
- [Vulkan SDK](https://vulkan.lunarg.com/) 

preparation (need to run only once)
```sh
bb prep
# this does
bb lets prep-kondo
bb lets build-sdl3
bb lets compile-shaders
```

devel
```sh
# run-main
bb jank

# repl-dev
bb repl
# and calva connect

# aot compile
bb lets workaround
bb compile

./target/bermain.exe
# ayo bermain jank!

# about workaround:
# when running with run-main, jank require `lib` prefix for .dll files, while the compiled one require no `lib` prefix
```

cleaning
```sh
bb lets clean
bb lets clean-shaders
```

## note on wrong expectation

while tinkering, these were some aspect about jank that took me a bit of time to figure out.

- #cpp float values

```clojure
; I thought below would work
(cpp/SDL_FColor #cpp 1.0 #cpp 1.0 #cpp 1.0 #cpp 1.0)
; but SDL_FColor require floats, and clojure is double by default
(cpp/SDL_FColor (cpp/float 1.0) (cpp/float 1.0) (cpp/float 1.0) (cpp/float 1.0))
```

- I thought `.` was necessary

```clojure
(cpp/SDL_Event.)
(cpp/SDL_Event)
; is the same?
```
