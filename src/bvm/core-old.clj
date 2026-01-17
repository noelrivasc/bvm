(ns bvm.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn mm-to-px
  [mm]
  (/ (* mm 300) 25.4))

;; Anatomy of a drawing sketch
;; The q/sketch call is merely a wrapper of the setup
;; The setup function is where all the action happens; setup must call q/exit so there is no looping
;; Each generator has two components: a state generator that takes a seed and produces a drawing instruction, and a drawing function that takes the drawing instruction and produces graphics according to it
;; Each generator also has input and instruction specs, so they can be used with generators and for pre validation
;; An arpeggiator produces a sequence of instructions
;; A layout engine takes a configuration engine, a drawing function and a vector of instructions produced by the arpeggiator
;; The layout engine calculates the position and rotation for each generated object and calls the drawing function, wrapped in with-translation and with-rotation as needed

;; Simplest generator: draws a circle of a given radius
;; Simplest arpeggiator: just passes the arguments received to the instruction generator, produces vec of 1 item
;; Simplest layout engine: just places first object at given coordinates, discards the rest

;; The layout engine calculates centers (not top-left corners) so it's OK for generators to start drawing at 0,0

(defn spawn-drawing
  [generator-fn definition layout]
  (println "Spawning a drawing:" definition layout)
  (let [default-translation [0 0]
        default-rotation [0]
        translation (:translation layout default-translation)
        rotation (:rotation layout default-rotation)]
    (q/with-translation translation
      (q/with-rotation rotation
        (generator-fn definition)))))

(defn draw [config layout-fn iterator-fn definition-generator generator-fn]
  (when (:debug config) (println "Entering draw ✍️"))
  (let [definitions (iterator-fn definition-generator (:definition-generator config))
        layout (layout-fn (:layout config))]
    (mapv #(spawn-drawing generator-fn %1 %2) definitions layout)))

(defn create-sketch [config vera]
  (q/sketch
   :draw (fn []
           (when (:debug config) (println "Entering :setup"))
           (try
             (q/do-record (q/create-graphics
                           (get-in config [:canvas :width])
                           (get-in config [:canvas :height])
                           :pdf (:file-path config))
                          (vera)
                          (q/exit))
             (catch Exception e (println "Something went wrong: " (.getMessage e)))))))

(comment
  (defn layout-single-item [config]
    [{:translation [(:x config) (:y config)]}])

  (defn draw-circle [i]
    (q/no-fill)
    (q/stroke 60)
    (q/stroke-weight 2)
    (q/ellipse 0 0 (* 2 (:radius i)) (* 2 (:radius i))))

  (defn generate-circle [config]
    {:radius (:radius config)})

  (defn generate-single-item [definition-generator definition-generator-config]
    [(definition-generator definition-generator-config)])

  (def sketch-config {:debug true
                      :file-path "vera-out.pdf"
                      :canvas {:width (mm-to-px 100)
                               :height (mm-to-px 100)}
                      :definition-generator {:radius 20}
                      :layout {:x 100 :y 100}})
  (create-sketch sketch-config #(draw sketch-config layout-single-item generate-single-item generate-circle draw-circle)))

;; #_(defn setup []
;;   ; Set frame rate to 30 frames per second.
;;     (q/frame-rate 30)
;;   ; Set color mode to HSB (HSV) instead of default RGB.
;;     (q/color-mode :hsb)
;;   ; setup function returns initial state. It contains
;;   ; circle color and position.
;;     {:color 0
;;      :angle 0})
;; 
;; #_(defn update-state [state]
;;   ; Update sketch state by changing circle color and position.
;;     {:color (mod (+ (:color state) 0.7) 255)
;;      :angle (+ (:angle state) 0.01)})
;; 
;; #_(defn draw-state [state]
;;   ; Clear the sketch by filling it with light-grey color.
;;     (q/background 240)
;;   ; Set circle color.
;;     (q/fill (:color state) 255 255)
;;   ; Calculate x and y coordinates of the circle.
;;     (let [angle (:angle state)
;;           x (* 150 (q/cos angle))
;;           y (* 150 (q/sin angle))]
;;     ; Move origin point to the center of the sketch.
;;       (q/with-translation [(/ (q/width) 2)
;;                            (/ (q/height) 2)]
;;       ; Draw the circle.
;;         (q/ellipse x y 100 100))))
;; 
;; #_{:clj-kondo/ignore [:unresolved-symbol]}
;; #_(q/defsketch quil-pdf
;;     :title "You spin my circle right round"
;;     :size [500 500]
;;   ; setup function called only once, during sketch initialization.
;;     :setup setup
;;   ; update-state is called on each iteration before draw-state.
;;     :update update-state
;;     :draw draw-state
;;     :features [:keep-on-top]
;;   ; This sketch uses functional-mode middleware.
;;   ; Check quil wiki for more info about middlewares and particularly
;;   ; fun-mode.
;;     :middleware [m/fun-mode])
;; 
;; #_(defn setup []
;;   {:obj []})
;; 
;; #_(defn generate-random-points
;;   "Generate a flat vector of N x, y point pairs.
;;    Example [x1 y1 x2 y2 x3 y3... xn yn]"
;;   [n x-min x-max y-min y-max]
;;   (let [x-range (- x-max x-min)
;;         y-range (- y-max y-min)
;;         random-pairs (repeatedly n
;;                                  (fn []
;;                                    (let [r (q/random-2d)]
;;                                      [(+ x-min (* (abs (first r)) x-range))
;;                                       (+ y-min (* (abs (second r)) y-range))])))]
;;     (vec (apply concat random-pairs))))
;; 
;; #_(defn draw! [_]
;; 
;;   ;; Draw an ellipse at 100, 100, radius 10
;;   (q/ellipse 100 100 20 20)
;;   ;; Draw a simple curve
;;   (q/curve 200 200 250 250 200 300 150 250)
;;   ;; Draw the same curve, with translation
;;   (q/with-translation [100 100]
;;     (q/fill 100)
;;     (q/curve 200 200 250 250 200 300 150 250))
;; 
;;   ;; Draw a dark square, and try to draw a full curve inside
;;   (q/with-translation [200 100]
;;     (q/fill 190)
;;     (q/no-stroke)
;;     (q/rect 0 0 100 100)
;;     (q/no-fill)
;;     (q/stroke-weight 1)
;;     (q/stroke 30)
;;     (q/curve-tightness -2)
;;     (q/curve 0 0 0 0 100 100 100 0))
;; 
;;   (q/exit)
;;   #_(let [points (generate-random-points 10 1 90 1 90)
;;           chunks (partition 8 6 points)]
;;       (println points)
;;       (println chunks)
;;       (q/no-fill)
;;       (q/stroke-weight 4)
;;       (q/stroke 120)
;;       (q/with-translation [200 200 0]
;;         (doseq [chunk chunks]
;;           (apply q/curve chunk)))))
;; 
;; #_(q/sketch
;;  ; Setting the renderer to PDF is another way, but
;;  ; using (q/do-record) is the one recommended in the
;;  ; documentation.
;;  ; :renderer :pdf
;;  ; :size [1000 1000]
;;  ; :output-file "something.pdf"
;; 
;;  :draw (fn [state]
;;          (try
;;            (q/do-record (q/create-graphics (mm-to-px 100) (mm-to-px 100) :pdf "out.pdf")
;;                         (draw! state))
;;            (catch Exception e (println "Something went wrong: " (.getMessage e))))
;;          (q/exit))
;;  :setup setup
;;  :middleware [m/fun-mode])
