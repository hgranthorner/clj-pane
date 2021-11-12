(ns pane.core
  (:require [seesaw.core :as ss]
            [seesaw.dev :refer (show-events show-options)]
            [pane.indexer :as indexer]
            [clojure.core.async :as async]
            [clojure.string :as s])
  (:import [javax.swing UIManager DefaultListModel]
           [javax.swing.event DocumentEvent]
           [org.pushingpixels.radiance.theming.api.skin RadianceTwilightLookAndFeel]))

(def *state (atom {:files #{} :indexing? true}))

(defn- select-first
  "Helper for selecting the first element by class."
  [frame kw]
  (first (ss/select frame [kw])))

(defn load-files-async!
  "Asynchronously loads all executables under the starting directory to the passed in atom."
  [*atom starting-directory]
  (future
    (let [channel (async/chan 1000)]
      (indexer/bfs-file-seq-async channel starting-directory)
      (loop [files (async/<!! channel)]
        (if-not (nil? files)
          (do
            (swap! *atom (fn [x] (update x :files #(into % files))))
            (recur (async/<!! channel)))
          (swap! *atom (fn [x] (update x :indexing? not))))))))

(defn -main [& _]
  (println "Starting counting!")
  (let [fut (load-files-async! *state "/")]
    (ss/native!)
    (ss/invoke-later
     (UIManager/setLookAndFeel (RadianceTwilightLookAndFeel.))
     (let [file-search (ss/text :editable? true)
           list-box (ss/listbox :model ["file 1" "file 2"])
           scroll (ss/scrollable list-box)
           btn (ss/button :text "Cancel Index")
           v-panel (ss/vertical-panel :items [file-search scroll btn])
           frame (ss/frame :title "Pane"
                           :content v-panel
                           :on-close :dispose)]
       (ss/listen btn :action
                  (fn [_] (future-cancel fut)))
       (ss/listen file-search
                  :key-released
                  (fn [_]
                    (let [txt (ss/config file-search :text)]
                      (println txt))))
       (def ^:dynamic *frame* frame)
       (-> frame
           (ss/pack!)
           (ss/move-to! 0 0)
           ss/show!)))))

(comment
  (-main)
  (type (ss/listbox))
  (count (:files @*state))
  (filter (comp (partial s/includes? "node") str) (:files @*state))
  (type *state)
  (ss/config (first (ss/select *frame* [:JTextField])) :text)
  (let [text-field (first (ss/select *frame* [:JTextField]))
        list-box (first (ss/select *frame* [:JList]))]
    (ss/listen text-field :key-released (fn [_] (println (ss/config text-field :text)))))
  (let [text-field (first (ss/select *frame* [:JTextField]))]
    (doseq [l (.getKeyListeners text-field)]
      (.removeKeyListener text-field l)))

  (let [list-box (select-first *frame* :JList)
        model (DefaultListModel.)
        search (ss/config (select-first *frame* :JTextField) :text)
        xf (comp (filter #(s/includes? % search)) (map #(str (.getName %) " | " %)))
        results (take 10 (sequence xf (:files @*state)))]
    (ss/invoke-later
     (doseq [x (sort results)]
       (.addElement model x))
     (.setModel list-box model))
     (ss/pack! *frame*))

  (take 10 (filter #(s/includes? % "n") (map #(.getName %) (:files @*state))))
  (ss/invoke-later (ss/pack! *frame*))
  (ss/invoke-later
   (let [new-panel (ss/vertical-panel :items [(ss/text :text "Hello world")])]
     (ss/config! *frame* :content new-panel)))
  (.getContentPane *frame*)
  (ss/config (first (ss/select *frame* [:JTextField])) :text)
  (ss/config *frame* :content)
  (show-events (ss/text))
  ;; (show-options (ss/scrollable))
  (-main)
  )