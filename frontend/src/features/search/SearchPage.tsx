import * as React from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Search, Sparkles, X, Volume2, Loader2, AlertCircle } from 'lucide-react';
import { Button } from '../../components/ui/button';
import { EmptyState } from '../../components/primitives/EmptyState';
import { cn } from '../../lib/utils';

interface SearchResult {
  cardId: string;
  word: string;
  article?: string;
  english: string;
  hindi: string;
  chapterId: string;
  chapterTitle?: string;
  score?: number;
  exampleSentences?: { sentenceDe: string; sentenceEn: string }[];
}

interface AiExplanation {
  word: string;
  explanation: string;
  examples: string[];
}

interface SearchPageProps {
  token: string;
}

const API = '/api';

async function speakTts(cardId: string, token: string) {
  try {
    const res = await fetch(`${API}/tts/${cardId}`, { headers: { Authorization: `Bearer ${token}` } });
    const blob = await res.blob();
    const url = URL.createObjectURL(blob);
    const audio = new Audio(url);
    audio.play().catch((_e: unknown) => {});
    setTimeout(() => URL.revokeObjectURL(url), 10000);
  } catch { /* silent */ }
}

/** Semantic search over vocabulary + AI "explain this word" panel. */
export function SearchPage({ token }: SearchPageProps) {
  const [query, setQuery] = React.useState('');
  const [results, setResults] = React.useState<SearchResult[]>([]);
  const [loading, setLoading] = React.useState(false);
  const [error, setError] = React.useState('');
  const [selected, setSelected] = React.useState<SearchResult | null>(null);
  const [aiLoading, setAiLoading] = React.useState(false);
  const [aiText, setAiText] = React.useState('');
  const [ttsLoading, setTtsLoading] = React.useState<string | null>(null);
  const inputRef = React.useRef<HTMLInputElement>(null);
  const debounceRef = React.useRef<number>(0);

  React.useEffect(() => {
    inputRef.current?.focus();
  }, []);

  React.useEffect(() => {
    clearTimeout(debounceRef.current);
    if (!query.trim()) { setResults([]); setError(''); return; }
    debounceRef.current = setTimeout(() => search(query), 350);
    return () => clearTimeout(debounceRef.current);
  }, [query]);

  async function search(q: string) {
    setLoading(true);
    setError('');
    try {
      const res = await fetch(`${API}/search?q=${encodeURIComponent(q)}&k=8`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error('Search failed');
      const data: SearchResult[] = await res.json();
      setResults(data);
    } catch {
      setError('Search failed. The search index may not be ready yet.');
      setResults([]);
    } finally {
      setLoading(false);
    }
  }

  async function explainWord(result: SearchResult) {
    setSelected(result);
    setAiText('');
    setAiLoading(true);
    try {
      const res = await fetch(`${API}/ai/hint`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        body: JSON.stringify({
          word: result.article ? `${result.article} ${result.word}` : result.word,
          english: result.english,
          context: 'Explain this German word: its meaning, usage, grammar notes, and 2 example sentences.',
          wrongAnswer: '',
        }),
      });
      if (!res.ok) throw new Error('AI failed');
      const data = await res.json();
      setAiText(data.hint ?? data.message ?? JSON.stringify(data));
    } catch {
      setAiText('Could not generate explanation. Please try again.');
    } finally {
      setAiLoading(false);
    }
  }

  async function handleTts(result: SearchResult) {
    if (ttsLoading) return;
    setTtsLoading(result.cardId);
    await speakTts(result.cardId, token);
    setTtsLoading(null);
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-6 space-y-5">
      <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}>
        <h1 className="font-display text-2xl font-black text-foreground">Search</h1>
        <p className="text-sm text-muted-foreground mt-1">Semantic search across all vocabulary</p>
      </motion.div>

      {/* Search input */}
      <motion.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.05 }}
        className="relative"
      >
        <div className="flex items-center gap-3 rounded-2xl border-2 border-border bg-card px-4 py-3 transition-colors focus-within:border-primary focus-within:shadow-glow-sm">
          {loading
            ? <Loader2 className="h-5 w-5 shrink-0 text-primary animate-spin" aria-hidden="true" />
            : <Search className="h-5 w-5 shrink-0 text-muted-foreground" aria-hidden="true" />
          }
          <input
            ref={inputRef}
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Search words, meanings, or phrases…"
            className="flex-1 bg-transparent text-sm font-medium text-foreground placeholder:text-muted-foreground focus:outline-none"
            aria-label="Search vocabulary"
            aria-describedby="search-hint"
          />
          {query && (
            <button
              onClick={() => { setQuery(''); setResults([]); setSelected(null); inputRef.current?.focus(); }}
              className="text-muted-foreground hover:text-foreground focus-ring rounded-lg"
              aria-label="Clear search"
            >
              <X className="h-4 w-4" />
            </button>
          )}
        </div>
        <p id="search-hint" className="sr-only">Type to search. Results appear below.</p>
      </motion.div>

      {/* Error state */}
      {error && (
        <div className="flex items-center gap-3 rounded-2xl border-2 border-danger/30 bg-danger/5 px-4 py-3">
          <AlertCircle className="h-5 w-5 shrink-0 text-danger" aria-hidden="true" />
          <p className="text-sm text-danger">{error}</p>
        </div>
      )}

      {/* Results + AI panel */}
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        {/* Results list */}
        <div>
          {!query && !loading && (
            <EmptyState
              emoji="🔍"
              title="Start searching"
              description="Type any German word, English meaning, or phrase to search semantically across all vocabulary."
            />
          )}

          {query && !loading && results.length === 0 && !error && (
            <EmptyState
              emoji="🤷"
              title="No results"
              description={`No vocabulary matches "${query}". Try a different search term.`}
            />
          )}

          <AnimatePresence mode="popLayout">
            {results.map((r, i) => (
              <motion.button
                key={r.cardId}
                layout
                initial={{ opacity: 0, y: 12 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -12 }}
                transition={{ duration: 0.2, delay: i * 0.04 }}
                onClick={() => explainWord(r)}
                className={cn(
                  'mb-2 w-full rounded-2xl border-2 p-4 text-left transition-all',
                  'hover:border-primary/40 hover:-translate-y-0.5 hover:shadow-card-hover',
                  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2',
                  selected?.cardId === r.cardId
                    ? 'border-primary bg-primary/5 shadow-glow-sm'
                    : 'border-border bg-card',
                )}
                aria-pressed={selected?.cardId === r.cardId}
                aria-label={`${r.article ? r.article + ' ' : ''}${r.word}: ${r.english}`}
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="min-w-0">
                    <p className="font-display font-bold text-foreground">
                      {r.article && <span className="text-muted-foreground">{r.article} </span>}
                      {r.word}
                    </p>
                    <p className="text-sm text-muted-foreground truncate">{r.english}</p>
                    {r.chapterTitle && (
                      <p className="mt-1 text-xs text-primary/70">{r.chapterTitle}</p>
                    )}
                  </div>
                  <div className="flex items-center gap-1.5 shrink-0">
                    <button
                      onClick={(e) => { e.stopPropagation(); handleTts(r); }}
                      disabled={ttsLoading !== null}
                      className="flex h-8 w-8 items-center justify-center rounded-xl bg-muted hover:bg-primary/10 hover:text-primary transition-colors focus-ring disabled:opacity-50"
                      aria-label={`Pronounce ${r.word}`}
                    >
                      {ttsLoading === r.cardId
                        ? <Loader2 className="h-3.5 w-3.5 animate-spin" />
                        : <Volume2 className="h-3.5 w-3.5" />
                      }
                    </button>
                    <Sparkles className="h-4 w-4 text-primary/40" aria-hidden="true" />
                  </div>
                </div>
              </motion.button>
            ))}
          </AnimatePresence>
        </div>

        {/* AI explanation panel */}
        <AnimatePresence>
          {selected && (
            <motion.div
              key={selected.cardId}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: 20 }}
              transition={{ duration: 0.25, ease: [0.16, 1, 0.3, 1] }}
              className="rounded-2xl border-2 border-primary/30 bg-card p-5 h-fit"
              aria-live="polite"
              aria-label="AI word explanation"
            >
              <div className="flex items-start justify-between mb-4">
                <div>
                  <p className="text-xs font-bold uppercase tracking-wide text-primary mb-1">
                    <Sparkles className="inline h-3.5 w-3.5 mr-1" aria-hidden="true" />
                    AI Explanation
                  </p>
                  <h2 className="font-display text-2xl font-black text-foreground">
                    {selected.article && <span className="text-muted-foreground">{selected.article} </span>}
                    {selected.word}
                  </h2>
                  <p className="text-sm text-muted-foreground">{selected.english}</p>
                </div>
                <button
                  onClick={() => { setSelected(null); setAiText(''); }}
                  className="text-muted-foreground hover:text-foreground focus-ring rounded-lg"
                  aria-label="Close explanation"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>

              {/* Example sentence */}
              {selected.exampleSentences && selected.exampleSentences.length > 0 && (
                <div className="mb-4 rounded-xl bg-muted/50 px-4 py-3">
                  <p className="text-xs font-semibold text-muted-foreground mb-1 uppercase tracking-wide">Example</p>
                  <p className="text-sm italic text-foreground">{selected.exampleSentences[0].sentenceDe}</p>
                  <p className="text-xs text-muted-foreground mt-0.5">{selected.exampleSentences[0].sentenceEn}</p>
                </div>
              )}

              {/* AI content */}
              {aiLoading ? (
                <div className="flex items-center gap-3 py-6">
                  <Loader2 className="h-5 w-5 animate-spin text-primary shrink-0" />
                  <p className="text-sm text-muted-foreground">Generating explanation…</p>
                </div>
              ) : aiText ? (
                <div className="prose prose-sm dark:prose-invert max-w-none">
                  <div className="rounded-xl bg-primary/5 border border-primary/20 px-4 py-3 text-sm text-foreground whitespace-pre-wrap leading-relaxed">
                    {aiText}
                  </div>
                </div>
              ) : (
                <div className="flex items-center gap-3 py-4">
                  <AlertCircle className="h-4 w-4 text-muted-foreground shrink-0" />
                  <p className="text-sm text-muted-foreground">Click a word to get an AI explanation.</p>
                </div>
              )}

              <div className="mt-4 flex gap-2">
                <button
                  onClick={() => handleTts(selected)}
                  disabled={ttsLoading !== null}
                  className="flex items-center gap-1.5 rounded-xl bg-muted px-3 py-2 text-xs font-bold text-foreground hover:bg-primary/10 hover:text-primary transition-colors focus-ring disabled:opacity-50"
                  aria-label={`Play pronunciation for ${selected.word}`}
                >
                  <Volume2 className="h-3.5 w-3.5" />
                  Pronounce
                </button>
                {aiText && (
                  <button
                    onClick={() => explainWord(selected)}
                    className="flex items-center gap-1.5 rounded-xl bg-muted px-3 py-2 text-xs font-bold text-foreground hover:bg-primary/10 hover:text-primary transition-colors focus-ring"
                    aria-label="Regenerate explanation"
                  >
                    <Sparkles className="h-3.5 w-3.5" />
                    Regenerate
                  </button>
                )}
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
