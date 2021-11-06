(ns core
  (:require [seesaw.core :as ss]
            [seesaw.dev :refer (show-events show-options)])
  (:import [javax.swing UIManager]
           [org.pushingpixels.radiance.theming.api.skin RadianceTwilightLookAndFeel]))

(show-events (ss/label))

(defn -main [& _]
  (ss/native!)
  (ss/invoke-later
   (UIManager/setLookAndFeel (RadianceTwilightLookAndFeel.))
   (let [btn (ss/button :text "push me"
                        :id :the-button)
         list-box (ss/listbox :model ["This" "is" "a" "vertical" "stack of" "JLabels"])
         v-panel (ss/vertical-panel :items [list-box btn])]
     (ss/listen
      btn
      :action
      (fn [_]
        (.addElement (.getModel list-box) "another thing")))
     (->
      (ss/frame
       :title "Example"
       :content v-panel)
      ss/pack!
      ss/show!))))

(comment
  (-main)
  (show-options (ss/vertical-panel))
  (show-options (ss/button))
  (show-options (ss/action)))


