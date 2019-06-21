(ns hx-frame-storage.core
  (:require
   [alandipert.storage-atom :refer [local-storage]]
   [hx-frame.interceptor :as i]))

(def storage-atoms (atom {}))

(defn- register-store [store-key]
  (when-not (@storage-atoms store-key)
    (swap! storage-atoms assoc store-key
           (local-storage (atom nil) store-key))))

(defn- ->store [store-key data]
  (reset! (@storage-atoms store-key) data))

(defn- <-store [store-key]
  @(@storage-atoms store-key))

(defn persist-db [store-key db-key]
  (register-store store-key)
  (i/->interceptor
   {:id (keyword (str db-key "->" store-key))
    :before (fn [context]
              (assoc-in context [:coeffects :db db-key]
                        (<-store store-key)))
    :after (fn [context]
             (when-let [value (get-in context [:effects :db db-key])]
               (->store store-key value))
             context)}))
