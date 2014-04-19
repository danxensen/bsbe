(ns bsbe.game.areas.hotelroom
  (:use [bsbe.game.command]
        [bsbe.game.actions]))

(defentity hotelbed
  (make-commands [[:look "bed"] "It's a well aged hotel bed. The blanket has an atrocious mixture of brown and green pattern. The springs squeek with the sigh of a decade's use."]))

(defarea hotelroom
  "The hotel room you are staying at this weekend."
  [hotelbed] ; entities
  (make-commands)
  {})
