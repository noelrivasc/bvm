(ns bvm.sketches.rec-wave-progression-demo
  "Throwaway sketch exercising every progression + the easing hook of
   bvm.fields.rec-wave-progression.

   Each band should visibly differ along every axis so a regression
   in any one progression is spottable."
  (:require
   [bvm.core :as bvm]
   [bvm.utils.interpolation :refer [ease-in-out]]
   [bvm.fields.rec-wave-progression :refer [rec-wave-progression]]))

(def field-config
  {:num-bands 25
   :blend-mode :multiply

   ;; particles-per-band progression (linear)
   :particles-per-band-initial 40
   :particles-per-band-final 140

   ;; Layout progressions — each with its own easing
   :amplitude-initial 0.0
   :amplitude-final 0.06
   :amplitude-ease ease-in-out

   :frequency-initial 1.0
   :frequency-final 3.0

   :length-initial 0.6
   :length-final 0.95

   :offset-initial -0.02
   :offset-final 0.02

   :field-position-initial 0.1
   :field-position-final 0.9

   :obj-width-initial 0.005
   :obj-width-final 0.08
   :obj-width-ease ease-in-out

   :obj-height-initial 0.002
   :obj-height-final 0.12

   :vertex-variance-initial 0.0
   :vertex-variance-final 0.35

   :layout-variance-initial 0.0
   :layout-variance-final 0.2

   :size-variance-initial 0.0
   :size-variance-final 0.15

   :phase false
   :direction :horizontal

   ;; 4-corner style params — vary on both axes to exercise the full grid.
   ;; Colours are LCH [L C H]: L=[0,100], C=[0,~130], H degrees.
   :stroke-width-initial-start 1
   :stroke-width-initial-end 1
   :stroke-width-final-start 3
   :stroke-width-final-end 0

   :stroke-color-initial-start [25 30 280]   ; dark indigo
   :stroke-color-initial-end [25 45 240]     ; dark blue
   :stroke-color-final-start [30 60 20]      ; dark red
   :stroke-color-final-end [20 40 310]       ; dark magenta
   :stroke-color-field-ease ease-in-out

   :stroke-opacity-initial-start 180
   :stroke-opacity-initial-end 60
   :stroke-opacity-final-start 60
   :stroke-opacity-final-end 180

   :fill-color-initial-start [90 15 85]      ; cream
   :fill-color-initial-end [85 15 240]       ; pale blue
   :fill-color-final-start [55 55 10]        ; warm red
   :fill-color-final-end [60 40 260]         ; dusty blue

   :fill-opacity-initial-start 220
   :fill-opacity-initial-end 20
   :fill-opacity-final-start 180
   :fill-opacity-final-end 10

   :color-space :srgb

   :seed-base-layout 100
   :seed-step-layout 17
   :seed-base-drawing 500
   :seed-step-drawing 23})

(def config
  {:renderer :java2d
   :filename "rec-wave-progression-demo"
   :background-color [245 240 232]
   :canvas {:canvas-width 1200
            :canvas-height 1600}
   :layers (rec-wave-progression field-config)})

(defn -main [& _args]
  (bvm/vera-multi config))
