(ns u1stgames.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [u1stgames.utils :as u]
            [cljs.core.async :refer [put! chan <! >! pub sub unsub unsub-all close!]]
            [ajax.core :refer [GET POST]]
            [cljs.reader :as reader])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def page-size 20)

(def like-box-string "<div class=\"fb-page\" data-href=\"https://www.facebook.com/U1stGamesOfficial/\" data-tabs=\"timeline\" data-width=\"300\" data-height=\"300\" data-small-header=\"true\" data-adapt-container-width=\"true\" data-hide-cover=\"false\" data-show-facepile=\"true\"><div class=\"fb-xfbml-parse-ignore\"><blockquote cite=\"https://www.facebook.com/U1stGamesOfficial/\"><a href=\"https://www.facebook.com/U1stGamesOfficial/\">U1st Games</a></blockquote></div></div>")

(def ad-string "")

(def yt-init-chan (chan))
(def yt-init-pub (pub yt-init-chan :msg-type))

(defn handler [response]
  #_(println (type (cljs.reader/read-string response)))
  (println (reader/read-string response))
  #_(.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn chan-get [url chan]
  (GET url {:handler #(put! chan (reader/read-string %))}))

(defn ^:export youtubeReady []
  (put! yt-init-chan {:msg-type :init}))

(def base-app-data (atom []))

(def
  app-state
  (atom {:home {:id (u/uid-gen "logo") :logo "/img/u1st_logo_square.png"}
         :sort {:id (u/uid-gen "sortby")}
         :games []}))

#_(defn games-ref []
  (om/ref-cursor (:games (om/root-cursor app-state))))

(comment
  (GET "/fbgames" {:handler handler})
  (.api js/FB "/1557991804501532" "get" #js {} #(println %))
  @base-app-data
  (sort-by :monthly_active_users @base-app-data)
  )


(defn home-block
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
                   :style #js  {:backgroundSize "cover"
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

              (dom/div #js {:style #js {:height "300px"}
                            :dangerouslySetInnerHTML #js {:__html like-box-string}})))))

(defn sort-block
  [games owner]
  (reify
    om/IRender
    (render [_]
      (dom/li {:id (u/uid-gen "sortblock")}
              (dom/div #js {:style #js {:color "black"
                                        :fontSize "20px"}} "sort by text")
              (dom/button
                #js {:onClick (fn [x]
                                (om/transact!
                                  games
                                  (fn [games]
                                    (sort-by
                                      (fn [x]
                                        (int (:monthly_active_users x)))
                                      >
                                      games))))}
                "Users")
              (dom/button
                #js {:onClick (fn [x]
                                (om/transact!
                                  games
                                  (fn [games]
                                    (vec
                                      (sort-by
                                      :title
                                      games)
                                      )
                                    )))}
                "Alphabetically")))))


(defmulti img-block (fn [data owner] (:type data)))

(defmethod img-block :advert
  [{:keys [adz] :as data} owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player]}]
      (dom/li nil
              (dom/div #js {:dangerouslySetInnerHTML #js {:__html  "
                                                <ins class=\"adsbygoogle\"
                                                style=\"display:inline-block;width:125px;height:125px\"
                                                data-ad-client=\"ca-pub-2815558012050620\"
                                                data-ad-slot=\"8353125252\"></ins>
                                                " }})))))




(defmethod img-block :game
  [{:keys [title appid subcategory picture monthly_active_users pics ytvideo] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:uid (u/uid-gen "yt")
       :hover-uid (u/uid-gen "hover")
       :bg-cover-uid (u/uid-gen "bgcover")
       :player {}
       :slider-id (u/uid-gen "slider")
       :unslider []})

    om/IDidMount
    (did-mount [_]
      (let [ytid (if
                   (empty? ytvideo)
                   "scPbcEUCiec"
                   (->
                     (nth (clojure.string/split ytvideo #"=") 1)
                     (clojure.string/split #"&")
                     (first)))
            {:keys [uid hover-uid bg-cover-uid player slider-id]} (om/get-state owner)
            unslider (.unslider (js/$ (str "#" slider-id)) #js {:autoplay true
                                                                :speed 300
                                                                :delay 800})]

        (.unslider unslider "stop")

        (.hover
            (js/$ (str "#" hover-uid))
            (fn []
              (.animate
                  (js/$ (str "#" bg-cover-uid))
                  #js {:marginTop "-300px"} 200)
              (.unslider unslider "start")
                #_(om/set-state!
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
                                                            :onStateChange #(println "state change")}})))

            (fn [x]
              (.animate
                (js/$ (str "#" bg-cover-uid))
                #js {:marginTop "0px"} 200)
              (.unslider unslider "stop")
              #_(om/get-state owner :unslider)
              #_(.destroy (om/get-state owner :player))))))


    om/IRenderState
    (render-state [_ {:keys [uid hover-uid bg-cover-uid player slider-id unslider]}]
      (let []
        (dom/li #js {:id hover-uid
                     :style #js  {:backgroundSize "cover"
                                  :backgroundRepeat "no-repeat"}}
                (dom/div #js {:id bg-cover-uid
                              :className "bg-cover"
                              :style #js {:backgroundImage (str "url(" (:url (first pics)) ")")
                                          :backgroundSize "cover"
                                          :backgroundRepeat "no-repeat"
                                          :backgroundColour "blue"
                                          :backgroundPosition "50% 50%"
                                          :marginTop "0px"}})

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "50px"
                                          :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}})

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "50px"
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

                ;Image slider

                (dom/div #js{:id slider-id}
                         (apply dom/ul nil
                                 (om/build-all
                                   (fn [data owner]
                                     (reify
                                       om/IRender
                                       (render [_]
                                         (dom/li nil
                                                 (dom/img #js {:src (:url data) :width "300"
                                                               :style #js {:zIndex "-100"
                                                                           :position "relative"
                                                                           :top "50%"
                                                                           :transform "translateY(-50%)"}})))))
                                   (rest pics)
                                   {:key :uid}
                                   )))

                #_(dom/div #js {:id uid})

                )))))



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


(defn load-more [games]
  (let [next-12 (take page-size @base-app-data)]
    (when (not (empty? next-12))
      (swap! base-app-data (fn [x] (drop page-size x)))
      (om/transact!
        games
        (fn [games]
          (into games next-12))))))

(defn load-more-export [games]
  (defn ^:export loadMoreNow []
   (load-more games)))

(defn root-component [{:keys [home games]} owner]
  (reify

    om/IWillMount
    (will-mount [_]
     (load-more-export games))

    om/IDidMount
    (did-mount [_]

      (GET
        "/fbgames"
        {:handler (fn [all-games]
                    (let [read-games (reader/read-string all-games)]
                      (reset! base-app-data (drop page-size read-games))
                      (om/transact!
                        games
                        (fn [games]
                          (into games (take page-size read-games))))))}))

    om/IRender
    (render [_]
      (dom/div nil
               (apply dom/ul nil
              #_(om/build home-block home {:key :id})
              #_(om/build sort-block games {:key :id})
              (om/build-all img-block games {:key :id}))))))

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})


