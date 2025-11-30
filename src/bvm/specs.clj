(ns bvm.specs
  (:require [clojure.spec.alpha :as s]))

; **************************************
; TRANSFORM MAP
; **************************************
; A transform map describes the normalized position, rotation and size of the object
; The position (x,y) and dimensions (width, height) are expressed as floats between 0 and 1,
; and represent a fraction of the canvas
;
; For example, if width is 0.25 and the canvas has a width of 600px, the object will have
; a width of 75px (600/4)
(s/def ::index int?)
(s/def ::x (s/and float? #(<= 0 % 1)))
(s/def ::y (s/and float? #(<= 0 % 1)))
(s/def ::rotation (s/and float? #(<= -360 % 360))) ; Rotation in degrees; can be negative
(s/def ::width (s/and float? #(<= 0 % 1)))
(s/def ::height (s/and float? #(<= 0 % 1)))

(s/def ::transform-map
  (s/keys :req-un [::index ::x ::y ::rotation ::width ::height]))

; **************************************
; STYLE MAP
; **************************************
; A Style map describes the visual characteristics of an object
(s/def ::rgb-color (s/tuple (s/int-in 0 256) (s/int-in 0 256) (s/int-in 0 256)))
(s/def ::fill-color ::rgb-color)
(s/def ::fill-opacity (s/int-in 0 256))
(s/def ::stroke-color ::rgb-color)
(s/def ::stroke-width int?)
(s/def ::stroke-opacity (s/int-in 0 256))

(s/def ::style-map
  (s/keys :req-un [::fill-color ::fill-opacity ::stroke-color ::stroke-width ::stroke-opacity]))

; **************************************
; DRAWING GEOMETRY
; **************************************
; The drawing geometry map describes the actual dimensions and position of an object in
; the canvas. These are not floats between 0 and 1 that represent a fraction of the canvas,
; but actual pixel values that can be used directly for drawing.
(s/def ::drawing-x int?)
(s/def ::drawing-y int?)
(s/def ::drawing-rotation (s/and float? #(<= -360 % 360)))
(s/def ::drawing-width int?)
(s/def ::drawing-height int?)

(s/def ::drawing-geometry
  (s/keys :req-un [::drawing-x ::drawing-y ::drawing-rotation ::drawing-width ::drawing-height]))

; **************************************
; SKETCH CONFIGURATION OBJECT
; **************************************
(s/def ::style-defaults ::style-map)
(s/def ::style-config
  (s/keys :req-un [::style-defaults]))

(s/def ::renderer #{:pdf :canvas})

(s/def ::canvas-width int?)
(s/def ::canvas-height int?)
(s/def ::canvas
  (s/keys :req-un [::canvas-width ::canvas-height]))

; The number of objects to draw / steps to iterate on
(s/def ::num-steps int?)

(s/def ::draw-fn ifn?)
(s/def ::layout-fn ifn?)
(s/def ::style-fn ifn?)

(s/def ::sketch-config
  (s/and
   (s/keys :req-un [::renderer ::canvas ::num-steps ::layout-fn ::style-fn ::draw-fn]
           :opt-un [::style-config])
   (fn [conf]
     (or (not= (:renderer conf) :pdf)
         (contains? conf :filename)))))

; **************************************
; FUNCTION SPECS
; **************************************
; Layout function
(s/def ::index (s/or :positive-integer pos-int? :zero #(= % 0)))
(s/def ::layout-fn-spec
  (s/fspec :args (s/cat :index ::index :num-steps ::num-steps)
           :ret ::transform-map))

; Style function
(s/def ::style-fn-spec
  (s/fspec :args (s/cat :transform-map ::transform-map :style-config ::style-config)
           :ret ::style-map))

; Drawing function
(s/def ::draw-fn-spec
  (s/fspec :args (s/cat :drawing-geometry ::drawing-geometry :style-map ::style-map)
           :ret nil?))


