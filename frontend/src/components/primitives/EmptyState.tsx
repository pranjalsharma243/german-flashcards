import * as React from 'react';
import { cn } from '../../lib/utils';

interface EmptyStateProps {
  icon?: React.ReactNode;
  emoji?: string;
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

/** Friendly empty state with optional emoji, icon, and CTA. */
export function EmptyState({ icon, emoji, title, description, action, className }: EmptyStateProps) {
  return (
    <div className={cn('flex flex-col items-center justify-center py-16 text-center px-6', className)}>
      {emoji ? (
        <span className="mb-4 text-5xl" role="img" aria-hidden="true">{emoji}</span>
      ) : icon ? (
        <div className="mb-4 rounded-2xl bg-muted p-4 text-muted-foreground">{icon}</div>
      ) : null}
      <h3 className="font-display text-lg font-bold text-foreground">{title}</h3>
      {description && (
        <p className="mt-2 max-w-xs text-sm text-muted-foreground">{description}</p>
      )}
      {action && <div className="mt-6">{action}</div>}
    </div>
  );
}
