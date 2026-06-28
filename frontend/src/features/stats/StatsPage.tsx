import * as React from 'react';
import { motion } from 'framer-motion';
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, BarChart, Bar,
} from 'recharts';
import { StreakWidget } from '../../components/primitives/StreakWidget';
import { ProgressRing } from '../../components/primitives/ProgressRing';
import { EmptyState } from '../../components/primitives/EmptyState';
import { cn } from '../../lib/utils';

interface ServerStats {
  totalReviews: number;
  currentStreak: number;
  longestStreak: number;
  averageRetention: number;
  dailyActivity: Record<string, number>;
  forecast?: Array<{ date: string; dueCount: number }>;
  perChapter?: Record<string, {
    dueNow: number; totalCards: number; reviewedCards: number;
    avgStability: number; avgDifficulty: number;
  }>;
}

interface ChapterSummary { id: string; level: string; title: string; theme: string; cardCount: number; }

interface StatsPageProps {
  stats: ServerStats | null;
  chapters: ChapterSummary[];
  loading?: boolean;
  isDark?: boolean;
}

const CHART_COLORS = {
  primary: 'hsl(244, 76%, 58%)',
  gold: 'hsl(43, 96%, 56%)',
  success: 'hsl(96, 100%, 40%)',
  warning: 'hsl(35, 100%, 50%)',
  danger: 'hsl(10, 75%, 55%)',
  info: 'hsl(198, 91%, 53%)',
  muted: 'hsl(220, 9%, 46%)',
};

const AXIS_COLOR = 'hsl(220, 9%, 56%)';
const GRID_COLOR = 'hsl(220, 13%, 88% / 0.5)';

/** Full stats dashboard: heatmap, retention gauge, line chart, mastery donut. */
export function StatsPage({ stats, chapters, loading, isDark }: StatsPageProps) {
  const axisColor = isDark ? 'hsl(215, 10%, 55%)' : AXIS_COLOR;
  const gridColor = isDark ? 'hsl(222, 14%, 25%)' : GRID_COLOR;

  if (loading) {
    return (
      <div className="mx-auto max-w-2xl space-y-4 px-4 py-6">
        {[...Array(4)].map((_, i) => <div key={i} className="skeleton h-40 rounded-2xl" />)}
      </div>
    );
  }

  if (!stats) {
    return <EmptyState emoji="📊" title="No stats yet" description="Complete some reviews to see your progress here." />;
  }

  // Build last-60-days heatmap
  const today = new Date();
  const heatmapDays = Array.from({ length: 60 }, (_, i) => {
    const d = new Date(today);
    d.setDate(today.getDate() - (59 - i));
    const key = d.toISOString().split('T')[0];
    return { date: key, count: stats.dailyActivity[key] ?? 0 };
  });

  const maxActivity = Math.max(...heatmapDays.map(d => d.count), 1);

  // Activity line data (last 14 days)
  const lineData = heatmapDays.slice(-14).map(({ date, count }) => ({
    date: new Date(date + 'T12:00:00').toLocaleDateString('en', { weekday: 'short' }),
    reviews: count,
  }));

  // Mastery donut
  const totalCards = chapters.reduce((s, c) => s + c.cardCount, 0);
  const reviewedCards = stats.perChapter
    ? Object.values(stats.perChapter).reduce((s, c) => s + c.reviewedCards, 0)
    : 0;
  const masteredPct = totalCards > 0 ? Math.round((reviewedCards / totalCards) * 100) : 0;
  const donutData = [
    { name: 'Mastered', value: reviewedCards, color: CHART_COLORS.success },
    { name: 'Remaining', value: Math.max(0, totalCards - reviewedCards), color: gridColor },
  ];

  // Per-chapter bar data
  const chapterBarData = chapters.slice(0, 8).map(ch => ({
    name: ch.title.length > 12 ? ch.title.slice(0, 12) + '…' : ch.title,
    due: stats.perChapter?.[ch.id]?.dueNow ?? 0,
    reviewed: stats.perChapter?.[ch.id]?.reviewedCards ?? 0,
  }));

  const activeDays = Object.entries(stats.dailyActivity).filter(([, v]) => v > 0).map(([d]) => d);

  return (
    <div className="mx-auto max-w-2xl space-y-5 px-4 py-6">
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="font-display text-2xl font-black text-foreground">Your Progress</h1>
        <p className="text-sm text-muted-foreground mt-1">Track your German learning journey</p>
      </motion.div>

      {/* Top stats */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="grid grid-cols-2 gap-3"
      >
        <div className="rounded-2xl border-2 border-border bg-card p-4 flex flex-col items-center justify-center gap-2">
          <ProgressRing
            value={Math.round((stats.averageRetention ?? 0) * 100)}
            size={80}
            strokeWidth={8}
            color={CHART_COLORS.gold}
            label="Average retention"
          >
            <span className="font-display text-sm font-black text-foreground">
              {Math.round((stats.averageRetention ?? 0) * 100)}%
            </span>
          </ProgressRing>
          <p className="text-xs font-semibold text-muted-foreground">Avg Retention</p>
        </div>

        <div className="rounded-2xl border-2 border-border bg-card p-4 flex flex-col items-center justify-center">
          <StreakWidget
            streak={stats.currentStreak}
            activeDays={activeDays}
            compact
            className="mb-1"
          />
          <p className="text-xs font-semibold text-muted-foreground">Current streak</p>
          <div className="mt-2 text-center">
            <p className="font-display text-2xl font-black tabular-nums text-foreground">{stats.totalReviews.toLocaleString()}</p>
            <p className="text-xs text-muted-foreground">total reviews</p>
          </div>
        </div>
      </motion.div>

      {/* Activity heatmap */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1 }}
        className="rounded-2xl border-2 border-border bg-card p-5"
      >
        <h3 className="font-display font-bold text-foreground mb-3">60-day activity</h3>
        <div
          className="grid gap-1"
          style={{ gridTemplateColumns: 'repeat(12, 1fr)' }}
          aria-label="Daily activity heatmap"
          role="img"
        >
          {heatmapDays.map(({ date, count }) => {
            const intensity = count / maxActivity;
            const opacity = count === 0 ? 0.08 : 0.15 + intensity * 0.85;
            return (
              <div
                key={date}
                className="aspect-square rounded-sm"
                style={{ backgroundColor: count === 0 ? 'hsl(var(--muted))' : CHART_COLORS.primary, opacity }}
                title={`${date}: ${count} review${count !== 1 ? 's' : ''}`}
                aria-label={`${date}: ${count} reviews`}
              />
            );
          })}
        </div>
        <div className="mt-2 flex items-center gap-2 justify-end">
          <span className="text-xs text-muted-foreground">Less</span>
          {[0.1, 0.3, 0.5, 0.7, 1].map(o => (
            <div key={o} className="h-3 w-3 rounded-sm" style={{ backgroundColor: CHART_COLORS.primary, opacity: o }} aria-hidden="true" />
          ))}
          <span className="text-xs text-muted-foreground">More</span>
        </div>
      </motion.div>

      {/* Reviews over time */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15 }}
        className="rounded-2xl border-2 border-border bg-card p-5"
      >
        <h3 className="font-display font-bold text-foreground mb-4">Reviews (last 14 days)</h3>
        <ResponsiveContainer width="100%" height={160}>
          <LineChart data={lineData} margin={{ top: 4, right: 4, bottom: 4, left: -24 }}>
            <CartesianGrid strokeDasharray="3 3" stroke={gridColor} />
            <XAxis dataKey="date" tick={{ fill: axisColor, fontSize: 11 }} />
            <YAxis tick={{ fill: axisColor, fontSize: 11 }} />
            <Tooltip
              contentStyle={{
                background: isDark ? '#1e1e24' : '#fff',
                border: '1px solid hsl(var(--border))',
                borderRadius: 12,
                fontSize: 12,
                fontFamily: 'Nunito',
              }}
            />
            <Line
              type="monotone"
              dataKey="reviews"
              stroke={CHART_COLORS.primary}
              strokeWidth={2.5}
              dot={{ r: 3, fill: CHART_COLORS.primary }}
              activeDot={{ r: 5 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </motion.div>

      {/* Mastery donut + chapter bars */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.2 }}
        className="grid grid-cols-1 sm:grid-cols-2 gap-3"
      >
        {/* Mastery donut */}
        <div className="rounded-2xl border-2 border-border bg-card p-5 flex flex-col items-center">
          <h3 className="font-display font-bold text-foreground mb-3 self-start">Overall mastery</h3>
          <div className="relative">
            <ResponsiveContainer width={140} height={140}>
              <PieChart>
                <Pie
                  data={donutData}
                  cx="50%"
                  cy="50%"
                  innerRadius={42}
                  outerRadius={62}
                  dataKey="value"
                  startAngle={90}
                  endAngle={-270}
                  strokeWidth={0}
                >
                  {donutData.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="font-display text-2xl font-black text-foreground">{masteredPct}%</span>
              <span className="text-xs text-muted-foreground">mastered</span>
            </div>
          </div>
          <p className="text-xs text-muted-foreground mt-2 text-center">
            {reviewedCards} of {totalCards} words reviewed
          </p>
        </div>

        {/* Per-chapter bars */}
        <div className="rounded-2xl border-2 border-border bg-card p-5">
          <h3 className="font-display font-bold text-foreground mb-4">By chapter</h3>
          <ResponsiveContainer width="100%" height={140}>
            <BarChart data={chapterBarData} margin={{ top: 0, right: 0, bottom: 0, left: -32 }}>
              <XAxis dataKey="name" tick={{ fill: axisColor, fontSize: 9 }} />
              <YAxis tick={{ fill: axisColor, fontSize: 9 }} />
              <Tooltip
                contentStyle={{
                  background: isDark ? '#1e1e24' : '#fff',
                  border: '1px solid hsl(var(--border))',
                  borderRadius: 8,
                  fontSize: 11,
                }}
              />
              <Bar dataKey="reviewed" fill={CHART_COLORS.primary} radius={[4, 4, 0, 0]} name="Reviewed" />
              <Bar dataKey="due" fill={CHART_COLORS.warning} radius={[4, 4, 0, 0]} name="Due" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </motion.div>
    </div>
  );
}
