import * as React from 'react';
import { cva, type VariantProps } from 'class-variance-authority';
import { cn } from '../../lib/utils';

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-sm font-bold transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 tap-highlight-none select-none',
  {
    variants: {
      variant: {
        default:
          'bg-primary text-primary-foreground btn-3d btn-3d-primary hover:brightness-105',
        secondary:
          'bg-gold text-[#1F2937] btn-3d btn-3d-secondary hover:brightness-105',
        destructive:
          'bg-danger text-white btn-3d btn-3d-danger hover:brightness-105',
        outline:
          'border-2 border-border bg-card text-foreground hover:bg-muted hover:border-primary/30 transition-colors',
        ghost:
          'hover:bg-muted/80 hover:text-foreground transition-colors',
        success:
          'bg-success text-white btn-3d btn-3d-success hover:brightness-105',
        warning:
          'bg-warning text-[#1F2937] btn-3d btn-3d-warning hover:brightness-105',
        info:
          'bg-info text-white btn-3d btn-3d-info hover:brightness-105',
        glass:
          'border border-white/30 bg-white/20 text-white backdrop-blur-md hover:bg-white/30',
      },
      size: {
        default: 'h-11 px-6 py-2.5',
        sm: 'h-9 rounded-xl px-4 text-xs',
        lg: 'h-13 rounded-2xl px-8 text-base',
        xl: 'h-14 rounded-2xl px-10 text-lg',
        icon: 'h-10 w-10 rounded-xl',
        'icon-sm': 'h-8 w-8 rounded-lg text-xs',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'default',
    },
  },
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, ...props }, ref) => (
    <button
      className={cn(buttonVariants({ variant, size, className }))}
      ref={ref}
      {...props}
    />
  ),
);
Button.displayName = 'Button';

export { Button, buttonVariants };
