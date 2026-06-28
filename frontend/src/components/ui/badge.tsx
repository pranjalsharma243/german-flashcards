import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '../../lib/utils';

const badgeVariants = cva(
  'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-bold transition-all duration-200',
  {
    variants: {
      variant: {
        default: 'border-transparent bg-primary text-primary-foreground',
        secondary: 'border-transparent bg-gold/90 text-[#1F2937]',
        outline: 'border-border text-foreground bg-card',
        muted: 'border-transparent bg-muted text-muted-foreground',
        success: 'border-transparent bg-success/15 border-success/30 text-success',
        destructive: 'border-transparent bg-danger/15 border-danger/30 text-danger',
        warning: 'border-transparent bg-warning/15 border-warning/30 text-[#1F2937] dark:text-warning',
        info: 'border-transparent bg-info/15 border-info/30 text-info',
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
