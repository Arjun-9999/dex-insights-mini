package com.dex.dex_insights_mini.controller;

import com.dex.dex_insights_mini.model.Store;
import com.dex.dex_insights_mini.service.StoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
    @RequestMapping("/v1/stores")
    @CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
    public class StoreController {
                private static final Logger log = LoggerFactory.getLogger(StoreController.class);
        private final StoreService service;

        public StoreController(StoreService service) {
            this.service = service;
        }

        @GetMapping
        public Page<Store> getStores(@RequestParam(required=false) String brand,
                                     @RequestParam(required=false) String status,
                                     @RequestParam(defaultValue="false") boolean sortByOfflinePumps,
                                     @RequestParam(defaultValue="0") int page,
                                     @RequestParam(defaultValue="10") int size) {
            log.info("event=get_stores brand={} status={} sortByOfflinePumps={} page={} size={}",
                    brand, status, sortByOfflinePumps, page, size);
            Pageable pageable = PageRequest.of(page, size);
            return service.getStores(brand, status, sortByOfflinePumps, pageable);
        }

        @GetMapping("/{storeId}")
        public Store getStore(@PathVariable String storeId) {
            log.info("event=get_store storeId={}", storeId);
            return service.getStoreById(storeId);
        }
    }
