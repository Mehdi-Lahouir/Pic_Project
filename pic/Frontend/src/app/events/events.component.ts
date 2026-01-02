import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

type EventSummary = {
  id?: string | number;
  name: string;
  when?: string;
  location?: string;
};

@Component({
  selector: 'app-events',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './events.component.html',
  styleUrls: ['./events.component.css']
})
export class EventsComponent implements OnInit {
  private readonly http = inject(HttpClient);

  loading = signal(true);
  error = signal<string | null>(null);
  events = signal<EventSummary[]>([]);
  rawResponse = signal<string | null>(null);

  ngOnInit(): void {
    this.loadEvents();
  }

  loadEvents(): void {
    this.loading.set(true);
    this.error.set(null);

    this.http
      .get('/api/events', { responseType: 'text' })
      .subscribe({
        next: (text) => {
          this.rawResponse.set(text);
          const parsed = this.parseEvents(text);
          this.events.set(parsed);
          this.loading.set(false);
        },
        error: (err) => {
          this.error.set('Unable to fetch events. Check that the backend is running on :8080.');
          if (err?.message) this.error.set(err.message);
          this.loading.set(false);
        }
      });
  }

  private parseEvents(text: string): EventSummary[] {
    let payload: any;
    try {
      payload = JSON.parse(text);
    } catch (e) {
      this.error.set('Backend responded, but the payload was not valid JSON.');
      return [];
    }

    const eventsArray =
      Array.isArray(payload) ? payload
        : payload?._embedded?.events
          ?? payload?.events
          ?? [];

    if (!Array.isArray(eventsArray)) return [];

    return eventsArray.map((e: any): EventSummary => ({
      id: e.id ?? e.eventId ?? e.key ?? undefined,
      name: e.name ?? e.title ?? e.eventName ?? 'Unnamed event',
      when: e.startTime ?? e.start ?? e.date ?? e.begin ?? undefined,
      location: e.location ?? e.venue ?? e.city ?? undefined
    }));
  }
}
