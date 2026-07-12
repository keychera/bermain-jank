# bermain.game

> `bermain` is an indonesian word for `play`

prereq:
- [jank](https://github.com/jank-lang/jank)
- [babashka](https://book.babashka.org/#getting_started)
- [Vulkan SDK](https://vulkan.lunarg.com/) to precompile shaders
- and more listed in `bb prep` section

this jank project uses deps.edn via [tools.bbuild](https://github.com/babashka/tools.bbuild) (this deps should be automatically fetched by bb on the first call)

preparation (need to run only once)
```sh
bb prep
# this does
bb lets prep-kondo # this shells out to clj-kondo (optional, this fetch the configs from dependencis via --copy-configs)
bb lets download-sdl3 # download SDL3 release to a local `.libs` folder, then unzip
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
bb compile

./target/bermain.exe
# ayo bermain jank!
```

cleaning
```sh
bb lets clean
bb lets clean-shaders
```

# catching up

## error 1

jank print-binary-version
x86_64-w64-windows-gnu-a20d44e2d447c35eb9da21888fdf1b2b152d81a3591594cd1b2610c1123c7da4

when building we encountered error on running `./bin/compile`

```
compiler+runtime/src/cpp/clojure/data/json_native.cpp:1:10: fatal error: 'nlohmann/json.hpp' file not found
    1 | #include <nlohmann/json.hpp>

```

solution:
pacman -S mingw-w64-clang-x86_64-nlohmann-json

## error raylib blocker

we wanted to try out raylib but blocked by: https://github.com/raysan5/raylib/issues/1217

## error 2

```clojure
(defmacro must-be-true
  ([form] (must-be-true form nil))
  ([form callback]
   (let [first-sym (str (first form))]
     `(when (not ~form)
        ~callback
        (throw-sdl-error (str "Error on " ~first-sym))))))
```

internal error
```
JIT session error: Symbols not found: [ bermain_sdl3_common_must_be_true_12_2 ]
─ analyze/macro-expansion-exception ────────────────────────────────────────────────────────────────
error: Uncaught exception while expanding macro.                                                    

─────┬──────────────────────────────────────────────────────────────────────────────────────────────
     │ src\bermain\sdl3\sdl3.jank
─────┼──────────────────────────────────────────────────────────────────────────────────────────────
  8  │ (declare poll-events cleanup)
  9  │
 10  │ (defn main []
     │ ^ Expanded from this macro.
 11  │   (must-be-true (cpp/SDL_Init (bit-or cpp/SDL_INIT_VIDEO cpp/SDL_INIT_GAMEPAD)))
     │   ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ Found here. 
─────┴──────────────────────────────────────────────────────────────────────────────────────────────
─ internal/failure ─────────────────────────────────────────────────────────────────────────────────
error: Failed to find symbol: 'bermain_sdl3_common_must_be_true_12_4'  
```

## it's magick!

source: https://opengameart.org/content/animated-horse

```sh
magick HorseRun_4.gif -coalesce +append horserun.png
```

