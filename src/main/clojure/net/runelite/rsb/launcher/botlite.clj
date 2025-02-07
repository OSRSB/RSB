(ns net.runelite.rsb.launcher.botlite
  (:require [clojure.core.async :as async :refer [chan go >! <! >!! close!]]
            [clj-http.client :as client]
            [clojure.java.shell :refer [sh]]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [cemerick.pomegranate :as pomegranate]
            [cemerick.pomegranate.aether :as aether])
  (:import
   [clojure.lang  DynamicClassLoader]
   [java.io File]))

(defn classloader-classpath [classloader]
  (->> (seq (.getURLs classloader))
       (map #(.getPath %))
       (clojure.string/join ":")))

(defn get-runelite-version []
  (let [url "http://repo.runelite.net/net/runelite/client/maven-metadata.xml"
        response (client/get url {:as :stream})]
    (with-open [reader (io/reader (:body response))]
      (some  #(when (str/includes? % "<release>")
                (str/trim (str/replace % #"</?release>" "")))
             (line-seq reader)))))

(defn add-dependency [classloader group-id artifact-id version]
  (pomegranate/add-dependencies
   :classloader classloader
   :coordinates [[(symbol (str group-id "/" artifact-id)) version]]
   :repositories (merge aether/maven-central
                        {"clojars" "https://repo.clojars.org/"
                         "jitpack" "https://jitpack.io"
                         "central" "https://repo1.maven.org/maven2/"
                         "runelite" "https://repo.runelite.net"})))


(defn find-lombok-jar [classloader]
  (->> (seq (.getURLs classloader))
       (map #(.getPath %))
       (filter #(re-matches #".*lombok-.*\.jar$" %))
       first))


(defn compile-java-files [classloader]
  (spit "java-files.txt"
        (str/join "\n"
                  (map #(.getPath %)
                       (filter #(str/ends-with? (.getName %) ".java")
                               (file-seq (io/file "src/main/java"))))))
  (let [lombok-jar (find-lombok-jar classloader)
        processorpath (if lombok-jar
                        lombok-jar
                        (throw (Exception. "Lombok JAR not found on classpath")))]
    (sh "javac" "-d" "output" "-cp" (classloader-classpath classloader) "-processorpath" processorpath "@java-files.txt")))

(defn handle-deps [classloader]
  (add-dependency classloader "net.runelite" "client" (get-runelite-version))
  (add-dependency classloader "net.runelite" "cache" (get-runelite-version))
  (add-dependency classloader "org.clojure" "clojure" "1.10.3")
  (add-dependency classloader "org.slf4j" "slf4j-simple" "1.7.36")
  (add-dependency classloader "org.projectlombok" "lombok" "1.18.32")
  (add-dependency classloader "com.github.joonasvali.naturalmouse" "naturalmouse" "2.0.3")
  (add-dependency classloader "com.github.OSRSB" "OSRSBPlugin" "main-SNAPSHOT")
  (add-dependency classloader "javassist" "javassist" "3.12.1.GA")
  (add-dependency classloader "net.sf.jopt-simple" "jopt-simple" "5.0.4"))

(defn instantiate-class [lc]
  (.newInstance lc))

(defn get-injector [lc]
  (let [injector (.getDeclaredField lc "injector")]
    (.setAccessible injector true)
    (.get injector nil)))

(def output-dir (File. "output"))

(defn list-all-dirs [dir]
  (let [dirs (file-seq dir)]
    (filter #(.isDirectory %) dirs)))

(defn add-dirs-to-classloader [classloader dirs]
  (doseq [dir dirs]
    (let [url (.toURL (.toURI dir))]
      (.addURL classloader url))))

(defn create-botlite-instance [show-ui]
  (let [class-name "net.runelite.rsb.botLauncher.BotLite"
        class-loader (DynamicClassLoader. (ClassLoader/getSystemClassLoader))
        deps (handle-deps class-loader)
        outputDeps (add-dirs-to-classloader class-loader (list-all-dirs output-dir))
        javaDeps (compile-java-files class-loader)
        loaded-class (.loadClass class-loader class-name)
        emptylite (instantiate-class loaded-class)
        test (.launch emptylite (into-array String ["--bot-runelite" "--developer-mode"]))
        botlite (.getInjectorInstance emptylite)]
    #_(.launch botlite (into-array String ["--bot-runelite" "--developer-mode"]))
      (.init botlite true)
      {:class-loader class-loader
       :loaded-class loaded-class
       :class-name class-name
       :empty-class emptylite
       :botlite botlite}))