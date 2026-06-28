import * as React from 'react';
import { AnimatePresence, motion } from 'framer-motion';
import confetti from 'canvas-confetti';
import { ArrowLeft, RotateCcw, Volume2, Zap } from 'lucide-react';
import { FlashCard, FlashCardItem } from './FlashCard';
import { RatingBar } from './RatingBar';
import { Button } from '../../components/ui/button';
import { EmptyState } from '../../components/primitives/EmptyState';
import { cn } from '../../lib/utils';

type FsrsRating = 1 | 2 | 3 | 4;

interface FsrsQueueItem extends FlashCardItem {
  chapterId: string;
  type: string;
  isNew: boolean;
  stability?: number;
  difficulty?: number;
  reps?: number;
  lapses?: number;
  state?: string;
  dueAt?: string;
  previewIntervals: number[];
}

interface SessionStats {
  total: number;
  again: number;
  hard: number;
  good: number;
  easy: number;
}

interface ReviewPageProps {
  token: string;
  chapterId: string;
  chapterTitle?: string;
  onBack: () => void;
  onXp: (pts: number) => void;
}

const API = '/api';

function fireConfetti(type: 'allCorrect' | 'session') {
  const reduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduced) return;

  if (type === 'allCorrect') {
    confetti({ particleCount: 120, spread: 80, origin: { y: 0.6 }, disableForReducedMotion: true });
  } else {
    confetti({ particleCount: 60, spread: 60, origin: { y: 0.6 }, colors: ['#4F46E5', '#FBBF24', '#58CC02'], disableForReducedMotion: true });
  }
}

async function speakTts(cardId: string, token: string) {
  try {
    const res = await fetch(`${API}/tts/${cardId}`, { headers: { Authorization: `Bearer ${token}` } });
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const audio = new Audio(url);
    audio.play().catch(() => {});
    audio.onended = () => URL.revokeObjectURL(url);
  } catch { /* silent */ }
}

/** Full FSRS spaced-repetition review session. */
export function ReviewPage({ token, chapterId, chapterTitle, onBack, onXp }: ReviewPageProps) {
  const [queue, setQueue] = React.useState<FsrsQueueItem[]>([]);
  const [idx, setIdx] = React.useState(0);
  const [flipped, setFlipped] = React.useState(false);
  const [loading, setLoading] = React.useState(true);
  const [submitting, setSubmitting] = React.useState(false);
  const [done, setDone] = React.useState(false);
  const [stats, setStats] = React.useState<SessionStats>({ total: 0, again: 0, hard: 0, good: 0, easy: 0 });
  const [ttsLoading, setTtsLoading] = React.useState(false);

  const rateRef = React.useRef<((r: FsrsRating) => void) | null>(null);
  const flippedRef = React.useRef(false);
  React.useEffect(() => { flippedRef.current = flipped; }, [flipped]);

  React.useEffect(() => {
    function onKey(e: KeyboardEvent) {
      if ((e.target as HTMLElement).closest('input,textarea,select,button')) return;
      if (e.key === ' ') { e.preventDefault(); if (!flippedRef.current) setFlipped(true); return; }
      if (!flippedRef.current) return;
      const r = parseInt(e.key);
      if (r >= 1 && r <= 4) rateRef.current?.(r as FsrsRating);
    }
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, []);

  React.useEffect(() => { loadQueue(); }, [chapterId]);

  async function loadQueue() {
    setLoading(true);
    setIdx(0);
    setFlipped(false);
    setDone(false);
    setStats({ total: 0, again: 0, hard: 0, good: 0, easy: 0 });
    try {
      const url = chapterId
        ? `${API}/review/queue?chapterId=${encodeURIComponent(chapterId)}&limit=20`
        : `${API}/review/queue?limit=20`;
      const res = await fetch(url, { headers: { Authorization: `Bearer ${token}` } });
      const data: FsrsQueueItem[] = await res.json();
      setQueue(data);
    } catch { setQueue([]); }
    finally { setLoading(false); }
  }

  const cur = queue[idx] ?? null;

  async function rate(rating: FsrsRating) {
    if (!cur || submitting) return;
    setSubmitting(true);
    try {
      await fetch(`${API}/review/${cur.cardId}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating }),
      });
      const key = (['again', 'hard', 'good', 'easy'] as const)[rating - 1];
      setStats(s => ({ ...s, total: s.total + 1, [key]: s[key] + 1 }));
      if (rating >= 3) onXp(rating === 4 ? 15 : 10);

      const next = idx + 1;
      if (next >= queue.length) {
        setDone(true);
        const newStats = { ...stats, total: stats.total + 1, [key]: ((stats as unknown as Record<string, number>)[key] ?? 0) + 1 };
        const allGood = newStats.again === 0 && newStats.hard === 0;
        fireConfetti(allGood ? 'allCorrect' : 'session');
      } else {
        setIdx(next);
        setFlipped(false);
      }
    } finally { setSubmitting(false); }
  }

  rateRef.current = rate;

  async function handleTts() {
    if (!cur || ttsLoading) return;
    setTtsLoading(true);
    await speakTts(cur.cardId, token);
    setTtsLoading(false);
  }

  const progress = queue.length > 0 ? ((idx) / queue.length) * 100 : 0;

  if (loading) return <ReviewSkeleton />;

  if (queue.length === 0) {
    return (
      <EmptyState
        emoji="🎉"
        title="All caught up!"
        description="No cards are due for review right now. Come back later or start a new chapter."
        action={<Button onClick={onBack} variant="outline">← Back to chapters</Button>}
      />
    );
  }

  if (done) {
    return (
      <SessionSummary
        stats={stats}
        total={queue.length}
        onRestart={loadQueue}
        onBack={onBack}
        chapterTitle={chapterTitle}
      />
    );
  }

  return (
    <div className="flex flex-col gap-6 px-4 py-6 max-w-lg mx-auto w-full">
      {/* Header */}
      <div className="flex items-center justify-between">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors focus-ring"
          aria-label="Back to chapters"
        >
          <ArrowLeft className="h-4 w-4" />
          Back
        </button>
        <div className="text-center">
          <p className="font-display text-sm font-bold text-foreground">
            {chapterTitle ?? 'Review'}
          </p>
          <p className="text-xs text-muted-foreground">{idx + 1} / {queue.length}</p>
        </div>
        <div className="flex items-center gap-1.5 text-sm font-semibold text-amber-500">
          <Zap className="h-4 w-4" />
          <span className="tabular-nums">{stats.good * 10 + stats.easy * 15} XP</span>
        </div>
      </div>

      {/* Progress bar */}
      <div
        className="h-3 w-full overflow-hidden rounded-full bg-muted"
        role="progressbar"
        aria-valuenow={Math.round(progress)}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-label="Review session progress"
      >
        <div
          className="h-full rounded-full bg-primary transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>

      {/* New / Review badge */}
      <div className="flex justify-center">
        {cur?.isNew ? (
          <span className="rounded-full bg-info/15 border border-info/30 px-3 py-1 text-xs font-bold text-info">
            🆕 New word
          </span>
        ) : (
          <span className="rounded-full bg-primary/10 border border-primary/20 px-3 py-1 text-xs font-bold text-primary">
            🔁 Review
          </span>
        )}
      </div>

      {/* Flashcard */}
      <AnimatePresence mode="wait">
        <motion.div
          key={idx}
          initial={{ opacity: 0, y: 20, scale: 0.96 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: -20, scale: 0.96 }}
          transition={{ duration: 0.2, ease: [0.16, 1, 0.3, 1] }}
        >
          {cur && (
            <FlashCard
              card={cur}
              flipped={flipped}
              onFlip={() => setFlipped(true)}
              onSwipe={(dir) => rate(dir === 'right' ? 3 : 1)}
              onTts={handleTts}
              ttsLoading={ttsLoading}
            />
          )}
        </motion.div>
      </AnimatePresence>

      {/* Rating bar (after flip) */}
      <AnimatePresence>
        {flipped && (
          <motion.div
            initial={{ opacity: 0, y: 24 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 24 }}
            transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
          >
            <RatingBar
              onRate={rate}
              previewIntervals={cur?.previewIntervals}
              disabled={submitting}
            />
          </motion.div>
        )}
      </AnimatePresence>

      {!flipped && (
        <p className="text-center text-xs text-muted-foreground animate-fade-in">
          Press <kbd className="rounded border border-border bg-muted px-1.5 py-0.5 text-xs font-mono">Space</kbd> or tap the card to reveal
        </p>
      )}
    </div>
  );
}

function ReviewSkeleton() {
  return (
    <div className="flex flex-col gap-6 px-4 py-6 max-w-lg mx-auto w-full">
      <div className="skeleton h-5 w-32 mx-auto rounded-full" />
      <div className="skeleton h-3 w-full rounded-full" />
      <div className="skeleton h-80 w-full rounded-3xl" />
      <div className="grid grid-cols-4 gap-2">
        {[...Array(4)].map((_, i) => <div key={i} className="skeleton h-16 rounded-xl" />)}
      </div>
    </div>
  );
}

function SessionSummary({
  stats,
  total,
  onRestart,
  onBack,
  chapterTitle,
}: {
  stats: SessionStats;
  total: number;
  onRestart: () => void;
  onBack: () => void;
  chapterTitle?: string;
}) {
  const accuracy = total > 0 ? Math.round(((stats.good + stats.easy) / total) * 100) : 0;

  return (
    <div className="flex flex-col items-center gap-6 px-4 py-10 max-w-md mx-auto w-full text-center animate-scale-in">
      <div className="text-6xl animate-bounce-in">{accuracy >= 80 ? '🏆' : accuracy >= 60 ? '⭐' : '💪'}</div>
      <div>
        <h2 className="font-display text-3xl font-black">Session Complete!</h2>
        {chapterTitle && <p className="text-muted-foreground mt-1">{chapterTitle}</p>}
      </div>

      <div className="w-full rounded-2xl bg-card border-2 border-border p-6 space-y-4">
        <div className="text-center">
          <p className="font-display text-5xl font-black text-primary">{accuracy}%</p>
          <p className="text-sm text-muted-foreground mt-1">accuracy</p>
        </div>
        <div className="grid grid-cols-4 gap-2 text-center">
          {[
            { label: 'Again', val: stats.again, color: 'text-danger' },
            { label: 'Hard', val: stats.hard, color: 'text-warning' },
            { label: 'Good', val: stats.good, color: 'text-success' },
            { label: 'Easy', val: stats.easy, color: 'text-info' },
          ].map(({ label, val, color }) => (
            <div key={label} className="rounded-xl bg-muted/50 py-2">
              <p className={cn('font-display text-xl font-black', color)}>{val}</p>
              <p className="text-xs text-muted-foreground">{label}</p>
            </div>
          ))}
        </div>
        <div className="flex items-center justify-center gap-2 rounded-xl bg-gold/10 border border-gold/20 py-2">
          <Zap className="h-4 w-4 text-gold" />
          <span className="font-bold text-gold text-sm">
            +{stats.good * 10 + stats.easy * 15} XP earned
          </span>
        </div>
      </div>

      <div className="flex gap-3 w-full">
        <Button variant="outline" onClick={onBack} className="flex-1">
          ← Back
        </Button>
        <Button onClick={onRestart} className="flex-1 gap-2">
          <RotateCcw className="h-4 w-4" />
          Review again
        </Button>
      </div>
    </div>
  );
}
