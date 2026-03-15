# bermain.game

need jank + babashka, tested only in windows 

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
bb compile

./target/bermain.exe
# ayo bermain jank!

# clean
bb lets clean
```



