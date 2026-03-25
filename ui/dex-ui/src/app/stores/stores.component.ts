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

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('StoresComponent initialized');
    this.loadStores();
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
    this.errorMessage = '';
    this.loading = true;
    const url = `/v1/stores?${this.buildQuery()}`;
    console.log('Loading stores from:', url);

    this.http.get<any>(url).subscribe({
      next: (data) => {
        console.log('Stores loaded successfully:', data);
        this.stores = data?.content ?? [];
        this.totalElements = data?.totalElements ?? 0;
        this.loading = false;
      },
      error: (error) => {
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
