import * as React from 'react';
import { cn } from '../../lib/utils';

type FsrsRating = 1 | 2 | 3 | 4;

interface RatingBarProps {
  onRate: (r: FsrsRating) => void;
  previewIntervals?: number[];
  disabled?: boolean;
}

const RATINGS: {
  rating: FsrsRating;
  label: string;
  shortcut: string;
  colorClasses: string;
  description: string;
}[] = [
  {
    rating: 1,
    label: 'Again',
    shortcut: '1',
    colorClasses: 'bg-danger text-white btn-3d btn-3d-danger hover:brightness-110',
    description: 'Didn\'t remember',
  },
  {
    rating: 2,
    label: 'Hard',
    shortcut: '2',
    colorClasses: 'bg-warning text-[#1F2937] btn-3d btn-3d-warning hover:brightness-110',
    description: 'Remembered with difficulty',
  },
  {
    rating: 3,
    label: 'Good',
    shortcut: '3',
    colorClasses: 'bg-success text-white btn-3d btn-3d-success hover:brightness-110',
    description: 'Remembered correctly',
  },
  {
    rating: 4,
    label: 'Easy',
    shortcut: '4',
    colorClasses: 'bg-info text-white btn-3d btn-3d-info hover:brightness-110',
    description: 'Instant recall',
  },
];

function formatInterval(secs?: number): string {
  if (secs == null) return '';
  if (secs < 60) return '<1m';
  if (secs < 3600) return `${Math.round(secs / 60)}m`;
  if (secs < 86400) return `${Math.round(secs / 3600)}h`;
  return `${Math.round(secs / 86400)}d`;
}

/** FSRS 4-button rating bar with keyboard hints and interval previews. */
export function RatingBar({ onRate, previewIntervals = [], disabled = false }: RatingBarProps) {
  return (
    <div
      role="radiogroup"
      aria-label="How well did you remember this word?"
      className="animate-slide-up"
    >
      <p className="mb-3 text-center text-xs font-semibold text-muted-foreground uppercase tracking-wide">
        How well did you remember?
      </p>
      <div className="grid grid-cols-4 gap-2">
        {RATINGS.map(({ rating, label, shortcut, colorClasses, description }, i) => (
          <button
            key={rating}
            onClick={() => onRate(rating)}
            disabled={disabled}
            role="radio"
            aria-checked={false}
            aria-label={`${label}: ${description}${previewIntervals[i] ? `, next review in ${formatInterval(previewIntervals[i])}` : ''}`}
            className={cn(
              'flex flex-col items-center gap-1 rounded-xl px-2 py-3 font-bold transition-all',
              'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2',
              'disabled:pointer-events-none disabled:opacity-50',
              colorClasses,
            )}
          >
            <span className="text-sm font-black">{label}</span>
            {previewIntervals[i] != null && (
              <span className="text-xs opacity-80 font-medium">
                {formatInterval(previewIntervals[i])}
              </span>
            )}
            <kbd className="rounded bg-black/15 px-1 py-0.5 text-xs font-mono opacity-60" aria-hidden="true">
              {shortcut}
            </kbd>
          </button>
        ))}
      </div>
    </div>
  );
}
