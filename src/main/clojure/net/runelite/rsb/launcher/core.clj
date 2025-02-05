(ns net.runelite.rsb.launcher.core
  (:require [clj-http.client :as client]
            [clojure.java.io :as io] [clojure.string :as str] 
            [cemerick.pomegranate :as pomegranate]
            [cemerick.pomegranate.aether :as aether]
            )
  (:import [net.runelite.rsb.botLauncher Application])
  (:gen-class))

(defn get-runelite-version [] 
  (let [url "http://repo.runelite.net/net/runelite/client/maven-metadata.xml"
        response (client/get url {:as :stream})]
          (with-open [reader (io/reader (:body response))]
            (some  #(when (str/includes? % "<release>")
                (str/trim (str/replace % #"</?release>" "")))
                   (line-seq reader)))))

(defn add-dependency [group-id artifact-id version]
  (let [current-thread (Thread/currentThread)
        context-loader (.getContextClassLoader current-thread)]
    (try
      (.setContextClassLoader current-thread (clojure.lang.DynamicClassLoader. context-loader))
      (pomegranate/add-dependencies
       :coordinates [[(symbol (str group-id "/" artifact-id)) version]]
       :repositories (merge aether/maven-central
                            {"clojars" "https://repo.clojars.org/"
                             "runelite" "https://repo.runelite.net"}))
      (finally
        (.setContextClassLoader current-thread context-loader)))))

(defn handle-deps []
  (add-dependency "net.runelite" "client" (get-runelite-version))
  (add-dependency "net.runelite" "cache" (get-runelite-version))
  (add-dependency "org.projectlombok" "lombok" "1.18.24")
  (add-dependency "javassist" "javassist" "3.12.1.GA")
  (add-dependency "net.sf.jopt-simple" "jopt-simple" "5.0.4")
  )

(defn -main [& args] 
  #_(handle-deps) ;; This will be used when we convert ALL code from Java and can do runtime compilation only
  (Application/main (into-array String args))
  )