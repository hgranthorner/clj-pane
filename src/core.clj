(ns core
  (:require [seesaw.core :as ss]
            [seesaw.dev :refer (show-events show-options)])
  (:import [javax.swing UIManager]
           [javax.swing.event DocumentEvent]
           [org.pushingpixels.radiance.theming.api.skin RadianceTwilightLookAndFeel]))

(defn -main [& _]
  (ss/native!)
  (ss/invoke-later
   (UIManager/setLookAndFeel (RadianceTwilightLookAndFeel.))
   (let [text-input (ss/text :editable? true)
         btn (ss/button :text "push me"
                        :id :the-button)
         list-box (ss/listbox :model ["This" "is" "a" "vertical" "stack of" "JLabels"])
         v-panel (ss/vertical-panel
                  :items [text-input
                          list-box
                          btn
                          (ss/combobox :model ["a" "b" "c"]
                                       :editable? true)])]
     (ss/listen btn
                :action (fn [_]
                          (.addElement
                           (.getModel list-box)
                           "another thing")))

     (ss/listen text-input
                :document (fn [^DocumentEvent e]
                            (let [d (.getDocument e)]
                              (println (.getText d 0 (.getLength d))))))
     (->
      (ss/frame
       :title "Example"
       :content v-panel
       :on-close :dispose)
      ss/pack!
      (ss/move-to! 0 0)
      ss/show!))))

(comment
  (-main)
  (show-events (ss/text))
  (show-options (ss/frame))
  (show-options (ss/vertical-panel)))