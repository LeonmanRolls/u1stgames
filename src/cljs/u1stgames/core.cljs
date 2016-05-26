(ns u1stgames.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [u1stgames.utils :as u]
            [cljs.core.async :refer [put! chan <! >! pub sub unsub unsub-all close!]]
            [ajax.core :refer [GET POST]]
            [cljs.reader :as reader]
            [cljs.pprint :refer [pprint]]
            [cemerick.url :refer (url url-encode)])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(def page-size 12)

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
         :games []
         :products []
         :modal-data {:display "none"
                      :title "Default Title"
                      :description "Default description"
                      :price "1.00"}}))

(defn modal-data-ref []
      (om/ref-cursor (:modal-data (om/root-cursor app-state))))

(defn product-block
  [{:keys [body_html images title variants] :as data} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:slider-id (u/uid-gen "slider")
       :hover-uid (u/uid-gen "hover")
       :bg-cover-uid (u/uid-gen "bgcover")
       :unslider []})

    om/IDidMount
    (did-mount [_]
      (let [{:keys [uid hover-uid bg-cover-uid player slider-id]} (om/get-state owner)

            unslider (.unslider (js/$ (str "#" slider-id)) #js {:autoplay true
                                                                :speed 300
                                                                :delay 800})]

        #_(.unslider unslider "stop")

        #_(.hover
            (js/$ (str "#" hover-uid))
            (fn []
              (.animate
                  (js/$ (str "#" bg-cover-uid))
                  #js {:marginTop "-300px"} 200)
              (.unslider unslider "start"))

            (fn [x]
              (.animate
                (js/$ (str "#" bg-cover-uid))
                #js {:marginTop "0px"} 200)
              (.unslider unslider "stop")))))

    om/IRenderState
    (render-state [_ {:keys [hover-uid bg-cover-uid]}]
      (let [modal-data (om/observe owner (modal-data-ref))]
        (dom/li #js {:onClick (fn [x] (om/transact! modal-data (fn [x] (assoc x :display "inherit")))  )
                   :id hover-uid}

              (dom/div #js {:className "bg-cover"
                            :style #js {:backgroundImage (str "url(" (:src (first images)) ")")
                                        :backgroundSize "cover"
                                        :backgroundRepeat "no-repeat"
                                        :backgroundColour "blue"
                                        :backgroundPosition "50% 50%"
                                        :marginTop "0px"}}

                       (dom/p #js {:style #js {:top "5px" :left "5px" :width "200px"
                                               :background "black" :padding "3px"
                                               :textAlign "left"}}
                              title)

                       (dom/p #js {:style #js {:bottom "5px" :left "5px" :background "black"
                                               :padding "5px" :textAlign "left"}}
                              (str "$" (-> variants first :price)))

                       (dom/p #js {:style #js {:bottom "5px" :right "5px" :background "black"
                                               :padding "5px" :textAlign "center"
                                               :border "2px solid white"
                                               :box-shadow "0 0 0 3px black"}}
                              "ADD TO CART")



                       )

              #_(apply dom/ul nil
                     (om/build-all
                       (fn [data owner]
                         (reify
                           om/IRender
                           (render [_]
                             (dom/li nil
                                     (dom/img #js {:src (:src data) :width "300"
                                                   :style #js {:zIndex "-100"
                                                               :position "relative"
                                                               :top "50%"
                                                               :transform "translateY(-50%)"}})))))
                       images
                       {:key :id}))

              (dom/div #js {:style #js {:position "absolute" :width "100%" :height "80px"
                                        :top "0px"
                                        :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}})

              #_(dom/div #js {:style #js {:position "absolute" :width "100%" :height "150px"
                                        :bottom "0px"
                                        :background "linear-gradient(to bottom, rgba(0,0,0,0), rgba(0,0,0,1))"}})


              )))))

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
                                                    :fontWeight "bold" :cursor "crosshair"
                                                    :zIndex "50"}}
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

(defn get-products [shop-client collectionid c]
  (->
    shop-client
    (.fetchQueryProducts #js {:collection_id collectionid})
    (.then (fn [x] (put! c (js->clj x :keywordize-keys true))))
    (.catch (fn [x] (println "request failed")))))

(defn get-shop-client [] (js/buildShopClient))

(defn raw->clj-products [products]
  (->
    products
    (aget "tail")
    (as-> xs (map #(aget % "attrs") xs))
    (js->clj :keywordize-keys true)
    (vec)))

(defn product-modal [{:keys [title description price display] :as modal-data} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:id "test-modal"
                    :className "child"
                    :style #js {:width "90%" :height "90%" :zIndex "100"
                                :background "#2c3e50" :display display}}
               (dom/div #js {:style #js {:position "absolute" :left "0"
                                         :width "50%" :height "100%"}}
                        (dom/img #js {:src "https://cdn.shopify.com/s/files/1/1292/2419/products/keychain.jpg?v=1463221578"
                                      :className "child" :style #js {}}))

               (dom/div #js {:style #js {:position "absolute" :right "0"
                                         :width "50%" :height "100%"}}

                        (dom/div #js {:onClick (fn [_] (om/transact!
                                                         modal-data
                                                         (fn [x] (assoc x :display "none"))))
                                      :style #js {:color "white"
                                                  :float "right":fontSize "3em"
                                                  :margin "10px"}} "X")

                        (dom/div #js {:className "parent"}

                                 (dom/div #js {:style #js {:width "100%" :textAlign "center"}
                                               :className "child"}

                        (dom/p #js {:style #js {:fontWeight "bold" :color "white"
                                                :fontSize "2em"}} title)

                        (dom/p #js {:style #js {:fontWeight "bold" :color "white"
                                                :fontSize "1.5em" :marginTop "-10px"}}
                               (str "$" price " or 10,000 U1st Points"))

                        (dom/p #js {:style #js {:fontWeight "" :color "white"
                                                :fontSize "1em"}} description)

                        (dom/p #js {:style #js {:background "black" :padding "5px"
                                                :textAlign "center" :margin "30px"
                                               :border "2px solid white" :color "white"
                                               :box-shadow "0 0 0 3px black"}}
                              "ADD TO CART")

                                          )



                                 )



                        )))))

(defn root-component [{:keys [home games products modal-data]} owner]
  (reify

    om/IInitState
    (init-state [_]
      {:init-route (:path (url (-> js/location .-href)))})

    om/IWillMount
    (will-mount [_]
      (load-more-export games)
      (go
        (let [p-chan (chan)
              shop-client (get-shop-client)
              testing (get-products shop-client 278313223 p-chan)
              got-products (raw->clj-products (<! p-chan))]
          (om/update! products got-products)
          (println "got products: " got-products))))

    om/IDidMount
    (did-mount [_]

      (GET
        "/fbgames"
        {:handler (fn [all-games]
                    (let [read-games (reader/read-string all-games)
                          sorted-games (sort-by
                                         (fn [x]
                                           (int (:monthly_active_users x)))
                                         >
                                         read-games)]
                      (reset! base-app-data (drop page-size sorted-games))
                      (om/transact!
                        games
                        (fn [games]
                          (into games (take page-size sorted-games))))
                      (js/biggerInitial)))}))

    om/IRender
    (render [_]
      (let [path (:path (url (-> js/location .-href)))
            {:keys [init-route]} (om/get-state owner)]

        (dom/div #js {:style #js {:position "relative" :height "100%"}}

                (om/build product-modal modal-data)

                 (cond
                   (= init-route "/") (apply dom/ul nil
                                             #_(om/build home-block home {:key :id})
                                             #_(om/build sort-block games {:key :id})
                                             (om/build-all img-block games {:key :id}))

                   (= init-route "/shop") (dom/div #js {:style #js {:position "relative"
                                                                    :zIndex "0"}}
                                                   (apply dom/ul nil
                                                          (om/build home-block home {:key :id})
                                                          #_(om/build sort-block games {:key :id})
                                                          (om/build-all product-block products {:key :id})))

                   :else (om/build home-block home {:key :id})))))))


(comment

  (def t-c (chan))
  (get-products (get-shop-client) 278313223 t-c)
  (go (def products (js->clj (<! t-c) :keywordize-keys true)))

  (def clj-products (raw->clj-products products))
  clj-products

  (pprint (:variants (first clj-products)))
  (pprint (-> clj-products first :variants first :price))

  (type products)
  (pprint "hi")
  (pprint (->
            products
            (aget "tail")
            (as-> xs (map #(aget % "attrs") xs))
            (js->clj :keywordize-keys true)
            (first)
            ))

  (.dir js/console (->
                     products
                     (aget "tail")
                     ))

  (.dir js/console (->
                     products
                     (aget "tail")
                     (aget 0)
                     (aget "attrs")
                     (aget "body_html")
                     ))

  (.dir js/console (first products))
  (println (first products))
  (js->clj (first products) :keywordize-keys true)

  (go (.dir js/console (js->clj (<! t-c) :keywordize-keys true)))

  (->
    shop-client
    (.fetchQueryProducts #js {:collection_id 278313223})
    (.then (fn [x] (.dir js/console x)))
    (.catch (fn [x] (println "request failed"))))

 (type (js/buildShopClient))

  )

(om/root
 root-component
 app-state
 {:target (js/document.getElementById "app")})


