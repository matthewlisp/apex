;; Copyright © 2020, JUXT LTD.

(ns juxt.apex.http.http-test
  (:require
   [ring.mock.request :refer [request]]
   [juxt.apex.alpha.http.core :as http]
   [clojure.test :refer [deftest is]]
   [juxt.apex.alpha.http.header-names :refer [wrap-headers-normalize-case]]))

(comment
  (let [h (->
           (http/handler
            (reify
              http/ResourceLocator
              (locate-resource [_ uri]
                (if (= (.getPath uri) "/hello.txt")
                  {:apex.http/content "Hello World!"}))
              http/ResponseBody
              (send-ok-response
                  [_ resource response request respond raise]
                (respond
                 (conj response [:body (:apex.http/content resource)])))))
           wrap-headers-normalize-case)]
    (h (request :get "/hello.txt"))))

(defn wrap-dissoc-date [h]
  (fn [req]
    (->
     (h req)
     (update :headers dissoc "date"))))

(deftest basic-test
  (let [h (-> (http/handler
               (reify
                 http/ResourceLocator
                 (locate-resource [_ uri]
                   (when (= (.getPath uri) "/hello.txt")
                     {:apex.http/content "Hello World!"}))
                 http/ResponseBody
                 (send-ok-response
                     [_ resource response request respond raise]
                     (respond
                      (conj response [:body (:apex.http/content resource)])))))
              wrap-dissoc-date)]
    (is (=
         {:status 200
          :headers {}
          :body "Hello World!"}
         (h (request :get "/hello.txt"))))

    (is (=
         {:status 404
          :headers {}}
         (h (request :get "/not-exists"))))))
