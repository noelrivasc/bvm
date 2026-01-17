(ns bvm.sketches.wave-depth
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.wavy-band :refer [wavy-band]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

(defn- lerp [a b t] (+ a (* (- b a) t)))

(defn- lerp-color [[r1 g1 b1] [r2 g2 b2] t]
  [(int (lerp r1 r2 t))
   (int (lerp g1 g2 t))
   (int (lerp b1 b2 t))])

(defn- make-band
  "Creates a band config for a given depth (0 = top, 1 = bottom)."
  [depth index]
  (let [;; Position: start at 0.12, end around 0.88
        position (lerp 0.12 0.88 depth)
        ;; Amplitude: small at top, larger at bottom
        amplitude (lerp 0.005 0.05 depth)
        ;; Vertex variance: perfect at top, wild at bottom
        vertex-var (lerp 0.0 0.45 depth)
        ;; Size variance: subtle at top, more at bottom
        size-var (lerp 0.0 0.25 depth)
        ;; Object size: slightly larger at bottom
        obj-size (lerp 0.012 0.018 depth)
        ;; Colors: desaturated at top, more saturated at bottom
        ;; Top: almost grey with hint of green-blue [155 160 158] -> [152 158 162]
        ;; Bottom: muted sea colors [95 135 125] -> [85 120 140]
        top-initial [158 162 160]
        top-final [155 160 165]
        bottom-initial [95 140 130]
        bottom-final [80 125 145]
        initial-color (lerp-color top-initial bottom-initial depth)
        final-color (lerp-color top-final bottom-final depth)
        ;; Stroke colors slightly darker
        stroke-initial (mapv #(- % 25) initial-color)
        stroke-final (mapv #(- % 25) final-color)
        ;; Opacity: slightly more transparent at top
        fill-opacity (int (lerp 180 230 depth))
        ;; Stroke width: thinner at top
        stroke-width-start (int (lerp 1 1 depth))
        stroke-width-end (int (lerp 1 2 depth))]
    {:num-steps 100
     :layout-fn wavy-band
     :layout-options {:amplitude amplitude
                      :frequency 2.0
                      :length 0.92
                      :position position
                      :object-width obj-size
                      :object-height obj-size
                      :variance (* 0.15 depth)
                      ; :phase (even? index)
                      :phase true
                      :direction :horizontal
                      :seed (+ 100 (* index 17))}
     :style-fn linear-fade
     :style-options {:initial-fill-color initial-color
                     :final-fill-color final-color
                     :initial-fill-opacity fill-opacity
                     :final-fill-opacity fill-opacity
                     :initial-stroke-color stroke-initial
                     :final-stroke-color stroke-final
                     :initial-stroke-width stroke-width-start
                     :final-stroke-width stroke-width-end
                     :initial-stroke-opacity 200
                     :final-stroke-opacity 200}
     :draw-fn rectangle
     :drawing-options {:vertex-variance vertex-var
                       :size-variance size-var
                       :seed (+ 500 (* index 23))}}))

(def num-bands 20)

(def layers
  (mapv (fn [i]
          (let [depth (/ (double i) (dec num-bands))]
            (make-band depth i)))
        (range num-bands)))

(def config
  {:renderer :pdf
   :filename "wave-depth.pdf"
   :canvas {:canvas-width 1200
            :canvas-height 800}
   :layers layers})

(defn -main [& args]
  (bvm/vera-multi config))
