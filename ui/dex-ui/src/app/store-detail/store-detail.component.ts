import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-store-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './store-detail.component.html',
  styleUrls: ['./store-detail.component.css']
})
export class StoreDetailComponent implements OnInit {
  store: any;
  errorMessage = '';
  loading = false;
  private readonly relativeStorePathPrefix = '/v1/stores/';

  constructor(private route: ActivatedRoute, private http: HttpClient) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')?.trim();
    if (!id) {
      this.errorMessage = 'Store id is missing in route.';
      return;
    }

    const encodedId = encodeURIComponent(id);
    this.loading = true;

    this.http.get(`${this.relativeStorePathPrefix}${encodedId}`).subscribe({
      next: (data) => {
        this.store = data;
        this.loading = false;
      },
      error: (relativeError) => {
        // Fallback: fetch paged stores and locate by id for environments where /v1/stores/{id} fails.
        this.http.get<any>('/v1/stores?page=0&size=1000').subscribe({
          next: (data) => {
            const stores = data?.content ?? [];
            const found = stores.find((s: any) => (s?.STOREID ?? s?.storeId) === id);

            if (found) {
              this.store = found;
              this.loading = false;
              return;
            }

            this.loading = false;
            this.errorMessage = `Unable to load store details. Store ${id} was not found.`;
          },
          error: (listError) => {
            this.loading = false;
            this.errorMessage = `Unable to load store details. (detail: ${relativeError?.status ?? 'n/a'}, list: ${listError?.status ?? 'n/a'})`;
          }
        });
      }
    });
  }
}
