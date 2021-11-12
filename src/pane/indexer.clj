(ns pane.indexer
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async :refer (go go-loop <! >! <!! >!!)]
            [clojure.string :as s])
  (:import [clojure.lang PersistentQueue]))

(def folders-to-ignore #{"node_modules" ".svn" ".git" ".hg" "CVS" ".Trash"})

(defn separate-exes-and-dirs [^java.io.File folder]
  (let [files (.listFiles folder)]
    (reduce (fn [acc file]
              (if (and (.isDirectory file) (not (contains? folders-to-ignore (last (s/split (str file) #"/")))))
                (update acc :dirs #(conj % file))
                (if (.canExecute file)
                  (update acc :exes #(conj % file))
                  acc)))
            {:exes [] :dirs []} files)))

(defn bfs-file-seq-async [channel starting-directory]
  (go-loop [^PersistentQueue queue (conj (PersistentQueue/EMPTY) (io/file starting-directory))]
    (let [folder (peek queue)
          {exes :exes dirs :dirs} (separate-exes-and-dirs folder)
          new-queue (into (pop queue) dirs)]
      (when-not (empty? exes)
        (async/>! channel exes))
      (if (empty? new-queue)
        (async/close! channel)
        (recur new-queue)))))

(comment
  (separate-exes-and-dirs (io/file "/usr/local/lib/erlang"))
  (let [d (async/chan 1000)]
    (bfs-file-seq-async d "/usr/local/lib/erlang")
    (loop [f (<!! d)]
      (println (str f))
      (when-not (nil? f)
        (recur (<!! d)))))
  (let [channel (async/chan)]
    (go (async/onto-chan! channel ["a" "b" "c"]))
    (println (<!! channel))
    (println (<!! channel))
    (println (<!! channel)))
  (let [starting-folder "/usr/local/lib/erlang"
        channel (async/chan 50)]
    (bfs-file-seq-async channel starting-folder)
    (loop [file (str (<!! channel))]
      (println file)
      (if-not (nil? file)
        (recur (str (<!! channel))))))
  (let [q (conj (clojure.lang.PersistentQueue/EMPTY) "abc")]
    (pop q)
    (pop q))
  (type (async/chan))
  (let [c (async/chan)]
    (future
      (for [f (file-seq (io/file "/usr"))]
        (do
          (println (str "putting" f "onto chan"))
          (>!! c f))))
    (loop [val (<!! c)]
      (println (str "taking" val "off chan"))
      (recur (<!! c))))
  (+ 1 1)
  (take
   10
   (map str
        (filter #(and (not (.isDirectory %)) (.canExecute %))
                (file-seq
                 (io/file "/Applications")))))
  )
