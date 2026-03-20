# bermain.game

> `bermain` is an indonesian word for `play`

prereq:
- [jank (currently using not-yet upstreamed windows port)](https://github.com/ikappaki/jank-win)
- and jank's cpp toolchain (windows: [msys2](https://www.msys2.org/), untested in other OS)
- [babashka](https://book.babashka.org/#getting_started)
- [Vulkan SDK](https://vulkan.lunarg.com/) to precompile shaders
- and more listed in `bb prep` section

this jank project uses deps.edn via [tools.bbuild](https://github.com/babashka/tools.bbuild) (this deps should be automatically fetched by bb on the first call)

preparation (need to run only once)
```sh
bb prep
# this does
bb lets prep-kondo # this shells out to clj-kondo (optional, this fetch the configs from dependencis via --copy-configs)
bb lets build-sdl3 # this git clone SDL3 repo to an adjacent folder, then shells out to whatever SDL3 needs to build
bb lets compile-shaders # this shells out to Vulkan SDK's `glslc`, and compile everything in `shaders` folder
```

devel
```sh
# run-main
bb jank

# repl-dev
bb repl-dev
# and calva connect

# aot compile
bb lets workaround
bb compile

./target/bermain.exe
# ayo bermain jank!

# about workaround:
# when running with run-main, jank require `lib` prefix for .dll files, while the compiled one require no `lib` prefix
# reported here: https://github.com/ikappaki/jank-win/issues/37
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
