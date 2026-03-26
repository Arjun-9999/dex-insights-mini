import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {
  question = '';
  response: any = null;
  stores: any[] = [];
  selectedStoreId = '';
  loading = false;
  loadingStores = false;
  errorMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    console.log('ChatComponent initialized');
    this.loadStoresForChat();
  }

  loadStoresForChat() {
    this.loadingStores = true;
    console.log('Loading stores for chat from /v1/stores?page=0&size=100');

    this.http.get<any>('/v1/stores?page=0&size=100').subscribe({
      next: (data) => {
        console.log('Stores for chat loaded successfully:', data);
        this.stores = data?.content ?? [];
        if (!this.selectedStoreId && this.stores.length > 0) {
          this.selectedStoreId = this.stores[0].storeId ?? this.stores[0].STOREID;
        }
        this.loadingStores = false;
      },
      error: (error) => {
        console.error('Error loading stores for chat:', error);
        this.loadingStores = false;
        this.errorMessage = `Unable to load stores for chat: ${error.status} - ${error.statusText}. Make sure the backend is running.`;
      }
    });
  }

  onStoreContextChange(storeId: string) {
    this.selectedStoreId = storeId ?? '';
    // Clear previous answer/citations so UI reflects the newly selected store context.
    this.response = null;
    this.errorMessage = '';
  }

  ask() {
    if (!this.question.trim()) {
      this.errorMessage = 'Please enter a question.';
      return;
    }

    this.errorMessage = '';
    this.loading = true;
    console.log('Sending chat question to store:', this.selectedStoreId);

    const payload: Record<string, string> = {
      question: this.question.trim()
    };

    if (this.selectedStoreId) {
      payload['storeId'] = this.selectedStoreId;
    }

    this.http.post('/v1/chat', payload).subscribe({
      next: (data) => {
        console.log('Chat response received:', data);
        this.response = data;
        this.loading = false;
        this.question = '';
      },
      error: (error) => {
        console.error('Error getting chat response:', error);
        this.loading = false;
        this.errorMessage = `Unable to get response: ${error.status} - ${error.statusText || error.error?.message || 'Unknown error'}`;
      }
    });
  }

  getCitationStoreId(citation: any): string {
    return citation?.storeId ?? citation?.STOREID ?? '';
  }
}
