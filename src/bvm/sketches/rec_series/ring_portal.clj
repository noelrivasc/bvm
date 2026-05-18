(ns bvm.sketches.rec-series.ring-portal
  (:require
   [clojure.tools.cli :refer [parse-opts]]
   [clojure.string :as string]
   [bvm.core :as bvm]
   [bvm.utils.color :as color]
   [bvm.fields.rec-ring-progression :refer [rec-ring-progression]])
  (:import
   [java.util Random]))

(def palettes
  {:chromo
   {:bg [80.56 10.76 101.48]
    :c1 [80 54.13 255]
    :c2 [75.59 102.47 70.94]}})

(def base-ring ;; a configuration map for the rec-ring-progression field
  {:num-rings 67
   :particles-per-ring-initial 90
   :particles-per-ring-final 360
   :blend-mode :multiply

   :align-to-path true

   ;; Inner -> outer ring
   :ellipse-width-initial 0.08
   :ellipse-width-final 0.43
   :ellipse-height-initial 0.08
   :ellipse-height-final 0.43

   ;; Full ring, slight rotational drift across field
   :start-initial 0.0
   :start-final 0.15
   :completion-initial 1.0

   :center-x-initial 0.5
   :center-y-initial 0.5

   ;; Small tangent-aligned rects; grow slightly outward
   :obj-width-initial 0.04
   :obj-height-initial 0.02
   :obj-width-final 0.02
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

   :fill-color-initial-start nil
   :fill-color-final-start nil
   :stroke-color-initial-start nil
   :stroke-color-initial-end nil
   :stroke-color-final-start nil
   :stroke-color-final-end nil

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

(defn colorize [ring palette]
  (assoc ring
         :fill-color-initial-start (:c1 palette)
         :fill-color-final-start (:c1 palette)

         :stroke-color-initial-start (:c1 palette)
         :stroke-color-initial-end   (:c1 palette)
         :stroke-color-final-start   (:c2 palette)
         :stroke-color-final-end     (:c2 palette)))

(defn random-ring
  ([base seed] (random-ring base seed 0.9 0.9))
  ([base seed w-scale h-scale]
   (let [rng (Random. seed)
         wi (* w-scale (.nextDouble rng))
         hi (* h-scale (.nextDouble rng))
         wf (* w-scale (.nextDouble rng))
         hf (* h-scale (.nextDouble rng))]

     (assoc base
            :ellipse-width-initial wi
            :ellipse-height-initial hi
            :ellipse-width-final wf
            :ellipse-height-final hf))))

(defn do-config [cli-opts] ;; cli-opts: the full map produced by parse-opts
  (let [options (:options cli-opts)
        seed (:seed options)
        px-width (:width options)
        px-height (:height options)
        palette-id (:palette options)
        palette (palette-id palettes)
        _ (prn palette-id)
        _ (prn palettes)
        _ (prn palette)
        randomized (random-ring base-ring seed)
        colorized (colorize randomized palette)
        ring (rec-ring-progression colorized)]
    {:renderer :java2d
     :filename (str "ring-portal--s" seed "--" (name palette-id) "--" px-width "x" px-height)
     :background-color (color/lch->rgb-bytes (:bg palette) :srgb)
     :canvas {:canvas-width px-width
              :canvas-height px-height}
     :layers ring}))

(def cli-options
  [["-s" "--seed SEED" "Seed Number"
    :default 1
    :parse-fn #(Integer/parseInt %)]
   ["-w" "--width WIDTH" "Width in pixels"
    :default 1200
    :parse-fn #(Integer/parseInt %)]

   ["-h" "--height HEIGHT" "Height in pixels"
    :default 1200
    :parse-fn #(Integer/parseInt %)]
   ["-p" "--palette PALETTE" "Palette name"
    :default :chromo
    :parse-fn #(keyword %)]])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn -main [& args]
  (let [opts (parse-opts args cli-options)
        errors (get-in opts [:errors])]
    (if errors
      (error-msg errors)
      (bvm/vera-multi (do-config opts)))))

