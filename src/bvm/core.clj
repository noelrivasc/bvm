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

  {:drawing-x (* (:x transform-map) (:canvas-width canvas))
   :drawing-y (* (:y transform-map) (:canvas-height canvas))
   :drawing-width (* (:width transform-map) (:canvas-width canvas))
   :drawing-height (* (:height transform-map) (:canvas-height canvas))
   :drawing-rotation (:rotation transform-map)})

(defn generate-instructions
  "Produces a vector of tuples in which the first item is the projected
  transform map, and the second is the style map. Each tuple is meant to
  be fed to the drawing function."
  [conf]
  (let [num-steps (:num-steps conf)
        steps (take num-steps (range))
        transform-maps (mapv
                        #((:layout-fn conf) % num-steps)
                        steps)
        drawing-geometries (mapv
                            #(do-projection % (:canvas conf))
                            transform-maps)
        style-config (:style-config conf)
        style-maps (mapv #((:style-fn conf) style-config %) transform-maps)]
    (mapv vector drawing-geometries style-maps)))

(defn vera
  "Main entrypoint of the program. Takes a full configuration map and
  orchestrates the generation of instructions, and the drawing."
  [conf]
  {:pre (s/valid? :bvm.specs/sketch-config conf)}

  (let [instructions (generate-instructions conf)
        draw-fn (:draw-fn conf)
        width (get-in conf [:canvas :canvas-width])
        height (get-in conf [:canvas :canvas-height])
        filename (str "out/" (:filename conf))]
    (prn (str "Width: " width " height: " height " filename: " filename))
    (q/sketch
     :draw (fn []
             (try
               (q/do-record (q/create-graphics
                             width
                             height
                             :pdf filename)
                            (run! #(apply draw-fn %) instructions)
                            (q/exit))

               (catch Exception e (println "Something went wrong:" (.getMessage e))))))))

