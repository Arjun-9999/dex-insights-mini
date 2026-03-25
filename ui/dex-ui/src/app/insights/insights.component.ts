import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-insights',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './insights.component.html',
  styleUrls: ['./insights.component.css']
})
export class InsightsComponent implements OnInit {
  overview: any;
  errorMessage = '';
  loading = false;
  private readonly insightsPath = '/v1/insights/overview';
  private readonly directInsightsUrl = 'http://localhost:8080/v1/insights/overview';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('InsightsComponent initialized');
    this.loadOverview();
  }

  loadOverview() {
    this.errorMessage = '';
    this.loading = true;
    console.log('Loading insights overview from', this.directInsightsUrl);

    this.http.get(this.directInsightsUrl).subscribe({
      next: (data) => {
        console.log('Insights loaded successfully:', data);
        this.overview = data;
        this.loading = false;
      },
      error: (error) => {
        // Fallback for environments where only same-origin relative paths are available.
        if (error?.status === 0 || error?.status === 404) {
          console.warn('Direct backend insights call failed; retrying with relative API path');
          this.loadOverviewFromRelativePath();
          return;
        }

        console.error('Error loading insights:', error);
        this.loading = false;
        this.errorMessage = `Unable to load insights overview: ${error.status} - ${error.statusText}. Make sure the backend is running.`;
      }
    });
  }

  private loadOverviewFromRelativePath() {
    this.http.get(this.insightsPath).subscribe({
      next: (data) => {
        console.log('Insights loaded successfully from relative API path:', data);
        this.overview = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading insights from relative API path:', error);
        this.loading = false;
        this.errorMessage = `Unable to load insights overview: ${error.status} - ${error.statusText}. Make sure the backend is running on http://localhost:8080.`;
      }
    });
  }
}
