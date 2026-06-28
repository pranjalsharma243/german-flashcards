import * as React from 'react';
import { motion } from 'framer-motion';
import { Moon, Sun, Monitor, Target, Volume2, Zap, LogOut, User } from 'lucide-react';
import { Button } from '../../components/ui/button';
import { cn } from '../../lib/utils';

type ThemeMode = 'light' | 'dark' | 'system';

interface SettingsPageProps {
  theme: ThemeMode;
  onThemeChange: (t: ThemeMode) => void;
  dailyGoal: number;
  onDailyGoalChange: (g: number) => void;
  username: string;
  onLogout: () => void;
  ttsSpeed?: number;
  onTtsSpeedChange?: (s: number) => void;
}

function SettingSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-2xl border-2 border-border bg-card overflow-hidden">
      <div className="px-5 py-3 border-b border-border">
        <h3 className="font-display font-bold text-foreground text-sm">{title}</h3>
      </div>
      <div className="divide-y divide-border">{children}</div>
    </div>
  );
}

function SettingRow({ label, description, children }: { label: string; description?: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between gap-4 px-5 py-4">
      <div>
        <p className="font-semibold text-foreground text-sm">{label}</p>
        {description && <p className="text-xs text-muted-foreground mt-0.5">{description}</p>}
      </div>
      <div className="shrink-0">{children}</div>
    </div>
  );
}

const THEME_OPTIONS: { value: ThemeMode; label: string; icon: React.ReactNode }[] = [
  { value: 'light', label: 'Light', icon: <Sun className="h-4 w-4" /> },
  { value: 'dark', label: 'Dark', icon: <Moon className="h-4 w-4" /> },
  { value: 'system', label: 'System', icon: <Monitor className="h-4 w-4" /> },
];

const GOAL_OPTIONS = [
  { value: 20, label: '20 XP', sub: 'Casual' },
  { value: 50, label: '50 XP', sub: 'Regular' },
  { value: 100, label: '100 XP', sub: 'Serious' },
  { value: 200, label: '200 XP', sub: 'Intense' },
];

const TTS_SPEEDS = [0.75, 1.0, 1.25, 1.5];

/** App settings: theme, daily goal, TTS, account. */
export function SettingsPage({
  theme,
  onThemeChange,
  dailyGoal,
  onDailyGoalChange,
  username,
  onLogout,
  ttsSpeed = 1.0,
  onTtsSpeedChange,
}: SettingsPageProps) {
  return (
    <div className="mx-auto max-w-lg space-y-5 px-4 py-6">
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="font-display text-2xl font-black text-foreground">Settings</h1>
        <p className="text-sm text-muted-foreground mt-1">Customize your learning experience</p>
      </motion.div>

      {/* Account */}
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.05 }}>
        <SettingSection title="Account">
          <SettingRow
            label="Signed in as"
            description={username}
          >
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <User className="h-4 w-4" />
            </div>
          </SettingRow>
          <div className="px-5 py-4">
            <Button variant="destructive" size="sm" onClick={onLogout} className="gap-2">
              <LogOut className="h-4 w-4" />
              Sign out
            </Button>
          </div>
        </SettingSection>
      </motion.div>

      {/* Appearance */}
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }}>
        <SettingSection title="Appearance">
          <SettingRow label="Theme" description="Choose your preferred color scheme">
            <div className="flex gap-1.5" role="radiogroup" aria-label="Theme selection">
              {THEME_OPTIONS.map(({ value, label, icon }) => (
                <button
                  key={value}
                  onClick={() => onThemeChange(value)}
                  role="radio"
                  aria-checked={theme === value}
                  aria-label={label}
                  className={cn(
                    'flex items-center gap-1.5 rounded-xl px-3 py-2 text-xs font-bold transition-all',
                    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary',
                    theme === value
                      ? 'bg-primary text-white shadow-sm'
                      : 'bg-muted text-muted-foreground hover:text-foreground',
                  )}
                >
                  {icon}
                  <span className="hidden sm:inline">{label}</span>
                </button>
              ))}
            </div>
          </SettingRow>
        </SettingSection>
      </motion.div>

      {/* Daily goal */}
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }}>
        <SettingSection title="Learning">
          <SettingRow label="Daily XP goal" description="How much XP you want to earn each day">
            <div className="flex gap-1.5" role="radiogroup" aria-label="Daily XP goal">
              {GOAL_OPTIONS.map(({ value, label, sub }) => (
                <button
                  key={value}
                  onClick={() => onDailyGoalChange(value)}
                  role="radio"
                  aria-checked={dailyGoal === value}
                  aria-label={`${label} – ${sub}`}
                  className={cn(
                    'flex flex-col items-center rounded-xl px-2.5 py-2 text-xs font-bold transition-all',
                    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary',
                    dailyGoal === value
                      ? 'bg-primary text-white'
                      : 'bg-muted text-muted-foreground hover:text-foreground',
                  )}
                >
                  <Zap className="h-3.5 w-3.5 mb-0.5" />
                  <span>{label}</span>
                  <span className="opacity-70 font-normal">{sub}</span>
                </button>
              ))}
            </div>
          </SettingRow>
        </SettingSection>
      </motion.div>

      {/* TTS */}
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }}>
        <SettingSection title="Pronunciation">
          <SettingRow label="Speech speed" description="Speed of text-to-speech playback">
            <div className="flex gap-1.5" role="radiogroup" aria-label="TTS speech speed">
              {TTS_SPEEDS.map(speed => (
                <button
                  key={speed}
                  onClick={() => onTtsSpeedChange?.(speed)}
                  role="radio"
                  aria-checked={ttsSpeed === speed}
                  aria-label={`${speed}x speed`}
                  className={cn(
                    'rounded-xl px-2.5 py-2 text-xs font-bold transition-all',
                    'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary',
                    ttsSpeed === speed
                      ? 'bg-primary text-white'
                      : 'bg-muted text-muted-foreground hover:text-foreground',
                  )}
                >
                  {speed}×
                </button>
              ))}
            </div>
          </SettingRow>
        </SettingSection>
      </motion.div>

      {/* Accessibility */}
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.25 }}>
        <SettingSection title="Accessibility">
          <div className="px-5 py-4">
            <p className="text-sm text-muted-foreground">
              This app respects your system's <strong>prefers-reduced-motion</strong> setting — animations are automatically disabled when enabled.
              Use <kbd className="rounded border border-border bg-muted px-1.5 py-0.5 text-xs font-mono">Space</kbd> to flip cards,
              <kbd className="rounded border border-border bg-muted px-1.5 py-0.5 text-xs font-mono ml-1">1</kbd>–
              <kbd className="rounded border border-border bg-muted px-1.5 py-0.5 text-xs font-mono">4</kbd> to rate.
            </p>
          </div>
        </SettingSection>
      </motion.div>
    </div>
  );
}
