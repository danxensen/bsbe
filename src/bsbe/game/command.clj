(ns bsbe.game.command)

; commands are usually of the form
;   verb
;   verb object
;   verb specifiers object
; express commands using this grammar
;   :keywords are non-terminal and map to more symbols
;   "strings" are terminal and directly match inputs
;   use | to separate alternatives
;     :get -> "get" | "take"
; when using make-command, only specify the right-hand side for this command
; full mappings are defined in the command dictionary
;   using make-command-dictionary

(defn parse-rules
  "parses the right-hand side of a command grammar rule"
  [rules]
  (let [rules (partition-by #(= '| %) rules)
        rules (filter #(not= '(|) %) rules)]
    rules))

(defn convert-action
  "ensures action is converted to appropriate format"
  [action]
  (if (string? action)
    `(bsbe.game.actions/message ~action)
    action))

(defn parse-command
  "parses an entire command definition"
  [rules & actions]
  (let [rules (parse-rules rules)
        actions (map convert-action actions)]
    ; make a new id for the command, and return the rules and actions
    `{:id (gensym) :rules '~rules :actions '~actions}))

(defmacro make-commands
  "defines commands - rules for matching, and actions to take"
  [& commands]
  (vec (map #(apply parse-command %) commands)))

(defn parse-dictionary-entry
  "parses a dictionary entry [name [rules]]"
  [[name rules]]
  `[~name '~(parse-rules rules)])

(defmacro make-command-dictionary
  "defines the dictionary used by the game commands"
  [& dictionary-entries]
  (into {} (map parse-dictionary-entry dictionary-entries)))
