import * as React from 'react';
import { motion } from 'framer-motion';
import { ArrowRight, BookOpen, Brain, Flame, Zap } from 'lucide-react';
import { StreakWidget } from '../../components/primitives/StreakWidget';
import { DailyGoalRing } from '../../components/primitives/DailyGoalRing';
import { Button } from '../../components/ui/button';
import { cn } from '../../lib/utils';

interface ChapterSummary {
  id: string;
  level: string;
  title: string;
  theme: string;
  cardCount: number;
}

interface ServerStats {
  totalReviews: number;
  currentStreak: number;
  longestStreak: number;
  averageRetention: number;
  dailyActivity: Record<string, number>;
  forecast?: Array<{ date: string; dueCount: number }>;
  perChapter?: Record<string, {
    dueNow: number;
    totalCards: number;
    reviewedCards: number;
    avgStability: number;
    avgDifficulty: number;
  }>;
}

interface XpData {
  xp: number;
  streak: number;
  lastStudyDate: string;
  todayXp: number;
}

interface DashboardPageProps {
  username: string;
  xpData: XpData;
  chapters: ChapterSummary[];
  stats: ServerStats | null;
  dueTotal: number;
  onStartReview: (chapterId?: string) => void;
  onGoChapters: () => void;
  onGoStats: () => void;
  dailyGoal?: number;
}

const STAT_ITEMS = [
  { key: 'totalReviews', label: 'Total reviews', icon: Brain, color: 'text-primary', bg: 'bg-primary/10' },
  { key: 'averageRetention', label: 'Avg retention', icon: Zap, color: 'text-gold', bg: 'bg-gold/10', pct: true },
  { key: 'longestStreak', label: 'Best streak', icon: Flame, color: 'text-amber-500', bg: 'bg-amber-500/10' },
];

/** Main dashboard: streak, XP, daily goal, quick actions, stats overview. */
export function DashboardPage({
  username,
  xpData,
  chapters,
  stats,
  dueTotal,
  onStartReview,
  onGoChapters,
  onGoStats,
  dailyGoal = 50,
}: DashboardPageProps) {
  const hour = new Date().getHours();
  const greeting = hour < 12 ? 'Good morning' : hour < 18 ? 'Good afternoon' : 'Good evening';
  const activeDays = stats ? Object.entries(stats.dailyActivity)
    .filter(([, v]) => v > 0)
    .map(([d]) => d) : [];

  // Most due chapter
  const mostDueChapter = stats?.perChapter
    ? chapters.reduce<ChapterSummary | null>((best, ch) => {
        const due = stats.perChapter![ch.id]?.dueNow ?? 0;
        const bestDue = best ? (stats.perChapter![best.id]?.dueNow ?? 0) : -1;
        return due > bestDue ? ch : best;
      }, null)
    : chapters[0] ?? null;

  return (
    <div className="mx-auto max-w-2xl space-y-6 px-4 py-6">
      {/* Greeting */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, ease: [0.16, 1, 0.3, 1] }}
      >
        <h1 className="font-display text-2xl font-black text-foreground">
          {greeting}, {username.split('@')[0].split('.')[0]}! 👋
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {dueTotal > 0
            ? `You have ${dueTotal} card${dueTotal === 1 ? '' : 's'} due for review.`
            : 'All caught up! No cards due right now.'}
        </p>
      </motion.div>

      {/* Top gamification row */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.05, ease: [0.16, 1, 0.3, 1] }}
        className="grid grid-cols-3 gap-3"
      >
        {/* Streak */}
        <div className="col-span-1 flex flex-col items-center justify-center gap-1 rounded-2xl border-2 border-border bg-card p-4">
          <StreakWidget streak={xpData.streak} activeDays={activeDays} compact />
          <p className="text-xs text-muted-foreground font-medium">Streak</p>
        </div>

        {/* Daily goal ring */}
        <div className="col-span-1 flex flex-col items-center justify-center rounded-2xl border-2 border-border bg-card p-3">
          <DailyGoalRing current={xpData.todayXp} goal={dailyGoal} size={60} />
        </div>

        {/* Total XP */}
        <div className="col-span-1 flex flex-col items-center justify-center rounded-2xl border-2 border-border bg-card p-4">
          <div className="flex items-center gap-1">
            <Zap className="h-4 w-4 text-gold" />
            <span className="font-display text-xl font-black tabular-nums text-foreground">{xpData.xp.toLocaleString()}</span>
          </div>
          <p className="text-xs text-muted-foreground font-medium mt-1">Total XP</p>
        </div>
      </motion.div>

      {/* Continue studying CTA */}
      {dueTotal > 0 && mostDueChapter && (
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
          className="rounded-2xl bg-gradient-to-r from-primary to-primary/80 p-5 text-white"
        >
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-wide opacity-80 mb-1">Continue studying</p>
              <h2 className="font-display text-lg font-black leading-tight">{mostDueChapter.title}</h2>
              <p className="mt-1 text-sm opacity-80">
                {(stats?.perChapter?.[mostDueChapter.id]?.dueNow ?? 0)} cards due
              </p>
            </div>
            <Button
              onClick={() => onStartReview(mostDueChapter.id)}
              className="bg-white text-primary btn-3d shrink-0"
              style={{ boxShadow: '0 4px 0 rgba(0,0,0,0.2)' }}
            >
              Start <ArrowRight className="h-4 w-4" />
            </Button>
          </div>
          {/* Mini progress bar */}
          <div className="mt-4 h-2 w-full overflow-hidden rounded-full bg-white/20">
            <div
              className="h-full rounded-full bg-white transition-all duration-700"
              style={{
                width: `${Math.round(((stats?.perChapter?.[mostDueChapter.id]?.reviewedCards ?? 0) / Math.max(1, mostDueChapter.cardCount)) * 100)}%`
              }}
            />
          </div>
        </motion.div>
      )}

      {/* Quick actions */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4, delay: 0.15, ease: [0.16, 1, 0.3, 1] }}
        className="grid grid-cols-2 gap-3"
      >
        <button
          onClick={onGoChapters}
          className="group flex flex-col gap-2 rounded-2xl border-2 border-border bg-card p-4 text-left hover:border-primary/40 hover:-translate-y-0.5 hover:shadow-card-hover transition-all focus-ring"
          aria-label="Browse chapters"
        >
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary">
            <BookOpen className="h-5 w-5" />
          </div>
          <div>
            <p className="font-display font-bold text-foreground">Chapters</p>
            <p className="text-xs text-muted-foreground">{chapters.length} available</p>
          </div>
        </button>

        <button
          onClick={() => onStartReview()}
          className="group flex flex-col gap-2 rounded-2xl border-2 border-border bg-card p-4 text-left hover:border-primary/40 hover:-translate-y-0.5 hover:shadow-card-hover transition-all focus-ring"
          aria-label="Start smart review"
        >
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gold/10 text-gold">
            <Brain className="h-5 w-5" />
          </div>
          <div>
            <p className="font-display font-bold text-foreground">Smart Review</p>
            <p className="text-xs text-muted-foreground">All due cards</p>
          </div>
        </button>
      </motion.div>

      {/* Server stats */}
      {stats && (
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
          className="rounded-2xl border-2 border-border bg-card p-4"
        >
          <div className="flex items-center justify-between mb-4">
            <h3 className="font-display font-bold text-foreground">Your stats</h3>
            <button
              onClick={onGoStats}
              className="text-xs font-bold text-primary hover:underline focus-ring"
            >
              View all →
            </button>
          </div>
          <div className="grid grid-cols-3 gap-3">
            {STAT_ITEMS.map(({ key, label, icon: Icon, color, bg, pct }) => (
              <div key={key} className="flex flex-col items-center gap-1.5 rounded-xl bg-muted/50 p-3">
                <div className={cn('flex h-8 w-8 items-center justify-center rounded-lg', bg)}>
                  <Icon className={cn('h-4 w-4', color)} />
                </div>
                <p className={cn('font-display text-xl font-black tabular-nums', color)}>
                  {pct
                    ? `${Math.round(((stats as unknown as Record<string, number>)[key] ?? 0) * 100)}%`
                    : (((stats as unknown as Record<string, number>)[key]) ?? 0).toLocaleString()}
                </p>
                <p className="text-xs text-muted-foreground text-center">{label}</p>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {/* Upcoming reviews forecast */}
      {stats?.forecast && stats.forecast.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.25, ease: [0.16, 1, 0.3, 1] }}
          className="rounded-2xl border-2 border-border bg-card p-4"
        >
          <h3 className="font-display font-bold text-foreground mb-3">Upcoming reviews</h3>
          <div className="flex gap-1.5 overflow-x-auto pb-1">
            {stats.forecast.slice(0, 7).map(({ date, dueCount }) => {
              const d = new Date(date + 'T12:00:00');
              const day = d.toLocaleDateString('en', { weekday: 'short' });
              const maxDue = Math.max(...stats.forecast!.map(f => f.dueCount), 1);
              const heightPct = (dueCount / maxDue) * 64;
              return (
                <div key={date} className="flex min-w-[44px] flex-col items-center gap-1">
                  <div className="flex items-end h-16 w-full justify-center">
                    <div
                      className="w-6 rounded-t-md bg-primary/40 transition-all"
                      style={{ height: `${Math.max(4, heightPct)}px` }}
                    />
                  </div>
                  <span className="text-2xs font-bold text-muted-foreground">{day}</span>
                  <span className="text-2xs font-bold text-foreground">{dueCount}</span>
                </div>
              );
            })}
          </div>
        </motion.div>
      )}
    </div>
  );
}
