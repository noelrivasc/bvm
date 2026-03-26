(ns bvm.core
  (:require [clojure.spec.alpha :as s]
            [quil.core :as q]
            [bvm.specs]))

(defn- do-projection
  "Projects a transform map onto the canvas and returns a
  bvm.specs/drawing-geometry with the projection"
  [transform-map canvas]
  {:pre [(s/valid? :bvm.specs/transform-map transform-map)
         (s/valid? :bvm.specs/canvas canvas)]
   :post (s/valid? :bvm.specs/drawing-geometry %)}

  {:index (:index transform-map)
   :drawing-x (* (:x transform-map) (:canvas-width canvas))
   :drawing-y (* (:y transform-map) (:canvas-height canvas))
   :drawing-width (* (:width transform-map) (:canvas-width canvas))
   :drawing-height (* (:height transform-map) (:canvas-height canvas))
   :drawing-rotation (:rotation transform-map)})

(defn- generate-instructions-for-layer
  "Produces a vector of tuples in which the first item is the projected
  transform map, the second is the style map, and the third is the drawing
  options. Each tuple is meant to be fed to the drawing function. Canvas is
  passed separately to support multi-layer sketches where canvas is shared."
  [layer-conf canvas]
  (let [num-steps (:num-steps layer-conf)
        steps (take num-steps (range))
        layout-options (get layer-conf :layout-options {})
        style-options (get layer-conf :style-options {})
        drawing-options (get layer-conf :drawing-options {})
        transform-maps (mapv
                        #((:layout-fn layer-conf) % num-steps layout-options)
                        steps)
        drawing-geometries (mapv
                            #(do-projection % canvas)
                            transform-maps)
        style-config (:style-config layer-conf)
        style-options-with-num (assoc style-options :num-steps num-steps)
        style-maps (mapv #((:style-fn layer-conf) style-config % style-options-with-num) transform-maps)]
    (mapv vector drawing-geometries style-maps (repeat drawing-options))))

(defn generate-instructions
  "Produces a vector of tuples in which the first item is the projected
  transform map, and the second is the style map. Each tuple is meant to
  be fed to the drawing function."
  [conf]
  (generate-instructions-for-layer conf (:canvas conf)))

(defn vera
  "Main entrypoint of the program. Takes a full configuration map and
  orchestrates the generation of instructions, and the drawing."
  [conf]
  {:pre (s/valid? :bvm.specs/sketch-config conf)}

  (let [instructions (generate-instructions conf)
        draw-fn (:draw-fn conf)
        renderer (:renderer conf)
        width (get-in conf [:canvas :canvas-width])
        height (get-in conf [:canvas :canvas-height])
        ext (case renderer :pdf ".pdf" :java2d ".png")
        filename (str "out/" (:filename conf) ext)]
    (prn (str "Width: " width " height: " height " filename: " filename))
    (q/sketch
     :size [width height]
     :draw (fn []
             (try
               (case renderer
                 :pdf (q/do-record (q/create-graphics width height :pdf filename)
                                   (run! #(apply draw-fn %) instructions)
                                   (q/exit))
                 :java2d (do
                           (run! #(apply draw-fn %) instructions)
                           (q/save filename)
                           (q/exit)))
               (catch Exception e (println "Something went wrong:" (.getMessage e))))))))

(defn- draw-layer
  "Generates instructions for a layer and draws them using the layer's draw-fn."
  [layer-conf canvas]
  (let [instructions (generate-instructions-for-layer layer-conf canvas)
        draw-fn (:draw-fn layer-conf)]
    (run! #(apply draw-fn %) instructions)))

(defn vera-multi
  "Renders multiple layers to a single output. Takes a multi-sketch config
  with shared canvas/renderer settings and a vector of layer configs.
  Layers are drawn in order (later layers render on top)."
  [conf]
  {:pre (s/valid? :bvm.specs/multi-sketch-config conf)}

  (let [canvas (:canvas conf)
        layers (:layers conf)
        renderer (:renderer conf)
        width (:canvas-width canvas)
        height (:canvas-height canvas)
        ext (case renderer :pdf ".pdf" :java2d ".png")
        filename (str "out/" (:filename conf) ext)]
    (prn (str "Width: " width " height: " height " filename: " filename))
    (prn (str "Rendering " (count layers) " layers"))
    (q/sketch
     :size [width height]
     :draw (fn []
             (try
               (case renderer
                 :pdf (q/do-record (q/create-graphics width height :pdf filename)
                                   (doseq [layer layers]
                                     (draw-layer layer canvas))
                                   (q/exit))
                 :java2d (do
                           (doseq [layer layers]
                             (draw-layer layer canvas))
                           (q/save filename)
                           (q/exit)))
               (catch Exception e (println "Something went wrong:" (.getMessage e))))))))

