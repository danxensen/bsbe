(ns bsbe.game.uis.core)

(defn animate-message
  "animates a message being written one character at a time"
  [state]
  ; must continually re-grab animated-message and -index from state
  ; because we make a few changes to state to update them
  (let [message (:message state)
        message-changed? (not= message (:current-message state))
        ; check if the animation needs to be restarted
        state (if message-changed?
                (-> state
                    (assoc-in [:animated-message] "")
                    (assoc-in [:current-message] message)
                    (assoc-in [:animated-index] 0))
                state)
        ; increment how much of the message is displayed
        index-low? (< (:animated-index state) (count (:current-message state)))
        state (if index-low?
                (update-in state [:animated-index] inc)
                state)
        ; take first x out of the message
        new-message (take (:animated-index state) (:current-message state))
        state (assoc-in state [:animated-message] new-message)]
    state))
