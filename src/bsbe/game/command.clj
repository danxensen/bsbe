(ns bsbe.game.command
  (:refer bsbe.game.actions))

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
        rules (filter #(not= '(|) %) rules)
        rules (map vec rules)]
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
    `{:id (keyword (gensym)) :rules ~(vec rules) :actions ~(vec actions)}))

; (make-commands [["this" | :that] "actions" (to take)]
(defmacro make-commands
  "defines commands - rules for matching, and actions to take"
  [& commands]
  (vec (map #(apply parse-command %) commands)))

(defn commands->dictionary
  "extracts dictionary entries from commands"
  [commands]
  (into {} (map #(hash-map (:id %) (:rules %)) commands)))

(defn parse-dictionary-entry
  "parses a dictionary entry [name [rules]]"
  [[name rules]]
  `[~name '~(parse-rules rules)])

; (make-command-dictionary [:key ["this" | :that]])
(defmacro make-command-dictionary
  "defines the dictionary used by the game commands"
  [& dictionary-entries]
  (into {} (map parse-dictionary-entry dictionary-entries)))

; (defentity thing (make-commands) state)
(defmacro defentity
  "defines an entity"
  ([name commands]
     `(defentity ~name ~commands {}))
  ([name commands state]
     `(defn ~name []
        {:id (keyword '~name) :commands ~commands :state ~state})))

; (defarea place "description" [entities] (make-commands) state)
(defmacro defarea
  "defines an area"
  ([name description entities commands]
     `(defarea ~name ~description ~entities ~commands {}))
  ([name description entities commands state]
     (let [entities (map eval entities)]
       `(defn ~name []
          {:id (keyword '~name) :description ~description :entities ~entities :commands ~commands :state ~state}))))

(defn non-terminal?
  "determines if a symbol is non-terminal"
  [symbol]
  (keyword? symbol))

(defn expand
  "expands a non-terminal symbol's rules"
  [expand-symbol commands inputs symbols]
  (let [expansions (get commands expand-symbol) ; get the symbol expansions
        ; add the remaining symbols to each of the new expansions
        symbol-lists (map #(concat % symbols) expansions)
        ; group the inputs with the symbol lists
        new-parses (map #(list inputs %) symbol-lists)]
    new-parses))

(defn parse-command-grammar
  "parses an input against a specific rule"
  [inputs symbols commands]
  (let [top-input (first inputs)
        top-symbol (first symbols)
        no-inputs (empty? inputs)
        no-symbols (empty? symbols)]
    (cond
     ; success - both inputs and symbols empty
     (and no-inputs no-symbols) true
     ; nil/empty - just drop the nil symbol and continue
     (= top-symbol nil) [[inputs (rest symbols)]]
     ; match - input and symbol are equal
     (= top-input top-symbol) [[(rest inputs) (rest symbols)]]
     ; expand - non-terminal symbol, get its expansions
     (non-terminal? top-symbol) (expand top-symbol commands inputs (rest symbols))
     :else false)))

(defn match-command
  "determines if an input matches a command's rules"
  [inputs rule commands]
  (let [symbols [rule]] ; start symbol stack with the selected rule
    (loop [inputs-symbols [[inputs symbols]]] ; list of input-symbol pairs
      (if (empty? inputs-symbols) ; if no more pairs, didn't match
        false
        (let [[inputs symbols] (first inputs-symbols) ; get first pair
              parsed (parse-command-grammar inputs symbols commands)] ; parse it
          (cond
           ; if seq, still more pairs to parse
           (seq? parsed) (recur (concat (rest inputs-symbols) parsed))
           ; if parsed, found match
           parsed true
           ; else no match this branch, continue checking other pairs
           :else (recur (rest inputs-symbols))))))))

(defn find-best-match
  "finds the best matching command for an input"
  [inputs commands]
  (let [symbols (keys commands) ; check each symbol in commands
        ; find the first that matches the input
        match (first (filter #(match-command inputs % commands) symbols))]
    match))
