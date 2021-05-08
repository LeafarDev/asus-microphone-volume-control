(ns asus-microphone-volume-control.core
  (:require [cljfx.api :as fx]
            [clojure.string :as str])
  (:use [clojure.java.shell :only [sh]])
  (:gen-class))

(def device-name "alsa_input.usb-ASUSTeK_XONAR_SOUND_CARD-00.analog-stereo")
(def min-value 500)
(def max-value 1000)

(defn- objectify-array
  [arr]
  (map #(as-> % data
              (str/split data #":")
              {(keyword (str/replace (get data 0) #" " ""))
               (str/replace (reduce str (rest data)) #" " "")}
              ) arr))

(defn- get-pactl
  []
  (let [pactl-str (:out (sh "bash" "-c" "pactl list sources"))
        pactl-splited (as-> pactl-str data
                            (str/replace data #"\t\t" "t")
                            (str/replace data #"\t        " "")
                            (str/replace data #"\n" " ")
                            (str/split data #"Fonte #")
                            (map #(str/split % #"\t") data)
                            (map #(subvec % 1) data))
        pactl-objectified (as-> pactl-splited data
                                (map #(objectify-array %) data)
                                (apply list data)
                                (vec data)
                                (subvec data 1)
                                (map #(reduce merge %) data)
                                (vec data))]
    pactl-objectified))

(defn- find-device-pactl
  [name]
  (let [pactl-list (get-pactl)
        device (first (filter #(= name (:Nome %)) pactl-list))]
    device))

(defn- str-to-int
  [n]
  (Integer. (re-find #"\d+" n)))

(defn- get-device-volume-percentage
  [device-name]
  (let [device (find-device-pactl device-name)
        volume-percentage (str/replace (second (str/split (:Volume device) #"/")) #"%" "")]
    (str-to-int volume-percentage)))

(defn- set-volume
  [device-name new-volume-value]
  (println (str "changing to >> " new-volume-value))
  (sh "bash" "-c" (str "pactl set-source-volume" " " device-name " " new-volume-value "%"))
  (println (str "changed to >> " (get-device-volume-percentage device-name)))
  (get-device-volume-percentage device-name))

(defn- open-gui
  []
  (let [current-volume (get-device-volume-percentage device-name)]
    (when (< current-volume min-value)
      (set-volume device-name min-value))
    (fx/on-fx-thread
      (fx/create-component
        {:fx/type   :stage
         :showing   true
         :title     "Asus Microphone Volume"
         :width     600
         :height    600
         :icons     ["https://github.com/OlegIlyenko/clojure-icons/blob/master/clojure-dark-blue-icon.png"]
         :on-hidden (fn [_]
                      (System/exit 0))
         :scene     {:fx/type :scene
                     :root    {:fx/type   :v-box
                               :alignment :center
                               :children  [{:fx/type :label
                                            :text    "Selecione o Volume"}
                                           {:fx/type          :slider
                                            :show-tick-marks  true
                                            :show-tick-labels true
                                            :min              min-value
                                            :max              max-value
                                            :value            current-volume
                                            :on-value-changed #(set-volume device-name (int %))}]}}}))))
(defn- set-default-volume
  []
  (do
    (when (< (get-device-volume-percentage device-name) min-value)
      (set-volume device-name min-value))
    (System/exit 0)))

(defn -main
  [& args]
  (if (nil? args)
    (open-gui)
    (if (= false (:gui (read-string (first args))))
      (set-default-volume)
      (open-gui))))
