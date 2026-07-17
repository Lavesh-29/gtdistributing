const CACHE_NAME = "gtdist-cache-v1";
const urlsToCache = [
                      "/",
                      "/index.html",
                      "/css/styles.css",
                      "/public/images/icon-192.png",
                      "/public/images/icon-512.png"
                    ]

self.addEventListener("install", event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(urlsToCache);
    })
  );
});

self.addEventListener("fetch", event => {
  event.respondWith(
    caches.match(event.request).then(response => {
      return response || fetch(event.request);
    })
  );

});
