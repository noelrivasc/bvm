(ns bvm.sketches.abstract-layers
  (:require
   [bvm.core :as bvm]
   [bvm.layouts.simple-grid :refer [simple-grid]]
   [bvm.layouts.chaotic-spiral :refer [chaotic-spiral]]
   [bvm.styles.linear-fade :refer [linear-fade]]
   [bvm.drawing.circle :refer [circle]]
   [bvm.drawing.rectangle :refer [rectangle]]))

;; Muted Japanese painting palette
;; Warm grays, soft blues, dusty pinks, aged creams

;; Layer 1: Large background grid of pale rectangles
(def layer-1
  {:num-steps 12
   :layout-fn simple-grid
   :layout-options {:max-cols 4 :gap-x 0.01}
   :style-fn linear-fade
   :style-options {:initial-fill-color [235 228 218]
                   :final-fill-color [220 215 208]
                   :initial-fill-opacity 180
                   :final-fill-opacity 160
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.02 :seed 101}})

;; Layer 2: Tight spiral of tiny circles emanating from center-left
(def layer-2
  {:num-steps 80
   :layout-fn chaotic-spiral
   :layout-options {:turns 4
                    :min-radius 0.02
                    :max-radius 0.35
                    :initial-width 0.008
                    :initial-height 0.012
                    :end-width 0.025
                    :end-height 0.035
                    :variance 0.15
                    :dispersion -0.3
                    :direction :counter-clockwise
                    :seed 201}
   :style-fn linear-fade
   :style-options {:initial-fill-color [180 175 168]
                   :final-fill-color [160 155 150]
                   :initial-fill-opacity 120
                   :final-fill-opacity 80
                   :initial-stroke-color [140 135 130]
                   :final-stroke-color [120 115 110]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 100
                   :final-stroke-opacity 60}
   :draw-fn circle})

;; Layer 3: Sparse large circles with dusty rose tint
(def layer-3
  {:num-steps 8
   :layout-fn chaotic-spiral
   :layout-options {:turns 1
                    :min-radius 0.1
                    :max-radius 0.45
                    :initial-width 0.12
                    :initial-height 0.12
                    :end-width 0.08
                    :end-height 0.08
                    :variance 0.3
                    :dispersion 0.5
                    :direction :clockwise
                    :seed 301}
   :style-fn linear-fade
   :style-options {:initial-fill-color [195 175 175]
                   :final-fill-color [185 170 172]
                   :initial-fill-opacity 60
                   :final-fill-opacity 40
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn circle})

;; Layer 4: Dense grid of small rectangles with slight chaos
(def layer-4
  {:num-steps 120
   :layout-fn simple-grid
   :layout-options {:max-cols 15 :gap-x 0.008}
   :style-fn linear-fade
   :style-options {:initial-fill-color [168 178 185]
                   :final-fill-color [155 165 175]
                   :initial-fill-opacity 50
                   :final-fill-opacity 70
                   :initial-stroke-color [130 140 150]
                   :final-stroke-color [140 150 160]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 40
                   :final-stroke-opacity 50}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.15 :size-variance 0.3 :seed 401}})

;; Layer 5: Organic spiral of wobbly rectangles
(def layer-5
  {:num-steps 45
   :layout-fn chaotic-spiral
   :layout-options {:turns 2
                    :min-radius 0.08
                    :max-radius 0.42
                    :initial-width 0.03
                    :initial-height 0.02
                    :end-width 0.05
                    :end-height 0.04
                    :variance 0.2
                    :dispersion 0.0
                    :direction :counter-clockwise
                    :align-to-path true
                    :seed 501}
   :style-fn linear-fade
   :style-options {:initial-fill-color [175 168 158]
                   :final-fill-color [190 182 172]
                   :initial-fill-opacity 90
                   :final-fill-opacity 110
                   :initial-stroke-color [145 138 128]
                   :final-stroke-color [160 152 142]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 70
                   :final-stroke-opacity 90}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.25 :seed 502}})

;; Layer 6: Ethereal large circles - soft blue-gray
(def layer-6
  {:num-steps 5
   :layout-fn chaotic-spiral
   :layout-options {:turns 0.5
                    :min-radius 0.15
                    :max-radius 0.4
                    :initial-width 0.18
                    :initial-height 0.25
                    :end-width 0.22
                    :end-height 0.28
                    :variance 0.4
                    :dispersion 0.2
                    :direction :clockwise
                    :seed 601}
   :style-fn linear-fade
   :style-options {:initial-fill-color [175 182 190]
                   :final-fill-color [165 172 182]
                   :initial-fill-opacity 35
                   :final-fill-opacity 25
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn circle})

;; Layer 7: Fine grid texture - warm undertone
(def layer-7
  {:num-steps 200
   :layout-fn simple-grid
   :layout-options {:max-cols 20 :gap-x 0.005}
   :style-fn linear-fade
   :style-options {:initial-fill-color [205 195 182]
                   :final-fill-color [195 188 178]
                   :initial-fill-opacity 30
                   :final-fill-opacity 45
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn circle})

;; Layer 8: Clockwise spiral - contrasting direction
(def layer-8
  {:num-steps 60
   :layout-fn chaotic-spiral
   :layout-options {:turns 3
                    :min-radius 0.05
                    :max-radius 0.38
                    :initial-width 0.015
                    :initial-height 0.015
                    :end-width 0.04
                    :end-height 0.04
                    :variance 0.1
                    :dispersion -0.5
                    :direction :clockwise
                    :seed 801}
   :style-fn linear-fade
   :style-options {:initial-fill-color [188 180 175]
                   :final-fill-color [172 165 162]
                   :initial-fill-opacity 70
                   :final-fill-opacity 50
                   :initial-stroke-color [158 150 145]
                   :final-stroke-color [142 135 132]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 50
                   :final-stroke-opacity 35}
   :draw-fn circle})

;; Layer 9: Medium rectangles with heavy distortion
(def layer-9
  {:num-steps 25
   :layout-fn chaotic-spiral
   :layout-options {:turns 1.5
                    :min-radius 0.12
                    :max-radius 0.4
                    :initial-width 0.06
                    :initial-height 0.05
                    :end-width 0.04
                    :end-height 0.035
                    :variance 0.25
                    :dispersion 0.3
                    :direction :counter-clockwise
                    :seed 901}
   :style-fn linear-fade
   :style-options {:initial-fill-color [165 158 165]
                   :final-fill-color [178 172 178]
                   :initial-fill-opacity 55
                   :final-fill-opacity 75
                   :initial-stroke-color [135 128 135]
                   :final-stroke-color [148 142 148]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 45
                   :final-stroke-opacity 60}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.35 :size-variance 0.2 :seed 902}})

;; Layer 10: Delicate small circles - cream colored
(def layer-10
  {:num-steps 100
   :layout-fn chaotic-spiral
   :layout-options {:turns 5
                    :min-radius 0.03
                    :max-radius 0.45
                    :initial-width 0.006
                    :initial-height 0.009
                    :end-width 0.012
                    :end-height 0.018
                    :variance 0.08
                    :dispersion -0.2
                    :direction :counter-clockwise
                    :seed 1001}
   :style-fn linear-fade
   :style-options {:initial-fill-color [228 222 212]
                   :final-fill-color [218 212 205]
                   :initial-fill-opacity 85
                   :final-fill-opacity 65
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn circle})

;; Layer 11: Sparse large wobbly rectangles
(def layer-11
  {:num-steps 6
   :layout-fn simple-grid
   :layout-options {:max-cols 3 :gap-x 0.05}
   :style-fn linear-fade
   :style-options {:initial-fill-color [182 175 165]
                   :final-fill-color [172 168 160]
                   :initial-fill-opacity 25
                   :final-fill-opacity 35
                   :initial-stroke-color [152 145 135]
                   :final-stroke-color [142 138 130]
                   :initial-stroke-width 2
                   :final-stroke-width 3
                   :initial-stroke-opacity 40
                   :final-stroke-opacity 55}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.12 :size-variance 0.15 :seed 1101}})

;; Layer 12: Tight inner spiral - accent color (muted teal)
(def layer-12
  {:num-steps 35
   :layout-fn chaotic-spiral
   :layout-options {:turns 2
                    :min-radius 0.02
                    :max-radius 0.2
                    :initial-width 0.01
                    :initial-height 0.01
                    :end-width 0.025
                    :end-height 0.025
                    :variance 0.12
                    :dispersion 0.0
                    :direction :clockwise
                    :seed 1201}
   :style-fn linear-fade
   :style-options {:initial-fill-color [158 175 172]
                   :final-fill-color [148 168 165]
                   :initial-fill-opacity 80
                   :final-fill-opacity 60
                   :initial-stroke-color [128 145 142]
                   :final-stroke-color [118 138 135]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 60
                   :final-stroke-opacity 45}
   :draw-fn circle})

;; Layer 13: Outer ring of medium circles
(def layer-13
  {:num-steps 20
   :layout-fn chaotic-spiral
   :layout-options {:turns 1
                    :min-radius 0.35
                    :max-radius 0.48
                    :initial-width 0.035
                    :initial-height 0.05
                    :end-width 0.045
                    :end-height 0.06
                    :variance 0.15
                    :dispersion 0.0
                    :direction :counter-clockwise
                    :seed 1301}
   :style-fn linear-fade
   :style-options {:initial-fill-color [192 185 178]
                   :final-fill-color [185 180 175]
                   :initial-fill-opacity 55
                   :final-fill-opacity 70
                   :initial-stroke-color [162 155 148]
                   :final-stroke-color [155 150 145]
                   :initial-stroke-width 1
                   :final-stroke-width 1
                   :initial-stroke-opacity 45
                   :final-stroke-opacity 55}
   :draw-fn circle})

;; Layer 14: Fine scattered rectangles
(def layer-14
  {:num-steps 50
   :layout-fn chaotic-spiral
   :layout-options {:turns 3
                    :min-radius 0.1
                    :max-radius 0.44
                    :initial-width 0.018
                    :initial-height 0.012
                    :end-width 0.028
                    :end-height 0.02
                    :variance 0.3
                    :dispersion 0.2
                    :direction :clockwise
                    :align-to-path true
                    :seed 1401}
   :style-fn linear-fade
   :style-options {:initial-fill-color [200 192 185]
                   :final-fill-color [188 182 178]
                   :initial-fill-opacity 65
                   :final-fill-opacity 85
                   :initial-stroke-width 0
                   :final-stroke-width 0
                   :initial-stroke-opacity 0
                   :final-stroke-opacity 0}
   :draw-fn rectangle
   :drawing-options {:vertex-variance 0.2 :seed 1402}})

;; Layer 15: Top accent - sparse floating circles (slightly warmer)
(def layer-15
  {:num-steps 12
   :layout-fn chaotic-spiral
   :layout-options {:turns 1
                    :min-radius 0.08
                    :max-radius 0.42
                    :initial-width 0.04
                    :initial-height 0.055
                    :end-width 0.055
                    :end-height 0.07
                    :variance 0.35
                    :dispersion 0.1
                    :direction :counter-clockwise
                    :seed 1501}
   :style-fn linear-fade
   :style-options {:initial-fill-color [205 195 185]
                   :final-fill-color [198 190 182]
                   :initial-fill-opacity 50
                   :final-fill-opacity 65
                   :initial-stroke-color [175 165 155]
                   :final-stroke-color [168 160 152]
                   :initial-stroke-width 1
                   :final-stroke-width 2
                   :initial-stroke-opacity 40
                   :final-stroke-opacity 55}
   :draw-fn circle})

(def config
  {:renderer :pdf
   :filename "abstract-layers.pdf"
   :canvas {:canvas-width 1200
            :canvas-height 800}
   :layers [layer-1
            layer-2
            layer-3
            layer-4
            layer-5
            layer-6
            layer-7
            layer-8
            layer-9
            layer-10
            layer-11
            layer-12
            layer-13
            layer-14
            layer-15]})

(defn -main [& _args]
  (bvm/vera-multi config))
