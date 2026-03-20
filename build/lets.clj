(ns build.lets
  (:require
   [clojure.string :as str]
   [clojure.tools.build.api :as b]
   [babashka.fs :as fs])
  (:import
   [java.io File]))

(defonce deps-basis (delay (b/create-basis {:project "deps.edn" :aliases [:release]})))
(defonce repl-dev-basis (delay (b/create-basis {:project "deps.edn" :aliases [:repl-dev]})))
(def target-dir (delay (or (-> @deps-basis :jank :target-dir) "target")))

(defn log [& stuff]
  (apply println "[bermain@jank]" stuff))

;; some manual selfbuild
(def selfbuild-home "../.selfbuild")

;; https://mccue.dev/pages/12-26-24-sdl3-java
(defn build-sdl3
  [{}]
  (let [sdl-home  (str selfbuild-home "/sdl3")
        sdl-build (str sdl-home "/build")]
    (b/process {:command-args ["git" "clone" "--depth" "1" "https://github.com/libsdl-org/SDL" sdl-home] :out :inherit :err :inherit})
    (fs/create-dirs sdl-build)
    (b/process {:dir sdl-build :command-args ["cmake" "-DCMAKE_BUILD_TYPE=Release" ".."] :out :inherit :err :inherit})
    (b/process {:dir sdl-build :command-args ["cmake" "--build" "." "--config" "Release" "--parallel"] :out :inherit :err :inherit})
    ;; workaround, reported here https://github.com/ikappaki/jank-win/issues/37
    (fs/move (str sdl-build "/Release/SDL3.dll") (str sdl-build "/Release/libSDL3.dll"))
    (log "build-sdl3 done")))

(defn check [& _]
  (prn @deps-basis))

(defn workaround [& _]
  (let [sdl-home  (str selfbuild-home "/sdl3")
        sdl-build (str sdl-home "/build")]
    (fs/copy (str sdl-build "/Release/libSDL3.dll") (str @target-dir "/SDL3.dll") {:replace-existing true})
    (log "workaround done")))

;; jank pseudo-deps-edn, powered by https://github.com/babashka/tools.bbuild

(defn ->jank-deps-edn [basis]
  (let [{:keys [jank classpath]} basis]
    (assoc jank :module-path (str/join File/pathSeparator (keys classpath)))))

(defn get-main-module [deps-edn]
  (-> deps-edn :argmap :main str))

(defn ->flags [flag coll]
  (into [] (mapcat (fn [entry] [flag entry])) coll))

(defn jank-command [jank-deps-edn command {:keys [main-module extra]}]
  (let [{:keys [module-path include-dirs library-dirs linked-libraries]} jank-deps-edn]
    (into []
          (remove nil?)
          (concat
           ["jank" "--module-path" module-path]
           (->flags "-I" include-dirs)
           (->flags "-L" library-dirs)
           (->flags "-l" linked-libraries)
           [command main-module]
           extra))))

(def shader-home "shaders")

(defn compile-shaders
  [{}]
  (let [shaders
        (eduction
         (filter (every-pred fs/regular-file?
                             #(or (str/ends-with? % ".frag.glsl")
                                  (str/ends-with? % ".vert.glsl"))))
         (map (fn [shader-file]
                (let [[shader-name shader-type] (str/split (fs/file-name shader-file) #"\.")]
                  [(fs/parent shader-file) (str shader-file) shader-name shader-type])))
         (file-seq (fs/file shader-home)))]
    (doseq [[parent shader-file shader-name shader-type] shaders]
      (let [spv-out  (str parent "/" shader-name ".spv")
            -fshader (case shader-type
                       "vert" "-fshader-stage=vertex"
                       "frag" "-fshader-stage=fragment")]
        (println "compiling to" spv-out)
        (b/process {:command-args ["glslc" -fshader shader-file "-o" spv-out]})))))

(defn clean-shaders 
  [{}]
  (doseq [spv (eduction
               (filter (every-pred fs/regular-file? #(str/ends-with? % ".spv")))
               (file-seq (fs/file shader-home)))]
    (println "deleting" spv)
    (fs/delete spv)))

(defn prep-kondo
  [{}]
  (try
    (let [{:keys [module-path]} (->jank-deps-edn @deps-basis)]
      (b/process {:command-args ["clj-kondo" "--lint" module-path
                                 "--dependencies" "--copy-configs" "--skip-lint"]}))
    (catch Throwable err
      (log "error when running clj-kondo! cause:" (:cause (Throwable->map err))))))

(defn prep [args]
  (prep-kondo args)
  (build-sdl3 args)
  (compile-shaders args))

(defn clean [& _]
  (println "cleaning target...")
  (b/delete {:path @target-dir}))

(defn jank
  {:org.babashka/cli {:exec-args {:args nil}}}
  [{:keys [args]}]
  (log "setting up jank pseudo deps.edn project" args)
  (let [jedn (->jank-deps-edn @deps-basis)
        jcmd (jank-command jedn "run-main" {:main-module (get-main-module @deps-basis)
                                            :extra ["--" args]})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn repl-dev
  [{}]
  (let [jedn (->jank-deps-edn @repl-dev-basis)
        jcmd (jank-command jedn "repl" {:main-module (get-main-module @repl-dev-basis)})]
    (log jcmd)
    (b/process {:command-args jcmd})))

(defn compile-jank
  [{}]
  (let [jedn (->jank-deps-edn @deps-basis)
        jout (str @target-dir "/" (-> @deps-basis :jank :compile-out))
        jcmd (jank-command jedn "compile" {:main-module (get-main-module @deps-basis) :extra ["-o" jout]})]
    (log jcmd)
    (b/process {:command-args jcmd})))
