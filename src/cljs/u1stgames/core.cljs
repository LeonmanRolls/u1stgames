(ns u1stgames.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def
  app-state
  (atom {:games [{:img "/img/u1st_logo_square.png"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xtp1/t39.2082-0/p528x396/12679461_1065986690126640_622421424_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xap1/t39.2082-0/p528x396/12532990_208055976241061_513677896_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xfa1/t39.2082-0/p528x396/12521777_822095034567909_245141124_n.jpg"}
                 {:img "https://external-tpe1-1.xx.fbcdn.net/safe_image.php?d=AQDHfbzhSPvVuH1a&w=400&h=225&url=https%3A%2F%2Fscontent-tpe1-1.xx.fbcdn.net%2Fhphotos-xal1%2Ft39.2082-0%2Fp528x396%2F12350990_989179941141650_728204544_n.jpg&cfs=1&blur=0"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xtp1/t39.2082-0/p528x396/12679461_1065986690126640_622421424_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xap1/t39.2082-0/p528x396/12532990_208055976241061_513677896_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xfa1/t39.2082-0/p528x396/12521777_822095034567909_245141124_n.jpg"}
                 {:img "https://external-tpe1-1.xx.fbcdn.net/safe_image.php?d=AQDHfbzhSPvVuH1a&w=400&h=225&url=https%3A%2F%2Fscontent-tpe1-1.xx.fbcdn.net%2Fhphotos-xal1%2Ft39.2082-0%2Fp528x396%2F12350990_989179941141650_728204544_n.jpg&cfs=1&blur=0"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xtp1/t39.2082-0/p528x396/12679461_1065986690126640_622421424_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xap1/t39.2082-0/p528x396/12532990_208055976241061_513677896_n.jpg"}
                 {:img "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xfa1/t39.2082-0/p528x396/12521777_822095034567909_245141124_n.jpg"}
                 {:img "https://external-tpe1-1.xx.fbcdn.net/safe_image.php?d=AQDHfbzhSPvVuH1a&w=400&h=225&url=https%3A%2F%2Fscontent-tpe1-1.xx.fbcdn.net%2Fhphotos-xal1%2Ft39.2082-0%2Fp528x396%2F12350990_989179941141650_728204544_n.jpg&cfs=1&blur=0"}
                 ]}))

(defn block-li [{:keys [img]} owner]
 (reify
  om/IRender
   (render [_]
    (dom/li #js {:style #js {:backgroundImage (str "url(" img ")")
                             :backgroundSize "cover"
                             :backgroundRepeat "no-repeat"
                             }} )

     )
   )
  )


(defn root-component [{:keys [games]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/ul nil
              (dom/li #js {:style #js{}}
                      (dom/iframe #js {:src "https://www.youtube.com/embed/scPbcEUCiec?rel=0&amp;controls=0&amp;showinfo=0"
                                       :width "300"
                                       :height "300"
                                       :scrolling "no"
                                       :frameborder "0"
                                       :allowTransparency "true"
                                       :allowFullScreen "true"
                                       :style #js {:border "none"
                                                   :overflow "hidden"}}))

             (om/build-all block-li games)

              )
      )))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})
