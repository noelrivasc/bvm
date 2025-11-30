(ns bvm.core
  (:require [clojure.spec.alpha :as s]
            [quil.core :as q]
            [bvm.specs]))

; Top-level control surface of Bonjour Vera
; These are the only non-dev entrypoints

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
  transform map, and the second is the style map."
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

(defn vera [conf]
  {:pre (s/valid? :bvm.specs/sketch-config conf)}
  (prn "We got this far!")

  (let [instructions (generate-instructions conf)
        draw-fn (:draw-fn conf)
        width (get-in conf [:canvas :canvas-width])
        height (get-in conf [:canvas :canvas-height])
        filename (:filename conf)]
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

; Um... pensando en casos realistas. El dibujo es una función de ¿qué?
; La retícula de cuadrados con perspectiva: dimensiones? min por fila, max por fila
; > un objeto de configuración específico para el layout
; El layout recibe una función de estilos, que recibe datos de layout y estilos
; predeterminados; su valor de retorno es un mapa de estilos

; Entonces, las funciones de layout tienen por entrada:
; - Un mapa de configuración, cuyo spec les es particular
; - Un mapa de estilos predeterminados
; - Una función de override de estilos
; Y su valor de retorno es un vector de mapas que describen tanto
; la geometría a dibujar, sus transformaciones y estilos

; Ahora... ¿qué pasa si quiero usar el mismo layout pero con círculos en vez de cuadrados?
; Mientras pase un generador compatible (en ese caso, sólo tiene 1 valor de tamaño), lo puedo
; reemplazar.
; ¿Qué pasa si quiero usar un rectángulo? - pues parece que necesito entonces un layout con
; geometría de 2 dimensiones. Podría pensar entonces que el layout toma un conjunto de funciones
; unidimensionales que obtienen una posición de una dimensión (step), un objeto de configuración, 
; y a partir de eso producen valores también unidimensionales.
; En ese escenario,habría una función para X, una para Y, una para escala, otra para rotación, etc

; Ejemplo de llamada a layout:
(defn resolve-layout [_] nil)
(resolve-layout
 {:x (fn [step] (* step 2))
  :y (fn [step] (+ step 1))
  :rotation (fn [step] (+ step 1))
  :scale (fn [step] (+ step 1))})

; Ejemplo de una llamada a generación de estilos para un item
(defn resolve-style [_] nil) ;
(defn some-drawing-fn [layout style] nil) ; esta función es la que dibuja usando Quil
(resolve-style
 {:stroke-width (fn [item-layout] (* (:x item-layout) 2))
  :stroke-color (fn [_] "#333333")
  :fill-color (fn [_] "#333333")
  :opacity (fn [_] 0.5)
  :drawing-fn some-drawing-fn})

; Y entonces, el flujo de un Vera iría completo algo como:
; config tiene:
; - layout-fn which is a function that takes step and produces a map of transform characteristics
; - style-fn is a function that takes a transform map and produces a style map
; - canvas a map that defines the canvas size
; - n-steps int, number of steps or items
; - include-zero, whether to start at 0 or the first positive value
; - include-one, whether to end at 1 or a step below that
; DEFINE: transform map, style map
; DEFINE: Layout fn (arg int step; ret transform map)
; DEFINE: style fn (arg transform map; ret style map)
; DEFINE: draw fn (args transform map, style map; ret nil)
; DEFINE: config. Has canvas (size), num steps, layout-fn, style-fn, layout-config, style config

