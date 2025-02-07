(ns net.runelite.rsb.launcher.application
  (:require [clojure.core.async :as async :refer [chan go <! <!! >!! close!]]
            [clojure.string :as str]
            [net.runelite.rsb.launcher.botlite :as botlite])
  (:import            #_[net.runelite.rsb.botLauncher BotLite]))

(def bots [])

(defn get-classloader
  "Gets the classloader of an object."
  [obj]
  ((.getClassLoader (.getClass obj))))

(defn get-bot
  "Retrieves the Bot for any object loaded in its client."
  [obj]
  (doseq [bot bots]
    (when (== (get-classloader obj) (get-classloader bot))
      bot)))

(defn add-bot
  ""
  [show-ui]
  (let [bot (botlite/create-botlite-instance show-ui)]
    (println bot)
    (println "Test")
    #_(.launch bot (into-array String ["--bot-runelite" "--developer-mode"]))
    #_(conj bots bot)))

(defn command-line-listener
  "Handles command-line interfacing with the client.
   The REPL is likely to be more immediately useful in most cases."
  []
  (go (loop [command (str/trim (read-line))]
        (case (first (str/split (str/lower-case command) #" "))
          "runscript" (println "runscript")
          "stopscript" (println "stopscript")
          "addbot" (do
                     (println "Adding bot")
                     (add-bot true))
          "checkstate" (println "checkstate")
          (println "Invalid command"))
        (recur (str/trim (read-line))))))

(defn start
  "Parses command-line arguments passed to the program and then passes the remaining 
   to the command-line-listener method to continue handling the next inputs if applicable."
  [args]
  (println args)
  (cond
    (contains? args "--bot-runelite")
    (do
      (add-bot true)
      (command-line-listener))
    (contains? args "--salsa-bowl")
    (println "Random Behavior")
    #_(println "S")
    #_(.main Runelite (into-array String args))))

