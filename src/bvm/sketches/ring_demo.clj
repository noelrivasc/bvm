(ns bvm.sketches.ring-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.ring :refer [ring]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

(def outer-ring
  {:num-steps 60
   :layout-fn ring
   :layout-options {:ellipse-width 0.75
                    :ellipse-height 0.75
                    :object-width 0.035
                    :object-height 0.035
                    :start 0.0
                    :completion 1.0
                    :align-to-path true}
   :style-fn linear-fade
   :style-options {:initial-fill-color [185 160 145]      ; warm taupe
                   :final-fill-color [145 165 175]        ; cool grey-blue
                   :initial-fill-opacity 220
                   :final-fill-opacity 220
                   :initial-stroke-color [155 130 115]
                   :final-stroke-color [115 135 145]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 240
                   :final-stroke-opacity 240}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.08
                     :size-variance 0.1
                     :seed 101}})

(def middle-arc
  {:num-steps 35
   :layout-fn ring
   :layout-options {:ellipse-width 0.5
                    :ellipse-height 0.5
                    :object-width 0.028
                    :object-height 0.028
                    :start 0.6
                    :completion 0.7
                    :align-to-path true}
   :style-fn linear-fade
   :style-options {:initial-fill-color [175 185 165]      ; sage
                   :final-fill-color [155 170 160]
                   :initial-fill-opacity 230
                   :final-fill-opacity 200
                   :initial-stroke-color [145 155 135]
                   :final-stroke-color [125 140 130]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 220}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.12
                     :size-variance 0.08
                     :seed 202}})

(def inner-arc
  {:num-steps 25
   :layout-fn ring
   :layout-options {:ellipse-width 0.28
                    :ellipse-height 0.28
                    :object-width 0.022
                    :object-height 0.022
                    :start 0.1
                    :completion 0.5
                    :align-to-path true}
   :style-fn linear-fade
   :style-options {:initial-fill-color [195 175 165]      ; blush
                   :final-fill-color [175 160 155]
                   :initial-fill-opacity 235
                   :final-fill-opacity 210
                   :initial-stroke-color [165 145 135]
                   :final-stroke-color [145 130 125]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 230}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.15
                     :size-variance 0.12
                     :seed 303}})

(def config
  {:renderer :java2d
   :filename "ring-demo"
   :canvas {:canvas-width 6000
            :canvas-height 6000}
   :layers [outer-ring middle-arc inner-arc]})

(defn -main [& args]
  (bvm/vera-multi config))
