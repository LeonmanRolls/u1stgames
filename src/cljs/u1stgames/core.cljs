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
  (atom {:privacy "<div class=\"modal-body\">\n<h1>U1st Games: Privacy Policy</h1>\n<p>At U1st Games, we want to have a great relationship with you. Don't worry, your boyfriend or girlfriend shouldn't be jealous: we just want to be crystal-clear about what information we have about you, because we try not to be evil :) Incidentally, if you like our privacy policy, please \"like\" us, and if you don't like it, have any feedback or have a question about how your information is handled, please leave us a comment and we will get right back to you!</p>\n<h2>What information does U1st Games collect about me, and why?</h2>\n<p>Well, it depends. If you are just browsing here, and have not signed into our site with your Facebook account (which you should, it's great!), then we don't store any info about you. However, when you visit our site you are also loading up content from other services for things like ads. If you are interested, you can read more about what these services are doing by visiting our <a href=\"#\">cookie policy</a>.</p>\n<p>If you have signed up to play Pandemic: American Swine, good for you! When you sign up via Facebook, Facebook gives us these details about you:</p>\n<ul>\n<li><b>Your name</b>. We just use this to say hi to you when you play the game. Hi!</li>\n<li><b>Your Facebook ID number</b>. We need to know this so that when you are logged into Facebook, you are also logged into Pandemic: American Swine, and without knowing your Facebook ID, this wouldn't be possible!</li>\n<li><b>Your email address</b>. Sometimes we send you some emails, e.g. when new content is added to Pandemic: American Swine or if we make any cool new games that you might be interested in.</li>\n<li><b>Your locale</b>. As we are writing this (10th of June, 2012, 2 am in the morning...), we do not really use your <i>locale</i> (what country you are in and what language you speak). Some time in the future, we might show you special content on our site depending on what language you speak or where you live, but not now. Remember that we get this information from your Facebook settings, so if you use FB in Pirate English, then FB will tell us that you speak Pirate. Arrrr!</li>\n</ul>\n<p>In summary, Facebook tells us some stuff about you and we use it to personalise your experience on our website.</p>\n<h2>What about external/third-party links?</h2>\n<p>As far as I can think of, there is only one way you can click on a link on Pandemic: American Swine which will take you out of our website to somewhere else:</p>\n<ul>\n<li>You could click on an advert.</li>\n</ul>\n<p>In all of these interesting scenarios, your personal information will not be shared with that website. However, that site can check that you have come from Pandemic: American Swine, so don't be too alarmed if it says \"Hello Pandemic: American Swine visitor!\" or something like that. But just to emphasise, clicking on a link will not reveal your identity to the other websites.</p>\n      </div>"
         :terms "<div class=\"modal-body\">\n
         <h1>Terms and conditions</h1>\n<p>By accessing this web site, you are agreeing
         to be bound by these web site Terms and Conditions of Use, all applicable laws
         and regulations, and agree that you are responsible for compliance with any
         applicable local laws. If you do not agree with any of these terms, you are
         prohibited from using or accessing this site. The materials contained in this
         web site are protected by applicable copyright and trade mark law.</p>\n
         <h2>Use License</h2>\n<p>Permission is granted to temporarily download one copy
         of the materials (information or software) on U1stGames's web site for personal,
         non-commercial transitory viewing only. This is the grant of a license, not a
         transfer of title, and under this license you may not:</p>\n
         <ul style=\"font-size: 1em;\">\n
         <li>modify or copy the materials;</li>\n<li>use the materials for any commercial
         purpose, or for any public display (commercial or non-commercial);</li>\n
         <li>attempt to decompile or reverse engineer any software contained on U1stGames's web site;</li>\n<li>remove any copyright or other proprietary notations from the materials; or</li>\n<li>transfer the materials to another person or \"mirror\" the materials on any other server.</li>\n</ul>\n<p>This license shall automatically terminate if you violate any of these restrictions and may be terminated by U1stGames at any time. Upon terminating your viewing of these materials or upon the termination of this license, you must destroy any downloaded materials in your possession whether in electronic or printed format.</p>\n\n<h2>Disclaimer</h2>\n<p>The materials on U1stGames's web site are provided \"as is\". U1stGames makes no warranties, expressed or implied, and hereby disclaims and negates all other warranties, including without limitation, implied warranties or conditions of merchantability, fitness for a particular purpose, or non-infringement of intellectual property or other violation of rights. Further, U1stGames does not warrant or make any representations concerning the accuracy, likely results, or reliability of the use of the materials on its Internet web site or otherwise relating to such materials or on any sites linked to this site.</p>\n\n<h2>Limitations</h2>\n<p>In no event shall U1stGames or its suppliers be liable for any damages (including, without limitation, damages for loss of data or profit, or due to business interruption,) arising out of the use or inability to use the materials on U1stGames's Internet site, even if U1stGames or a U1stGames authorized representative has been notified orally or in writing of the possibility of such damage. Because some jurisdictions do not allow limitations on implied warranties, or limitations of liability for consequential or incidental damages, these limitations may not apply to you.</p>\n\n<h2>Revisions and Errata</h2>\n<p>The materials appearing on U1stGames's web site could include technical, typographical, or photographic errors. U1stGames does not warrant that any of the materials on its web site are accurate, complete, or current. U1stGames may make changes to the materials contained on its web site at any time without notice. U1stGames does not, however, make any commitment to update the materials.</p>\n\n<h2>Links</h2>\n<p>U1stGames has not reviewed all of the sites linked to its Internet web site and is not responsible for the contents of any such linked site. The inclusion of any link does not imply endorsement by U1stGames of the site. Use of any such linked web site is at the user's own risk.</p>\n\n<h2>Site Terms of Use Modifications</h2>\n<p>U1stGames may revise these terms of use for its web site at any time without notice. By using this web site you are agreeing to be bound by the then current version of these Terms and Conditions of Use.</p>\n\n<h2>Governing Law</h2>\n<p>Any claim relating to U1stGames's web site shall be governed by the laws of the State of London without regard to its conflict of law provisions.</p>\n      </div>"

         :home {:id (u/uid-gen "logo") :logo "/img/u1st_logo_square.png"}
         :sort {:id (u/uid-gen "sortby")}
         :games []
         :products []
         :modal-data {:display "none"
                      :title "Default Title"
                      :description "Default description"
                      :img "http://placehold.it/300x300"
                      :price "1.00"
                      :product {}}
         :cart []
         :client []}))

(defn all-data-ref []
  (om/ref-cursor (om/root-cursor app-state)))

(defn products-data-ref []
  (om/ref-cursor (:products (om/root-cursor app-state))))

(defn modal-data-ref []
  (om/ref-cursor (:modal-data (om/root-cursor app-state))))

(defn product-block
  [{:keys [body_html images title variants] :as product-data} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:slider-id (u/uid-gen "slider")
       :hover-uid (u/uid-gen "hover")
       :bg-cover-uid (u/uid-gen "bgcover")
       :unslider []})

    om/IRenderState
    (render-state [_ {:keys [hover-uid bg-cover-uid]}]
      (let [modal-data (om/observe owner (modal-data-ref))
            price (-> variants first :price)
            src (:src (first images))]

        (dom/li #js {:id hover-uid
                     :onClick (fn [x]
                                (om/transact!
                                  modal-data
                                  (fn [x]
                                    (assoc x
                                      :title title
                                      :price price
                                      :img src
                                      :product product-data
                                      :display "inherit"))))
                     :style #js {:cursor "pointer"}}

                (dom/div #js {:className "bg-cover"
                              :style #js {:backgroundImage (str "url(" src ")")
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
                                (str "$" price))

                         (dom/p #js {:style #js {:bottom "5px" :right "5px" :background "black"
                                                 :padding "5px" :textAlign "center"
                                                 :border "2px solid white"
                                                 :boxShadow "0 0 0 3px black"}}
                                "ADD TO CART"))

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "80px"
                                          :top "0px"
                                          :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}}))))))

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

(defn cart-block
  [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:slider-id (u/uid-gen "slider")
       :hover-uid (u/uid-gen "hover")
       :bg-cover-uid (u/uid-gen "bgcover")
       :unslider []})

    om/IRenderState
    (render-state [_ {:keys [hover-uid bg-cover-uid]}]
      (let []
        (dom/li #js {:id hover-uid
                     :style #js {:cursor "pointer"}}

                (dom/div #js {:className "bg-cover"
                              :style #js {:backgroundImage (str "url(" "http://placehold.it/300x300" ")")
                                          :backgroundSize "cover"
                                          :backgroundRepeat "no-repeat"
                                          :backgroundColour "blue"
                                          :backgroundPosition "50% 50%"
                                          :marginTop "0px"}}

                         (.dir js/console data)
                         (println "type: " (type data))

                         (dom/p #js {:style #js {:top "5px" :left "5px" :width "200px"
                                                 :background "black" :padding "3px"
                                                 :textAlign "left"}}
                                "title")

                         (dom/p #js {:style #js {:bottom "5px" :left "5px" :background "black"
                                                 :padding "5px" :textAlign "left"}}
                                (str "$"))

                         (dom/p #js {:style #js {:bottom "5px" :right "5px" :background "black"
                                                 :padding "5px" :textAlign "center"
                                                 :border "2px solid white"
                                                 :boxShadow "0 0 0 3px black"}}
                                "ADD TO CART"))

                (dom/div #js {:style #js {:position "absolute" :width "100%" :height "80px"
                                          :top "0px"
                                          :background "linear-gradient(to top, rgba(0,0,0,0), rgba(0,0,0,1))"}}))))))


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
              (dom/div nil)))))



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

(defn product-modal [{:keys [title description price display img] :as modal-data} owner]
  (reify
    om/IRender
    (render [_]
      (let [all-data (om/observe owner (all-data-ref))]
        (dom/div #js {:id "test-modal"
                      :className "child"
                      :style #js {:width "90%" :height "90%" :zIndex "100"
                                  :background "#2c3e50" :display display}}

                 (dom/div #js {:style #js {:position "absolute" :left "0"
                                           :width "50%" :height "100%"}}
                          (dom/img #js {:src img
                                        :className "child" :style #js {}}))

                 (dom/div #js {:style #js {:position "absolute" :right "0"
                                           :width "50%" :height "100%"}}

                          (dom/div #js {:onClick (fn [_] (om/transact!
                                                           modal-data
                                                           (fn [x] (assoc x :display "none"))))
                                        :style #js {:color "white" :cursor "pointer"
                                                    :float "right":fontSize "3em"
                                                    :margin "10px"}} "X")

                          (dom/div #js {:className "parent"}

                                   (dom/div #js {:style #js {:width "100%" :textAlign "center"
                                                             :maxWidth "600px"}
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
                                                                    :boxShadow "0 0 0 3px black"}
                                                        :onClick (fn [x]
                                                                   (->
                                                                     (first (:cart all-data))
                                                                     (.addVariants #js {:variant (->
                                                                                                   (:product modal-data)
                                                                                                   :variants
                                                                                                   first)
                                                                                        :quantity 2})
                                                                     (.then (fn [cart]
                                                                              (do
                                                                                #_(.dir js/console cart)
                                                                                (om/update! all-data :cart [cart]))))))}
                                                   "ADD TO CART")))))))))

(defn shopify-init [app-data products]
  (go
    (let [p-chan (chan)
          shop-client (get-shop-client)
          _ (get-products shop-client 278313223 p-chan)
          got-products (raw->clj-products (<! p-chan))]

      (om/update! products got-products)
      (om/update! app-data :cart shop-client)
      (->
        shop-client
        (.createCart)
        (.then
          (fn [new-cart]
            (om/update! app-data :cart [new-cart])))))))

(defn simple-text [html-string]
  (dom/div #js {:style #js {:position "relative"
                            :zIndex "0"
                            :height "100%"}}

           (dom/div #js {:className "child"
                         :dangerouslySetInnerHTML #js {:__html html-string}
                         :style #js {:width "90%"
                                     :height "90%"
                                     :color "white"
                                     :fontWeight "bold"}})))

(defn root-component [{:keys [home games products modal-data privacy terms cart]
                       :as app-data} owner]
  (reify

    om/IInitState
    (init-state [_]
      {:init-route (:path (url (-> js/location .-href)))})

    om/IWillMount
    (will-mount [_]
      (load-more-export games)

      #_(->
          shop-client
          (.createCart)
          (.then
            (fn [new-cart]
              (om/transact!
                app-data
                (fn [x]
                  (assoc x
                    :cart new-cart))))))

      #_(shopify-init app-data products)
      )

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
                      (println "/fbgames: " all-games)
                      (js/biggerInitial)))}))

    om/IRender
    (render [_]
      (let [path (:path (url (-> js/location .-href)))
            {:keys [init-route]} (om/get-state owner)]

        (dom/div #js {:style #js {:position "relative" :height "100%"}}

                 #_(om/build product-modal modal-data)

                 (cond
                   (= init-route "/") (apply dom/ul nil
                                             (om/build home-block home {:key :id})
                                             #_(om/build sort-block games {:key :id})
                                             (om/build-all img-block games {:key :id}))

                   (= init-route "/shop") (dom/div #js {:style #js {:position "relative"
                                                                    :zIndex "0"}}
                                                   (apply dom/ul nil
                                                          (om/build home-block home {:key :id})
                                                          (om/build cart-block [])
                                                          (om/build-all product-block products {:key :id})))

                   (= init-route "/privacy") (simple-text privacy)

                   (= init-route "/terms") (simple-text terms)

                   :else (om/build home-block home {:key :id})))))))

(om/root
  root-component
  app-state
  {:target (js/document.getElementById "app")})

(comment

  (:cart @app-state)
  (:products @app-state)

  (->
    (:cart @app-state)
    (.addVariants #js {:variant (-> (:products @app-state) first :variants first) :quantity 2})
    (.then (fn [cart] (.dir js/console cart)))
    )

  (-> (:products @app-state) first :variants first)

  (def t-c (chan))
  (def shop-client (get-shop-client))

  (get-products (get-shop-client) 278313223 t-c)
  (go (def products (js->clj (<! t-c) :keywordize-keys true)))

  (def clj-products (raw->clj-products products))
  clj-products

  )




