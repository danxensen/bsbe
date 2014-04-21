(ns bsbe.game.areas.hotelroom
  (:use [bsbe.game.command]
        [bsbe.game.actions]
        [bsbe.game.util]))

(defentity hotelbed
  (make-commands [[:look "bed"] "It's a well aged hotel bed. The blanket has an atrocious mixture of brown and green pattern. The springs squeek with the sigh of a decade's use."]))

(defarea hotelroom
  "Your hotel room"
  [hotelbed]
  (make-commands [[:look]
                  "A one-bed hotel room. The decor reminds you of your parents' generation - old and out of style."
                  (when (entity-here? :hotelbed) "The room is well lit from the window, its curtains too thin to block much light.")
                  "The bathroom is near the exit, to the south."]))
