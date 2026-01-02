import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

type Punch = {
  id: number;
  card: string;
  code: number;
  modem: string;
  time: number;
  timeText?: string;
};

@Component({
  selector: 'app-event-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private stream?: EventSource;
  private afterId = 0;

  eventId = signal<string>('');
  loading = signal(true);
  error = signal<string | null>(null);
  punches = signal<Punch[]>([]);

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (!id) {
        this.error.set('Missing event id.');
        return;
      }
      this.eventId.set(id);
      this.afterId = 0;
      this.stopStreaming();
      this.loadPunches(id);
      this.startStreaming(id);
    });
  }

  ngOnDestroy(): void {
    this.stopStreaming();
  }

  loadPunches(id: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.http
      .get<Punch[]>(`/api/events/${id}/punches?afterId=0`)
      .subscribe({
        next: (data) => {
          this.punches.set(data);
          this.afterId = data.reduce((max, p) => Math.max(max, p.id), 0);
          this.loading.set(false);
        },
        error: (err) => {
          const msg = err?.message ?? 'Unable to fetch punches.';
          this.error.set(msg);
          this.loading.set(false);
        }
      });
  }

  private startStreaming(id: string): void {
    const url = `/api/events/${id}/punches/stream?afterId=${this.afterId}`;
    const es = new EventSource(url);
    this.stream = es;

    es.addEventListener('ready', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data);
        if (data?.afterId) this.afterId = Math.max(this.afterId, Number(data.afterId));
      } catch (_) {}
    });

    es.addEventListener('punch', (event: MessageEvent) => {
      try {
        const data = JSON.parse(event.data) as Punch;
        this.afterId = Math.max(this.afterId, Number(data.id));
        this.punches.update(list => {
          const exists = list.some(p => p.id === data.id);
          if (exists) return list;
          return [...list, data].sort((a, b) => a.id - b.id);
        });
      } catch (_) {}
    });

    es.onerror = () => {
      this.error.set('Stream connection lost; retrying…');
      this.stopStreaming();
      setTimeout(() => this.startStreaming(id), 1500);
    };
  }

  private stopStreaming(): void {
    if (this.stream) {
      this.stream.close();
      this.stream = undefined;
    }
  }
}
