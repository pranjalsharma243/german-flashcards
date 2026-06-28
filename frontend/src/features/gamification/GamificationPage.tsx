import * as React from 'react';
import { motion } from 'framer-motion';
import { Flame, Star, Trophy, Zap, Target, BookOpen, Brain, Crown } from 'lucide-react';
import { StreakWidget } from '../../components/primitives/StreakWidget';
import { ProgressRing } from '../../components/primitives/ProgressRing';
import { cn } from '../../lib/utils';

interface XpData {
  xp: number;
  streak: number;
  lastStudyDate: string;
  todayXp: number;
}

interface ServerStats {
  totalReviews: number;
  currentStreak: number;
  longestStreak: number;
  averageRetention: number;
  dailyActivity: Record<string, number>;
  perChapter?: Record<string, { dueNow: number; totalCards: number; reviewedCards: number; avgStability: number; avgDifficulty: number }>;
}

interface GamificationPageProps {
  xpData: XpData;
  stats: ServerStats | null;
  dailyGoal: number;
}

interface Achievement {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  threshold: number;
  value: number;
  color: string;
  bg: string;
}

const XP_LEVELS = [
  { level: 1, name: 'Beginner', minXp: 0, maxXp: 100, icon: '🌱' },
  { level: 2, name: 'Learner', minXp: 100, maxXp: 300, icon: '📚' },
  { level: 3, name: 'Student', minXp: 300, maxXp: 600, icon: '🎓' },
  { level: 4, name: 'Scholar', minXp: 600, maxXp: 1200, icon: '🧑‍🏫' },
  { level: 5, name: 'Expert', minXp: 1200, maxXp: 2500, icon: '⭐' },
  { level: 6, name: 'Master', minXp: 2500, maxXp: 5000, icon: '🏆' },
  { level: 7, name: 'Meister', minXp: 5000, maxXp: Infinity, icon: '👑' },
];

function getCurrentLevel(xp: number) {
  let result = XP_LEVELS[0];
  for (const l of XP_LEVELS) { if (xp >= l.minXp) result = l; }
  return result;
}

/** Streak detail, achievements grid, XP level progression. */
export function GamificationPage({ xpData, stats, dailyGoal }: GamificationPageProps) {
  const currentLevel = getCurrentLevel(xpData.xp);
  const nextLevel = XP_LEVELS.find(l => l.level === currentLevel.level + 1);
  const levelPct = nextLevel
    ? Math.min(100, ((xpData.xp - currentLevel.minXp) / (currentLevel.maxXp - currentLevel.minXp)) * 100)
    : 100;

  const activeDays = stats
    ? Object.entries(stats.dailyActivity).filter(([, v]) => v > 0).map(([d]) => d)
    : [];

  const achievements: Achievement[] = [
    {
      id: 'first-review',
      title: 'First Steps',
      description: 'Complete your first review',
      icon: <Star className="h-5 w-5" />,
      threshold: 1,
      value: stats?.totalReviews ?? 0,
      color: 'text-gold',
      bg: 'bg-gold/10',
    },
    {
      id: 'streak-7',
      title: 'Week Warrior',
      description: '7-day streak',
      icon: <Flame className="h-5 w-5" />,
      threshold: 7,
      value: stats?.longestStreak ?? 0,
      color: 'text-amber-500',
      bg: 'bg-amber-500/10',
    },
    {
      id: 'streak-30',
      title: 'Monthly Master',
      description: '30-day streak',
      icon: <Crown className="h-5 w-5" />,
      threshold: 30,
      value: stats?.longestStreak ?? 0,
      color: 'text-amber-500',
      bg: 'bg-amber-500/10',
    },
    {
      id: 'xp-500',
      title: 'XP Collector',
      description: 'Earn 500 total XP',
      icon: <Zap className="h-5 w-5" />,
      threshold: 500,
      value: xpData.xp,
      color: 'text-primary',
      bg: 'bg-primary/10',
    },
    {
      id: 'xp-2000',
      title: 'XP Hoarder',
      description: 'Earn 2000 total XP',
      icon: <Zap className="h-5 w-5" />,
      threshold: 2000,
      value: xpData.xp,
      color: 'text-primary',
      bg: 'bg-primary/10',
    },
    {
      id: 'reviews-50',
      title: 'Dedicated Learner',
      description: '50 total reviews',
      icon: <Brain className="h-5 w-5" />,
      threshold: 50,
      value: stats?.totalReviews ?? 0,
      color: 'text-info',
      bg: 'bg-info/10',
    },
    {
      id: 'reviews-500',
      title: 'Review Machine',
      description: '500 total reviews',
      icon: <Brain className="h-5 w-5" />,
      threshold: 500,
      value: stats?.totalReviews ?? 0,
      color: 'text-info',
      bg: 'bg-info/10',
    },
    {
      id: 'goal-met',
      title: 'Goal Crusher',
      description: 'Hit your daily XP goal',
      icon: <Target className="h-5 w-5" />,
      threshold: dailyGoal,
      value: xpData.todayXp,
      color: 'text-success',
      bg: 'bg-success/10',
    },
    {
      id: 'chapters',
      title: 'Chapter Champion',
      description: 'Review from 5+ chapters',
      icon: <BookOpen className="h-5 w-5" />,
      threshold: 5,
      value: stats?.perChapter ? Object.keys(stats.perChapter).length : 0,
      color: 'text-success',
      bg: 'bg-success/10',
    },
    {
      id: 'streak-100',
      title: 'Century Streak',
      description: '100-day streak',
      icon: <Trophy className="h-5 w-5" />,
      threshold: 100,
      value: stats?.longestStreak ?? 0,
      color: 'text-gold',
      bg: 'bg-gold/10',
    },
  ];

  return (
    <div className="mx-auto max-w-2xl space-y-6 px-4 py-6">
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="font-display text-2xl font-black text-foreground">Achievements</h1>
        <p className="text-sm text-muted-foreground mt-1">Track your milestones and progress</p>
      </motion.div>

      {/* XP Level card */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="rounded-3xl bg-gradient-to-br from-primary to-primary/70 p-6 text-white"
      >
        <div className="flex items-center gap-4">
          <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-white/20 text-4xl">
            {currentLevel.icon}
          </div>
          <div className="flex-1">
            <p className="text-sm font-bold opacity-80">Level {currentLevel.level}</p>
            <h2 className="font-display text-2xl font-black">{currentLevel.name}</h2>
            <p className="text-sm opacity-80">{xpData.xp.toLocaleString()} XP total</p>
          </div>
          <ProgressRing
            value={levelPct}
            size={64}
            strokeWidth={6}
            color="white"
            trackColor="rgba(255,255,255,0.2)"
            label="Level progress"
          >
            <span className="text-xs font-bold text-white">{Math.round(levelPct)}%</span>
          </ProgressRing>
        </div>

        {nextLevel && (
          <div className="mt-4">
            <div className="flex items-center justify-between text-xs font-semibold opacity-80 mb-1">
              <span>{currentLevel.name}</span>
              <span>{nextLevel.name} ({nextLevel.minXp} XP)</span>
            </div>
            <div className="h-2.5 w-full overflow-hidden rounded-full bg-white/20">
              <div
                className="h-full rounded-full bg-white transition-all duration-700"
                style={{ width: `${levelPct}%` }}
              />
            </div>
          </div>
        )}
      </motion.div>

      {/* Streak detail */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="rounded-2xl border-2 border-border bg-card p-5"
      >
        <h3 className="font-display font-bold text-foreground mb-4">Streak</h3>
        <div className="flex items-center justify-around">
          <StreakWidget
            streak={stats?.currentStreak ?? xpData.streak}
            activeDays={activeDays}
          />
          <div className="text-center">
            <p className="font-display text-4xl font-black tabular-nums text-amber-500">
              {stats?.longestStreak ?? 0}
            </p>
            <p className="text-xs text-muted-foreground mt-1">best streak</p>
          </div>
        </div>
      </motion.div>

      {/* Achievements grid */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
      >
        <h3 className="font-display font-bold text-foreground mb-3">Badges</h3>
        <div className="grid grid-cols-2 gap-3">
          {achievements.map((ach, i) => {
            const unlocked = ach.value >= ach.threshold;
            const progress = Math.min(100, (ach.value / ach.threshold) * 100);
            return (
              <motion.div
                key={ach.id}
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.15 + i * 0.04 }}
                className={cn(
                  'rounded-2xl border-2 p-4 transition-all',
                  unlocked
                    ? 'border-gold/40 bg-gold/5'
                    : 'border-border bg-card opacity-70',
                )}
              >
                <div className="flex items-start gap-3">
                  <div
                    className={cn(
                      'flex h-10 w-10 shrink-0 items-center justify-center rounded-xl',
                      unlocked ? ach.bg : 'bg-muted',
                    )}
                  >
                    <span className={cn(unlocked ? ach.color : 'text-muted-foreground')}>
                      {ach.icon}
                    </span>
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-1">
                      <p className="font-bold text-sm text-foreground truncate">{ach.title}</p>
                      {unlocked && <span className="text-gold text-sm">✓</span>}
                    </div>
                    <p className="text-xs text-muted-foreground">{ach.description}</p>
                    {!unlocked && (
                      <div className="mt-2 h-1.5 w-full overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full rounded-full bg-primary transition-all duration-700"
                          style={{ width: `${progress}%` }}
                        />
                      </div>
                    )}
                    {!unlocked && (
                      <p className="text-2xs text-muted-foreground mt-1">
                        {Math.min(ach.value, ach.threshold)} / {ach.threshold}
                      </p>
                    )}
                  </div>
                </div>
              </motion.div>
            );
          })}
        </div>
      </motion.div>
    </div>
  );
}
