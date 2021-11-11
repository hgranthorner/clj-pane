(ns pane.indexer
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async :refer (go <! >! <!! >!!)]))

(def folders-to-ignore #{"node_modules" ".svn" ".git" ".hg" "CVS" ".Trash"})

(defn- ignore? [^java.io.File file]
  (and
   (not (.isDirectory file))
   (.canExecute file)
   (not (contains? folders-to-ignore (str file)))))

(defn find-executables []
  (filter ignore? (file-seq (io/file "/"))))

(defn find-executables-async
  "Find executables on machine, then call passed in callback function with the new value of the state."
  [^clojure.lang.Atom *atom f]
  (future
    (let [files (find-executables)]
      (reset! *atom {:indexing? false
                     :files files})
      (f *atom))))

(comment

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

  (first (filter ignore? (file-seq (io/file "/"))))
  )