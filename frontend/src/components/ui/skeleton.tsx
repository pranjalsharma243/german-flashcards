import * as React from 'react';
import { cn } from '../../lib/utils';

/** Shimmer skeleton loader block. */
export function Skeleton({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return <div className={cn('skeleton', className)} {...props} />;
}
