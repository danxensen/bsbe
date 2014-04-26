(ns bsbe.game.command
  (:use bsbe.game.actions))

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

(defn parse-command
  "parses an entire command definition"
  [rules & actions]
  (let [rules (parse-rules rules)]
    ; make a new id for the command, and return the rules and actions
    `{:id (keyword (gensym)) :rules ~(vec rules) :actions '~(vec actions)}))

; (make-commands [["this" | :that] "actions" (when (condition) (more actions))])
(defmacro make-commands
  "defines commands - rules for matching, and actions to take"
  [& commands]
  (vec (map #(apply parse-command %) `~commands)))

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
     (and (not no-symbols) (= top-symbol nil)) [[inputs (rest symbols)]]
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
           ; if coll, still more pairs to parse
           (coll? parsed) (recur (concat (rest inputs-symbols) parsed))
           ; if parsed, found match
           parsed true
           ; else no match this branch, continue checking other pairs
           :else (recur (rest inputs-symbols))))))))

(defn find-best-match
  "finds the best matching command for an input"
  [inputs commands]
  (let [symbols (keys commands) ; check each symbol in commands
        ; find the first that matches the input (or nil if none)
        match (first (filter #(match-command inputs % commands) symbols))]
    match))

(defn entities->commands
  "extracts and combines the commands from a collection of entities"
  [entities]
  (let [commands (map :commands entities)
        commands (reduce concat commands)]
    commands))

(defn convert-action
  "ensures action is converted to appropriate format"
  [action]
  (if (string? action)
    (message action)
    action))

(defn parse-actions
  "parses actions into a single state->state fn"
  [state actions]
  (binding [; bind *state*, used by the action conditions
            *state* state
            ; bind *ns*, because command action fns won't hold their ns
            *ns* (create-ns 'bsbe.game.actions)]
    (let [; eval actions and convert any that need it
          parse (comp convert-action eval)
          parsed-actions (map parse actions)
          parsed-actions (filter (comp not nil?) parsed-actions)
          parsed-actions (reverse parsed-actions) ; reverse before comping
          ; reduce actions into a single state->state action
          action (apply comp parsed-actions)]
      action)))

(defn process-command
  "finds the command for the given input and executes it"
  [{:keys [input areas location inventory dictionary unknown-command] :as state}]
  (let [; reset the message before processing
        state (assoc-in state [:message] "")
        ; split the "input string" by spaces, and use lower case
        inputs (clojure.string/split (clojure.string/lower-case input) #"\s")
        current-area (get areas location)
        ; match - commands -> id that matches input, or nil
        match #(find-best-match inputs
                                ; combine command rules and dictionary
                                (merge dictionary
                                       (commands->dictionary %)))
        ; find-match - commands -> command that matches input, or nil
        find-match (fn [commands] (first (filter #(= (:id %) (match commands))
                                                commands)))
        ; process - commands -> actions that match input, or nil
        process #(let [command (find-match %)
                       actions (:actions command)
                       action (parse-actions state actions)]
                   ; if actions nil, comp nil returns identity - use nil
                   (if (nil? actions)
                     nil
                     action))
        found-process (or ; use or to stop at the first match
                       ; check entities at current-area
                       (process (entities->commands (:entities current-area)))
                       ; check inventory
                       (process (entities->commands (:inventory state)))
                       ; check current-area
                       (process (:commands current-area))
                       ; check state
                       (process (:commands state))
                       ; or no matching command found
                       unknown-command)
        ; apply process to state
        state (found-process state)
        ; empty out the input now that it is processed
        state (assoc-in state [:input] "")]
    state))
