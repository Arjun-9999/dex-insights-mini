package com.dex.dex_insights_mini.controller;

import com.dex.dex_insights_mini.model.Store;
import com.dex.dex_insights_mini.model.StoreAddress;
import com.dex.dex_insights_mini.exception.StoreNotFoundException;
import com.dex.dex_insights_mini.service.StoreService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @Test
    void getStores_returnsPagedResponseWithFiltersAndSort() throws Exception {
        Store first = store("S1", "Shell", "ONLINE", 5, "Austin", "TX");
        Store second = store("S2", "Shell", "ONLINE", 2, "Dallas", "TX");

        when(storeService.getStores(eq("Shell"), eq("ONLINE"), eq(true), any()))
                .thenReturn(new PageImpl<>(List.of(first, second), PageRequest.of(0, 2), 2));

        mockMvc.perform(get("/v1/stores")
                        .param("brand", "Shell")
                        .param("status", "ONLINE")
                        .param("sortByOfflinePumps", "true")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].STOREID").value("S1"))
                .andExpect(jsonPath("$.content[0].BRAND").value("Shell"))
                .andExpect(jsonPath("$.content[0].status").value("ONLINE"))
                .andExpect(jsonPath("$.content[0].offlinePumps").value(5))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void getStoreById_returnsStoreDetails() throws Exception {
        Store store = store("S3", "BP", "OFFLINE", 1, "Houston", "TX");

        when(storeService.getStoreById("S3")).thenReturn(store);

        mockMvc.perform(get("/v1/stores/S3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.STOREID").value("S3"))
                .andExpect(jsonPath("$.BRAND").value("BP"))
                .andExpect(jsonPath("$.status").value("OFFLINE"))
                .andExpect(jsonPath("$.storeAddress.city").value("Houston"));
    }

    @Test
    void getStoreById_returnsNotFoundPayloadWhenStoreMissing() throws Exception {
        when(storeService.getStoreById("MISSING")).thenThrow(new StoreNotFoundException("MISSING"));

        mockMvc.perform(get("/v1/stores/MISSING"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Store not found: MISSING"))
                .andExpect(jsonPath("$.path").value("/v1/stores/MISSING"));
    }

    private static Store store(String id, String brand, String status, int offlinePumps, String city, String state) {
        StoreAddress address = new StoreAddress();
        address.setCity(city);
        address.setState(state);

        Store store = new Store();
        store.setStoreId(id);
        store.setBrand(brand);
        store.setStatus(status);
        store.setOfflinePumps(offlinePumps);
        store.setStoreAddress(address);
        return store;
    }
}
