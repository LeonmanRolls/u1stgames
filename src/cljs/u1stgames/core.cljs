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
  #_(println (type (cljs.reader/read-string response)))
  (println (cljs.reader/read-string response))
  #_(.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn chan-get [url chan]
  (GET url {:handler #(put! chan (cljs.reader/read-string %))}))

(defn ^:export youtubeReady []
  (put! yt-init-chan {:msg-type :init}))

(def
  app-state
  (atom {:games [{:picture "/img/u1st_logo_square.png"}
                 {:title ""
                  :subcategory ""
                  :appid ""
                  :id ""
                  :picture "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xap1/t39.2082-0/p528x396/12532990_208055976241061_513677896_n.jpg"
                  :monthly_active_user ""
                  :ytvideo ""}
                 {:picture "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xap1/t39.2082-0/p528x396/12532990_208055976241061_513677896_n.jpg"}
                 {:picture "https://scontent-tpe1-1.xx.fbcdn.net/hphotos-xfa1/t39.2082-0/p528x396/12521777_822095034567909_245141124_n.jpg"}
                 ]}))

(comment

  (GET "/fbgames" {:handler handler})

  (fn []


    )

  (.api js/FB "/1557991804501532" "get" #js {} #(println %))

  )


(defn block-li [{:keys [picture]} owner]
 (reify
  om/IRender
   (render [_]
    (dom/li #js {:style #js {:backgroundImage (str "url(" picture ")")
                             :backgroundSize "cover"
                             :backgroundRepeat "no-repeat"}}))))

(defn img-block [{:keys [picture] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:uid (u/uid-gen "yt")
       :hover-uid (u/uid-gen "hover")
       :bg-cover-uid (u/uid-gen "bgcover")
       :player {}})

    om/IDidMount
    (did-mount [_]
      (let [subscriber (chan)
            {:keys [uid hover-uid bg-cover-uid player]} (om/get-state owner)]
        (sub yt-init-pub :init subscriber)
        #_(go
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
          (js/$ (str "#" hover-uid))
          #(do
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "-300px"} 200)
            )

          #(do
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "0px"} 200)
            )
          )

        ))

    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player]}]
      (dom/li #js {:id hover-uid
                   ;  :onMouseOver #_(.playVideo player)
                   ; :onMouseOut #_(.pauseVideo player)
                   }
              (dom/div #js {:id uid})
              (dom/div #js {:id bg-cover-uid
                            :className "bg-cover"
                            :style #js {:backgroundImage (str "url(" picture ")")
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"
                                        :backgroundColour "blue"}})))))

(defn root-component [{:keys [games]} owner]
  (reify

    om/IDidMount
    (did-mount [_]

      (GET
        "/fbgames"
        {:handler (fn [all-games]
                    (let [read-games (cljs.reader/read-string all-games)]
                      (om/transact!
                        games
                        (fn [games]
                          #_(println "conj: " (conj games read-games))
                          (into games read-games)))))}))

    om/IRender
    (render [_]
      (dom/ul nil
              (om/build-all img-block games)
              (om/build-all block-li games)))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})


