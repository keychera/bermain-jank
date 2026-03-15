# bermain.game

need jank + babashka, tested only in windows. most likely also require c++ compiler toolchain.

preparation (need to run only once)
```
bb lets build-sdl3
bb prep
```

devel
```sh
# run-main
bb jank

# run jank repl (jepl)
bb jepl

# aot compile
bb lets workaround
bb compile

./target/bermain.exe
# ayo bermain jank!

# about workaround:
# when running with run-main, jank require `lib` prefix for .dll files, while the compiled one require no `lib` prefix

# clean
bb lets clean
```
