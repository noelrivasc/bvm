(ns bvm.sketches.cumple-ma-rings
  (:require
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.fields.rec-ring-progression :refer [rec-ring-progression]]))

(def palette
  {:light [40 10 10]
   :dark [10 0 0]})

(def artwork
  (let [canvas-width-mm 140
        canvas-height-mm 140
        in-to-mm 25.4
        canvas-width-in (/ canvas-width-mm in-to-mm)
        canvas-height-in (/ canvas-height-mm in-to-mm)
        canvas-dpi 300]
    {:canvas-width-mm canvas-width-mm
     :canvas-height-mm canvas-height-mm
     :canvas-width (Math/round (* canvas-width-in canvas-dpi))
     :canvas-height (Math/round (* canvas-height-in canvas-dpi))}))

(def field-config
  {:num-rings 36
   :particles-per-ring-initial 17
   :particles-per-ring-final 360
   :blend-mode :blend

   :align-to-path true

   ;; Inner -> outer ring
   :ellipse-width-initial 0.08
   :ellipse-width-final 0.7
   :ellipse-height-initial 0.08
   :ellipse-height-final 0.7

   ;; Full ring, slight rotational drift across field
   :start-initial 0.0
   :start-final 0.15
   :completion-initial 1.0

   :center-x-initial 0.5
   :center-y-initial 0.4

   ;; Small tangent-aligned rects; grow slightly outward
   :obj-width-initial 0.03
   :obj-height-initial 0.03
   :obj-width-final 0.003
   :obj-height-final 0.003

   ;; Layout jitter grows outward
   :layout-variance-initial 0.0
   :layout-variance-final 0.12

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.05

   :size-variance-initial 0.0
   :size-variance-final 0.2

   ;; Fill off, stroke carries the colour.
   :fill-opacity-initial-start 1
   :fill-opacity-initial-end 1
   :fill-opacity-final-start 0
   :fill-opacity-final-end 0

   :fill-color-initial-start (:dark palette)
   :fill-color-final-start (:light palette)

   ;; Stroke colour: magenta at the inner ring, gold at the outer ring.
   ;; Within each ring, fade from the ring's base hue toward the other
   ;; pole so the two fields cross-hatch perceptually.
   :stroke-color-initial-start (:dark palette)
   :stroke-color-initial-end   (:dark palette)
   :stroke-color-final-start   (:light palette)
   :stroke-color-final-end     (:light palette)

   :stroke-width-initial-start 1
   :stroke-width-initial-end 1
   :stroke-width-final-start 1
   :stroke-width-final-end 1

   :stroke-opacity-initial-start 255
   :stroke-opacity-initial-end 180
   :stroke-opacity-final-start 200
   :stroke-opacity-final-end 140

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

(def config
  {:renderer :java2d
   :filename "cumple-ma-rings"
   :canvas {:canvas-width (:canvas-width artwork)
            :canvas-height (:canvas-height artwork)}
   :layers (rec-ring-progression field-config)})

(defn -main [& _args]
  (bvm/vera-multi config))
