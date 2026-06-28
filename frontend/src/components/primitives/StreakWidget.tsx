import * as React from 'react';
import { cn } from '../../lib/utils';

interface StreakWidgetProps {
  streak: number;
  /** ISO date strings of the last 7 active days */
  activeDays?: string[];
  hasFreezeToken?: boolean;
  compact?: boolean;
  className?: string;
}

/** Displays a flame + day count + 7-day weekly dots. */
export function StreakWidget({
  streak,
  activeDays = [],
  hasFreezeToken = false,
  compact = false,
  className,
}: StreakWidgetProps) {
  const today = new Date();
  const last7 = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() - (6 - i));
    return d.toISOString().split('T')[0];
  });

  if (compact) {
    return (
      <div className={cn('flex items-center gap-1.5', className)}>
        <span className={cn('text-lg', streak > 0 ? 'animate-bounce-in' : 'opacity-30')} aria-hidden="true">
          🔥
        </span>
        <span className={cn('font-display font-bold tabular-nums', streak > 0 ? 'text-amber-500' : 'text-muted-foreground')}>
          {streak}
        </span>
      </div>
    );
  }

  return (
    <div className={cn('flex flex-col items-center gap-3', className)}>
      <div className="flex items-center gap-2">
        <span
          className={cn(
            'text-4xl transition-all duration-300',
            streak > 0 ? 'drop-shadow-[0_0_8px_rgba(251,146,36,0.6)]' : 'opacity-25',
          )}
          aria-label={`${streak}-day streak`}
        >
          🔥
        </span>
        <div>
          <p className="font-display text-3xl font-black tabular-nums text-amber-500 leading-none">
            {streak}
          </p>
          <p className="text-xs text-muted-foreground font-medium mt-0.5">day streak</p>
        </div>
      </div>

      {/* 7-day dots */}
      <div className="flex items-center gap-1.5" role="list" aria-label="Last 7 days activity">
        {last7.map((date, i) => {
          const isActive = activeDays.includes(date);
          const dayLabel = ['S', 'M', 'T', 'W', 'T', 'F', 'S'][new Date(date + 'T12:00:00').getDay()];
          return (
            <div key={date} role="listitem" className="flex flex-col items-center gap-1">
              <div
                className={cn(
                  'h-3 w-3 rounded-full transition-all duration-300',
                  isActive
                    ? 'bg-amber-400 shadow-[0_0_6px_rgba(251,191,36,0.6)]'
                    : i === 6
                    ? 'border-2 border-border bg-background'
                    : 'bg-muted',
                )}
                aria-label={`${date}: ${isActive ? 'active' : 'inactive'}`}
              />
              <span className="text-2xs text-muted-foreground">{dayLabel}</span>
            </div>
          );
        })}
      </div>

      {hasFreezeToken && (
        <div className="flex items-center gap-1 text-xs text-info font-medium">
          <span>🧊</span>
          <span>Streak freeze ready</span>
        </div>
      )}
    </div>
  );
}
