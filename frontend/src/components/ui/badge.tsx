import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '../../lib/utils';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold transition-all duration-200',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground shadow-sm',
        secondary: 'border-transparent bg-secondary/90 text-secondary-foreground shadow-sm',
        outline: 'text-foreground bg-white/50 dark:bg-slate-900/50',
        muted: 'border-transparent bg-muted/80 text-muted-foreground',
        success: 'border-transparent bg-accent/90 text-accent-foreground shadow-sm',
        destructive: 'border-transparent bg-destructive/90 text-destructive-foreground shadow-sm',
        glass: 'border-white/20 bg-white/15 text-white backdrop-blur-sm',
      },
    },
    defaultVariants: {
      variant: 'default',
    },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return <div className={cn(badgeVariants({ variant }), className)} {...props} />;
}

export { Badge, badgeVariants };