package com.dex.dex_insights_mini.service;

import com.dex.dex_insights_mini.model.Store;
import com.dex.dex_insights_mini.repository.JsonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private JsonRepository jsonRepository;

    private StoreService storeService;

    @BeforeEach
    void setUp() {
        when(jsonRepository.loadStores()).thenReturn(List.of(
                store("S1", "Shell", "ONLINE", 2),
                store("S2", "Shell", "ONLINE", 5),
                store("S3", "BP", "OFFLINE", 1),
                store("S4", "Shell", "OFFLINE", 7)
        ));

        storeService = new StoreService(jsonRepository);
    }

    @Test
    void getStores_filtersSortsAndPaginates() {
        Page<Store> page = storeService.getStores("shell", "online", true, PageRequest.of(0, 1));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStoreId()).isEqualTo("S2");
    }

    @Test
    void getStoreById_returnsStoreWhenPresent() {
        Store store = storeService.getStoreById("S3");

        assertThat(store.getBrand()).isEqualTo("BP");
    }

    @Test
    void getStoreById_throwsWhenMissing() {
        assertThatThrownBy(() -> storeService.getStoreById("S99"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Store not found");
    }

    private static Store store(String id, String brand, String status, int offlinePumps) {
        Store store = new Store();
        store.setStoreId(id);
        store.setBrand(brand);
        store.setStatus(status);
        store.setOfflinePumps(offlinePumps);
        return store;
    }
}
