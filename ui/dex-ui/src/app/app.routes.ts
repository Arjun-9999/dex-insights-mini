import { Routes } from '@angular/router';
import { StoresComponent } from './stores/stores.component';
import { StoreDetailComponent } from './store-detail/store-detail.component';
import { InsightsComponent } from './insights/insights.component';
import { ChatComponent } from './chat/chat.component';

export const routes: Routes = [
  { path: '', redirectTo: '/stores', pathMatch: 'full' },
  { path: 'stores', component: StoresComponent },
  { path: 'stores/:id', component: StoreDetailComponent },
  { path: 'insights', component: InsightsComponent },
  { path: 'chat', component: ChatComponent }
];
