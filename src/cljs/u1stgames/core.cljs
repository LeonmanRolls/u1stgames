(ns u1stgames.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [u1stgames.utils :as u]
            [cljs.core.async :refer [put! chan <! >! pub sub unsub unsub-all close!]]
            [ajax.core :refer [GET POST]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def yt-init-chan (chan))
(def yt-init-pub (pub yt-init-chan :msg-type))

(defn handler [response]
  (println (type (cljs.reader/read-string response)))
  #_(.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn chan-get [url chan]
  (GET url {:handler #(put! chan (cljs.reader/read-string %))}))

(comment

  (GET "/fbgames" {:handler handler})

  )

(defn ^:export youtubeReady []
  (put! yt-init-chan {:msg-type :init}))

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
                             :backgroundRepeat "no-repeat"}}))))

(defn img-block [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:uid (u/uid-gen "yt")
       :player {}})

    om/IDidMount
    (did-mount [_]
      (let [subscriber (chan)
            {:keys [uid player]} (om/get-state owner)]
        (sub yt-init-pub :init subscriber)
        (go
          (let [inited (<! subscriber)]
            (om/set-state!
              owner
              :player
              (new js/YT.Player uid #js {:height "300"
                                         :width "300"
                                         :videoId "scPbcEUCiec"
                                         :playerVars #js {:controls 0
                                                          :showinfo 0
                                                          :modestbranding 1}
                                         :events #js {:onReady #(println "ready")
                                                      :onStateChange #(println "state change")}}))))
        (.hover
          (js/$ "#lili")
          #(do
            (println "mouseover")
            (.animate
              (js/$ "#bg-cover")
              #js {:marginTop "-300px"} 500)
            )

          #(do
            (println "mouseOut")
            (.animate
              (js/$ "#bg-cover")
              #js {:marginTop "0px"} 500)
            )
          )

        ))

    om/IRenderState
    (render-state [_ {:keys [uid player]}]
      (dom/li #js {:id "lili"
                   :onMouseOver #(.playVideo player)
                   :onMouseOut #(.pauseVideo player)}
              (dom/div #js {:id uid})
              (dom/div #js {:id "bg-cover"})))))

(defn root-component [{:keys [games]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/ul nil
             (om/build img-block {})

             (om/build-all block-li games)

              )
      )))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})


