import * as React from 'react';
import { ProgressRing } from './ProgressRing';
import { cn } from '../../lib/utils';

interface DailyGoalRingProps {
  current: number;
  goal: number;
  size?: number;
  className?: string;
}

/** Circular daily-XP progress ring with Zap icon. */
export function DailyGoalRing({ current, goal, size = 72, className }: DailyGoalRingProps) {
  const pct = Math.min(100, (current / Math.max(1, goal)) * 100);
  const done = pct >= 100;

  return (
    <div className={cn('flex flex-col items-center gap-1', className)}>
      <ProgressRing
        value={pct}
        size={size}
        strokeWidth={7}
        color={done ? 'hsl(var(--gold))' : 'hsl(var(--primary))'}
        label={`Daily goal: ${current} of ${goal} XP`}
      >
        <div className="flex flex-col items-center">
          <span className="text-lg" aria-hidden="true">{done ? '⭐' : '⚡'}</span>
          <span className="text-xs font-bold tabular-nums text-foreground">{current}</span>
        </div>
      </ProgressRing>
      <p className={cn('text-xs font-semibold', done ? 'text-gold' : 'text-muted-foreground')}>
        {done ? 'Goal met! 🎉' : `${Math.max(0, goal - current)} XP left`}
      </p>
    </div>
  );
}
