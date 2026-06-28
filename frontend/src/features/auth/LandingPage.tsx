import * as React from 'react';
import { motion } from 'framer-motion';
import { BookOpen, Brain, Flame, Languages, Menu, Sparkles, Volume2, X, Zap, Check } from 'lucide-react';
import { Button } from '../../components/ui/button';
import { cn } from '../../lib/utils';

interface LandingPageProps {
  onLogin: () => void;
  onRegister: () => void;
}

const FEATURES = [
  {
    icon: <Brain className="h-6 w-6" />,
    title: 'Smart Spaced Repetition',
    desc: 'FSRS-6 algorithm adapts to your memory. Review cards exactly when you need to — not too early, not too late.',
    color: 'bg-primary/10 text-primary',
  },
  {
    icon: <Volume2 className="h-6 w-6" />,
    title: 'Native Pronunciation',
    desc: 'Hear every word spoken by a native voice. Build your ear alongside your vocabulary.',
    color: 'bg-success/10 text-success',
  },
  {
    icon: <Flame className="h-6 w-6" />,
    title: 'Streak & XP System',
    desc: 'Build daily habits with streaks, earn XP, and unlock achievements as you level up your German.',
    color: 'bg-amber-500/10 text-amber-500',
  },
  {
    icon: <Sparkles className="h-6 w-6" />,
    title: 'AI-Powered Tutor',
    desc: 'Chat with Deutschi, your AI German tutor. Get instant explanations, grammar help, and example sentences.',
    color: 'bg-info/10 text-info',
  },
  {
    icon: <BookOpen className="h-6 w-6" />,
    title: 'B1 Vocabulary Chapters',
    desc: 'Structured chapters covering A1–B2 vocabulary with Hindi + English translations.',
    color: 'bg-gold/10 text-gold',
  },
  {
    icon: <Zap className="h-6 w-6" />,
    title: 'Multiple Study Modes',
    desc: 'Flashcards, MCQ, typing, cloze, articles, grammar — 8 modes to keep learning fresh.',
    color: 'bg-danger/10 text-danger',
  },
];

const STATS = [
  { value: '2000+', label: 'Vocabulary words' },
  { value: '20+', label: 'Chapters' },
  { value: '8', label: 'Study modes' },
  { value: '∞', label: 'Free forever' },
];

/** Playful, vibrant landing page in the Duolingo spirit. */
export function LandingPage({ onLogin, onRegister }: LandingPageProps) {
  const [mobileNav, setMobileNav] = React.useState(false);

  return (
    <div className="min-h-dvh bg-background">
      {/* Navbar */}
      <header className="fixed inset-x-0 top-0 z-50 border-b border-border/50 bg-background/85 backdrop-blur-2xl">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-5">
          {/* Logo */}
          <div className="flex items-center gap-2.5">
            <div className="grid h-9 w-9 place-items-center rounded-xl bg-primary text-white shadow-glow-sm">
              <Languages className="h-5 w-5" />
            </div>
            <span className="font-display text-base font-black text-foreground">Deutsch Meister</span>
          </div>

          <nav className="hidden gap-6 md:flex" aria-label="Main navigation">
            <a href="#features" className="text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors">Features</a>
            <a href="#how" className="text-sm font-semibold text-muted-foreground hover:text-foreground transition-colors">How it works</a>
          </nav>

          <div className="hidden gap-2 md:flex">
            <Button variant="outline" size="sm" onClick={onLogin}>Sign in</Button>
            <Button size="sm" onClick={onRegister} className="gap-1.5">
              <Zap className="h-3.5 w-3.5" /> Get started free
            </Button>
          </div>

          <button
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-muted md:hidden focus-ring"
            onClick={() => setMobileNav(v => !v)}
            aria-expanded={mobileNav}
            aria-label="Toggle menu"
          >
            {mobileNav ? <X className="h-4 w-4" /> : <Menu className="h-4 w-4" />}
          </button>
        </div>

        {mobileNav && (
          <div className="animate-fade-down border-t border-border bg-background/95 p-4 md:hidden">
            <div className="flex flex-col gap-2">
              <a href="#features" onClick={() => setMobileNav(false)} className="rounded-xl p-3 text-sm font-semibold hover:bg-muted transition-colors">Features</a>
              <a href="#how" onClick={() => setMobileNav(false)} className="rounded-xl p-3 text-sm font-semibold hover:bg-muted transition-colors">How it works</a>
              <hr className="border-border" />
              <Button variant="outline" size="sm" onClick={onLogin} className="w-full">Sign in</Button>
              <Button size="sm" onClick={onRegister} className="w-full gap-1.5">
                <Zap className="h-3.5 w-3.5" /> Get started free
              </Button>
            </div>
          </div>
        )}
      </header>

      {/* Hero */}
      <section className="relative min-h-dvh flex items-center pt-14" aria-label="Hero section">
        {/* Background blobs */}
        <div className="pointer-events-none absolute inset-0 overflow-hidden" aria-hidden="true">
          <div className="absolute -left-32 top-32 h-80 w-80 rounded-full bg-primary/10 blur-[100px] animate-float" />
          <div className="absolute -right-32 top-64 h-64 w-64 rounded-full bg-gold/10 blur-[80px] animate-float stagger-4" />
          <div className="absolute bottom-20 left-1/3 h-48 w-48 rounded-full bg-success/8 blur-[60px] animate-float stagger-2" />
        </div>

        <div className="relative mx-auto w-full max-w-6xl px-5 pb-16 pt-8 md:pt-16">
          <div className="grid gap-12 lg:grid-cols-2 lg:items-center">
            {/* Text */}
            <div>
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
              >
                <span className="mb-5 inline-flex items-center gap-1.5 rounded-full bg-primary/10 border border-primary/20 px-3 py-1 text-xs font-bold text-primary">
                  <Sparkles className="h-3.5 w-3.5" />
                  Free German vocabulary trainer
                </span>
              </motion.div>

              <motion.h1
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.1 }}
                className="font-display text-4xl font-black leading-tight tracking-tight text-foreground sm:text-5xl lg:text-6xl"
              >
                Learn German,{' '}
                <span className="gradient-text">one word at a time.</span>
              </motion.h1>

              <motion.p
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.2 }}
                className="mt-5 text-lg text-muted-foreground leading-relaxed max-w-lg"
              >
                Master B1 German vocabulary with smart flashcards, voice pronunciation, and a gamified learning system that keeps you coming back.
              </motion.p>

              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5, delay: 0.3 }}
                className="mt-8 flex flex-wrap gap-3"
              >
                <Button onClick={onRegister} size="lg" className="gap-2">
                  <Zap className="h-5 w-5" />
                  Start learning free
                </Button>
                <Button variant="outline" size="lg" onClick={onLogin}>
                  Sign in
                </Button>
              </motion.div>

              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.4 }}
                className="mt-6 flex flex-wrap gap-2"
              >
                {['No credit card', 'Free forever', 'Works offline (PWA)'].map(badge => (
                  <span key={badge} className="flex items-center gap-1 text-xs font-semibold text-muted-foreground">
                    <Check className="h-3.5 w-3.5 text-success" />
                    {badge}
                  </span>
                ))}
              </motion.div>
            </div>

            {/* Hero card mockup */}
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
              className="relative"
            >
              {/* Glow */}
              <div className="absolute -inset-4 rounded-3xl bg-gradient-to-br from-primary/15 via-transparent to-gold/10 blur-xl" aria-hidden="true" />

              {/* Main card */}
              <div className="relative rounded-3xl border-2 border-border bg-card p-8 shadow-flashcard">
                <div className="mb-3 flex items-center justify-between">
                  <span className="rounded-full bg-info/15 border border-info/30 px-2.5 py-1 text-xs font-bold text-info">NEW</span>
                  <button className="flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10 text-primary" aria-label="Play pronunciation">
                    <Volume2 className="h-4 w-4" />
                  </button>
                </div>
                <div className="py-6 text-center">
                  <p className="font-display text-base font-bold text-muted-foreground">die</p>
                  <h2 className="font-display text-5xl font-black text-foreground mt-1">Reise</h2>
                  <div className="mx-auto mt-6 h-px w-16 bg-border" />
                  <div className="mt-5 space-y-2">
                    <div className="inline-flex items-center gap-2 rounded-xl bg-success/10 border border-success/20 px-4 py-2">
                      <span className="text-xs font-bold text-success uppercase">EN</span>
                      <span className="font-bold text-foreground">Journey / Travel</span>
                    </div>
                    <div className="inline-flex items-center gap-2 rounded-xl bg-info/10 border border-info/20 px-4 py-2">
                      <span className="text-xs font-bold text-info uppercase">HI</span>
                      <span className="font-bold text-foreground">यात्रा</span>
                    </div>
                  </div>
                </div>

                {/* Rating bar preview */}
                <div className="grid grid-cols-4 gap-1.5 mt-4">
                  {[
                    { l: 'Again', c: 'bg-danger text-white btn-3d-danger', s: '0 3px 0 #962818' },
                    { l: 'Hard', c: 'bg-warning text-[#1F2937] btn-3d-warning', s: '0 3px 0 #cc7000' },
                    { l: 'Good', c: 'bg-success text-white btn-3d-success', s: '0 3px 0 #3a9a00' },
                    { l: 'Easy', c: 'bg-info text-white btn-3d-info', s: '0 3px 0 #0a8fcf' },
                  ].map(({ l, c, s }) => (
                    <div key={l} className={cn('rounded-xl py-2.5 text-center text-xs font-black btn-3d', c)} style={{ boxShadow: s }}>
                      {l}
                    </div>
                  ))}
                </div>
              </div>

              {/* Floating badges */}
              <div className="absolute -left-5 top-8 rounded-2xl border-2 border-border bg-card px-3 py-2 shadow-card-hover animate-float">
                <p className="text-xs font-bold text-amber-500">🔥 14-day streak!</p>
              </div>
              <div className="absolute -right-4 top-24 rounded-2xl border-2 border-border bg-card px-3 py-2 shadow-card-hover animate-float stagger-3">
                <p className="text-xs font-bold text-primary">⚡ +10 XP</p>
              </div>
              <div className="absolute -left-3 bottom-16 rounded-2xl border-2 border-border bg-card px-3 py-2 shadow-card-hover animate-float stagger-5">
                <p className="text-xs font-bold text-success">✓ 42/50 known</p>
              </div>
            </motion.div>
          </div>

          {/* Stats row */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.5 }}
            className="mt-16 grid grid-cols-2 gap-4 sm:grid-cols-4"
          >
            {STATS.map(({ value, label }) => (
              <div key={label} className="rounded-2xl border-2 border-border bg-card p-4 text-center">
                <p className="font-display text-3xl font-black text-primary">{value}</p>
                <p className="text-xs font-semibold text-muted-foreground mt-1">{label}</p>
              </div>
            ))}
          </motion.div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-20 bg-muted/30" aria-labelledby="features-heading">
        <div className="mx-auto max-w-6xl px-5">
          <div className="text-center mb-12">
            <h2 id="features-heading" className="font-display text-3xl font-black text-foreground sm:text-4xl">
              Everything you need to master German
            </h2>
            <p className="mt-4 text-lg text-muted-foreground max-w-2xl mx-auto">
              A complete vocabulary training system with proven spaced repetition, gamification, and AI assistance.
            </p>
          </div>

          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((f, i) => (
              <motion.div
                key={f.title}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.4, delay: i * 0.08 }}
                className="rounded-2xl border-2 border-border bg-card p-6 hover:border-primary/30 hover:-translate-y-1 hover:shadow-card-hover transition-all"
              >
                <div className={cn('inline-flex h-12 w-12 items-center justify-center rounded-2xl mb-4', f.color)}>
                  {f.icon}
                </div>
                <h3 className="font-display text-lg font-bold text-foreground">{f.title}</h3>
                <p className="mt-2 text-sm text-muted-foreground leading-relaxed">{f.desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how" className="py-20" aria-labelledby="how-heading">
        <div className="mx-auto max-w-4xl px-5 text-center">
          <h2 id="how-heading" className="font-display text-3xl font-black text-foreground sm:text-4xl">
            Start learning in 30 seconds
          </h2>
          <div className="mt-12 grid gap-6 sm:grid-cols-3">
            {[
              { step: '1', title: 'Create your account', desc: 'Sign up free — no credit card needed. Or continue with Google.', emoji: '✍️' },
              { step: '2', title: 'Set your goal', desc: 'Pick your daily XP goal and start with any chapter.', emoji: '🎯' },
              { step: '3', title: 'Review & level up', desc: 'Flip cards, rate your memory, earn XP and build your streak.', emoji: '🚀' },
            ].map(({ step, title, desc, emoji }) => (
              <motion.div
                key={step}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                className="flex flex-col items-center gap-3 p-6"
              >
                <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-primary text-white text-2xl btn-3d btn-3d-primary">
                  {emoji}
                </div>
                <p className="text-xs font-bold text-muted-foreground uppercase tracking-wide">Step {step}</p>
                <h3 className="font-display text-lg font-bold text-foreground">{title}</h3>
                <p className="text-sm text-muted-foreground">{desc}</p>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-16 bg-primary" aria-label="Call to action">
        <div className="mx-auto max-w-2xl px-5 text-center">
          <h2 className="font-display text-3xl font-black text-white sm:text-4xl">
            Ready to learn German?
          </h2>
          <p className="mt-4 text-lg text-white/80">
            Join and start mastering German vocabulary today. It's completely free.
          </p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Button
              onClick={onRegister}
              size="xl"
              className="bg-white text-primary hover:bg-white/90 gap-2"
              style={{ boxShadow: '0 4px 0 rgba(0,0,0,0.2)' }}
            >
              <Zap className="h-5 w-5" />
              Get started free
            </Button>
            <Button
              variant="ghost"
              size="xl"
              onClick={onLogin}
              className="text-white hover:bg-white/10"
            >
              Already have an account →
            </Button>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-border py-8 text-center" role="contentinfo">
        <div className="flex items-center justify-center gap-2 mb-2">
          <div className="grid h-7 w-7 place-items-center rounded-lg bg-primary text-white">
            <Languages className="h-4 w-4" />
          </div>
          <span className="font-display font-bold text-foreground">Deutsch Meister</span>
        </div>
        <p className="text-xs text-muted-foreground">Free German vocabulary trainer · Built with ❤️</p>
      </footer>
    </div>
  );
}
