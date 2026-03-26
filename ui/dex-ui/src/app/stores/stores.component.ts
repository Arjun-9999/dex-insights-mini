import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-stores',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './stores.component.html',
  styleUrls: ['./stores.component.css']
})
export class StoresComponent implements OnInit {
  stores: any[] = [];
  page = 0;
  size = 10;
  brandFilter = '';
  statusFilter = '';
  sortByOfflinePumps = false;
  totalElements = 0;
  errorMessage = '';
  loading = false;
  private latestRequestId = 0;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('StoresComponent initialized');
    this.loadStores();
  }

  private normalizeText(value: unknown): string {
    return typeof value === 'string' ? value.trim().toLowerCase() : '';
  }

  private getStoreValue(store: any, camelCaseKey: string, legacyKey: string): string {
    return store?.[camelCaseKey] ?? store?.[legacyKey] ?? '';
  }

  private matchesFilters(store: any): boolean {
    const selectedBrand = this.normalizeText(this.brandFilter);
    const selectedStatus = this.normalizeText(this.statusFilter);

    const storeBrand = this.normalizeText(this.getStoreValue(store, 'brand', 'BRAND'));
    const storeStatus = this.normalizeText(this.getStoreValue(store, 'status', 'STATUS'));

    const brandMatches = !selectedBrand || storeBrand === selectedBrand;
    const statusMatches = !selectedStatus || storeStatus === selectedStatus;
    return brandMatches && statusMatches;
  }

  private buildQuery(): string {
    const params: string[] = [
      `page=${this.page}`,
      `size=${this.size}`
    ];

    if (this.brandFilter.trim()) {
      params.push(`brand=${encodeURIComponent(this.brandFilter.trim())}`);
    }
    if (this.statusFilter.trim()) {
      params.push(`status=${encodeURIComponent(this.statusFilter.trim())}`);
    }
    params.push(`sortByOfflinePumps=${this.sortByOfflinePumps}`);

    return params.join('&');
  }

  loadStores() {
    const requestId = ++this.latestRequestId;
    this.errorMessage = '';
    this.loading = true;
    const url = `/v1/stores?${this.buildQuery()}`;
    console.log('Loading stores from:', url);

    this.http.get<any>(url).subscribe({
      next: (data) => {
        if (requestId !== this.latestRequestId) {
          return;
        }
        console.log('Stores loaded successfully:', data);

        const responseStores = Array.isArray(data) ? data : (data?.content ?? []);
        const filteredStores = responseStores.filter((store: any) => this.matchesFilters(store));

        // Guard against stale/unfiltered backend payloads so Apply always reflects selected filters.
        if (filteredStores.length !== responseStores.length) {
          console.warn('Applied client-side filter guard due to mismatched backend payload.');
        }

        this.stores = filteredStores;
        this.totalElements = Array.isArray(data)
          ? filteredStores.length
          : (data?.totalElements ?? filteredStores.length);
        this.loading = false;
      },
      error: (error) => {
        if (requestId !== this.latestRequestId) {
          return;
        }
        console.error('Error loading stores:', error);
        this.stores = [];
        this.totalElements = 0;
        this.loading = false;
        this.errorMessage = `Unable to load stores: ${error.status} - ${error.statusText}. Make sure the backend is running on http://localhost:8080`;
      }
    });
  }

  applyFilters() {
    this.page = 0;
    this.loadStores();
  }

  resetFilters() {
    this.brandFilter = '';
    this.statusFilter = '';
    this.sortByOfflinePumps = false;
    this.page = 0;
    this.loadStores();
  }

  nextPage() {
    this.page++;
    this.loadStores();
  }

  prevPage() {
    if (this.page > 0) {
      this.page--;
      this.loadStores();
    }
  }
}
