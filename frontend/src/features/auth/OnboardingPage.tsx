import * as React from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import { ArrowRight, Check } from 'lucide-react';
import { Button } from '../../components/ui/button';
import { cn } from '../../lib/utils';

interface OnboardingData {
  goal: string;
  motivation: string;
  dailyGoal: number;
}

interface OnboardingPageProps {
  onComplete: (data: OnboardingData) => void;
}

const STEPS = [
  {
    id: 'goal',
    title: 'What\'s your German goal?',
    subtitle: 'We\'ll personalize your learning path.',
    options: [
      { value: 'travel', label: '✈️ Travel & tourism', desc: 'Communicate confidently abroad' },
      { value: 'work', label: '💼 Work & career', desc: 'Professional German skills' },
      { value: 'culture', label: '🎭 Culture & media', desc: 'Enjoy German content' },
      { value: 'exam', label: '📜 Pass an exam', desc: 'B1/B2 certification' },
    ],
  },
  {
    id: 'motivation',
    title: 'Why are you learning now?',
    subtitle: 'Knowing your "why" helps us keep you motivated.',
    options: [
      { value: 'fun', label: '🎮 It\'s fun', desc: 'I enjoy learning languages' },
      { value: 'challenge', label: '💪 Personal challenge', desc: 'I love pushing myself' },
      { value: 'necessity', label: '⚡ I need it', desc: 'For a specific reason' },
      { value: 'reconnect', label: '❤️ Reconnect with roots', desc: 'Family or heritage' },
    ],
  },
  {
    id: 'dailyGoal',
    title: 'Set your daily goal',
    subtitle: 'You can always change this later in Settings.',
    options: [
      { value: 20, label: '5 min/day', desc: '20 XP — Casual', badge: 'Relaxed' },
      { value: 50, label: '10 min/day', desc: '50 XP — Regular', badge: 'Regular' },
      { value: 100, label: '20 min/day', desc: '100 XP — Serious', badge: 'Serious' },
      { value: 200, label: '30+ min/day', desc: '200 XP — Intense', badge: 'Intense' },
    ],
  },
] as const;

type StepId = typeof STEPS[number]['id'];

/** Duolingo-style gradual engagement onboarding (3 steps). */
export function OnboardingPage({ onComplete }: OnboardingPageProps) {
  const [stepIdx, setStepIdx] = React.useState(0);
  const [answers, setAnswers] = React.useState<Record<string, string | number>>({});

  const step = STEPS[stepIdx];
  const selected = answers[step.id];
  const progress = ((stepIdx) / STEPS.length) * 100;

  function handleNext() {
    if (!selected) return;
    if (stepIdx < STEPS.length - 1) {
      setStepIdx(s => s + 1);
    } else {
      onComplete({
        goal: String(answers.goal ?? 'travel'),
        motivation: String(answers.motivation ?? 'fun'),
        dailyGoal: Number(answers.dailyGoal ?? 50),
      });
    }
  }

  return (
    <div className="min-h-dvh bg-background flex flex-col">
      {/* Progress bar */}
      <div
        className="h-2 bg-muted"
        role="progressbar"
        aria-valuenow={Math.round(progress + (1 / STEPS.length) * 100)}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label="Onboarding progress"
      >
        <div
          className="h-full bg-primary rounded-r-full transition-all duration-500"
          style={{ width: `${progress + (1 / STEPS.length) * 100}%` }}
        />
      </div>

      <div className="flex-1 flex flex-col items-center justify-center px-6 py-10">
        <AnimatePresence mode="wait">
          <motion.div
            key={step.id}
            initial={{ opacity: 0, x: 40 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: -40 }}
            transition={{ duration: 0.3, ease: [0.16, 1, 0.3, 1] }}
            className="w-full max-w-md"
          >
            {/* Step indicator */}
            <p className="text-xs font-bold text-muted-foreground uppercase tracking-wide mb-3">
              Step {stepIdx + 1} of {STEPS.length}
            </p>

            <h1 className="font-display text-3xl font-black text-foreground mb-2">
              {step.title}
            </h1>
            <p className="text-muted-foreground mb-8">{step.subtitle}</p>

            {/* Options */}
            <div className="space-y-3">
              {step.options.map((opt) => {
                const val = 'value' in opt ? opt.value : '';
                const isSelected = selected === val || selected === Number(val);
                return (
                  <button
                    key={String(val)}
                    onClick={() => setAnswers(a => ({ ...a, [step.id]: val }))}
                    className={cn(
                      'w-full rounded-2xl border-2 p-4 text-left transition-all duration-200',
                      'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2',
                      isSelected
                        ? 'border-primary bg-primary/5 shadow-glow-sm'
                        : 'border-border bg-card hover:border-primary/40 hover:bg-muted/30',
                    )}
                    aria-pressed={isSelected}
                  >
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="font-bold text-foreground">{opt.label}</p>
                        <p className="text-sm text-muted-foreground mt-0.5">{opt.desc}</p>
                      </div>
                      <div
                        className={cn(
                          'flex h-6 w-6 shrink-0 items-center justify-center rounded-full border-2 transition-all',
                          isSelected
                            ? 'border-primary bg-primary text-white'
                            : 'border-border bg-background',
                        )}
                        aria-hidden="true"
                      >
                        {isSelected && <Check className="h-3.5 w-3.5" />}
                      </div>
                    </div>
                  </button>
                );
              })}
            </div>
          </motion.div>
        </AnimatePresence>

        {/* Next button */}
        <div className="mt-8 w-full max-w-md">
          <Button
            onClick={handleNext}
            disabled={!selected}
            size="lg"
            className="w-full gap-2"
            aria-label={stepIdx < STEPS.length - 1 ? 'Next step' : 'Get started'}
          >
            {stepIdx < STEPS.length - 1 ? (
              <>Continue <ArrowRight className="h-5 w-5" /></>
            ) : (
              <>Let\'s go! 🚀</>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
}
