(defn get-runelite-version []
  ())

(defproject OSRSB "0.0.1"
  :description "A revised bot client on RuneLite (for now)"
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [clj-http "3.12.3"]
                 [org.clojure/core.async "1.7.701"]
                 [com.cemerick/pomegranate "1.1.0"]
                 [net.runelite/client "1.10.49"]
                 [net.runelite/cache "1.10.49"]
                 #_[org.projectlombok/lombok "1.18.32"]
                 #_[javassist/javassist "3.12.1.GA"]
                 #_[net.sf.jopt-simple/jopt-simple "5.0.4"]
                 #_[com.github.joonasvali.naturalmouse/naturalmouse "2.0.3"]
                 #_[com.github.OSRSB/OSRSBPlugin "main-SNAPSHOT"]]
  :repositories {"runelite" "https://repo.runelite.net"
                 "jitpack" "https://jitpack.io"
                 "central" "https://repo1.maven.org/maven2/"}
  :jvm-opts ["--add-opens=java.base/java.lang=ALL-UNNAMED"]
  :source-paths ["src/main/clojure"]
  #_:java-source-paths #_["src/main/java"]
  :javac-options ["-processor" "lombok.launch.AnnotationProcessorHider$AnnotationProcessor"]
  :main net.runelite.rsb.launcher.core)