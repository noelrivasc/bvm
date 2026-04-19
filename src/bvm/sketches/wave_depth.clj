(ns bvm.sketches.wave-depth
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :refer [lerp-rgb]]
   [bvm.utils.interpolation :refer [lerp]]
   [bvm.layouts.wavy-band :refer [wavy-band]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

(def artworks
  {:cascade {:background-color [255 255 255]
             :canvas-width 1200
             :canvas-height 4800
             :num-bands 37
             :num-steps 100
             :blend-mode :multiply
             :position-start 0.99
             :position-end 0.2
             :amplitude-start 0.0
             :amplitude-end 0.03
             :frequency 2.0
             :length 0.92
             :variance-factor 0.15
             :phase true
             :direction :horizontal
             :vertex-var-start 0.0
             :vertex-var-end 0.45
             :size-var-start 0.0
             :size-var-end 0.12
             :obj-size-start 0.001
             :obj-size-end 0.12
             ; NOTE that "top" means when the waves start, not
             ; necessarily the top of the drawing.
             :top-initial-color [158 162 160]
             :top-final-color [155 160 172]
             :bottom-initial-color [175 197 192]
             :bottom-final-color [167 190 197]
             :stroke-color-offset 25
             :fill-opacity-start 230
             :fill-opacity-end 3
             :stroke-width-start 1
             :stroke-width-end 0.025
             :stroke-opacity-initial 200
             :stroke-opacity-final 200
             :seed-base-layout 100
             :seed-step-layout 17
             :seed-base-drawing 500
             :seed-step-drawing 23}})

(def current-artwork (:cascade artworks))

(defn make-band
  "Creates a band config for a given depth (0 = start, 1 = end)."
  [artwork depth index]
  (let [position (lerp (:position-start artwork) (:position-end artwork) depth)
        amplitude (lerp (:amplitude-start artwork) (:amplitude-end artwork) depth)
        vertex-var (lerp (:vertex-var-start artwork) (:vertex-var-end artwork) depth)
        size-var (lerp (:size-var-start artwork) (:size-var-end artwork) depth)
        obj-size (lerp (:obj-size-start artwork) (:obj-size-end artwork) depth)
        top-initial (:top-initial-color artwork)
        top-final (:top-final-color artwork)
        bottom-initial (:bottom-initial-color artwork)
        bottom-final (:bottom-final-color artwork)
        initial-color (lerp-rgb top-initial bottom-initial depth)
        final-color (lerp-rgb top-final bottom-final depth)
        stroke-initial (mapv #(- % (:stroke-color-offset artwork)) initial-color)
        stroke-final (mapv #(- % (:stroke-color-offset artwork)) final-color)
        fill-opacity (int (lerp (:fill-opacity-start artwork) (:fill-opacity-end artwork) depth))
        stroke-width-start (int (lerp (:stroke-width-start artwork) (:stroke-width-end artwork) depth))
        stroke-width-end (int (lerp (:stroke-width-start artwork) (:stroke-width-end artwork) depth))]
    {:num-steps (:num-steps artwork)
     :layout-fn wavy-band
     :blend-mode (:blend-mode artwork)
     :layout-options {:amplitude amplitude
                      :frequency (:frequency artwork)
                      :length (:length artwork)
                      :offset (:offset artwork 0)
                      :position position
                      :object-width obj-size
                      :object-height obj-size
                      :variance (* (:variance-factor artwork) depth)
                      :phase (:phase artwork)
                      :direction (:direction artwork)
                      :seed (+ (:seed-base-layout artwork) (* index (:seed-step-layout artwork)))}
     :style-fn linear-fade
     :style-options {:initial-fill-color initial-color
                     :final-fill-color final-color
                     :initial-fill-opacity fill-opacity
                     :final-fill-opacity fill-opacity
                     :initial-stroke-color stroke-initial
                     :final-stroke-color stroke-final
                     :initial-stroke-width stroke-width-start
                     :final-stroke-width stroke-width-end
                     :initial-stroke-opacity (:stroke-opacity-initial artwork)
                     :final-stroke-opacity (:stroke-opacity-final artwork)}
     :draw-fn rectangle
     :drawing-options {:vertex-variance vertex-var
                       :size-variance size-var
                       :seed (+ (:seed-base-drawing artwork) (* index (:seed-step-drawing artwork)))}}))

(def num-bands (:num-bands current-artwork))

(def layers
  (mapv (fn [i]
          (let [depth (/ (double i) (dec num-bands))]
            (make-band current-artwork depth i)))
        (range num-bands)))

(def config
  {:renderer :java2d
   :filename "wave-depth"
   :background-color (:background-color current-artwork)
   :canvas {:canvas-width (:canvas-width current-artwork)
            :canvas-height (:canvas-height current-artwork)}
   :layers layers})

(defn -main [& args]
  (bvm/vera-multi config))
