export type BookStatus = 'to-read' | 'reading' | 'finished';

export interface Book {
  id: number;
  title: string;
  author: string;
  status: BookStatus;
  coverUrl?: string;
  year?: number;
}

/** How each status is written on screen */
export const STATUS_LABELS: Record<BookStatus, string> = {
  'to-read': 'To read',
  reading: 'Reading',
  finished: 'Finished',
};

/** The one-click "next step" for each status */
export const NEXT_ACTION: Record<BookStatus, { label: string; to: BookStatus }> = {
  'to-read': { label: 'Start reading', to: 'reading' },
  reading: { label: 'Mark finished', to: 'finished' },
  finished: { label: 'Read again', to: 'reading' },
};
