(ns pane.core
  (:require [seesaw.core :as ss]
            [seesaw.dev :refer (show-events show-options)])
  (:import [javax.swing UIManager]
           [javax.swing.event DocumentEvent]
           [org.pushingpixels.radiance.theming.api.skin RadianceTwilightLookAndFeel]))

(defn -main [& _]
  (ss/native!)
  (ss/invoke-later
    (UIManager/setLookAndFeel (RadianceTwilightLookAndFeel.))
    (let [file-search (ss/text :editable? true)
          list-box (ss/listbox :model ["file 1" "file 2"])
          v-panel (ss/vertical-panel :items [file-search list-box])
          frame (ss/frame :title "Pane"
                          :content v-panel
                          :on-close :dispose)]
      (def *frame* frame)
      (-> frame
          (ss/pack!)
          (ss/move-to! 0 0)
          ss/show!))))

(comment
  (ss/config! (ss/select *frame* [:JTextField]))
  (ss/invoke-later
   (let [new-panel (ss/vertical-panel :items [(ss/text :text "Hello world")])]
     (ss/config! *frame* :content new-panel)))
  (.getContentPane *frame*)
  (ss/config (first (ss/select *frame* [:JTextField])) :text)
  (ss/config *frame* :content)
  (-main)
  )