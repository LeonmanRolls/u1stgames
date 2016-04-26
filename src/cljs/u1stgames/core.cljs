(ns u1stgames.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [u1stgames.utils :as u]
            [cljs.core.async :refer [put! chan <! >! pub sub unsub unsub-all close!]]
            [ajax.core :refer [GET POST]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def like-box-string "<div class=\"fb-page\" data-href=\"https://www.facebook.com/U1stGamesOfficial/\" data-tabs=\"timeline\" data-width=\"300\" data-height=\"300\" data-small-header=\"true\" data-adapt-container-width=\"true\" data-hide-cover=\"false\" data-show-facepile=\"true\"><div class=\"fb-xfbml-parse-ignore\"><blockquote cite=\"https://www.facebook.com/U1stGamesOfficial/\"><a href=\"https://www.facebook.com/U1stGamesOfficial/\">U1st Games</a></blockquote></div></div>")

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
  (atom {:games [{:type :home :logo "/img/u1st_logo_square.png"}]}))

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

(defmulti img-block (fn [data owner] (:type data)))

(defmethod img-block :game
  [{:keys [title appid subcategory picture monthly_active_users pics ytvideo] :as data} owner]
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
            ytid (if
                   (empty? ytvideo)
                   "scPbcEUCiec"
                   (->
                     (nth (clojure.string/split ytvideo #"=") 1)
                     (clojure.string/split  #"&")
                     (first)))
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
          (fn []
            (do
              (.animate
                (js/$ (str "#" bg-cover-uid))
                #js {:marginTop "-300px"} 200)
              (om/set-state!
                owner
                :player
                (new js/YT.Player uid #js {:height "300"
                                           :width "300"
                                           :videoId ytid
                                           :playerVars #js {:controls 0
                                                            :showinfo 0
                                                            :modestbranding 1
                                                            :autoplay 1
                                                            :loop 1
                                                            :rel 0}
                                           :events #js {:onReady #(println "ready")
                                                        :onStateChange #(println "state change")}}))))

          (fn [x]
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "0px"} 200)
            (println (om/get-state owner :player))
            (.destroy (om/get-state owner :player))))))

    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player]}]
      (let []
        (dom/li #js {:id hover-uid
                     :style #js  {:backgroundSize "cover"
                                  :backgroundRepeat "no-repeat"}

                     ;  :onMouseOver #_(.playVideo player)
                     ; :onMouseOut #(.pauseVideo player)
                     }
                (dom/div #js {:id bg-cover-uid
                              :className "bg-cover"
                              :style #js {:backgroundImage (str "url(" (first pics) ")")
                                          :backgroundSize "cover"
                                          :backgroundRepeat "no-repeat"
                                          :backgroundColour "blue"
                                          :backgroundPosition "50% 50%"
                                          :marginTop "0px"}})

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                          :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}})

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                          :bottom "0px"
                                          :background "linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,1))"}})

                (dom/p #js {:style #js {:top "5px" :left "5px" :width "200px"
                                        :textAlign "left" :height "25px" :overflow "hidden"
                                        :textOverflow "ellipsis"}}
                       title)

                (dom/p #js {:style #js {:top "5px" :right "5px"}} subcategory)
                (dom/p #js {:style #js {:bottom "5px" :left "5px"}}
                       "Users: " monthly_active_users)

                (dom/a #js {:href (str "https://apps.facebook.com/" appid) :target "_blank"}
                       (dom/button #js {:style #js {:position "absolute" :bottom "5px" :right "5px"
                                                    :background "transparent" :color "white"
                                                    :border "1px solid white" :padding "5px"
                                                    :width "70px" :textTransform "uppercase"
                                                    :fontWeight "bold" :cursor "crosshair"}}
                                   "Play "
                                   (dom/i #js {:className "fa fa-gamepad" :ariaHidden "true"})))

                (dom/div #js {:id uid})

                )))))

(defmethod img-block :home
  [{:keys [logo] :as data} owner]
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

        (.hover
          (js/$ (str "#" hover-uid))
          #(do
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "-300px"} 200))

          #(do
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "0px"} 200)))))

    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player]}]
      (dom/li #js {:id hover-uid
                   :style #js  {
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"}}
              (dom/div #js {:id uid})
              (dom/div #js {:id bg-cover-uid
                            :className "bg-cover"
                            :style #js {:backgroundImage (str "url(" logo ")")
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"
                                        :backgroundColour "blue"
                                        :backgroundPosition "50% 50%"
                                        :marginTop "0px"}})

              (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                        :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}})

              (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                        :bottom "0px"
                                        :background "linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,1))"}})

              (dom/div #js {:dangerouslySetInnerHTML #js {:__html like-box-string}})

              #_(dom/p #js {:style #js {:top "5px" :left "5px" :width "200px"
                                      :textAlign "left" :height "25px" :overflow "hidden"
                                      :textOverflow "ellipsis"}}
                     title)

              #_(dom/p #js {:style #js {:top "5px" :right "5px"}} subcategory)
              #_(dom/p #js {:style #js {:bottom "5px" :left "5px"}}
                     "Users: " monthly_active_users)

              #_(dom/a #js {:href (str "https://apps.facebook.com/" appid) :target "_blank"}
              (dom/button #js {:style #js {:position "absolute" :bottom "5px" :right "5px"
                                           :background "transparent" :color "white"
                                           :border "1px solid white" :padding "5px"
                                           :width "70px" :textTransform "uppercase"
                                           :fontWeight "bold" :cursor "crosshair"}}
                          "Play "
                          (dom/i #js {:className "fa fa-gamepad" :ariaHidden "true"})))))))

(defmethod img-block :default
  [{:keys [title appid subcategory picture monthly_active_users pics] :as data} owner]
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
              #js {:marginTop "-300px"} 200))

          #(do
            (.animate
              (js/$ (str "#" bg-cover-uid))
              #js {:marginTop "0px"} 200)))))

    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player]}]
      (dom/li #js {:id hover-uid
                   :style #js  {
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"}
                   ;  :onMouseOver #_(.playVideo player)
                   ; :onMouseOut #_(.pauseVideo player)
                   }
              (dom/div #js {:id uid})
              (dom/div #js {:id bg-cover-uid
                            :className "bg-cover"
                            :style #js {:backgroundImage (str "url(" (last pics) ")")
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"
                                        :backgroundColour "blue"
                                        :backgroundPosition "50% 50%"
                                        :marginTop "0px"}})

              (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                        :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}})

              (dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                        :bottom "0px"
                                        :background "linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,1))"}})

              (dom/p #js {:style #js {:top "5px" :left "5px" :width "200px"
                                      :textAlign "left" :height "25px" :overflow "hidden"
                                      :textOverflow "ellipsis"}}
                     title)

              (dom/p #js {:style #js {:top "5px" :right "5px"}} subcategory)
              (dom/p #js {:style #js {:bottom "5px" :left "5px"}}
                     "Users: " monthly_active_users)

              (dom/a #js {:href (str "https://apps.facebook.com/" appid) :target "_blank"}
              (dom/button #js {:style #js {:position "absolute" :bottom "5px" :right "5px"
                                           :background "transparent" :color "white"
                                           :border "1px solid white" :padding "5px"
                                           :width "70px" :textTransform "uppercase"
                                           :fontWeight "bold" :cursor "crosshair"}}
                          "Play "
                          (dom/i #js {:className "fa fa-gamepad" :ariaHidden "true"})))))))

(defn root-component [{:keys [games]} owner]
  (reify

    om/IDidMount
    (did-mount [_]

      (GET
        "/fbgames"
        {:handler (fn [all-games]
                    (let [read-games (cljs.reader/read-string all-games)]
                      (println "all-games: " all-games)
                      (om/transact!
                        games
                        (fn [games]
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


