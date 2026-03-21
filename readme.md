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
bb lets download-sdl3 # download SDL3 release to a local `.libs` folder, then unzip
bb lets download-sdl3-image # download SDL3_image release to a local `.libs` folder, then unzip
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

## problem we encountered

1. we can't include `stb_image.h`. need some small repro case, we got some weird error like 

```sh
JIT session error: Symbols not found: [ __emutls_get_address ]
```

huh, the above solved with
https://clojurians.slack.com/archives/C03SRH97FDK/p1757880484171019?thread_ts=1757879532.260299&cid=C03SRH97FDK

2. `SDL_image.h` seems to be able to load but `IMG_Load` error with

```sh
JIT session error: Symbols not found: [ IMG_Load ]
─ internal/failure ─────────────────────────────────────────────────────────────────────────────────
error: Failed to find symbol: 'minusthree_platform_sdl3_sdl3_start_2_0'                             


Stack trace (most recent call first):
#0  in ZN4jank5error16internal_failureERKN3jtl16immutable_stringE at jank.exe
#1  in ZN4jank5error22throw_internal_failureERKN3jtl16immutable_stringE at jank.exe
#2  in ZNSt3__112__hash_tableINS_17__hash_value_typeIN3jtl16immutable_stringEN4jank7runtime6module6loader5entryEEENS_22__unordered_map_hasherIS3_NS_4pairIKS3_S8_EENS_4hashIS3_EENS_8equal_toIS3_EELb1EEENS_21__unordered_map_equalIS3_SD_SH_SF_Lb1EEE12gc_allocatorISD_EE4findIS3_EENS_15__hash_iteratorIPNS_11__hash_nodeIS9_PvEEEERKT_ at jank.exe
#3  in ZNK4jank3jit9processor15create_functionEhRKN3jtl16immutable_stringERKN5folly8fbvectorIh12gc_allocatorIhEEE at jank.exe
#4  in ZNK4jank7runtime3obj21deferred_cpp_function4callENS0_4orefINS0_6objectEEE at jank.exe
#5  in ZN4jank7runtime8apply_toENS0_4orefINS0_6objectEEES3_ at jank.exe
#6  in ZN4jank7runtime8apply_toENS0_4orefINS0_6objectEEES3_ at jank.exe
#7  in main at jank.exe
#8  in jank_init_with_pch at jank.exe
#9  in jank_init at jank.exe
#10 at jank.exe
#11 at jank.exe
#12 in BaseThreadInitThunk at KERNEL32.DLL
#13 in RtlUserThreadStart at ntdll.dll
```

if we deliberatelu run `IMG_Load` with wrong arity, jank knows about `IMG_Load` and throws arity error

```sh
libunwind:      pc not in table, pc=0x269D22F2926
─ analyze/invalid-cpp-call ─────────────────────────────────────────────────────────────────────────
error: No matching call to 'IMG_Load' function. With argument 0 having type                         
       'jank::runtime::oref<jank::runtime::object> &'. With argument 1 having type                  
       'jank::runtime::oref<jank::runtime::object> &'.                                              
─────┬──────────────────────────────────────────────────────────────────────────────────────────────
     │ src\minusthree\platform\sdl3\sdl3.jank
─────┼──────────────────────────────────────────────────────────────────────────────────────────────
 36  │
 37  │    ;; "resources/horse/running/horse-1.png" does not exist, want to see error
 38  │    (let [img (must-return (cpp/IMG_Load "resources/horse/running/horse-1.png" 1))]
     │                            ^^^^^^^^^^^^ Found here.
     │              ^ Expanded from this macro.
```

plus, if we AOT compile, IMG_Load seems to run just fine:

the following is deliberate passing file that doesn't exist
```sh
[sdl3 error] Error on cpp/IMG_Load , error from sdl: Couldn't open resources/horse/running/horse-1.png: The system cannot find the file specified.
Uncaught exception: {:error "Error on cpp/IMG_Load", :data {}, :cause {:data "Couldn't open resources/horse/running/horse-1.png: The system cannot find the file specified."}}

Stack trace (most recent call first):
#0  in _gxx_personality_seh0 at libc++.dll
#1  in GCC_specific_handler at libc++.dll
#2  in _gxx_personality_seh0 at libc++.dll
#3  in _chkstk at ntdll.dll
#4  in RtlLocateExtendedFeature at ntdll.dll
#5  in RtlRaiseException at ntdll.dll
#6  in RaiseException at KERNELBASE.dll
#7  in Unwind_RaiseException at libc++.dll
#8  in _cxa_throw at libc++.dll
#9  in minusthree_platform_sdl3_common_throw_sdl_error_975_1 at bermain.exe
#10 in minusthree_platform_sdl3_sdl3_start_582_1 at bermain.exe
#11 in ZN4jank7runtime8apply_toENS0_4orefINS0_6objectEEES3_ at bermain.exe
#12 in ZN4jank7runtime8apply_toENS0_4orefINS0_6objectEEES3_ at bermain.exe
#13 in main at bermain.exe
#14 in jank_init_with_pch at bermain.exe
#15 in main at bermain.exe
#16 in __tmainCRTStartup at crtexe.c:236
#17 in mainCRTStartup at crtexe.c:122
#18 in BaseThreadInitThunk at KERNEL32.DLL
#19 in RtlUserThreadStart at ntdll.dll
```
