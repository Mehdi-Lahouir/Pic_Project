import { Routes } from '@angular/router';
import { EventsComponent } from './events/events.component';
import { EventDetailComponent } from './events/event-detail.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'events' },
  { path: 'events', component: EventsComponent },
  { path: 'events/:id', component: EventDetailComponent }
];
