(ns teste-blockchain.core
  (:require [clojure.data.json :as json])
  (:import [java.security MessageDigest]
           [java.util Date])
  (:gen-class))




;;gerar hash a patir de string
(defn sha256 [s]
  (let [digest (MessageDigest/getInstance "SHA-256")]
    (->> (.digest digest (.getBytes s "UTF-8"))
         (map #(format "%02x" %))
         (apply str))))

;;gerar hash a partir de dados
(defn get-hash [index nonce transacao hash_antecessor]
  (sha256 (str index nonce transacao hash_antecessor)))

;;estruturar bloco
(defn bloco [index nonce transacao hash_antecessor hash]
  {:index index
   :nonce nonce
   :transacao transacao
   :hash_anterior hash_antecessor
   :hash hash})

;;minerar até conseguir o nonce
(defn mine [index transacao hash_antecessor]
  (loop [nonce 0]
    (let [hash (get-hash index nonce transacao hash_antecessor)]
      (if (.startsWith hash "0000")
        (bloco index nonce transacao hash_antecessor hash)
        (recur (inc nonce))))))

(defn criar-genesis []
  (mine 0 "bloco genesis" "0000000000000000000000000000000000000000000000000000000000000000"))
;;inicializar atom
(def block-atom (atom (conj [](criar-genesis))))

;;criar bloco de block chain
;;quando for adicionar um bloco, checar se existe bloco no atom
;;adicionar bloco no atom
(defn chain_block [transacao]
  #_{:clj-kondo/ignore [:missing-else-branch]}
  (if (empty? @block-atom)
    (reset! block-atom (conj [] (criar-genesis))))
  (let [ultimo-bloco (peek @block-atom)
        index-prox (inc (:index ultimo-bloco))
        novo-bloco (mine index-prox transacao (:hash ultimo-bloco))]
    
    (swap! block-atom conj novo-bloco))
  )
;;algo, uma mudança
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (print @block-atom)
  ;;Adicionar bloco de teste
  (chain_block "transacao1")
  (chain_block "transacao2")
  (doall (map println @block-atom))
  )
