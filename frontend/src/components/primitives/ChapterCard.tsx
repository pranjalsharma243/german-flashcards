import * as React from 'react';
import { BookOpen, Lock } from 'lucide-react';
import { ProgressRing } from './ProgressRing';
import { cn } from '../../lib/utils';

interface ChapterCardProps {
  id: string;
  title: string;
  theme: string;
  level: string;
  wordCount: number;
  masteryPct: number;
  dueCount: number;
  isLocked?: boolean;
  isSelected?: boolean;
  onClick?: () => void;
}

const LEVEL_COLORS: Record<string, string> = {
  A1: 'bg-success/15 text-success border-success/30',
  A2: 'bg-info/15 text-info border-info/30',
  B1: 'bg-primary/15 text-primary border-primary/30',
  B2: 'bg-warning/15 text-warning border-warning/30',
  C1: 'bg-danger/15 text-danger border-danger/30',
};

/** Chapter card with mastery ring, due badge, and lock state. */
export function ChapterCard({
  title,
  theme,
  level,
  wordCount,
  masteryPct,
  dueCount,
  isLocked = false,
  isSelected = false,
  onClick,
}: ChapterCardProps) {
  return (
    <button
      onClick={onClick}
      disabled={isLocked}
      className={cn(
        'group relative w-full rounded-2xl border-2 bg-card p-4 text-left transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2 focus-visible:ring-offset-background',
        isLocked
          ? 'cursor-not-allowed opacity-60'
          : 'cursor-pointer hover:-translate-y-1 hover:shadow-card-hover hover:border-primary/40',
        isSelected
          ? 'border-primary bg-primary/5 shadow-glow-sm'
          : 'border-border hover:border-primary/30',
      )}
      aria-pressed={isSelected}
      aria-label={`${title}${isLocked ? ' (locked)' : ''}: ${wordCount} words, ${Math.round(masteryPct)}% mastered`}
    >
      {/* Lock overlay */}
      {isLocked && (
        <div className="absolute inset-0 flex items-center justify-center rounded-2xl bg-background/60 backdrop-blur-sm">
          <Lock className="h-6 w-6 text-muted-foreground" />
        </div>
      )}

      <div className="flex items-start gap-3">
        {/* Mastery ring */}
        <ProgressRing
          value={masteryPct}
          size={52}
          strokeWidth={5}
          color={masteryPct >= 90 ? 'hsl(var(--gold))' : 'hsl(var(--primary))'}
          label={`${Math.round(masteryPct)}% mastered`}
        >
          <BookOpen className="h-4 w-4 text-primary" aria-hidden="true" />
        </ProgressRing>

        <div className="min-w-0 flex-1">
          <div className="flex items-start justify-between gap-2">
            <h3 className="font-display text-sm font-bold text-foreground leading-tight truncate">
              {title}
            </h3>
            {dueCount > 0 && !isLocked && (
              <span className="ml-auto shrink-0 rounded-full bg-danger px-2 py-0.5 text-2xs font-bold text-white">
                {dueCount} due
              </span>
            )}
          </div>
          <p className="mt-0.5 text-xs text-muted-foreground truncate">{theme}</p>
          <div className="mt-2 flex items-center gap-2">
            <span className={cn('rounded border px-1.5 py-0.5 text-2xs font-bold', LEVEL_COLORS[level] ?? LEVEL_COLORS.B1)}>
              {level}
            </span>
            <span className="text-2xs text-muted-foreground">{wordCount} words</span>
            <span className="text-2xs text-muted-foreground">·</span>
            <span className="text-2xs font-semibold text-primary">{Math.round(masteryPct)}%</span>
          </div>
        </div>
      </div>
    </button>
  );
}
