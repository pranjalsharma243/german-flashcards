import * as React from 'react';
import { motion, useMotionValue, useTransform, AnimatePresence } from 'framer-motion';
import { Volume2 } from 'lucide-react';
import { cn } from '../../lib/utils';

export interface FlashCardItem {
  cardId: string;
  article?: string;
  word: string;
  english: string;
  hindi: string;
  exampleSentences?: { sentenceDe: string; sentenceEn: string }[];
  isNew?: boolean;
}

interface FlashCardProps {
  card: FlashCardItem;
  flipped: boolean;
  onFlip: () => void;
  onSwipe?: (direction: 'left' | 'right') => void;
  onTts?: () => void;
  ttsLoading?: boolean;
  /** If the card is flipped and user presses left/right, call rate */
  showAnswer?: boolean;
}

const SWIPE_THRESHOLD = 80;

/** 3D flip flashcard with drag-to-swipe. */
export function FlashCard({
  card,
  flipped,
  onFlip,
  onSwipe,
  onTts,
  ttsLoading = false,
}: FlashCardProps) {
  const x = useMotionValue(0);
  const rotate = useTransform(x, [-200, 200], [-15, 15]);
  const knowOpacity = useTransform(x, [20, SWIPE_THRESHOLD], [0, 1]);
  const learnOpacity = useTransform(x, [-SWIPE_THRESHOLD, -20], [1, 0]);

  const prefersReduced = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  function handleDragEnd(_: unknown, info: { offset: { x: number } }) {
    if (!flipped) return;
    if (info.offset.x > SWIPE_THRESHOLD) onSwipe?.('right');
    else if (info.offset.x < -SWIPE_THRESHOLD) onSwipe?.('left');
  }

  const fullWord = card.article ? `${card.article} ${card.word}` : card.word;

  return (
    <div className="flashcard-scene w-full" style={{ height: 320 }}>
      <motion.div
        style={{ x: flipped ? x : 0, rotate: flipped ? rotate : 0 }}
        drag={flipped ? 'x' : false}
        dragConstraints={{ left: 0, right: 0 }}
        dragElastic={0.3}
        onDragEnd={handleDragEnd}
        className="relative h-full w-full cursor-pointer"
        onClick={!flipped ? onFlip : undefined}
        tabIndex={0}
        role="button"
        aria-label={flipped ? fullWord + ': ' + card.english : 'Flash card — press Space or click to reveal'}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            if (!flipped) onFlip();
          }
        }}
      >
        {/* "Know" swipe indicator (green right) */}
        {flipped && (
          <motion.div
            style={{ opacity: knowOpacity }}
            className="pointer-events-none absolute inset-0 z-10 flex items-center justify-end rounded-3xl bg-success/20 pr-8"
            aria-hidden="true"
          >
            <span className="text-4xl">✓</span>
          </motion.div>
        )}
        {/* "Still learning" swipe indicator (amber left) */}
        {flipped && (
          <motion.div
            style={{ opacity: learnOpacity }}
            className="pointer-events-none absolute inset-0 z-10 flex items-center justify-start rounded-3xl bg-warning/20 pl-8"
            aria-hidden="true"
          >
            <span className="text-4xl">↺</span>
          </motion.div>
        )}

        <div
          className={cn('flashcard-inner h-full w-full', flipped && !prefersReduced && 'flipped')}
        >
          {/* FRONT */}
          <div className="flashcard-face flashcard-front flex flex-col items-center justify-center bg-card border-2 border-border shadow-flashcard p-8 select-none">
            {/* New badge */}
            {card.isNew && (
              <span className="mb-4 rounded-full bg-info/15 px-3 py-1 text-xs font-bold text-info">
                NEW
              </span>
            )}
            {card.article && (
              <p className="mb-1 text-lg font-bold text-muted-foreground">{card.article}</p>
            )}
            <p className="font-display text-5xl font-black text-foreground text-center leading-tight">
              {card.word}
            </p>
            <p className="mt-8 text-sm text-muted-foreground">
              Tap or press <kbd className="rounded border border-border bg-muted px-1.5 py-0.5 text-xs font-mono">Space</kbd> to reveal
            </p>
          </div>

          {/* BACK */}
          <div className="flashcard-face flashcard-back flex flex-col bg-card border-2 border-primary/30 shadow-flashcard p-6 overflow-y-auto select-none">
            {/* Word + article */}
            <div className="flex items-center justify-between">
              <div>
                {card.article && (
                  <span className="text-sm font-semibold text-primary/70">{card.article}</span>
                )}
                <p className="font-display text-3xl font-black text-foreground leading-tight">
                  {card.word}
                </p>
              </div>
              {onTts && (
                <button
                  onClick={(e) => { e.stopPropagation(); onTts(); }}
                  disabled={ttsLoading}
                  className="flex h-10 w-10 items-center justify-center rounded-xl bg-primary/10 text-primary hover:bg-primary/20 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary disabled:opacity-50"
                  aria-label="Play pronunciation"
                >
                  <Volume2 className={cn('h-5 w-5', ttsLoading && 'animate-pulse')} />
                </button>
              )}
            </div>

            <div className="mt-4 space-y-2">
              <div className="rounded-xl bg-success/10 border border-success/20 px-4 py-2.5">
                <p className="text-xs font-semibold uppercase tracking-wide text-success mb-1">English</p>
                <p className="font-bold text-foreground">{card.english}</p>
              </div>
              {card.hindi && (
                <div className="rounded-xl bg-info/10 border border-info/20 px-4 py-2.5">
                  <p className="text-xs font-semibold uppercase tracking-wide text-info mb-1">Hindi</p>
                  <p className="font-bold text-foreground">{card.hindi}</p>
                </div>
              )}
            </div>

            {card.exampleSentences && card.exampleSentences.length > 0 && (
              <div className="mt-4 rounded-xl bg-muted/50 px-4 py-3">
                <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground mb-1.5">Example</p>
                <p className="text-sm font-medium text-foreground italic">
                  {card.exampleSentences[0].sentenceDe}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {card.exampleSentences[0].sentenceEn}
                </p>
              </div>
            )}

            {flipped && (
              <p className="mt-4 text-center text-xs text-muted-foreground">
                Swipe right = know · Swipe left = still learning · or use buttons below
              </p>
            )}
          </div>
        </div>
      </motion.div>
    </div>
  );
}
