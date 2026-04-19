(ns bvm.sketches.ring-progression-demo
  "Demo for bvm.fields.rec-ring-progression: concentric ring strokes
   progressing from a small inner ring to a large outer ring, with LCH
   colour interpolation on both the field axis and within each ring."
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.fields.rec-ring-progression :refer [rec-ring-progression]]))

(def palette
  {:gold        [70    88.93 88.41]
   :magenta     [37.55 72.08 350.19]
   :dead-purple [9.41  17.4  286.89]})

(def artwork
  {:background-rgb (color/lch->rgb-bytes (:dead-purple palette) :srgb)
   :canvas-width 3000
   :canvas-height 3000})

(def field-config
  {:num-rings 48
   :particles-per-ring-initial 90
   :particles-per-ring-final 360
   :blend-mode :blend

   :align-to-path true

   ;; Inner -> outer ring
   :ellipse-width-initial 0.08
   :ellipse-width-final 0.92
   :ellipse-height-initial 0.08
   :ellipse-height-final 0.92

   ;; Full ring, slight rotational drift across field
   :start-initial 0.0
   :start-final 0.15
   :completion-initial 1.0

   :center-x-initial 0.5
   :center-y-initial 0.5

   ;; Small tangent-aligned rects; grow slightly outward
   :obj-width-initial 0.004
   :obj-width-final 0.012
   :obj-height-initial 0.0015
   :obj-height-final 0.004

   ;; Layout jitter grows outward
   :layout-variance-initial 0.0
   :layout-variance-final 0.12

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.25

   :size-variance-initial 0.0
   :size-variance-final 0.2

   ;; Fill off, stroke carries the colour.
   :fill-opacity-initial-start 0
   :fill-opacity-initial-end 0
   :fill-opacity-final-start 0
   :fill-opacity-final-end 0

   :fill-color-initial-start (:magenta palette)
   :fill-color-final-start (:gold palette)

   ;; Stroke colour: magenta at the inner ring, gold at the outer ring.
   ;; Within each ring, fade from the ring's base hue toward the other
   ;; pole so the two fields cross-hatch perceptually.
   :stroke-color-initial-start (:magenta palette)
   :stroke-color-initial-end   (:gold palette)
   :stroke-color-final-start   (:gold palette)
   :stroke-color-final-end     (:magenta palette)

   :stroke-width-initial-start 1
   :stroke-width-initial-end 1
   :stroke-width-final-start 2
   :stroke-width-final-end 2

   :stroke-opacity-initial-start 200
   :stroke-opacity-initial-end 40
   :stroke-opacity-final-start 220
   :stroke-opacity-final-end 60

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

(def config
  {:renderer :java2d
   :filename "ring-progression-demo"
   :background-color (:background-rgb artwork)
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers (rec-ring-progression field-config)})

(defn -main [& _args]
  (bvm/vera-multi config))
