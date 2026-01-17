(ns bvm.sketches.wavy-bands-demo
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.wavy-band :refer [wavy-band]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.rectangle :refer [rectangle]]))

(def band-1
  {:num-steps 25
   :layout-fn wavy-band
   :layout-options {:amplitude 0.06
                    :frequency 1.5
                    :length 0.85
                    :position 0.2
                    :object-width 0.025
                    :object-height 0.025
                    :variance 0.15
                    :phase false
                    :direction :horizontal
                    :seed 101}
   :style-fn linear-fade
   :style-options {:initial-fill-color [200 160 140]      ; dusty rose
                   :final-fill-color [170 140 130]
                   :initial-fill-opacity 230
                   :final-fill-opacity 200
                   :initial-stroke-color [160 120 100]
                   :final-stroke-color [140 110 100]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 220}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.1
                     :size-variance 0.1
                     :seed 201}})

(def band-2
  {:num-steps 30
   :layout-fn wavy-band
   :layout-options {:amplitude 0.08
                    :frequency 2.0
                    :length 0.9
                    :position 0.4
                    :object-width 0.028
                    :object-height 0.028
                    :variance 0.12
                    :phase true
                    :direction :horizontal
                    :seed 102}
   :style-fn linear-fade
   :style-options {:initial-fill-color [180 190 170]      ; sage
                   :final-fill-color [150 170 155]
                   :initial-fill-opacity 220
                   :final-fill-opacity 190
                   :initial-stroke-color [140 155 130]
                   :final-stroke-color [120 140 125]
                   :initial-stroke-width 1
                   :final-stroke-width 3
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 200}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.12
                     :size-variance 0.08
                     :seed 202}})

(def band-3
  {:num-steps 20
   :layout-fn wavy-band
   :layout-options {:amplitude 0.05
                    :frequency 1.0
                    :length 0.75
                    :position 0.6
                    :object-width 0.032
                    :object-height 0.032
                    :variance 0.2
                    :phase false
                    :direction :horizontal
                    :seed 103}
   :style-fn linear-fade
   :style-options {:initial-fill-color [160 175 185]      ; slate blue
                   :final-fill-color [130 150 165]
                   :initial-fill-opacity 225
                   :final-fill-opacity 195
                   :initial-stroke-color [120 140 155]
                   :final-stroke-color [100 125 145]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 210}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.08
                     :size-variance 0.12
                     :seed 203}})

(def band-4
  {:num-steps 35
   :layout-fn wavy-band
   :layout-options {:amplitude 0.07
                    :frequency 2.5
                    :length 0.95
                    :position 0.8
                    :object-width 0.022
                    :object-height 0.022
                    :variance 0.1
                    :phase true
                    :direction :horizontal
                    :seed 104}
   :style-fn linear-fade
   :style-options {:initial-fill-color [210 185 155]      ; warm sand
                   :final-fill-color [185 165 140]
                   :initial-fill-opacity 235
                   :final-fill-opacity 205
                   :initial-stroke-color [175 145 115]
                   :final-stroke-color [155 130 105]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 255
                   :final-stroke-opacity 225}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.1
                     :size-variance 0.1
                     :seed 204}})

(def config
  {:renderer :pdf
   :filename "wavy-bands-demo.pdf"
   :canvas {:canvas-width 600
            :canvas-height 600}
   :layers [band-1 band-2 band-3 band-4]})

(defn -main [& args]
  (bvm/vera-multi config))
