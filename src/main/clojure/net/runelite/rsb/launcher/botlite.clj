(ns net.runelite.rsb.launcher.botlite
  (:import [net.runelite.client.modified RuneLite]))

(defn init []
  (proxy [RuneLite] []))